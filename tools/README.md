# Pizzeria structure tools

These scripts normalize the converted 1.21.1 NBT structures after a re-bake.
They require Python 3 and `pip install nbtlib`.
Run them in this order from any directory:

1. `python tools/fix_block_entities.py`
   - maps old `management_wanted:*` block entity ids to the new `fnafmw:*` ids;
   - adds missing terminal/neon-sign block entities;
   - removes block entity tags that have no equivalent in the new mod.

2. `python tools/clean_container_items.py`
   - removes old `management_wanted:*` items from container inventories,
     so the world does not log invalid-item errors.

3. `python tools/remove_air_bes.py`
   - removes block entity tags that sit on `minecraft:air` blocks.

4. `python tools/fix_tent.py`
   - fixes the large-tent locker facing, moves the floating roof poster
     onto the quartz wall, and adds a support block behind the prizes sign.

5. `python tools/add_loot.py`
   - fills pizzeria/tent/carnival lockers with static loot
     (`fnafmw:fazcoin`, `fnafmw:fazbuck`, cobwebs, rare items);
   - points chests/barrels at the `fnafmwpizzeria:chests/loot` loot table.

6. `python tools/add_booth_loot.py`
   - adds the FNAF4 booth locker block entities and their loot.

7. `python tools/validate_jar.py out/fnafmwpizzeria-1.1.0.jar`
   - checks the final jar for legacy ids, invalid block entities,
     old items, air block entities, and entities in structures.

The scripts modify `src/main/resources/data/fnafmwpizzeria/structure/pizzerias`
and `src/main/resources/data/fnafmwpizzeria/structure/worldgen_extras` in place.
`fix_block_entities.py` writes backups to `provenance/be_backup` (ignored by git).

Raw/pre-conversion NBT files are intentionally not included in this repository.
