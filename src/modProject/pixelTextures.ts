import { RAW_IE_TEXTURES } from './rawIETextures';

export interface PixelTextureDef {
  id: string;
  nameUk: string;
  kind: 'block' | 'item';
  zipPath: string;
  description: string;
  palette: Record<string, string>;
  grid: string[]; // 16 rows of 16 characters
  rawBase64?: string; // Optional direct binary override from IE
}

export interface RenderedTexture extends PixelTextureDef {
  pngBytes: Uint8Array;
  dataUrl: string;
}

// --- Pure TypeScript 16x16 RGBA PNG Encoder ---
const CRC_TABLE = (() => {
  const table = new Uint32Array(256);
  for (let n = 0; n < 256; n++) {
    let c = n;
    for (let k = 0; k < 8; k++) {
      c = c & 1 ? 0xedb88320 ^ (c >>> 1) : c >>> 1;
    }
    table[n] = c >>> 0;
  }
  return table;
})();

function crc32(bytes: Uint8Array): number {
  let c = 0xffffffff;
  for (let i = 0; i < bytes.length; i++) {
    c = CRC_TABLE[(c ^ bytes[i]) & 0xff] ^ (c >>> 8);
  }
  return (c ^ 0xffffffff) >>> 0;
}

function adler32(bytes: Uint8Array): number {
  let a = 1;
  let b = 0;
  for (let i = 0; i < bytes.length; i++) {
    a = (a + bytes[i]) % 65521;
    b = (b + a) % 65521;
  }
  return ((b << 16) | a) >>> 0;
}

function makeChunk(type: string, data: Uint8Array): Uint8Array {
  const chunk = new Uint8Array(12 + data.length);
  const view = new DataView(chunk.buffer);
  view.setUint32(0, data.length, false);
  for (let i = 0; i < 4; i++) {
    chunk[4 + i] = type.charCodeAt(i);
  }
  chunk.set(data, 8);
  const crc = crc32(chunk.subarray(4, 8 + data.length));
  view.setUint32(8 + data.length, crc, false);
  return chunk;
}

function hexToRgba(hex: string): [number, number, number, number] {
  const clean = hex.replace('#', '');
  if (clean.length === 8) {
    return [
      parseInt(clean.slice(0, 2), 16),
      parseInt(clean.slice(2, 4), 16),
      parseInt(clean.slice(4, 6), 16),
      parseInt(clean.slice(6, 8), 16),
    ];
  }
  return [
    parseInt(clean.slice(0, 2), 16),
    parseInt(clean.slice(2, 4), 16),
    parseInt(clean.slice(4, 6), 16),
    255,
  ];
}

function encodePng16x16(grid: string[], palette: Record<string, string>): Uint8Array {
  const width = 16;
  const height = 16;
  const raw = new Uint8Array(height * (1 + width * 4));

  for (let y = 0; y < height; y++) {
    const rowStr = grid[y] ?? '................';
    const rowOffset = y * (1 + width * 4);
    raw[rowOffset] = 0; // filter type 0
    for (let x = 0; x < width; x++) {
      const ch = rowStr[x] ?? '.';
      const hex = palette[ch];
      const [r, g, b, a] = hex ? hexToRgba(hex) : [0, 0, 0, 0];
      const pxOffset = rowOffset + 1 + x * 4;
      raw[pxOffset] = r;
      raw[pxOffset + 1] = g;
      raw[pxOffset + 2] = b;
      raw[pxOffset + 3] = a;
    }
  }

  // Zlib stream with single stored (uncompressed) DEFLATE block
  const zlib = new Uint8Array(2 + 5 + raw.length + 4);
  zlib[0] = 0x78;
  zlib[1] = 0x01;
  zlib[2] = 0x01; // BFINAL=1, BTYPE=00
  const len = raw.length;
  zlib[3] = len & 0xff;
  zlib[4] = (len >>> 8) & 0xff;
  const nlen = ~len & 0xffff;
  zlib[5] = nlen & 0xff;
  zlib[6] = (nlen >>> 8) & 0xff;
  zlib.set(raw, 7);
  const adler = adler32(raw);
  new DataView(zlib.buffer).setUint32(7 + raw.length, adler, false);

  const ihdr = new Uint8Array(13);
  const ihdrView = new DataView(ihdr.buffer);
  ihdrView.setUint32(0, width, false);
  ihdrView.setUint32(4, height, false);
  ihdr[8] = 8; // bit depth
  ihdr[9] = 6; // RGBA
  ihdr[10] = 0;
  ihdr[11] = 0;
  ihdr[12] = 0;

  const sig = new Uint8Array([137, 80, 78, 71, 13, 10, 26, 10]);
  const cIhdr = makeChunk('IHDR', ihdr);
  const cIdat = makeChunk('IDAT', zlib);
  const cIend = makeChunk('IEND', new Uint8Array(0));

  const out = new Uint8Array(sig.length + cIhdr.length + cIdat.length + cIend.length);
  let pos = 0;
  out.set(sig, pos); pos += sig.length;
  out.set(cIhdr, pos); pos += cIhdr.length;
  out.set(cIdat, pos); pos += cIdat.length;
  out.set(cIend, pos);
  return out;
}

function uint8ToBase64(bytes: Uint8Array): string {
  let bin = '';
  for (let i = 0; i < bytes.length; i++) {
    bin += String.fromCharCode(bytes[i]);
  }
  return btoa(bin);
}

function base64ToUint8(b64: string): Uint8Array {
  const pure = b64.includes(',') ? b64.split(',')[1] : b64;
  const bin = atob(pure);
  const bytes = new Uint8Array(bin.length);
  for (let i = 0; i < bin.length; i++) {
    bytes[i] = bin.charCodeAt(i);
  }
  return bytes;
}

// =========================================================================
// 16x16 Pixel Art Definitions matching Immersive Engineering & Vanilla Bucket
// =========================================================================
export const PIXEL_TEXTURE_DEFS: PixelTextureDef[] = [
  // -----------------------------------------------------------------------
  // 1. REACTOR CASING (Immersive Engineering Sheetmetal Steel with Rivets)
  // -----------------------------------------------------------------------
  {
    id: 'reactor_casing',
    nameUk: 'Корпус реактора (Сталевий лист IE)',
    kind: 'block',
    zipPath: 'src/main/resources/assets/ienuclear/textures/block/reactor_casing.png',
    description: 'Броньована сталева обшивка Immersive Engineering з характерними промисловими заклепками',
    rawBase64: RAW_IE_TEXTURES['mod_block_reactor_casing'],
    palette: {
      '#': '#1E232B',
      D: '#2A303C',
      M: '#3C4554',
      L: '#546074',
      H: '#75849C',
      R: '#A6B4C9', // Rivet highlight
    },
    grid: [
      '################',
      '#RLLMMMMMMMMLLR#',
      '#LHDDDDDDDDDDMD#',
      '#MDMMMMMMMMMMDM#',
      '#MDMDDDDDDDDMDM#',
      '#MDMDLLMMLLDMDM#',
      '#MDMDMDDDDMDMDM#',
      '#MDMDMDLLDMDMDM#',
      '#MDMDMDLLDMDMDM#',
      '#MDMDMDDDDMDMDM#',
      '#MDMDLLMMLLDMDM#',
      '#MDMDDDDDDDDMDM#',
      '#MDMMMMMMMMMMDM#',
      '#LMDDDDDDDDDDMD#',
      '#RLLMMMMMMMMLLR#',
      '################',
    ],
  },

  // -----------------------------------------------------------------------
  // 2. REACTOR CORE ASSEMBLY (IE Heavy Generator Core with Nuclear Channel)
  // -----------------------------------------------------------------------
  {
    id: 'reactor_core_rod',
    nameUk: 'Стрижень активної зони',
    kind: 'block',
    zipPath: 'src/main/resources/assets/ienuclear/textures/block/reactor_core_rod.png',
    description: 'Масивна активна зона з теплообмінними ребрами та центральним каналом поділу урану',
    rawBase64: RAW_IE_TEXTURES['mod_block_reactor_core_rod'],
    palette: {
      '#': '#151922',
      D: '#252C3B',
      M: '#3B465B',
      S: '#52607A',
      C: '#059669', // Emerald radiation
      G: '#10B981',
      W: '#6EE7B7',
      Y: '#FBBF24',
    },
    grid: [
      '################',
      '#SSSSMMMMMMSSSS#',
      '#SD#CGGGGGGC#DS#',
      '#MD#GWWWWWWG#DM#',
      '#MD#CGYYYYGC#DM#',
      '#SD#CGGGGGGC#DS#',
      '#MDMMMMMMMMMMDM#',
      '#SD#CGGGGGGC#DS#',
      '#MD#GWWWWWWG#DM#',
      '#MD#CGYYYYGC#DM#',
      '#SD#CGGGGGGC#DS#',
      '#MDMMMMMMMMMMDM#',
      '#SD#CGGGGGGC#DS#',
      '#MD#GWWWWWWG#DM#',
      '#SSSSMMMMMMSSSS#',
      '################',
    ],
  },

  // -----------------------------------------------------------------------
  // 3. REACTOR GLASS (Immersive Engineering Panzerglass)
  // -----------------------------------------------------------------------
  {
    id: 'reactor_glass',
    nameUk: 'Броньоване скло (Panzerglass)',
    kind: 'block',
    zipPath: 'src/main/resources/assets/ienuclear/textures/block/reactor_glass.png',
    description: 'Справжнє армоване радіаційно-стійке скло Immersive Engineering',
    rawBase64: RAW_IE_TEXTURES['mod_block_reactor_glass'],
    palette: {
      '#': '#1D242C',
      M: '#3D4958',
      T: '#162F3A88',
      S: '#38BDF8A0',
      W: '#E0F2FECC',
    },
    grid: [
      '################',
      '#MMMMMMMMMMMMMM#',
      '#MTTTTTTTTTTTTM#',
      '#MTTWTTTTTTTTTM#',
      '#MTWSTTTTTTTTTM#',
      '#MTSTTTTTTTTTTM#',
      '#MTTTTTTTTTTTTM#',
      '#MTTTTTTTTTTTTM#',
      '#MTTTTTTTTTTTTM#',
      '#MTTTTTTTTTWTTM#',
      '#MTTTTTTTTWSTTM#',
      '#MTTTTTTTTSTTTM#',
      '#MTTTTTTTTTTTTM#',
      '#MTTTTTTTTTTTTM#',
      '#MMMMMMMMMMMMMM#',
      '################',
    ],
  },

  // -----------------------------------------------------------------------
  // 4. REACTOR CONTROLLER FRONT (IDLE - IE Stamped Metal Panel with Gauge)
  // -----------------------------------------------------------------------
  {
    id: 'reactor_controller_front',
    nameUk: 'Контролер (режим очікування)',
    kind: 'block',
    zipPath: 'src/main/resources/assets/ienuclear/textures/block/reactor_controller_front.png',
    description: 'Лицьова індустріальна панель контролера з круглим манометром та рубильником',
    rawBase64: RAW_IE_TEXTURES['mod_block_reactor_controller_front'],
    palette: {
      '#': '#1B212B',
      D: '#2A323F',
      M: '#3E495B',
      L: '#5C6B82',
      B: '#B45309', // Brass bezel
      Y: '#FBBF24', // Dial face
      N: '#7F1D1D', // Needle
      R: '#DC2626', // Red stop light
      O: '#451A03',
    },
    grid: [
      '################',
      '#LLMMMMMMMMMMLL#',
      '#LM#BBBBBBBB#ML#',
      '#LMBBYYYYYYBBML#',
      '#LMBYYNNYYYYBML#',
      '#LMBYYYYYYYYBML#',
      '#LM#BBBBBBBB#ML#',
      '#LLMMMMMMMMMMLL#',
      '#LM#RR##DD##DML#',
      '#LM#RR##DD##DML#',
      '#LM#OO##OO##OML#',
      '#LMDDDDDDDDDDML#',
      '#LM#D#BBOO#D#ML#',
      '#LM#D#BBBB#D#ML#',
      '#LLMMMMMMMMMMLL#',
      '################',
    ],
  },

  // -----------------------------------------------------------------------
  // 5. REACTOR CONTROLLER FRONT (ACTIVE - Glowing Indicators & Core Monitor)
  // -----------------------------------------------------------------------
  {
    id: 'reactor_controller_front_active',
    nameUk: 'Контролер (активна реакція)',
    kind: 'block',
    zipPath: 'src/main/resources/assets/ienuclear/textures/block/reactor_controller_front_active.png',
    description: 'Лицьова індустріальна панель з активним пірометром та зеленим сигналом розгону',
    rawBase64: RAW_IE_TEXTURES['mod_block_reactor_controller_front_active'],
    palette: {
      '#': '#1B212B',
      D: '#2A323F',
      M: '#3E495B',
      L: '#5C6B82',
      B: '#B45309', // Brass bezel
      Y: '#FDE047', // Dial face glowing
      N: '#DC2626', // Needle rotated right
      G: '#10B981', // Active green lamp
      E: '#34D399',
      C: '#06B6D4',
      O: '#064E3B',
    },
    grid: [
      '################',
      '#LLMMMMMMMMMMLL#',
      '#LM#BBBBBBBB#ML#',
      '#LMBBYYYYYYBBML#',
      '#LMBYYYNNYYYBML#',
      '#LMBYYYYYNNYBML#',
      '#LM#BBBBBBBB#ML#',
      '#LLMMMMMMMMMMLL#',
      '#LM#DD##GG##CML#',
      '#LM#DD##EE##CML#',
      '#LM#OO##EE##OML#',
      '#LMDDDDDDDDDDML#',
      '#LM#D#BBGG#D#ML#',
      '#LM#D#BBBB#D#ML#',
      '#LLMMMMMMMMMMLL#',
      '################',
    ],
  },

  // -----------------------------------------------------------------------
  // 6. COOLANT INPUT PORT (IE Fluid Pump Flange with Valve Indicator)
  // -----------------------------------------------------------------------
  {
    id: 'coolant_input_port',
    nameUk: 'Порт входу охолоджувача (IE Pump Flange)',
    kind: 'block',
    zipPath: 'src/main/resources/assets/ienuclear/textures/block/coolant_input_port.png',
    description: 'Масивний рідинний фланець Immersive Engineering для підключення труб Fluid Pipe',
    rawBase64: RAW_IE_TEXTURES['mod_block_coolant_input_port'],
    palette: {
      '#': '#1C222C',
      M: '#3C4758',
      L: '#58677E',
      P: '#92400E', // Heavy copper/brass
      K: '#D97706',
      C: '#0891B2', // Coolant blue
      W: '#22D3EE',
      S: '#082F49',
    },
    grid: [
      '################',
      '#LLMMMMMMMMMMLL#',
      '#LMLLLLLLLLLLML#',
      '#LML#PPPPPP#LML#',
      '#LMLP#KKKK#PLML#',
      '#LMLPKSSSSKPLML#',
      '#LMLPKSCWCKPLML#',
      '#LMLPKSCWCKPLML#',
      '#LMLPKCCCCKPLML#',
      '#LMLPKSSSSKPLML#',
      '#LMLP#KKKK#PLML#',
      '#LML#PPPPPP#LML#',
      '#LMLLLLLLLLLLML#',
      '#LMCCMMMMMMCCML#',
      '#LLMMMMMMMMMMLL#',
      '################',
    ],
  },

  // -----------------------------------------------------------------------
  // 7. ENERGY OUTPUT PORT (IE HV Connector & Breaker Terminal)
  // -----------------------------------------------------------------------
  {
    id: 'energy_output_port',
    nameUk: 'Порт виходу енергії (IE HV Connector)',
    kind: 'block',
    zipPath: 'src/main/resources/assets/ienuclear/textures/block/energy_output_port.png',
    description: 'Високовольтний енергетичний ізолятор Immersive Engineering для відбору FE',
    rawBase64: RAW_IE_TEXTURES['mod_block_energy_output_port'],
    palette: {
      '#': '#1B212B',
      M: '#3C4758',
      L: '#5A6981',
      P: '#9A3412', // Terracotta insulator
      K: '#D97706', // Copper contact
      A: '#F59E0B',
      Y: '#FDE047',
      S: '#1C1917',
    },
    grid: [
      '################',
      '#LLMMMMMMMMMMLL#',
      '#LMLLLLLLLLLLML#',
      '#LML#PPPPPP#LML#',
      '#LMLP#KKKK#PLML#',
      '#LMLPKSYYSKPLML#',
      '#LMLPKSAASKPLML#',
      '#LMLPKYAAYKPLML#',
      '#LMLPKSAASKPLML#',
      '#LMLPKSYYSKPLML#',
      '#LMLP#KKKK#PLML#',
      '#LML#PPPPPP#LML#',
      '#LMLLLLLLLLLLML#',
      '#LMAAMMMMMMAAML#',
      '#LLMMMMMMMMMMLL#',
      '################',
    ],
  },

  // =======================================================================
  // ITEMS: NUCLEAR FUEL CAPSULES ("стержні зроби як капсули")
  // =======================================================================

  // -----------------------------------------------------------------------
  // 8. EMPTY FUEL CAPSULE (Reinforced Transparent Quartz-Steel Capsule)
  // -----------------------------------------------------------------------
  {
    id: 'empty_fuel_rod',
    nameUk: 'Порожня паливна капсула',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/empty_fuel_rod.png',
    description: 'Герметична кварцово-сталева циліндрична капсула з різьбовими кришками для палива',
    rawBase64: RAW_IE_TEXTURES['mod_item_empty_fuel_rod'],
    palette: {
      '.': '#00000000',
      O: '#1E242E',
      C: '#B45309', // Threaded brass endcap
      K: '#EA580C',
      S: '#475569', // Steel rib
      M: '#94A3B8', // Glass reflection
      G: '#E2E8F0', // Pure glass highlight
      T: '#38BDF833', // Subtle interior transparency
    },
    grid: [
      '.....OOOOOO.....',
      '....OCKKKKCO....',
      '....OCCCCCCCO...'.slice(0, 16),
      '....OSMMMMSS....',
      '....OSGGTTSS....',
      '....OSMTTTSS....',
      '....OSGTTTSS....',
      '....OSMTTTSS....',
      '....OSGTTTSS....',
      '....OSMTTTSS....',
      '....OSGTTTSS....',
      '....OSMMMMSS....',
      '....OCCCCCCCO...'.slice(0, 16),
      '....OCKKKKCO....',
      '.....OOOOOO.....',
      '................',
    ],
  },

  // -----------------------------------------------------------------------
  // 9. NUCLEAR FUEL CAPSULE (Loaded Uranium-238 / Natural Fuel Capsule)
  // -----------------------------------------------------------------------
  {
    id: 'nuclear_fuel_rod',
    nameUk: 'Ядерна паливна капсула',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/nuclear_fuel_rod.png',
    description: 'Споряджена капсула з пресованими таблетками природного урану в захисній оболонці',
    rawBase64: RAW_IE_TEXTURES['mod_item_nuclear_fuel_rod'],
    palette: {
      '.': '#00000000',
      O: '#1E242E',
      C: '#B45309', // Brass endcap
      K: '#EA580C',
      S: '#475569',
      M: '#CBD5E1', // Glass reflection
      U: '#4D7C0F', // Uranium core
      G: '#65A30D', // Glowing lime
      L: '#A3E635', // Highlight
      H: '#D9F99D',
    },
    grid: [
      '.....OOOOOO.....',
      '....OCKKKKCO....',
      '....OCCCCCCCO...'.slice(0, 16),
      '....OSMMMMSS....',
      '....OSMUGUSS....',
      '....OSMLLUSS....',
      '....OSMHHGSS....',
      '....OSMUGUSS....',
      '....OSMLLUSS....',
      '....OSMHHGSS....',
      '....OSMUGUSS....',
      '....OSMMMMSS....',
      '....OCCCCCCCO...'.slice(0, 16),
      '....OCKKKKCO....',
      '.....OOOOOO.....',
      '................',
    ],
  },

  // -----------------------------------------------------------------------
  // 10. ENRICHED NUCLEAR FUEL CAPSULE (U-235 High-Yield Cherenkov Capsule)
  // -----------------------------------------------------------------------
  {
    id: 'enriched_nuclear_fuel_rod',
    nameUk: 'Збагачена паливна капсула U-235',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/enriched_nuclear_fuel_rod.png',
    description: 'Високозбагачена капсула U-235 із попереджувальними смугами та інтенсивним світінням',
    rawBase64: RAW_IE_TEXTURES['mod_item_enriched_nuclear_fuel_rod'],
    palette: {
      '.': '#00000000',
      O: '#0F172A',
      Y: '#F59E0B', // Hazard warning stripe
      B: '#1E293B', // Dark hazard stripe
      S: '#334155', // Armored cage
      M: '#F8FAFC', // Glass glare
      E: '#047857', // Deep radioactive green
      C: '#10B981', // Brilliant emerald
      L: '#34D399', // Cherenkov radiation
      W: '#A7F3D0', // White-hot core
    },
    grid: [
      '.....OOOOOO.....',
      '....OYBYBYBO....',
      '....OYBYBYBO....',
      '....OSMMMMSS....',
      '....OSMEEWSS....',
      '....OSMCLLSS....',
      '....OSMWLCSS....',
      '....OSMEEWSS....',
      '....OSMCLLSS....',
      '....OSMWLCSS....',
      '....OSMEEWSS....',
      '....OSMMMMSS....',
      '....OYBYBYBO....',
      '....OYBYBYBO....',
      '.....OOOOOO.....',
      '................',
    ],
  },

  // -----------------------------------------------------------------------
  // 11. DEPLETED FUEL CAPSULE (Spent Nuclear Waste & Scorched Slag)
  // -----------------------------------------------------------------------
  {
    id: 'depleted_fuel_rod',
    nameUk: 'Відпрацьована паливна капсула',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/depleted_fuel_rod.png',
    description: 'Опалена розтріскана капсула з радіоактивним попелом та відпрацьованим шлаком',
    rawBase64: RAW_IE_TEXTURES['mod_item_depleted_fuel_rod'],
    palette: {
      '.': '#00000000',
      O: '#18181B',
      C: '#52525B', // Burnt metal endcaps
      K: '#3F3F46',
      S: '#27272A',
      M: '#71717A',
      D: '#1C1917', // Scorched slag
      A: '#78350F', // Radioactive residue
      Y: '#CA8A04', // Warning spots
    },
    grid: [
      '.....OOOOOO.....',
      '....OCKKKKCO....',
      '....OCCCCCCCO...'.slice(0, 16),
      '....OSSSSSSS....',
      '....OSMDDDSS....',
      '....OSDDADSS....',
      '....OSDADDSS....',
      '....OSDDYDSS....',
      '....OSMDDDSS....',
      '....OSDADDSS....',
      '....OSDDADSS....',
      '....OSSSSSSS....',
      '....OCCCCCCCO...'.slice(0, 16),
      '....OCKKKKCO....',
      '.....OOOOOO.....',
      '................',
    ],
  },

  // =======================================================================
  // ITEMS: CLASSIC MINECRAFT BUCKETS ("відра як в оригінальному minecraft")
  // =======================================================================

  // -----------------------------------------------------------------------
  // 12. HEAVY COOLANT BUCKET (Classic Minecraft Vanilla Bucket Silhouette)
  // -----------------------------------------------------------------------
  {
    id: 'coolant_bucket',
    nameUk: 'Відро важкого охолоджувача',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/coolant_bucket.png',
    description: 'Класичне залізне відро Minecraft із боро-водним охолоджувачем Heavy Coolant',
    rawBase64: RAW_IE_TEXTURES['mod_item_coolant_bucket'],
    palette: {
      '.': '#00000000',
      '#': '#383838', // Vanilla bucket dark border
      D: '#585858', // Dark iron
      M: '#8A8A8A', // Base iron
      L: '#B8B8B8', // Light iron
      H: '#DFDFDF', // Highlight rim
      C: '#0891B2', // Heavy Coolant liquid
      B: '#06B6D4',
      W: '#67E8F9',
    },
    grid: [
      '................',
      '................',
      '..##........##..',
      '.#HD#......#HD#.',
      '.#HL########ML#.',
      '.#HDBWWCCCCCLD#.',
      '..#MBWCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#MCCCCCCCCMD..',
      '...#LCCCCCCMD...',
      '...#MDCCCCDMD...',
      '....#DDDDDD#....',
      '.....######.....',
      '................',
    ],
  },

  // -----------------------------------------------------------------------
  // 13. URANIUM HEXAFLUORIDE BUCKET (Classic Minecraft Vanilla Bucket)
  // -----------------------------------------------------------------------
  {
    id: 'uranium_hexafluoride_bucket',
    nameUk: 'Відро гексафториду урану (UF6)',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/uranium_hexafluoride_bucket.png',
    description: 'Класичне залізне відро Minecraft із радіоактивним гексафторидом урану',
    rawBase64: RAW_IE_TEXTURES['mod_item_uranium_hexafluoride_bucket'],
    palette: {
      '.': '#00000000',
      '#': '#383838', // Vanilla bucket dark border
      D: '#585858', // Dark iron
      M: '#8A8A8A', // Base iron
      L: '#B8B8B8', // Light iron
      H: '#DFDFDF', // Highlight rim
      C: '#65A30D', // Toxic UF6 fluid
      B: '#84CC16',
      W: '#BEF264',
    },
    grid: [
      '................',
      '................',
      '..##........##..',
      '.#HD#......#HD#.',
      '.#HL########ML#.',
      '.#HDBWWCCCCCLD#.',
      '..#MBWCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#MCCCCCCCCMD..',
      '...#LCCCCCCMD...',
      '...#MDCCCCDMD...',
      '....#DDDDDD#....',
      '.....######.....',
      '................',
    ],
  },

  // -----------------------------------------------------------------------
  // 14. ENRICHED URANIUM SOLUTION BUCKET (Classic Minecraft Vanilla Bucket)
  // -----------------------------------------------------------------------
  {
    id: 'enriched_uranium_bucket',
    nameUk: 'Відро збагаченого розчину U-235',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/enriched_uranium_bucket.png',
    description: 'Класичне залізне відро Minecraft зі світною смарагдовою рідиною U-235',
    rawBase64: RAW_IE_TEXTURES['mod_item_enriched_uranium_bucket'],
    palette: {
      '.': '#00000000',
      '#': '#383838', // Vanilla bucket dark border
      D: '#585858', // Dark iron
      M: '#8A8A8A', // Base iron
      L: '#B8B8B8', // Light iron
      H: '#DFDFDF', // Highlight rim
      C: '#059669', // Emerald U-235 Solution
      B: '#10B981',
      W: '#6EE7B7',
    },
    grid: [
      '................',
      '................',
      '..##........##..',
      '.#HD#......#HD#.',
      '.#HL########ML#.',
      '.#HDBWWCCCCCLD#.',
      '..#MBWCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#LCCCCCCCCMD..',
      '..#MCCCCCCCCMD..',
      '...#LCCCCCCMD...',
      '...#MDCCCCDMD...',
      '....#DDDDDD#....',
      '.....######.....',
      '................',
    ],
  },

  // =======================================================================
  // ITEMS: URANIUM ORE PROCESSING (Immersive Engineering Style)
  // =======================================================================

  // -----------------------------------------------------------------------
  // 15. YELLOWCAKE URANIUM (Immersive Engineering Uranium Dust Style)
  // -----------------------------------------------------------------------
  {
    id: 'yellowcake_uranium',
    nameUk: 'Урановий концентрат (Yellowcake)',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/yellowcake_uranium.png',
    description: 'Жовтий оксид урану U3O8 у формі характерного пилу Immersive Engineering',
    rawBase64: RAW_IE_TEXTURES['mod_item_yellowcake_uranium'],
    palette: {
      '.': '#00000000',
      O: '#713F12',
      D: '#A16207',
      M: '#CA8A04',
      L: '#EAB308',
      H: '#FEF08A',
    },
    grid: [
      '................',
      '................',
      '.......OO.......',
      '......OMMO......',
      '.....OMLLMO.....',
      '....OMLHLLMO....',
      '...OMLLMMLLMO...',
      '...OMLMDDMLMO...',
      '..OMLMMDDMMLMO..',
      '..OMMDDMMMMDMO..',
      '.OMDDMMMMMMMDDMO'.slice(0, 16),
      '.ODMMMMMMMMMMDDO'.slice(0, 16),
      '.OODDDDDDDDDDDOO'.slice(0, 16),
      '..OOOOOOOOOOOO..',
      '................',
      '................',
    ],
  },

  // -----------------------------------------------------------------------
  // 16. ENRICHED URANIUM PELLET (IE Metal Press Uranium Slug)
  // -----------------------------------------------------------------------
  {
    id: 'enriched_uranium_pellet',
    nameUk: 'Паливна таблетка U-235',
    kind: 'item',
    zipPath: 'src/main/resources/assets/ienuclear/textures/item/enriched_uranium_pellet.png',
    description: 'Надщільна паливна таблетка з діоксиду збагаченого урану зі станка IE Metal Press',
    rawBase64: RAW_IE_TEXTURES['mod_item_enriched_uranium_pellet'],
    palette: {
      '.': '#00000000',
      O: '#064E3B',
      D: '#047857',
      M: '#059669',
      L: '#10B981',
      H: '#6EE7B7',
      Y: '#FBBF24', // Trefoil radiation mark
    },
    grid: [
      '................',
      '................',
      '.....OOOOOO.....',
      '....ODLLLLDO....',
      '...ODLHMYHLDO...',
      '...ODLMMMMMLO...',
      '...ODMMMYMMMDO..',
      '...ODMMYYYMMDO..',
      '...ODMYMYMYMDO..',
      '...ODMMMYMMMDO..',
      '...ODLLMMLLDDO..',
      '....ODDDDDDO....',
      '.....OOOOOO.....',
      '................',
      '................',
      '................',
    ],
  },
];

let cachedTextures: RenderedTexture[] | null = null;

export function getRenderedTextures(): RenderedTexture[] {
  if (cachedTextures) return cachedTextures;
  cachedTextures = PIXEL_TEXTURE_DEFS.map((def) => {
    // If the texture has a direct rawBase64 from IE (and is valid), use it, otherwise encode via pure PNG generator
    if (def.rawBase64) {
      try {
        const pngBytes = base64ToUint8(def.rawBase64);
        return {
          ...def,
          pngBytes,
          dataUrl: def.rawBase64,
        };
      } catch {
        // Fallback to procedural grid
      }
    }

    const pngBytes = encodePng16x16(def.grid, def.palette);
    const dataUrl = `data:image/png;base64,${uint8ToBase64(pngBytes)}`;
    return {
      ...def,
      pngBytes,
      dataUrl,
    };
  });
  return cachedTextures;
}

export function getTextureById(id: string): RenderedTexture | undefined {
  return getRenderedTextures().find((t) => t.id === id);
}
