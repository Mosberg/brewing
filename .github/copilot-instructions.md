# Copilot instructions (Brewing / Fabric 1.21.11)

This repo is **resource/data heavy** (JSON + assets) and intentionally **data-first**. Prefer small, surgical changes that keep IDs and resource paths stable.

## Big picture

- Server data (datapack) lives in `src/main/resources/data/brewing/**`.
  - Schemas: `src/main/resources/data/brewing/schemas/*-schema.json`
  - Content: `alcohol_types/`, `ingredients/`, `methods/`, `equipment/`, `containers/`, `beverages/`
- Client assets (resourcepack) live in `src/main/resources/assets/brewing/**`.
  - Dynamic item definitions: `assets/brewing/items/**/*.json`
  - Models/textures/lang: `assets/brewing/models/**`, `assets/brewing/textures/**`, `assets/brewing/lang/en_us.json`
- Entrypoints:
  - Common: `dk.mosberg.brewing.Brewing`
  - Client: `dk.mosberg.brewing.client.BrewingClient` (client-only source set)
  - Datagen: `dk.mosberg.brewing.datagen.BrewingDataGenerator` (currently stub)

## Current architecture (authoritative)

- Runtime data reload:
  - Loader: `dk.mosberg.brewing.data.loader.BrewingDataLoader`
  - Reload hook: `dk.mosberg.brewing.data.loader.BrewingDataReloadListener`
  - Access: `dk.mosberg.brewing.data.BrewingDataManager.get()`
- Dynamic registration:
  - Items: `dk.mosberg.brewing.registry.ModItems` scans `assets/brewing/items/**` and registers one `Item` per JSON file path.
  - Blocks: `dk.mosberg.brewing.registry.ModBlocks` registers container/equipment blocks from built-in `data/brewing/**` json files.
  - BEs: `dk.mosberg.brewing.registry.ModBlockEntities` registers a BE type for container blocks and equipment blocks.
  - Creative tab: `dk.mosberg.brewing.registry.ModItemGroups`

## Workflows (PowerShell)

- Build: `./gradlew build`
- Run: `./gradlew runClient` / `./gradlew runServer`
- Datagen: `./gradlew runDatagen` (outputs are included from `src/main/generated/resources` via `build.gradle`)
- Crash triage: `./gradlew runClient --no-daemon`, then check `run/crash-reports/`.

### Data validation (optional)

- Validate brewing JSON: `python tools/validate_brewing_data.py`

## Loom split source sets

## Loom split source sets

- Client-only code must stay under `src/client/java/**` (rendering, model layers, client-only Minecraft classes).
- Common/server-safe code stays under `src/main/java/**`.

## Mod metadata templating

- `src/main/resources/fabric.mod.json` uses `${...}` placeholders expanded from `gradle.properties` by `processResources` in `build.gradle`.
- Change mod id/name/version/links/license in `gradle.properties`, not in `fabric.mod.json`.

## Content conventions (examples)

- Ingredient data is defined under `src/main/resources/data/brewing/ingredients/*.json` (e.g. `water.json`).
- The matching client item definition lives under `src/main/resources/assets/brewing/items/**` and points at a model (e.g. `assets/brewing/items/ingredients/water.json` → `brewing:item/ingredients/water`).
- Item models should reference `brewing:item/...` textures (items atlas).
- `src/main/resources/assets/brewing/lang/en_us.json` should contain all user-facing names plus any `text.*_key` strings referenced from JSON definitions.

## Container `state_storage` rules (must stay consistent)

Containers define placement/break transfer and defaults under `state_storage` in `data/brewing/containers/*.json`.

Current implementation (do not silently break):

- Item payload storage uses `ItemStack` `CUSTOM_DATA` under `brewing.payload`.
- Block payload storage uses `dk.mosberg.brewing.block.entity.ContainerBlockEntity`.
- Placement and drops honor `mode`, `conversion.on_place`, `conversion.on_break`, and `merge_strategy`.
- If `placed_block.sync_to_client` is enabled, BE payload changes must call an update (server chunk markForUpdate).

## Local tooling (optional but useful)

- Validate all brewing JSON against shipped schemas: `python tools/validate_brewing_data.py` (deps in `tools/requirements.txt`).
- Texture helpers live in `tools/noise_map_generator.py` (usage in `tools/guide.md`).

## 1.21.11 gotchas when adding registrations

- Set registry keys on `Item.Settings` / `AbstractBlock.Settings` **before** constructing `Item`/`Block` (avoids “id not set” crashes).
- For `BlockWithEntity`, implement the required `MapCodec` (`getCodec()` override) for 1.21.x.
- When storing custom stack state, prefer components (`DataComponentTypes.CUSTOM_DATA`) over ad-hoc NBT.

## “Do / Don’t” when editing this repo

- Do keep ids stable (file names become ids in multiple places).
- Do keep resource paths stable (`assets/brewing/items/**` and `data/brewing/**`).
- Do update translations when you add `text.*_key` fields.
- Don’t add “one-off” hardcoded registrations; follow the existing dynamic scans unless there’s a strong reason.
- Don’t add client-only Minecraft classes to `src/main/java/**`.

## References

- Version-pinned Yarn/Fabric docs: `.github/remote-index.md`.
