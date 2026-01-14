# Remote index (Brewing)

This file exists so agents can quickly find and consult the exact external references this repo targets.

## API docs (version-pinned)

- Yarn mappings (1.21.11+build.4): https://maven.fabricmc.net/docs/yarn-1.21.11+build.4/
- Fabric API (0.141.1+1.21.11): https://maven.fabricmc.net/docs/fabric-api-0.141.1+1.21.11/
- Fabric Loader (0.18.4): https://maven.fabricmc.net/docs/fabric-loader-0.18.4/
- DataFixerUpper (4.0.0-SNAPSHOT API): https://kvverti.github.io/Documented-DataFixerUpper/snapshot/overview-summary.html

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
- DataFixerUpper docs: https://github.com/kvverti/Documented-DataFixerUpper

## Local/offline lookup

- Crash reports: `run/crash-reports/`
- Gradle/Loom caches: `.gradle/loom-cache/` (useful for pinned Minecraft + Yarn artifacts)
- If you need exact MC/Yarn signatures offline, extracting sources from the loom-cache jar into a temp folder works well (keep the extracted folder out of git).

## How to use these links

- Prefer the version-pinned Javadocs above when verifying method names/signatures for this project.
- Prefer upstream repos for implementation details, patterns, and migration notes.
- When updating dependencies, update `gradle.properties` first, then validate compilation with `./gradlew build`.
- After any API bump, also validate runtime with `./gradlew runClient --no-daemon` (many 1.21.x breaks are runtime-only).
