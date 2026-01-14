# Copilot instructions (Brewing / Fabric 1.21.11)

This repo is **resource/data heavy** (JSON + assets); Java code is currently minimal. Prefer small, surgical changes that keep IDs and resource paths stable.

## Big picture

- Data pack content lives in `src/main/resources/data/brewing/**` (schemas in `data/brewing/schemas/*.json`, content in `ingredients/`, `containers/`, `beverages/`, etc.).
- Resource pack assets live in `src/main/resources/assets/brewing/**` (item definitions, models, textures, lang).
- Entrypoints: `dk.mosberg.brewing.Brewing`, `dk.mosberg.brewing.client.BrewingClient`, `dk.mosberg.brewing.datagen.BrewingDataGenerator`.

## Workflows (PowerShell)

- Build: `./gradlew build`
- Run: `./gradlew runClient` / `./gradlew runServer`
- Datagen: `./gradlew runDatagen` (outputs are included from `src/main/generated/resources` via `build.gradle`)
- Crash triage: `./gradlew runClient --no-daemon`, then check `run/crash-reports/`.

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
- `src/main/resources/assets/brewing/lang/en_us.json` is currently empty; add translation keys when you introduce user-facing names.

## Local tooling (optional but useful)

- Validate all brewing JSON against shipped schemas: `python tools/validate_brewing_data.py` (deps in `tools/requirements.txt`).
- Texture helpers live in `tools/noise_map_generator.py` (usage in `tools/guide.md`).

## 1.21.11 gotchas when adding registrations

- Set registry keys on `Item.Settings` / `AbstractBlock.Settings` **before** constructing `Item`/`Block` (avoids “id not set” crashes).
- For `FlowableFluid`, only the flowing variant should add the `LEVEL` property.

## References

- Version-pinned Yarn/Fabric docs: `.github/remote-index.md`.

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
