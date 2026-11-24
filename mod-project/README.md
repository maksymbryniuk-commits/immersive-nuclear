# Immersive Nuclear (NeoForge 1.21.1 + Immersive Engineering)

Мод додає мультиблоковий ядерний реактор 3×3×3, сумісний із системою труб, дротів (FE) та верстатів **Immersive Engineering** для Minecraft **1.21.1 (NeoForge)**.

## Основні можливості
- **Мультиблок 3×3×3**:
  - `ienuclear:reactor_casing` — Корпус реактора (свинцево-сталевий захист)
  - `ienuclear:reactor_core_rod` — Стрижень активної зони (центр структури)
  - `ienuclear:reactor_controller` — Контролер (передня панель, відкриває GUI)
  - `ienuclear:coolant_input_port` — Вхід для охолоджувальної рідини (підтримує IE Fluid Pipes, 16,000 mB)
  - `ienuclear:energy_output_port` — Вихід живлення FE (автоматично передає енергію на HV/MV конектори IE, 1,000,000 FE буфер)
  - `ienuclear:reactor_glass` — Оглядове радіаційно-стійке скло (опціонально на стінах)
- **GUI Контролера**:
  - Шкала температури (20°C – 1200°C) із захисним авто-SCRAM при 1050°C
  - Рівень охолоджувальної рідини (mB)
  - Перемикач запуску / зупинки реакції (працює через серверний C2S пакет)
  - Слот ядерного палива та слот виводу відпрацьованих стрижнів
  - Індикатор накопиченої енергії (FE) та поточної генерації (FE/t)
- **Охолоджувальна рідина та збагачення урану (Immersive Engineering)**:
  - **Crusher**: Урановий пил + Редстоун -> `yellowcake_uranium`
  - **Mixer**: `yellowcake_uranium` + Вода -> рідина `uranium_hexafluoride`
  - **Mixer**: Вода + Лазурит + Селітра -> охолоджувач `heavy_coolant`
  - **Refinery**: `uranium_hexafluoride` + Біодизель -> `enriched_uranium_solution`
  - **Metal Press**: Сталеві пластини (`mold_rod`) -> `empty_fuel_rod`; Уран (`mold_bullet_casing`) -> `enriched_uranium_pellet`

## Компіляція (.jar)
1. Встановіть **JDK 21**.
2. Відкрийте папку проєкту в терміналі або IntelliJ IDEA.
3. Запустіть:
   ```bash
   ./gradlew build
   ```
4. Готовий файл зʼявиться у `build/libs/ienuclear-1.0.0.jar`.
5. Помістіть його в папку `mods/` разом із **Immersive Engineering 1.21.1**.
