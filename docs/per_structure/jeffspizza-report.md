# jeffspizza — per-structure conversion report

## Structure

- Old NBT: `fnafmwpizzeria/structures/jeffspizza.nbt` (DataVersion 3465)
- Size: **33 × 17 × 43** (x × y × z), 24123 block entries, 18106 of them air. File is UTF-8, no BOM.
- Floor surface is y=1, walls run y=1..4, roof/ceiling around y=5. The building is irregular/rotated: the west and interior walls sit at x=6, x=13, x=20 and x=23, the east facade at x=30, with a large open party room on the east side.

## Door openings (11 total)

| # | kind | facing | opening cells (lower y=1) | width × height | notes |
|---|------|--------|---------------------------|----------------|-------|
| 1 | double | east | (13,1,37) (13,1,38) | 2 × 2 | west facade pizzeria door, glass transom at y=3 |
| 2 | double | west | (23,1,37) (23,1,38) | 2 × 2 | east pizzeria door, glass transom at y=3 |
| 3 | double | east | (6,1,16) (6,1,17) | 2 × 3 | backstage office door, west wall |
| 4 | double | south | (9,1,23) (10,1,23) | 2 × 3 | backstage door, z=23 wall |
| 5 | double | south | (10,1,13) (11,1,13) | 2 × 3 | backstage door, z=13 facade wall |
| 6 | double | east | (13,1,19) (13,1,20) | 2 × 3 | backstage door, x=13 wall |
| 7 | double | east | (13,1,25) (13,1,26) | 2 × 3 | backstage door, x=13 wall |
| 8 | double | east | (20,1,14) (20,1,15) | 2 × 3 | backstage door, x=20 wall |
| 9 | double | east | (20,1,21) (20,1,22) | 2 × 3 | backstage door, x=20 wall |
| 10 | double | south | (26,1,32) (27,1,32) | 2 × 3 | open backstage door (`open=true`) |
| 11 | big_entrance | east | origin (30,1,37), spans z=36..38 | 3 × 3 | front entrance, old anchor was (30,2,37) |

Count reconciliation: the old file has 8 `pizzeria_door` **blocks** but they are 2 lower halves + 2 upper halves of **two 2-wide double doors**, not 8 separate doors. Likewise the 7 `backstage_door` + 1 `backstage_door_open` single blocks each rendered a 2-wide door, so each becomes one double opening. The result is exactly one door per real opening, no duplicates.

Hinge assignment follows the map's own pizzeria pairs (the leaf on `Direction.rotateYCCW(facing)` is `hinge=left`): east → north leaf left; south → east leaf left. Every pair has one `left` and one `right` leaf.

### Big entrance detail

The old `big_entrance_door` is a **single** block that renders a 3-wide, 3-tall model centred on itself (y from -1 to +2). The new `fnafmw:big_entrance_door` is a 15-part multiblock (3 wide × 3 tall × 2 deep, model on `part=4`) whose origin `part=0` is the **bottom-left** cell. Therefore the correct origin is **(30,1,37)**, one block lower than the old anchor; `part=4` then lands back on the old block position (30,2,37). Facing stays east.

## Changes relative to the global map

Only two families of block-name overrides:

1. **All 9 `window_*` → `minecraft:light_gray_stained_glass`** (37 blocks). Reason: `fnafmw:window` is a north/south-facing pane (glass sheets at z=2 and z=14, frame columns thin in X) and has only a `layout` property, no facing/rotation. All 37 windows are in the **x=30 east facade** (X-normal wall), so the new window model cannot be oriented correctly there. Glass keeps the exact 6×3 openings and matches the stained glass already used in the door transoms.
2. **`minecraft:grass` → `minecraft:short_grass`** — `minecraft:grass` was renamed in 1.21 and is otherwise an invalid id.

Everything else uses the existing `legacy_block_map.json` mapping.

## Rotations corrected

The important discovery: most new `facing` blocks are **180° opposite** the old block's facing name. The new mod uses `facing` for the wall/back side. This was verified two ways: (a) the blockstate `y` rotation for a given facing differs by 180°, and (b) model geometry (e.g. the locker, posters, hazard sign, arcade marquee all sit at the same block edge in old and new models while the state rotated). Applying identity would have put posters/signs/wires/lockers on the wrong side or inside walls.

`facing_map = {north↔south, east↔west}` applied to:

- locker, locker_void, backstage_shelf
- large_cardboard_box, small_cardboard_box (variant also forced)
- children_drawings, classic_freddy_poster, crying_child_poster, paper_pal, hazard_sign
- thin_wall_wires
- classic_monitor, open_neon_sign, cola_vending_machine, gumball_machine
- freddy_fazbear_pizza_sign, hanging_lamp, desk_fan (lit/powered also set)
- both arcade cabinet styles

Identity (kept unchanged) after checking the blockstate/model: `classic_chair`, `restaurant_chair` (diner booth), `terminal`, `light_switch` (forced `face=wall`), `ceiling_tile_black_stairs`, and the pizzeria door → `entrance_door` mapping.

Blocks whose new counterpart has **no `facing` property at all** were rotated via `position_fixes`:

- `classic_speaker` → `rotation=2` (= y 90, matches old facing east)
- `exit_sign` → `rotation=4`, `wall=true`, `centered=false`, `black=false`
- `out_of_order_sign` → `rotation=4`, `wall=false` (free-standing sign in the open room)

No `rotation_y` offsets were needed anywhere: after the facing map, every new variant produces the same y rotation as the old one. `modular_type` for tables is set by old part name (models are geometrically identical): `vertical_bottom=1, vertical_middle=5, vertical_top=4, bottom_left=3, middle_left=7, top_left=6, bottom_right=9, middle_right=13, top_right=12`. The 3 `backstage_table_*` parts use the 3-long vertical run (1/5/4).

Other per-instance fixes: each of the 8 old backstage door blocks is replaced by the correct **upper** leaf at that position (the lower halves and partner leaves come from the `doors` entries); the 5 monitors get the right `monitors`/`facing` (old blockstate 0..4 → 1..4 screens, clamped); the one open cardboard box keeps `open=true`.

## Doors — placement notes

- All 8 backstage openings are 2 wide × **3 tall**; the chosen 2-tall `employee_door` leaves fill y=1..2, leaving the y=3 cell open under the wall above. The alternative `fnafmw:big_employee_door` fills the 3rd row but is a **2-block-deep** multiblock that would overwrite a block inside the adjacent room at every door, so the standard double door was preferred.
- Pizzeria doors keep their original `hinge` values from the NBT, so their appearance is unchanged; only the unused old `powered` property is dropped.
- The old `backstage_door_open` keeps `open=true` on both leaves.

## Validation performed

- JSON parses; no `legacy_placeholder`; no `management_wanted:*` values remain (old ids appear only as lookup keys).
- Every target block id checked against `new_blockstates.txt`.
- Every property/value in `property_rules.set` and in `position_fixes.properties` checked against the new jar blockstates (0 errors; only warnings are intentional `drop` entries for properties the new block does not have).
- All 11 door openings re-checked against the NBT: both cells are air at y=1..3 (except the old door block itself), with floor below and wall at y=4.
- Double-door leaves verified adjacent and hinges opposite.

## Preview

Rendered (PIL, flat-colour block previews, not in-game):

- `jeffspizza-preview-plan-y2.png` — top-down y=2 plan, door leaves outlined in magenta. Shows 11 door groups: 8 backstage pairs, 2 pizzeria pairs, and the 3-wide big entrance at the east end.
- `jeffspizza-preview-east-facade.png` — elevation of x=30 showing the two 6×3 window openings (now glass) and the entrance position.

The preview uses the base block map (it does not expand the `doors` entries), so the big entrance shows only its `part=4` block.

## Remaining visual risks

1. Backstage doorways are 3 tall but the replacement doors are 2 tall — a 1-block open strip remains above each pair.
2. `hinge` left/right only mirrors the door texture; vanilla door geometry is identical, so a flipped hinge would be cosmetic, not structural.
3. `fnafmw:stagelight` is a block-entity-rendered block; `face=wall/facing=south/color=blue/lit=true` were set but cannot be verified without the game.
4. `basic_light_warm` is a modular connected ceiling light; the neighbour flags (`north/south/east/west/up/down`) are left at default, so the 61 ceiling lights may not merge into large panels.
5. `brown_facade_bricks_trim` (`face` left at default `floor`) uses a new trim band texture; the old tiled-red texture had two bands, so the band alignment will not match exactly.
6. The three `backstage_table_*` parts are mapped to the party-table 3-long run; the old backstage table model was 1.375 blocks long per piece, so the new run is a cleaner but not identical table.
7. `diner_booth` → `restaurant_chair` is the global map's choice; it is a chair, not a booth bench.
8. Windows lose their old frame detailing (glass instead of a window model); this was the only way to keep them in an X-normal wall.
