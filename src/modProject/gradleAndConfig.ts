export interface ModFileEntry {
  path: string;
  language: 'java' | 'json' | 'groovy' | 'toml' | 'properties' | 'markdown';
  category: 'gradle' | 'java' | 'resources' | 'recipes';
  description: string;
  content: string;
}

export const GRADLE_AND_CONFIG_FILES: ModFileEntry[] = [
  {
    path: 'settings.gradle',
    language: 'groovy',
    category: 'gradle',
    description: 'Налаштування Gradle Plugin Management та репозиторію NeoForged',
    content: `pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = 'https://maven.neoforged.net/releases' }
    }
}

plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}

rootProject.name = 'ImmersiveNuclear'
`
  },
  {
    path: 'gradle.properties',
    language: 'properties',
    category: 'gradle',
    description: 'Версії Minecraft 1.21.1, NeoForge 21.1 та Immersive Engineering',
    content: `org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false

minecraft_version=1.21.1
minecraft_version_range=[1.21.1,1.22)
neo_version=21.1.77
neo_version_range=[21.1,)
loader_version_range=[4,)
parchment_minecraft_version=1.21.1
parchment_mappings_version=2024.11.17

mod_id=ienuclear
mod_name=Immersive Nuclear
mod_license=MIT
mod_version=1.0.0
mod_group_id=com.ienuclear.reactor
mod_authors=Immersive Nuclear Team
mod_description=Multiblock nuclear reactor with coolant input, FE power output, thermal control GUI, and Immersive Engineering uranium enrichment recipes.

# Immersive Engineering for MC 1.21.1 (ModMaven)
ie_version=1.21.1-12.4.2-194
`
  },
  {
    path: 'build.gradle',
    language: 'groovy',
    category: 'gradle',
    description: 'Збірка NeoForge 1.21.1 з ModDevGradle та залежністю Immersive Engineering',
    content: `plugins {
    id 'java-library'
    id 'maven-publish'
    id 'net.neoforged.moddev' version '2.0.42-beta'
}

version = mod_version
group = mod_group_id

base {
    archivesName = mod_id
}

java.toolchain.languageVersion = JavaLanguageVersion.of(21)

neoForge {
    version = project.neo_version

    parchment {
        mappingsVersion = project.parchment_mappings_version
        minecraftVersion = project.parchment_minecraft_version
    }

    runs {
        client {
            client()
        }
        server {
            server()
            programArgument '--nogui'
        }
        data {
            data()
            programArguments.addAll '--mod', project.mod_id, '--all', '--output', file('src/generated/resources/').getAbsolutePath(), '--existing', file('src/main/resources/').getAbsolutePath()
        }
    }

    mods {
        "\${mod_id}" {
            sourceSet(sourceSets.main)
        }
    }
}

repositories {
    mavenLocal()
    maven {
        name = 'ModMaven'
        url = uri('https://modmaven.dev/')
        content {
            includeGroup('blusunrize.immersiveengineering')
        }
    }
}

dependencies {
    implementation "blusunrize.immersiveengineering:ImmersiveEngineering:\${ie_version}"
}

tasks.withType(ProcessResources).configureEach {
    var replaceProperties = [
        minecraft_version      : minecraft_version,
        minecraft_version_range: minecraft_version_range,
        neo_version            : neo_version,
        neo_version_range      : neo_version_range,
        loader_version_range   : loader_version_range,
        mod_id                 : mod_id,
        mod_name               : mod_name,
        mod_license            : mod_license,
        mod_version            : mod_version,
        mod_authors            : mod_authors,
        mod_description        : mod_description
    ]
    inputs.properties replaceProperties
    filesMatching(['META-INF/neoforge.mods.toml']) {
        expand replaceProperties
    }
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}
`
  },
  {
    path: 'src/main/resources/META-INF/neoforge.mods.toml',
    language: 'toml',
    category: 'resources',
    description: 'Маніфест моду NeoForge 1.21.1 з обовʼязковою залежністю від Immersive Engineering',
    content: `modLoader="javafml"
loaderVersion="\${loader_version_range}"
license="\${mod_license}"

[[mods]]
modId="\${mod_id}"
version="\${mod_version}"
displayName="\${mod_name}"
authors="\${mod_authors}"
description='''\${mod_description}'''

[[dependencies.\${mod_id}]]
    modId="neoforge"
    type="required"
    versionRange="\${neo_version_range}"
    ordering="NONE"
    side="BOTH"

[[dependencies.\${mod_id}]]
    modId="minecraft"
    type="required"
    versionRange="\${minecraft_version_range}"
    ordering="NONE"
    side="BOTH"

[[dependencies.\${mod_id}]]
    modId="immersiveengineering"
    type="required"
    versionRange="[12.0.0,)"
    ordering="AFTER"
    side="BOTH"
`
  },
  {
    path: 'README.md',
    language: 'markdown',
    category: 'gradle',
    description: 'Інструкція з компіляції моду, побудови мультиблоку 3×3×3 та крафту палива',
    content: `# Immersive Nuclear (NeoForge 1.21.1 + Immersive Engineering)

Мод додає мультиблоковий ядерний реактор 3×3×3, сумісний із системою труб, дротів (FE) та верстатів **Immersive Engineering** для Minecraft **1.21.1 (NeoForge)**.

## Основні можливості
- **Мультиблок 3×3×3**:
  - \`ienuclear:reactor_casing\` — Корпус реактора (свинцево-сталевий захист)
  - \`ienuclear:reactor_core_rod\` — Стрижень активної зони (центр структури)
  - \`ienuclear:reactor_controller\` — Контролер (передня панель, відкриває GUI)
  - \`ienuclear:coolant_input_port\` — Вхід для охолоджувальної рідини (підтримує IE Fluid Pipes, 16,000 mB)
  - \`ienuclear:energy_output_port\` — Вихід живлення FE (автоматично передає енергію на HV/MV конектори IE, 1,000,000 FE буфер)
  - \`ienuclear:reactor_glass\` — Оглядове радіаційно-стійке скло (опціонально на стінах)
- **GUI Контролера**:
  - Шкала температури (20°C – 1200°C) із захисним авто-SCRAM при 1050°C
  - Рівень охолоджувальної рідини (mB)
  - Перемикач запуску / зупинки реакції (працює через серверний C2S пакет)
  - Слот ядерного палива та слот виводу відпрацьованих стрижнів
  - Індикатор накопиченої енергії (FE) та поточної генерації (FE/t)
- **Охолоджувальна рідина та збагачення урану (Immersive Engineering)**:
  - **Crusher**: Урановий пил + Редстоун -> \`yellowcake_uranium\`
  - **Mixer**: \`yellowcake_uranium\` + Вода -> рідина \`uranium_hexafluoride\`
  - **Mixer**: Вода + Лазурит + Селітра -> охолоджувач \`heavy_coolant\`
  - **Refinery**: \`uranium_hexafluoride\` + Біодизель -> \`enriched_uranium_solution\`
  - **Metal Press**: Сталеві пластини (\`mold_rod\`) -> \`empty_fuel_rod\`; Уран (\`mold_bullet_casing\`) -> \`enriched_uranium_pellet\`

## Компіляція (.jar)
1. Встановіть **JDK 21**.
2. Відкрийте папку проєкту в терміналі або IntelliJ IDEA.
3. Запустіть:
   \`\`\`bash
   ./gradlew build
   \`\`\`
4. Готовий файл зʼявиться у \`build/libs/ienuclear-1.0.0.jar\`.
5. Помістіть його в папку \`mods/\` разом із **Immersive Engineering 1.21.1**.
`
  }
];
