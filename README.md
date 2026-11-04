# FNAFMW Pizzeria Dashboard

A fan-made addon for **FNAF: Management Wanted** on Minecraft **1.21.1 / NeoForge**.

It ports the old 0.3.3 pizzerias and structures, adds natural worldgen, a terminal block shop, deeds, and loot.

> This is an unofficial fan project. It is not affiliated with OVDR Studios, Scott Cawthon, or the FNAF franchise. See `NOTICE.md`.

## Features

- **Five placeable pizzerias** from the old version:
  - Discount Diner
  - Fredbear's Family Diner
  - Jeff's Pizza
  - Freddy Fazbear's Pizza (FNAF 1)
  - Grand Reopening (FNAF 2)
- **Natural worldgen** in new chunks:
  - the five pizzerias;
  - a small FNAF 4 franchise booth with a terminal;
  - a large circus tent and a carnival.
- **Pizzeria deeds** in the creative tab and in the terminal shop.
- **Terminal block shop** with deeds and 40+ FNAFMW blocks, English/Russian names.
- **Loot** in structures:
  - chests and barrels use `fnafmwpizzeria:chests/loot`;
  - lockers contain money (`fazcoin` / `fazbuck`), cobwebs, string, emeralds, diamonds, name tags, and rare masks.
- **Quality/visitor economy** for claimed pizzerias (from the base addon).
- **Commands**:
  - `/pizzeria` or `/pizzeria quality` — sector status;
  - `/pizzeria rescan` — rebuild POIs;
  - `/pizzeria claim` — claim the current chunk;
  - `/pizzeria spawn` — force a customer when an entrance is ready;
  - `/pizzeria cleanup` — remove legacy entities and restore chairs/cutouts;
  - `/pizzeria place <deed>` — place a pizzeria deed;
  - `/pizzeria loot` — fill empty lockers/chests/barrels in **loaded** chunks around the player (op).

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.235+** (tested on 21.1.235 and 21.1.265)
- **FNAF: Management Wanted** `1.0.0-snapshot-1` (`fnafmw`)
- GeckoLib 4.9.2 and Veil 4.5.2 (required by FNAFMW)
- Java 21

FNAFMW bundles Refraction and Vixis as jar-in-jar dependencies. Do not install the old 1.20.1 FNAFMW jar next to this addon.

## Installation

1. Install Minecraft 1.21.1 with NeoForge 21.1.235+.
2. Put FNAFMW, GeckoLib, Veil, and `fnafmwpizzeria-1.1.0.jar` into the `mods` folder.
3. Launch the game.

The prebuilt addon jar is in [`dist/fnafmwpizzeria-1.1.0.jar`](dist/fnafmwpizzeria-1.1.0.jar).

## Worldgen

Natural structures are generated **only in new chunks**. Existing worlds and already generated chunks will not update.

- Pizzerias: one structure type with five random templates, spacing 28 chunks.
- Extras: FNAF 4 booth, large tent, carnival, spacing 40 chunks.
- Oceans and rivers are skipped.
- The structure uses a fixed rotation, so doors, signs, lockers, and big doors keep their intended orientation.
- Old-style 2x3 doors are baked as big doors; the 3x3 entrance is baked as 15 parts.

Find them with:

```mcfunction
/locate structure fnafmwpizzeria:pizzerias
/locate structure fnafmwpizzeria:worldgen_extras
```

Alternatively, use a deed from the creative tab or buy one from the terminal shop.

## Loot

- **Chests and barrels** in the extras use the loot table `fnafmwpizzeria:chests/loot`.
- **Lockers** in pizzerias and extras are filled with baked loot.
- `/pizzeria loot` repairs **already generated** structures: it scans only loaded chunks around the player and fills empty lockers/containers. It does not load or generate chunks.
- This command fills every empty container in range, including player-owned empty chests. Run it inside the structure, not in your base.

## Build from source

The source uses the standard NeoForge ModDevGradle layout. Because FNAFMW is not published on a public Maven, its jars are not included in this repository.

1. Put the following jars into `libs/`:
   - `fnafmw-alpha-1.0.0-snapshot-1.jar`
   - `refraction-neoforge-1.21.1-1.3.0-mw1.jar`
   - `vixis-0.7.0.jar`
   - `geckolib-neoforge-1.21.1-4.9.2.jar`
   - `veil-neoforge-1.21.1-4.5.2.jar`
2. Run:

```bash
./gradlew build
```

On Windows use `gradlew.bat build`, or import the folder as a Gradle project in IntelliJ IDEA and run `build`.

The output jar is written to `build/libs/`.

If Gradle cannot resolve the ModDevGradle plugin version in `build.gradle`, update it to the version used by the current NeoForge MDK.

The manual NBT normalization/validation scripts are in [`tools/`](tools/).

## Repository layout

```
src/main/java        addon Java sources
src/main/resources   assets, data, structures, loot tables, mod metadata
docs/per_structure   conversion notes and previews
tools                NBT normalization and validation scripts
dist                 prebuilt release jar
libs                 local build dependencies (not committed)
```

## Known limitations

- Natural pizzerias do not generate income until their chunks are claimed; deed-placed pizzerias claim chunks automatically.
- Existing chunks and already placed tents/pizzerias do not update. New structures and a new world are required for the latest visual fixes.
- The Fazbear Hills biome is **not** restored yet. The new mod keeps the Fazhill blocks and tree, but the old biome injection was removed.
- Client-side visual/UI verification is manual; server generation and NBT data are validated.

## Credits

- **OVDR Studios** — FNAF: Management Wanted and the original 0.3.3 structures/assets.
- **Fork** — addon code, conversion pipeline, worldgen, shop, and loot integration.
- **Scott Cawthon** — Five Nights at Freddy's.

## License

The original addon code is released under the MIT License; see [`LICENSE`](LICENSE).
Converted structures, textures, names, and other assets derived from FNAFMW or FNAF remain the property of their respective owners; see [`NOTICE.md`](NOTICE.md).

Russian guide: [`README.ru.md`](README.ru.md).
