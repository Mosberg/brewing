# Remote index (Brewing)

This file is a curated “pinned reference set” for the exact Minecraft/Fabric/Yarn APIs this repo currently targets.

If you need to verify signatures, _prefer the version-pinned Javadocs links below_.

## Versions (authoritative)

- Yarn mappings (1.21.11+build.4): https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/
- Fabric API (0.141.1+1.21.11): https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/
- Fabric Loader (0.18.4): https://maven.fabricmc.net/docs/fabric-loader-0.18.4/
- Java 21 API: https://docs.oracle.com/en/java/javase/21/docs/api/

## Key APIs used by this repo (deep links)

### BlockEntity persistence (1.21.11 ReadView/WriteView)

- BlockEntity: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/block/entity/BlockEntity.html
- ReadView: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/storage/ReadView.html
- WriteView: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/storage/WriteView.html

Where it matters in this repo:

- `dk.mosberg.brewing.block.entity.ContainerBlockEntity` overrides `readData(ReadView)` / `writeData(WriteView)`.
- `dk.mosberg.brewing.state.ContainerPayload` serializes to/from views.

### ItemStack components (CUSTOM_DATA)

- ItemStack: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/item/ItemStack.html
- DataComponentTypes (field `CUSTOM_DATA`): https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/component/DataComponentTypes.html
- NbtComponent: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/component/type/NbtComponent.html

Where it matters in this repo:

- `dk.mosberg.brewing.state.BrewingCustomData` reads/writes `DataComponentTypes.CUSTOM_DATA`.
- `dk.mosberg.brewing.state.ContainerStateStorage` reads/writes container payload under `CUSTOM_DATA`.

### Resource reload (server datapack load/reload)

- Fabric ResourceLoader: https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/net/fabricmc/fabric/api/resource/v1/ResourceLoader.html
- Yarn SynchronousResourceReloader: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/resource/SynchronousResourceReloader.html
- Yarn ResourceType: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/resource/ResourceType.html
- Yarn ResourceManager: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/resource/ResourceManager.html

Where it matters in this repo:

- `dk.mosberg.brewing.data.loader.BrewingDataReloadListener` registers with `ResourceLoader.get(ResourceType.SERVER_DATA)`.
- `dk.mosberg.brewing.data.loader.BrewingDataLoader` loads JSON via `ResourceManager`.

### Misc: identifiers and registry keys

- Identifier: https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/net/minecraft/util/Identifier.html

## Internal code map (start here)

### Entrypoints

- `src/main/java/dk/mosberg/brewing/Brewing.java` (common init)
- `src/client/java/dk/mosberg/brewing/client/BrewingClient.java` (client init)

### Data layer (JSON -> typed runtime data)

- `src/main/java/dk/mosberg/brewing/data/` (runtime data types + manager)
- `src/main/java/dk/mosberg/brewing/data/loader/BrewingDataLoader.java` (loads built-in server data JSON)
- `src/main/java/dk/mosberg/brewing/data/loader/BrewingDataReloadListener.java` (reload hook)

### Dynamic registration

- `src/main/java/dk/mosberg/brewing/registry/ModItems.java` (registers items from `assets/brewing/items/**/*.json`)
- `src/main/java/dk/mosberg/brewing/registry/ModBlocks.java` (registers blocks from `data/brewing/**`)
- `src/main/java/dk/mosberg/brewing/registry/ModBlockEntities.java` (BE types)
- `src/main/java/dk/mosberg/brewing/registry/ModItemGroups.java` (creative tab)

### Container payload + state_storage

- `src/main/java/dk/mosberg/brewing/state/ContainerStateStorage.java` (schema-driven placement/break rules + merge)
- `src/main/java/dk/mosberg/brewing/state/BrewingCustomData.java` (ItemStack CUSTOM_DATA wrapper)
- `src/main/java/dk/mosberg/brewing/state/ContainerPayload.java` (payload serialization)
- `src/main/java/dk/mosberg/brewing/block/ContainerBlock.java` (placement + drops behavior)
- `src/main/java/dk/mosberg/brewing/block/entity/ContainerBlockEntity.java` (persistent + optional sync)

## Build tooling

- Fabric Loom (upstream): https://github.com/FabricMC/fabric-loom
- Gradle configuration cache (reference): https://docs.gradle.org/current/userguide/configuration_cache.html

## VS Code tooling

- Datapack Language Server (Spyglass-based): https://marketplace.visualstudio.com/items?itemName=SPGoding.datapack-language-server
- mcfunction syntax highlighting: https://marketplace.visualstudio.com/items?itemName=MinecraftCommands.syntax-mcfunction

Repo config:

- `spyglass.json` lives at the repo root and is used by the datapack language server for game-version-aware validation.

Quick troubleshooting:

- Most Spyglass diagnostics apply to `src/main/resources/data/**` (tags/recipes/loot/advancements).
- For optional cross-mod tag entries, prefer `{ "id": "othermod:thing", "required": false }` to avoid datapack load errors.

## Upstream repos

- Spyglass (MC data / language tooling): https://github.com/SpyglassMC/Spyglass
- fabricmc.net site: https://github.com/FabricMC/fabricmc.net
- Fabric API: https://github.com/FabricMC/fabric-api
- Yarn mappings: https://github.com/FabricMC/yarn
- Fabric Loader: https://github.com/FabricMC/fabric-loader
- Fabric example mod: https://github.com/FabricMC/fabric-example-mod
- Fabric Loom: https://github.com/FabricMC/fabric-loom
- Fabric docs: https://github.com/FabricMC/fabric-docs
- DataFixerUpper: https://github.com/Mojang/DataFixerUpper

## Local/offline lookup

- Crash reports: `run/crash-reports/`
- Gradle/Loom caches: `.gradle/loom-cache/` (useful for pinned Minecraft + Yarn artifacts)

Offline signature lookup tips:

- The best “truth source” offline is the mapped sources JAR Loom downloads (look under `.gradle/loom-cache/`).
- If you need to grep signatures, extract the sources JAR to a temp folder (keep it out of git) and search it like normal Java source.

## How to use these links

- Prefer the version-pinned Javadocs above when verifying method names/signatures for this project.
- Prefer upstream repos for implementation details, patterns, and migration notes.
- When updating dependencies, update `gradle.properties` first, then validate compilation with `./gradlew build`.
- After any API bump, also validate runtime with `./gradlew runClient --no-daemon` (many 1.21.x breaks are runtime-only).
