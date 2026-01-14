# 🍺 Brewing — Data‑Driven Alcohol for Minecraft (Fabric 1.21.11)

<p align="center">
  <img src="https://img.shields.io/badge/Minecraft-1.21.11-47A248?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Fabric_Loader-0.18.4-2C5E9E?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Fabric_API-0.141.1+1.21.11-blue?style=for-the-badge" />
  <img src="https://img.shields.io/badge/License-MIT-green?style=for-the-badge" />
  <img src="https://img.shields.io/badge/Status-Active-success?style=for-the-badge" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=flat-square" />
  <img src="https://img.shields.io/badge/Gradle-9.2.1-02303A?style=flat-square" />
  <img src="https://img.shields.io/badge/Contributions-Welcome-brightgreen?style=flat-square" />
</p>

**Brewing** is a schema‑driven brewing system for Fabric that defines alcoholic beverages, methods, equipment, containers, and their gameplay semantics through JSON (datapack/server data + resourcepack/client assets).

---

## 🔗 Links

- Homepage: https://mosberg.github.io/brewing
- Source Code: https://github.com/mosberg/brewing
- Issue Tracker: https://github.com/mosberg/brewing/issues

---

## 📥 Installation

1. Install Fabric Loader for Minecraft **1.21.11**.
2. Install Fabric API (matching the version range below).
3. Drop the Brewing `.jar` into your `mods/` folder.
4. Launch the game.

> This mod is data-driven. If you are a modpack author, you can customize/extend content via JSON (schemas).

---

## ✅ Compatibility

| Component     | Version         |
| ------------- | --------------- |
| Minecraft     | 1.21.11         |
| Fabric Loader | 0.18.4          |
| Fabric API    | 0.141.1+1.21.11 |
| Yarn Mappings | 1.21.11+build.4 |
| Loom          | 1.14.10         |
| Gradle        | 9.2.1           |
| Java          | 21              |

### Libraries

- Gson: 2.13.2
- SLF4J: 2.1.0-alpha1
- JetBrains Annotations: 26.0.2-1

### Testing

- JUnit: 6.1.0-M1

---

## ✨ What Brewing adds

Brewing introduces a complete alcohol-crafting pipeline to Minecraft. Players can:

- Combine diverse **ingredients**
- Apply authentic **brewing methods**
- Use specialized **equipment**
- Store output in distinct **containers**
- Produce beverages with different **rarity**, **difficulty**, and **effects**

Everything is driven by schema-validated JSON (beverages, ingredients, containers, methods, equipment, effects, and localization keys).

---

## 🧠 How it works (current implementation)

Brewing is designed to be **data-first**:

- **Server data (datapack)** is loaded from `data/brewing/**` and hot-reloaded via a Fabric resource reload listener.
- **Items** are dynamically registered by scanning `assets/brewing/items/**/*.json` and registering an `Item` per file path.
- **Blocks** are dynamically registered from built-in data definitions:
  - Containers: `data/brewing/containers/*.json` → block ids `brewing:containers/<file_base>`
  - Equipment: `data/brewing/equipment/*.json` → block ids `brewing:equipment/<file_base>` (with a small compatibility mapping for `brewing_kettle` → `brew_kettle`)
- **Block entities** are registered once for container blocks and equipment blocks.

### Runtime data reload

On datapack reload, Brewing loads and stores the full dataset:

- Loader: `dk.mosberg.brewing.data.loader.BrewingDataLoader`
- Reload listener: `dk.mosberg.brewing.data.loader.BrewingDataReloadListener`
- Runtime access: `dk.mosberg.brewing.data.BrewingDataManager.get()`

In dev, a small smoke check runs on reload to ensure a couple of key JSONs parse.

---

## 🧪 Container state storage (`state_storage`)

Containers can define schema-driven state transfer rules under `state_storage`.

**Current behavior implemented in code:**

- Item payload is stored in `ItemStack` `CUSTOM_DATA` under a mod-root compound: `brewing.payload`.
- Containers that can be placed store payload in a `ContainerBlockEntity`.
- `conversion.on_place` / `conversion.on_break` control whether payload is copied item→block and/or block→item.
- `conversion.merge_strategy` controls whether the incoming payload is applied as-is or merged with schema defaults.
- If `placed_block.sync_to_client` is enabled, payload updates trigger a server chunk update so clients receive BE changes.

---

---

## 🍷 Content reference (current set)

<details>
<summary><strong>Alcohol types</strong></summary>

- Absinthe (45–75%) — Glass only
- Ale (4–10%) — All variants
- Beer (3–12%) — All variants
- Brandy (35–60%) — Glass & wooden
- Cider (3–10%) — All variants
- Gin (35–50%) — Glass only
- Lager (4–8%) — All variants
- Mead (6–18%) — All variants
- Rum (35–55%) — Glass & wooden
- Stout (4–12%) — All variants
- Vodka (35–50%) — Glass only
- Whiskey (35–55%) — Glass & wooden
- Wine (8–16%) — Glass & wooden

</details>

<details>
<summary><strong>Container types</strong></summary>

- Glass Bottles — 750 mB
- Glass Flasks — 250 mB
- Metal Cans — 330 mB
- Metal Kegs — 30000 mB
- Pressurized Metal Kegs — 30000 mB
- Wooden Barrels — 50000 mB

</details>

<details>
<summary><strong>Brewing methods</strong></summary>

- Aging — Develops wood-derived flavors
- Boiling — Sterilization + botanicals
- Conditioning — Clarification + carbonation
- Distillation — Alcohol concentration
- Fermentation — Yeast conversion
- Filtration — Flavor polishing
- Maceration — Botanical extraction
- Mashing — Grain starch conversion

</details>

<details>
<summary><strong>Equipment types</strong></summary>

- Aging Barrel — Aging
- Botanical Basket — Maceration
- Brewing Kettle — Boiling, Mashing
- Carbonation Rig — Conditioning
- Charcoal Filter — Filtration
- Metal Distillery — Distillation
- Wooden Fermenter — Fermentation

</details>

<details>
<summary><strong>Ingredient types</strong></summary>

Anise, Apple, Barley, Charcoal, Corn, Fennel, Grapes, Honey, Hops, Juniper Berries, Molasses, Wooden Chips, Rye, Sugarcane, Water, Wheat, Wormwood, Yeast.

</details>

<details>
<summary><strong>Effects</strong></summary>

The mod supports vanilla effects plus brewing-themed ones.

- Negative: Slowness, Mining Fatigue, Instant Damage, Nausea, Blindness, Hunger, Weakness, Poison, Wither, Levitation, Bad Luck, Darkness, Infested, Oozing, Weaving, Wind Charged, Raid Omen, Trial Omen, Caring, Sharing
- Neutral: Glowing, Bad Omen
- Positive: Speed, Haste, Strength, Instant Health, Jump Boost, Regeneration, Resistance, Fire Resistance, Water Breathing, Invisibility, Night Vision, Health Boost, Absorption, Saturation, Luck, Slow Falling, Conduit Power, Dolphins Grace, Hero of the Village, Breath of the Nautilus

</details>

<details>
<summary><strong>Rarities</strong></summary>

Crude, Refined, Aged, Masterwork, Legendary.

</details>

<details>
<summary><strong>Difficulty levels</strong></summary>

- 0 — Easy
- 1 — Medium
- 2 — Hard
- 3 — Expert
- 4 — Legendary

</details>

---

## 📚 Schema references

Schemas live in:

```text
data/brewing/schemas/
```

- `alcohol-types-schema.json`
- `beverages-schema.json`
- `common-schema.json`
- `containers-schema.json`
- `equipment-schema.json`
- `ingredients-schema.json`
- `methods-schema.json`

---

## 🛠 Development

### Requirements

- Java 21
- Gradle 9.2.1

### Build

```bash
./gradlew build
```

### Run Client

```bash
./gradlew runClient
```

### Run Server

```bash
./gradlew runServer
```

### Validate JSON (optional)

There is a small Python validator for the shipped brewing JSON:

```bash
python tools/validate_brewing_data.py
```

---

## Project Structure

```text
brewing/
├─ src/
│  ├─ main/
│  │  ├─ java/dk/mosberg/brewing/
│  │  │  ├─ Brewing.java                     # Main mod initializer
│  │  │  ├─ block/                           # Runtime blocks (container/equipment)
│  │  │  ├─ block/entity/                    # Block entities
│  │  │  ├─ data/                            # Runtime data records
│  │  │  ├─ data/loader/                     # JSON loading + reload listener
│  │  │  ├─ datagen/                         # Datagen entrypoint (stub)
│  │  │  ├─ registry/                        # Dynamic registration
│  │  │  └─ state/                           # Container payload + item/BE state storage
│  │  └─ resources/
│  │     ├─ fabric.mod.json                  # Mod metadata
│  │     ├─ icon.png                         # Mod icon
│  │     ├─ data/brewing/                    # Data pack namespace (server data)
│  │     │  ├─ alcohol_types/                # Alcohol type definitions
│  │     │  ├─ beverages/                    # Beverage definitions
│  │     │  ├─ containers/                   # Container definitions
│  │     │  ├─ effects/                      # Effect definitions
│  │     │  ├─ equipment/                    # Equipment definitions
│  │     │  ├─ ingredients/                  # Ingredient definitions
│  │     │  ├─ methods/                      # Method definitions
│  │     │  ├─ schemas/                      # Ship schemas in-jar for tooling/runtime
│  │     │  └─ tags/                         # Tags (if any)
│  │     └─ assets/brewing/                  # Resource pack namespace
│  │        ├─ blockstates/
│  │        ├─ items/
│  │        ├─ lang/
│  │        ├─ models/block/
│  │        ├─ models/item/
│  │        ├─ particles/
│  │        ├─ shaders/
│  │        ├─ textures/block/
│  │        └─ textures/item/
│  │
│  ├─ client/
│  │  ├─ java/dk/mosberg/brewing/client/
│  │  │  ├─ BrewingClient.java               # Client initializer
│  │  │  ├─ render/                          # Renderers, layers, render helpers
│  │  │  ├─ model/                           # Client models (if code-driven)
│  │  │  ├─ screen/                          # Screens + screen handlers (if client-only)
│  │  │  └─ network/                         # Client packet handlers
│  │  └─ resources/                          # Only if truly client-only assets exist
│  │
│  ├─ test/
│  │  └─ java/...                            # Unit tests (optional)
│  │
│  └─ main/generated/                        # Datagen output (generated resources)
│
├─ gradle/wrapper/
├─ build.gradle
├─ gradle.properties
├─ settings.gradle
├─ README.md
└─ LICENSE
```

---

## 🤝 Contributing

Contributions are welcome. Suggested expectations:

- Schema-first changes (update schemas).
- Backwards-compatible schema evolution where possible.

---

## 🧭 Roadmap

- Expanded brewing interactions
- More equipment roles
- Additional beverage families
- In-game UI improvements
- Advancements \& progression
- Custom effect system expansion
- Modpack-friendly presets

---

## 📄 License

MIT (see `LICENSE`).

---

## Author

[Mosberg](https://github.com/mosberg)
