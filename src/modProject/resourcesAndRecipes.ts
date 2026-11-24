import type { ModFileEntry } from './gradleAndConfig';

export const RESOURCE_AND_RECIPE_FILES: ModFileEntry[] = [
  {
    path: 'src/main/resources/assets/ienuclear/lang/uk_ua.json',
    language: 'json',
    category: 'resources',
    description: 'Українська локалізація блоків, предметів, рідин та інтерфейсу реактора',
    content: `{
  "itemGroup.ienuclear": "Immersive Nuclear",
  "block.ienuclear.reactor_controller": "Контролер ядерного реактора",
  "block.ienuclear.reactor_casing": "Корпус реактора (Свинець-Сталь)",
  "block.ienuclear.reactor_core_rod": "Стрижень активної зони",
  "block.ienuclear.reactor_glass": "Радіаційно-стійке скло",
  "block.ienuclear.coolant_input_port": "Порт входу охолоджувача",
  "block.ienuclear.energy_output_port": "Порт виходу енергії (FE)",
  "item.ienuclear.yellowcake_uranium": "Урановий концентрат (Жовтий кек)",
  "item.ienuclear.enriched_uranium_pellet": "Таблетка збагаченого урану U-235",
  "item.ienuclear.empty_fuel_rod": "Порожня паливна капсула",
  "item.ienuclear.nuclear_fuel_rod": "Ядерна паливна капсула",
  "item.ienuclear.enriched_nuclear_fuel_rod": "Збагачена паливна капсула U-235 (2x)",
  "item.ienuclear.depleted_fuel_rod": "Відпрацьована паливна капсула",
  "item.ienuclear.coolant_bucket": "Відро важкого охолоджувача",
  "item.ienuclear.uranium_hexafluoride_bucket": "Відро гексафториду урану (UF6)",
  "item.ienuclear.enriched_uranium_bucket": "Відро збагаченого розчину урану",
  "fluid.ienuclear.heavy_coolant": "Важкий боро-водний охолоджувач",
  "fluid.ienuclear.uranium_hexafluoride": "Гексафторид урану (UF6)",
  "fluid.ienuclear.enriched_uranium_solution": "Збагачений розчин урану",
  "gui.ienuclear.start": "ЗАПУСК (РУБИЛЬНИК)",
  "gui.ienuclear.stop": "ЗУПИНКА (SCRAM)",
  "gui.ienuclear.meltdown_warning": "КРИТИЧНИЙ ПЕРЕГРІВ! ЗАГРОЗА РОЗПЛАВЛЕННЯ!",
  "message.ienuclear.multiblock_incomplete": "Мультиблок 3x3x3 не зібрано! Перевірте корпус, центр та порти.",
  "message.ienuclear.meltdown": "КАТАСТРОФІЧНИЙ ВИБУХ: Активна зона реактора розплавилась через відсутність охолоджувача!",
  "ie.manual.category.ienuclear.nuclear_category.name": "Immersive Nuclear",
  "ie.manual.entry.ienuclear.nuclear_reactor.name": "Ядерний реактор (3×3×3)",
  "ie.manual.entry.ienuclear.uranium_enrichment.name": "Збагачення урану, капсули та охолоджувач"
}
`
  },
  {
    path: 'src/main/resources/assets/ienuclear/lang/en_us.json',
    language: 'json',
    category: 'resources',
    description: 'English localization for blocks, items, fluids, and GUI',
    content: `{
  "itemGroup.ienuclear": "Immersive Nuclear",
  "block.ienuclear.reactor_controller": "Nuclear Reactor Controller",
  "block.ienuclear.reactor_casing": "Lead-Steel Reactor Casing",
  "block.ienuclear.reactor_core_rod": "Reactor Core Assembly",
  "block.ienuclear.reactor_glass": "Radiation-Shielded Glass",
  "block.ienuclear.coolant_input_port": "Coolant Input Port",
  "block.ienuclear.energy_output_port": "FE Energy Output Port",
  "item.ienuclear.yellowcake_uranium": "Yellowcake Uranium Concentrate",
  "item.ienuclear.enriched_uranium_pellet": "Enriched Uranium Pellet (U-235)",
  "item.ienuclear.empty_fuel_rod": "Empty Fuel Capsule",
  "item.ienuclear.nuclear_fuel_rod": "Nuclear Fuel Capsule",
  "item.ienuclear.enriched_nuclear_fuel_rod": "Enriched Nuclear Fuel Capsule (U-235)",
  "item.ienuclear.depleted_fuel_rod": "Depleted Fuel Capsule",
  "item.ienuclear.coolant_bucket": "Heavy Coolant Bucket",
  "item.ienuclear.uranium_hexafluoride_bucket": "Uranium Hexafluoride Bucket",
  "item.ienuclear.enriched_uranium_bucket": "Enriched Uranium Solution Bucket",
  "fluid.ienuclear.heavy_coolant": "Heavy Borated Coolant",
  "fluid.ienuclear.uranium_hexafluoride": "Uranium Hexafluoride",
  "fluid.ienuclear.enriched_uranium_solution": "Enriched Uranium Solution",
  "gui.ienuclear.start": "ENGAGE KNIFE SWITCH",
  "gui.ienuclear.stop": "EMERGENCY SCRAM",
  "gui.ienuclear.meltdown_warning": "CRITICAL OVERHEAT! MELTDOWN IMMINENT!",
  "message.ienuclear.multiblock_incomplete": "3x3x3 Multiblock incomplete! Check casing, core rod, and I/O ports.",
  "message.ienuclear.meltdown": "CATASTROPHIC MELTDOWN: Core breached due to lack of coolant!",
  "ie.manual.category.ienuclear.nuclear_category.name": "Immersive Nuclear",
  "ie.manual.entry.ienuclear.nuclear_reactor.name": "Nuclear Reactor (3x3x3)",
  "ie.manual.entry.ienuclear.uranium_enrichment.name": "Uranium Enrichment, Capsules & Coolant"
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/ie_crusher_yellowcake.json',
    language: 'json',
    category: 'recipes',
    description: 'IE Crusher: подрібнення сирого урану / руди в урановий концентрат (Yellowcake)',
    content: `{
  "type": "immersiveengineering:crusher",
  "energy": 4800,
  "input": {
    "tag": "c:raw_materials/uranium"
  },
  "result": {
    "count": 2,
    "id": "ienuclear:yellowcake_uranium"
  },
  "secondaries": [
    {
      "chance": 0.35,
      "output": {
        "count": 1,
        "id": "immersiveengineering:dust_uranium"
      }
    }
  ]
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/ie_mixer_uranium_hexafluoride.json',
    language: 'json',
    category: 'recipes',
    description: 'IE Mixer: розчинення Yellowcake Uranium + Redstone у воді -> Гексафторид урану (UF6)',
    content: `{
  "type": "immersiveengineering:mixer",
  "energy": 6400,
  "fluid": {
    "amount": 1000,
    "tag": "minecraft:water"
  },
  "inputs": [
    {
      "count": 2,
      "basePredicate": {
        "item": "ienuclear:yellowcake_uranium"
      }
    },
    {
      "count": 2,
      "basePredicate": {
        "tag": "c:dusts/redstone"
      }
    }
  ],
  "result": {
    "amount": 1000,
    "id": "ienuclear:uranium_hexafluoride"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/ie_refinery_enrich_uranium.json',
    language: 'json',
    category: 'recipes',
    description: 'IE Refinery: збагачення Гексафториду урану за допомогою етанолу/біодизелю -> Збагачений розчин U-235',
    content: `{
  "type": "immersiveengineering:refinery",
  "energy": 160,
  "input0": {
    "amount": 10,
    "fluid": "ienuclear:uranium_hexafluoride"
  },
  "input1": {
    "amount": 5,
    "tag": "c:ethanol"
  },
  "result": {
    "amount": 10,
    "id": "ienuclear:enriched_uranium_solution"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/ie_mixer_heavy_coolant.json',
    language: 'json',
    category: 'recipes',
    description: 'IE Mixer: виготовлення важкої охолоджувальної рідини (Вода + Лазурит + Селітра IE)',
    content: `{
  "type": "immersiveengineering:mixer",
  "energy": 3200,
  "fluid": {
    "amount": 1000,
    "tag": "minecraft:water"
  },
  "inputs": [
    {
      "count": 2,
      "basePredicate": {
        "tag": "c:gems/lapis"
      }
    },
    {
      "count": 2,
      "basePredicate": {
        "tag": "c:dusts/saltpeter"
      }
    }
  ],
  "result": {
    "amount": 1000,
    "id": "ienuclear:heavy_coolant"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/ie_metal_press_empty_rod.json',
    language: 'json',
    category: 'recipes',
    description: 'IE Metal Press: штампування сталевих пластин (mold_rod) у порожній паливний стрижень',
    content: `{
  "type": "immersiveengineering:metal_press",
  "energy": 3600,
  "input": {
    "count": 2,
    "basePredicate": {
      "tag": "c:plates/steel"
    }
  },
  "mold": "immersiveengineering:mold_rod",
  "result": {
    "count": 1,
    "id": "ienuclear:empty_fuel_rod"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/ie_metal_press_uranium_pellet.json',
    language: 'json',
    category: 'recipes',
    description: 'IE Metal Press: пресування злитків урану (mold_bullet_casing) у паливні таблетки',
    content: `{
  "type": "immersiveengineering:metal_press",
  "energy": 4200,
  "input": {
    "count": 1,
    "basePredicate": {
      "tag": "c:ingots/uranium"
    }
  },
  "mold": "immersiveengineering:mold_bullet_casing",
  "result": {
    "count": 2,
    "id": "ienuclear:enriched_uranium_pellet"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/crafting_nuclear_fuel_rod.json',
    language: 'json',
    category: 'recipes',
    description: 'Крафт стандартного ядерного паливного стрижня з порожнього стрижня та таблеток урану',
    content: `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    " P ",
    "PEP",
    " P "
  ],
  "key": {
    "P": { "item": "ienuclear:enriched_uranium_pellet" },
    "E": { "item": "ienuclear:empty_fuel_rod" }
  },
  "result": {
    "count": 1,
    "id": "ienuclear:nuclear_fuel_rod"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/crafting_enriched_fuel_rod.json',
    language: 'json',
    category: 'recipes',
    description: 'Крафт збагаченого паливного стрижня (2x FE/t) з відром збагаченого розчину U-235',
    content: `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "PBP",
    "ERE",
    "PBP"
  ],
  "key": {
    "P": { "item": "ienuclear:enriched_uranium_pellet" },
    "B": { "item": "ienuclear:enriched_uranium_bucket" },
    "E": { "tag": "c:plates/lead" },
    "R": { "item": "ienuclear:empty_fuel_rod" }
  },
  "result": {
    "count": 1,
    "id": "ienuclear:enriched_nuclear_fuel_rod"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/crafting_reactor_casing.json',
    language: 'json',
    category: 'recipes',
    description: 'Крафт корпусу ядерного реактора зі сталевих та свинцевих пластин Immersive Engineering',
    content: `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "SLS",
    "LCL",
    "SLS"
  ],
  "key": {
    "S": { "tag": "c:plates/steel" },
    "L": { "tag": "c:plates/lead" },
    "C": { "item": "immersiveengineering:sheetmetal_steel" }
  },
  "result": {
    "count": 4,
    "id": "ienuclear:reactor_casing"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/crafting_reactor_core_rod.json',
    language: 'json',
    category: 'recipes',
    description: 'Крафт центрального стрижня активної зони мультиблоку',
    content: `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "RCR",
    "RUR",
    "RCR"
  ],
  "key": {
    "R": { "item": "ienuclear:empty_fuel_rod" },
    "C": { "tag": "c:ingots/steel" },
    "U": { "tag": "c:storage_blocks/uranium" }
  },
  "result": {
    "count": 1,
    "id": "ienuclear:reactor_core_rod"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/crafting_reactor_controller.json',
    language: 'json',
    category: 'recipes',
    description: 'Крафт контролера реактора з компонентами IE',
    content: `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "SCS",
    "ERE",
    "SLS"
  ],
  "key": {
    "S": { "item": "ienuclear:reactor_casing" },
    "C": { "item": "immersiveengineering:component_electronic_adv" },
    "E": { "item": "immersiveengineering:coil_hv" },
    "R": { "tag": "c:dusts/redstone" },
    "L": { "tag": "c:plates/lead" }
  },
  "result": {
    "count": 1,
    "id": "ienuclear:reactor_controller"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/crafting_coolant_port.json',
    language: 'json',
    category: 'recipes',
    description: 'Крафт вхідного порту охолоджувальної рідини (сумісний із трубами IE)',
    content: `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "SCS",
    "PBP",
    "SCS"
  ],
  "key": {
    "S": { "item": "ienuclear:reactor_casing" },
    "C": { "tag": "c:plates/copper" },
    "P": { "item": "immersiveengineering:fluid_pipe" },
    "B": { "item": "minecraft:bucket" }
  },
  "result": {
    "count": 1,
    "id": "ienuclear:coolant_input_port"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/recipe/crafting_energy_port.json',
    language: 'json',
    category: 'recipes',
    description: 'Крафт вихідного порту живлення FE (сумісний із HV конекторами IE)',
    content: `{
  "type": "minecraft:crafting_shaped",
  "pattern": [
    "SCS",
    "HGH",
    "SCS"
  ],
  "key": {
    "S": { "item": "ienuclear:reactor_casing" },
    "C": { "item": "immersiveengineering:coil_hv" },
    "H": { "item": "immersiveengineering:connector_hv" },
    "G": { "tag": "c:ingots/electrum" }
  },
  "result": {
    "count": 1,
    "id": "ienuclear:energy_output_port"
  }
}
`
  },
  {
    path: 'src/main/resources/data/ienuclear/tags/fluid/coolant.json',
    language: 'json',
    category: 'resources',
    description: 'Тег охолоджувальної рідини для сумісності з іншими модами NeoForge 1.21.1',
    content: `{
  "replace": false,
  "values": [
    "ienuclear:heavy_coolant"
  ]
}
`
  }
];
