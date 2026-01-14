````instructions
# Copilot instructions (Brewing / Fabric 1.21.11)

Prefer small, surgical changes that match existing patterns. This repo is currently **resource/data heavy** with minimal Java stubs.

## Snapshot
- Minecraft 1.21.11, Java 21, Loader 0.18.4, Fabric API 0.141.1+1.21.11, mod id `brewing`.
- Entrypoints: `dk.mosberg.brewing.Brewing`, `dk.mosberg.brewing.client.BrewingClient`, `dk.mosberg.brewing.datagen.BrewingDataGenerator`.

## Where things live
- Common code: `src/main/java/**` (main initializer: `src/main/java/dk/mosberg/brewing/Brewing.java`).
- Client-only code: `src/client/java/**` (keep rendering/model-layer imports here; Loom uses split source sets).
- Data-driven content (datapack): `src/main/resources/data/brewing/**` (alcohol_types, beverages, containers, equipment, ingredients, methods).
- Resource-pack assets: `src/main/resources/assets/brewing/**` (item descriptors: `assets/brewing/items/**`; plus models, blockstates, textures, `lang/en_us.json`).
- Docs + schemas: `docs/` (start at `docs/TECHNICAL_README.md`; some docs describe planned systems not yet implemented in Java).

## Build / run (PowerShell)
- `./gradlew build`
- `./gradlew runClient` / `./gradlew runServer`
- `./gradlew runDatagen` (datagen is configured; entrypoint exists but currently does nothing)
- Crash triage: `./gradlew runClient --no-daemon`, then check `run/crash-reports/`.

## Mod metadata templating
- `src/main/resources/fabric.mod.json` is a template; values come from `gradle.properties` via `processResources`.
- Change mod id/name/version/links/license in `gradle.properties`, not in `fabric.mod.json`.

## Resource/data conventions
- Keep ids stable and lowercase; match resource paths (assets, models, blockstates).
- Optional cross-mod tag entries should use objects: `{ "id": "othermod:thing", "required": false }`.
- Item models should reference `brewing:item/...` textures (avoid `brewing:block/...` in item models to prevent atlas warnings).
- For animated textures, prefer omitting explicit `.mcmeta` `frames` unless you truly need a subset.

## 1.21.11 registration gotchas (when adding code)
- Set registry keys on `Item.Settings` / `AbstractBlock.Settings` **before** constructing `Item`/`Block` (avoids “id not set” crashes).
- For `FlowableFluid`, only the flowing variant should add the `LEVEL` property.

## Reference links
- Version-pinned API docs live in `.github/remote-index.md`.
```# Copilot instructions (Brewing / Fabric 1.21.11)

These instructions are optimized for this repo’s current Gradle + Fabric Loom setup and code layout. Prefer precise, minimal changes that match existing patterns.

## Quick facts

- Minecraft: 1.21.11
- Java: 21
- Loader: 0.18.4
- Fabric API: 0.141.1+1.21.11
- Mappings: yarn 1.21.11+build.4
- Mod id: `brewing` (see `dk.mosberg.brewing.Brewing#MOD_ID`)

## Architecture overview (data-driven design)

This mod is **data-driven**: beverages, containers, equipment, and ingredients are defined in JSON under `src/main/resources/data/brewing/`, not hardcoded. The vision:

1. **Individual files**: Each entity has its own file under subdirectories (`beverages/beer/beer.json`, `containers/aluminum_can.json`, etc.) with detailed definitions.
2. **Runtime loading**: `DataLoader` (TODO stub) will parse these JSONs and populate managers (`BeverageManager`, `ContainerManager`, etc.).
3. **Dynamic registration**: Once loaded, the data feeds into Fabric registries (`ModItems`, `ModBlocks`, etc.) to create actual game objects.

**Current state (early development)**: The JSON schemas and directory structure are complete; the Java implementation is mostly TODO stubs. When implementing:

- Start with `DataLoader` to parse JSONs into data classes (`BeverageData`, `ContainerData`, etc.).
- Populate managers in `dk.mosberg.brewing.manager` to hold parsed data.
- Wire managers into `Brewing#onInitialize()` to ensure they're loaded before registration.
- Dynamically register items/blocks from data (avoid hardcoding in `ModItems`/`ModBlocks`).

## Known 1.21.11 gotchas (runtime)

- `Item.Settings` must have a registry key set _before_ constructing an `Item` (otherwise you can crash at runtime with “Item id not set”). In practice: create `Item.Settings` with `.registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Brewing.MOD_ID, id)))`.
- `AbstractBlock.Settings` must have a registry key set _before_ constructing a `Block` (otherwise you can crash at runtime with “Block id not set”). In practice: create settings with `.registryKey(RegistryKey.of(RegistryKeys.BLOCK, Identifier.of(Brewing.MOD_ID, id)))`.
- `FlowableFluid` property registration: don’t add the `LEVEL` property in both the base class and the flowing subclass; only the flowing variant should add it.
- Animated textures: if a sprite sheet has N frames but `.mcmeta` only references a subset, Minecraft logs “Unused frames…”. Easiest fix is to omit the explicit `frames` list so all frames are used.
- Item models and atlases: item models should only reference `brewing:item/...` textures (items atlas). Referencing `brewing:block/...` from an item model triggers “Multiple atlases used…” warnings.

## Project layout (Loom split source sets)

This project uses `loom { splitEnvironmentSourceSets() }`:

- Common code: `src/main/java/dk/mosberg/brewing/**`
- Client-only code: `src/client/java/dk/mosberg/brewing/client/**`
- Resources: `src/main/resources/**`

Rule of thumb:

- Anything that imports client-only Minecraft classes (rendering, model layers, render layers) must live under `src/client/**`.
- Registries, blocks/items, fluids, and transfer logic should stay in `src/main/**`.

## Editor tooling (datapacks/resources)

This repo uses the Spyglass/Datapack language tooling via VS Code extensions:

- `SPGoding.datapack-language-server` (JSON + mcfunction validation/completions)
- `MinecraftCommands.syntax-mcfunction` (syntax highlighting)

Config:

- The workspace config file is `spyglass.json` at the repo root.
- This primarily helps when editing datapack-style files under `src/main/resources/data/**` (tags, loot tables, recipes, advancements) and command files (`*.mcfunction`) if you add them.

If diagnostics look “wrong”, verify the configured `gameVersion` in `spyglass.json` matches `gradle.properties`.

Common fixes (when diagnostics or runtime logs complain):

- Tag load errors about missing ids: for cross-mod/optional references in `src/main/resources/data/**/tags/**`, use `{ "id": "othermod:thing", "required": false }`.
- Tag translation warnings: add `tag.item.<namespace>.<path>` entries in `src/main/resources/assets/brewing/lang/en_us.json`.
- Item model “Multiple atlases used…”: ensure item models only reference `brewing:item/...` textures; move overlays into `assets/brewing/textures/item/` (or remove the extra layer).
- Animated texture “Unused frames…”: remove the explicit `frames` list in the `.png.mcmeta` so all frames are used.

## Build & run (Gradle/Loom)

Common commands (Windows PowerShell works with `./gradlew`):

- `./gradlew build` (CI runs this)
- `./gradlew runClient`
- `./gradlew runServer`
- `./gradlew runDatagen` (Fabric API datagen entrypoint is configured)
- `./gradlew projectInfo` (prints versions from `gradle.properties`)

Debugging workflow (when `runClient` crashes):

1. Re-run with `./gradlew runClient --no-daemon` so logs aren’t mixed across daemons.
2. Check `run/crash-reports/` for the full exception (the first meaningful stack trace line is usually the real cause).
3. Verify client-only imports live under `src/client/**` (common classloading issues look like `ClassNotFoundException` on render classes).

Performance defaults are tuned in `gradle.properties` (4G heap, configuration cache, parallel, caching). When editing Gradle scripts, keep them configuration-cache friendly (avoid doing work at configuration time; prefer `tasks.register` and capturing simple values).

## Mod metadata & resources (important)

### `fabric.mod.json` is a template

`src/main/resources/fabric.mod.json` contains placeholders like `${mod_id}`.
Those are expanded by `processResources` using values from `gradle.properties`.

Guidelines:

- Change mod id/name/version/links/license in `gradle.properties`.
- Do not hardcode those values in `fabric.mod.json` unless you remove the templating.

### Resource conventions (1.21.11)

- Item descriptors live in `assets/brewing/items/*.json`.
- Item models in `assets/brewing/models/item/*.json`.
- Block models in `assets/brewing/models/block/*.json`.
- Blockstates in `assets/brewing/blockstates/*.json`.
- Fluid rendering/definitions via `assets/brewing/fluids.json`.
- Localizations in `assets/brewing/lang/en_us.json`.

Data/tag conventions:

- If you keep “compat” tags that reference optional content from other namespaces, use object entries with `{ "id": "othermod:thing", "required": false }` to avoid datapack load errors.
- If Fabric Tag Conventions warns about missing tag translations, add `tag.item.<namespace>.<path>` entries in `en_us.json`.

When adding new content, always update both registration code and resource files.

## Code architecture (current implementation state)

### Package structure

````

brewing/
├─ src/
│ ├─ main/
│ │ ├─ java/dk/mosberg/brewing/
│ │ │ ├─ Brewing.java # Main mod initializer (common)
│ │ │ ├─ api/ # Public API (stable contracts)
│ │ │ ├─ impl/ # Internal implementations (non-API)
│ │ │ ├─ registry/ # Registration + registry helpers
│ │ │ ├─ data/ # Runtime data models (POJOs, codecs)
│ │ │ ├─ data/loader/ # JSON discovery/validation/loading
│ │ │ ├─ brewing/ # Brewing domain logic (pipeline, rules)
│ │ │ ├─ network/ # Shared networking (packets, channels)
│ │ │ ├─ util/ # Utilities (small, dependency-light)
│ │ │ └─ datagen/ # Datagen entry + shared providers
│ │ └─ resources/
│ │ ├─ fabric.mod.json
│ │ ├─ brewing.mixins.json
│ │ ├─ data/brewing/ # Data pack namespace (server data)
│ │ │ ├─ alcohol_types/
│ │ │ ├─ beverages/
│ │ │ ├─ containers/
│ │ │ ├─ equipment/
│ │ │ ├─ ingredients/
│ │ │ ├─ methods/
│ │ │ ├─ effects/
│ │ │ ├─ tags/
│ │ │ └─ schemas/ # Ship schemas in-jar for tooling/runtime
│ │ └─ assets/brewing/ # Resource pack namespace
│ │ ├─ lang/
│ │ ├─ blockstates/
│ │ ├─ models/block/
│ │ ├─ models/item/
│ │ ├─ textures/block/
│ │ ├─ textures/item/
│ │ ├─ particles/
│ │ └─ shaders/
│ │
│ ├─ client/
│ │ ├─ java/dk/mosberg/brewing/client/
│ │ │ ├─ BrewingClient.java # Client initializer
│ │ │ ├─ render/ # Renderers, layers, render helpers
│ │ │ ├─ model/ # Client models (if code-driven)
│ │ │ ├─ screen/ # Screens + screen handlers (if client-only)
│ │ │ └─ network/ # Client packet handlers
│ │ └─ resources/ # Only if truly client-only assets exist
│ │
│ ├─ test/
│ │ └─ java/... # Unit tests (optional)
│ │
│ └─ main/generated/ # Datagen output (generated resources)
│
├─ docs/
│ ├─ schemas/
│ ├─ systems/
│ └─ examples/
│
├─ gradle/wrapper/
├─ build.gradle
├─ gradle.properties
├─ settings.gradle
├─ README.md
└─ LICENSE

```

### Documentation Index

All detailed docs live in the `../docs/` folder:

[Documentation folder](../docs/)

```

../docs/
├─ schemas/ - schema documentation
├─ systems/ - system documentation
├─ examples/ - example JSON files
├─ OVERVIEW.md - architecture overview
├─ README.md - master index
└─ TECHNICAL_README.md - this file

````

### JSON data schema (authoritative definitions)

All content lives under `src/main/resources/data/brewing/` in individual files:

- `alcohol_types/*.json`: Base alcohol categories (absinthe, ale, beer, brandy, cider, gin, lager, mead, rum, stout, vodka, whisky, wine)
- `beverages/<type>/*.json`: Individual beverages (e.g., `beer/beer.json`, `whisky/oak_aged_whisky.json`)
- `containers/*.json`: Containers (aluminum_can, glass_bottle, oak_barrel, etc.)
- `equipment/*.json`: Brewing equipment (brew_kettle, copper_distillery, aging_barrel, etc.)
- `ingredients/*.json`: Ingredients (barley, hops, yeast, water, grape, honey, etc.)
- `methods/*.json`: Brewing methods (fermentation, distillation, aging, mashing, etc.)
- `tags/*.json`: Tag definitions for grouping

Example beverage schema (`beverages/beer/beer.json`):

```json
{
  "id": "brewing:beer/basic_beer",
  "display_name": "Basic Beer",
  "alcohol_type": "beer",
  "target_abv_pct": 5,
  "container_defaults": ["glass_bottle", "copper_keg", "aluminum_can"],
  "ingredient_profile": [
    { "ingredient": "water", "amount": 1000, "unit": "mB" },
    { "ingredient": "barley", "amount": 3, "unit": "item" }
  ],
  "process": [
    { "method": "mashing", "equipment": "brew_kettle" },
    { "method": "fermentation", "equipment": "oak_fermenter" }
  ],
  "tags": ["beer", "fermented", "grain"]
}
````

### Entrypoints

- Common entrypoint: `dk.mosberg.brewing.Brewing` (`ModInitializer`) - minimal, needs data loading wired in
- Client entrypoint: `dk.mosberg.brewing.client.BrewingClient` (`ClientModInitializer`) - empty stub
- Datagen entrypoint: `dk.mosberg.brewing.client.datagen.BrewingDataGenerator` - empty stub

If you add new registries/content, ensure it is actually initialized from `Brewing#onInitialize()` (either by calling `ModX.init()` methods or otherwise referencing the classes so their static initializers run).

### Implementation roadmap (for AI agents)

When implementing the data-driven system:

1. **Phase 1: Data layer** (parse JSONs into Java objects)

   - Implement `DataLoader` to read JSON files from subdirectories
   - Implement data classes (`BeverageData`, `ContainerData`, etc.) as immutable records
   - Handle JSON validation and error reporting

2. **Phase 2: Managers** (hold parsed data at runtime)

   - Implement managers to store and query loaded data
   - Wire managers into `Brewing#onInitialize()` before registry phase

3. **Phase 3: Dynamic registration** (create game objects from data)

   - Update `ModItems`, `ModBlocks`, etc. to iterate over manager data
   - Register items/blocks/effects dynamically (avoid hardcoding)
   - Ensure registry keys are set properly (see "Known 1.21.11 gotchas")

4. **Phase 4: Assets** (models/textures/translations)
   - Generate or verify `assets/brewing/items/*.json` matches registered items
   - Generate or verify `assets/brewing/models/item/*.json` models
   - Add translations to `assets/brewing/lang/en_us.json`

### Registries (future state)

Registrations will be centralized in `dk.mosberg.brewing.registry` (currently TODO stubs):

- `ModItems`: Will dynamically register items from beverage/container data
- `ModBlocks`: Will dynamically register blocks from equipment data
- `ModBlockEntities`: Block entity types for brewable containers
- `ModEffects`: Custom status effects from beverage definitions
- `ModItemGroups`: Creative tabs organization

Conventions:

- Keep registration IDs stable and lowercase (matches resource paths).
- Prefer using the mod id constant (`Brewing.MOD_ID`) when adding new registrations.

### Aluminum Keg (Transfer API)

The keg is a `BlockEntity` with a single-variant fluid tank:

- Storage: `SingleVariantStorage<FluidVariant>` with `Transaction` for insert/extract
- Volumes:
  - Can volume = `FluidConstants.BUCKET / 4`
  - Capacity = 16 cans

Interaction pattern:

- Block `onUse` exchanges items in-hand:
  - Empty can → filled can (extract from tank)
  - Filled can → empty can (insert into tank)

When extending behavior (more fluids, effects, UI):

- Keep transactions atomic (openOuter + commit only on full amount).
- Avoid client-side state mutations; server should be authoritative.

### Alcohol fluid

`dk.mosberg.brewing.fluid.AlcoholFluid` is a `FlowableFluid`:

- Still/flowing registered in `ModFluids`
- Uses `ModItems.ALUMINUM_CAN_ALCOHOL` as the “bucket item”
- Converts to `ModBlocks.ALCOHOL_BLOCK` via `toBlockState`

If you change fluid behavior (tick rate, speed, sounds), keep both still and flowing variants consistent.

## Making changes (fast checklists)

### Add a new beverage/container/ingredient (data-first workflow)

1. Add JSON file to `data/brewing/<type>/<name>.json`.
2. Implement corresponding data class in `dk.mosberg.brewing.data` if it doesn't exist.
3. Update `DataLoader` to parse the new data type.
4. Implement manager in `dk.mosberg.brewing.manager` to hold parsed data.
5. Update registries to dynamically create items/blocks from parsed data.
6. Add assets: item descriptor, model, texture, and translation.
7. Run `./gradlew runClient` to verify.

### Add a new item (legacy/direct registration)

1. Register it in `ModItems` (note: will be replaced by data-driven approach).
2. Add `assets/brewing/items/<id>.json`.
3. Add `assets/brewing/models/item/<id>.json`.
4. Add a translation key in `assets/brewing/lang/en_us.json`.
5. Run `./gradlew runClient` to sanity-check.

### Add a new block / block entity

1. Register block in `ModBlocks`.
2. If it has a BE, register in `ModBlockEntities`.
3. Add blockstate + models + item model + translations.
4. If rendering needs client hooks, wire it in `BrewingClient`.

### Add/adjust datagen

Datagen entrypoint is present (`BrewingDataGenerator`) but currently empty.
If you add generators, keep outputs in `src/main/generated/resources` (already configured in `build.gradle`).

## Tests

There are currently no `src/test/**` tests in this repo. Use `./gradlew build` and `./gradlew runClient` as smoke checks.
If you introduce pure Java logic (e.g., recipe parsing, config validation), add unit tests under `src/test/java` using JUnit.

## Remote references

See [.github/remote-index.md](remote-index.md) for the exact Yarn/Fabric docs and upstream repos used by this project.
