# fnaf2 — per-structure conversion report

Old NBT: `fnafmwpizzeria/structures/fnaf2.nbt`
Deliverables: `fnaf2.json`, this report, `fnaf2-preview.png`, `fnaf2-preview-cutaway.png`, `fnaf2-plan.png`

## 1. Structure size and layout

| item | value |
|---|---|
| NBT size | 31 (x) × 48 (y) × 40 (z) |
| blocks with content | y = 0 … 8 (rest of the y range is air) |
| ground / walk level | y = 0 solid floor, y = 1 first free block |
| ceiling | y = 6 (`ceiling_tile_gray`), shingle roof ring y = 5…7 |
| footprint used | x = 1…29, z = 1…38 |
| palette | 214 entries, 59 520 block entries, 66 distinct blocks after conversion |

The building is a single-storey corner of the pizzeria. The **front facade is at z = 38** (south), the
east facade is at **x = 29**, the north side is closed by the z = 1 wall and the west side by x = 1.

### Approximate door / entrance positions

```
BIG ENTRANCE      front wall z=38, opening x=21..23, y=1..3   (3 wide × 3 tall)
PARTS / SERVICE   inner wall z=8,  opening x=22..23, y=1..3   (2 wide × 3 tall)
BACKSTAGE         east wall  x=29, opening z=4..5,  y=1..3    (2 wide × 3 tall)
```

There are **52** `door_frame_*` blocks in the structure but **none of them is an opening**. Every frame
block has a solid wall block on all four horizontal sides (checked programmatically), i.e. they are
decorative frame trim set into solid walls. They are therefore *not* turned into doors; they are mapped
to a solid trim wall block. This is what "many door-frame decorations" means for this structure.

## 2. Doors (exactly one per real opening)

| opening | kind | old block(s) | new blocks | facing | hinge |
|---|---|---|---|---|---|
| front entrance | `big_entrance` (3 wide × 3 tall) | `big_entrance_door` @ 22,2,38 | `fnafmw:big_entrance_door` origin **22,1,38** + 14 parts | south | — |
| parts/service | `double` (2 leaves) | `parts_service_door` @ 23,2,8 | `fnafmw:parts_service_door` @ **22,1,8** + **23,1,8** | south | left / right |
| backstage | `double` (2 leaves) | `backstage_door` @ 29,2,4 | `fnafmw:employee_door` @ **29,1,4** + **29,1,5** | west | left / right |

* **Both halves:** every standard leaf is listed with `pos` = lower half (y = 1) and `upper_pos`
  (y = 2). The old custom door blocks sit at y = 2 only because the old GeckoLib models render one
  block lower than their own position (geo bounds y = −16…32 px). The real openings are y = 1..3.
* **Width:** the two interior openings are exactly 2 blocks wide, so they become 2-leaf double doors.
  The old GeckoLib models are 2 blocks wide as well (`partsservice_door.geo.json`
  x = −24…8 px, `backstagedoor.geo.json` x = −8…24 px → 32 px = 2 blocks), so a single 1-wide door
  would leave half the opening empty. The front opening is exactly 3 wide and 3 tall, which matches the
  new 3×3/15-part `big_entrance_door`.
* **Hinge:** hinges are on the outer edges so the leaves meet in the middle
  (facing south → `hinge=left` is the west edge; facing west → `hinge=left` is the north edge,
  per `IHingeDoorController.getHingeDirection`).
* **No duplicates:** the old structure contains exactly one block for each of the three doors, and no
  `*_door_open` variant. No door is added anywhere else.
* The y = 3 row of each 3-tall opening stays air above the 2-tall doors (transom), which is normal.

### Why y = 1 and not the old block y = 2
`fnafmw:big_entrance_door` is 3 tall starting at the origin, and vanilla doors are 2 tall starting at
`pos`. Using the old y = 2 would push the big entrance into the solid y = 4 wall and leave the
y = 1 row empty, so the origin/`pos` is the **bottom** cell of the opening in all three cases.

## 3. Changes relative to the global map

`block_overrides` (10 entries) — only one real change:

* `door_frame_single`, `door_frame_top`, `door_frame_side_left/right`,
  `door_frame_corner_left/right`, `door_frame_3x_3_bottom/side/corner/top`
  → **`fnafmw:retro_wall_blue_black_trim`** instead of `fnafmw:classic_pizzeria_wall_black_trim`.
  The global target is a Classic Pizzeria wall; this structure uses the retro (FNAF2) wall set
  (`retro_wall` / `retro_wall_top` / confetti tops), so the retro blue/black trim is the matching
  solid block. Both are full cubes, so placement is identical — this is purely a texture choice.

Everything else keeps the global mapping. Notable global decisions that were kept and verified:

* `fnaf_2_wall` → `retro_wall`, `fnaf_2_wall_top` → `retro_wall_top`,
  `fnaf_2_tiles` → `blue_green_tiles`, `tile` → `black_white_tiles` (floor tiles).
* `desk_horizontal_left/right` → air, `desk_horizontal_middle` → `classic_desk`.
* `trash`, `paper`, `pizza_box`, `cupcake`, `guitar`, `hanging_stars`, buntings, `storage_shelf`,
  shelves, `vent_trigger`, `paperdoll_bunting` → air (clutter removal).
* `trashcan` → `minecraft:cauldron`, `potted`-style boxes → `small/large_cardboard_box`.

## 4. Rotations and facings corrected

### 4.1 Facing flips (29 old block names)

For every directional old block I compared the **old blockstate `y` rotation** with the **new
blockstate `y` rotation** for the same `facing` value. Almost every block is authored 180° apart
(e.g. old `locker` `facing=north` → `y=0`, new `locker` `facing=north` → `y=180`). Those blocks get a
`facing_map` that swaps north↔south / east↔west, which reproduces the old rendered orientation
exactly:

```
backstage_shelf, bathroom_sign_female, bathroom_sign_male, cardboard_box,
children_drawings_2, children_drawings_3, desk_horizontal_middle, fan,
hanging_wall_wires, hanging_wires, hanging_wires_alt, hazard_sign, locker,
monitor, nebraska_poster, open_neon_sign, paper_pal, retro_monitor,
safety_rules, small_cardboard_box, toy_freddy_poster, wall_divider,
wall_wires, wall_wires_curved, wall_wires_curved_long, wall_wires_curved_thin,
wall_wires_dangling, wall_wires_thin
```

Verified **identity** (old and new `y` tables already match):
`fnaf_1_chair` → `classic_chair`, `laptop` → `terminal`, and all doors
(`pizzeria_door`/`staff_door` → `entrance_door`/`employee_door` use the same y table).

This supersedes/extends the mod's built-in `LegacyBlockRules.FLIPPED_FACING` list, which is missing
most of the wall decorations and `backstage_shelf`. **Do not apply both** — see risks.

### 4.2 Other rotation fixes

* `shingle_stair` → `fnafmw:cyan_shingle`: the new block's `y` table is offset by −90° from the old
  stair, so `facing_map` is `north→east, east→south, south→west, west→north`
  (`half`/`shape`/`waterlogged` dropped).
* `speaker` → `fnafmw:classic_speaker`: new block uses `rotation` (8 × 45°, cardinal = even).
  Per-instance `rotation` set: west → `6`.
* `out_of_order_sign` → `rotation` (16 × 22.5°): facing west → `rotation=12`, `wall=true`.
* `exit_sign` → `rotation`: facing north → `rotation=0`, `wall=true`.
* `hanging_lamp`: new block needs `face=ceiling` + `facing=down` (old only had `facing`).
* `band`-type blocks that only have a `face` property get `face=wall`
  (`door_frame_*`, `tiled_light_facade_bricks`).
* `normal_light` / `normal_light_on` → `basic_light_warm` with `lit=false` / `lit=true`.
* `open_neon_sign` and the two bathroom signs get `lit=true`.

### 4.3 Windows — `layout`, and the one case that cannot be rotated

`fnafmw:window` has **only** a `layout` property (0…46) and **no** facing/rotation. All 47 models are
fixed in the north/south plane (panes at z = 2 and z = 14, posts at x = 0 and x = 14). Layout values
encode which edges have a frame; decoded from the model JSON:

| layout | frame on |
|---|---|
| 3 | east post + sill |
| 6 | east post + head |
| 9 | west post + sill |
| 11 | sill only |
| 12 | west post + head |
| 14 | head only |

Front window (z = 38, facing north, 3 wide × 2 tall) → real `fnafmw:window`:

| pos | old | layout |
|---|---|---|
| 25,2,38 | window_bottom_right | 9 |
| 26,2,38 | window_bottom_mid | 11 |
| 27,2,38 | window_bottom_left | 3 |
| 25,3,38 | window_top_right | 12 |
| 26,3,38 | window_top_mid | 14 |
| 27,3,38 | window_top_left | 6 |

The two windows on the **east wall x = 29** (z = 24…26 and z = 34…36) need a 90° rotation that the
block cannot express, so those 12 blocks are replaced with **`minecraft:glass`** (a block that looks
correct from any side) rather than shipping a visibly sideways window model.

### 4.4 Modular tables — `modular_type`

`fnafmw:party_table` needs `modular_type` 0…15. The new models were decoded from
`modular/table/base/*` (legs at x = 2/14, z = 2/14), which shows **left = east, right = west,
bottom = north, top = south**. The old table parts in this structure are laid out exactly that way
(`bottom` = lower z, `left` = higher x), so the mapping is direct:

| old | modular_type |
|---|---|
| wood_table_bottom_left | 3 |
| wood_table_bottom_middle | 11 |
| wood_table_bottom_right | 9 |
| wood_table_middle_left | 7 |
| wood_table_middle_right | 13 |
| wood_table_top_left | 6 |
| wood_table_top_middle | 14 |
| wood_table_top_right | 12 |

Per-instance fix: the 3×3 table at x = 17…19 / z = 27…29 had its **centre** cell authored as
`wood_table_middle_left` (a duplicate of the real left cell). It is replaced with `modular_type=15`
(the plain `middle` piece) so the table top has no hole and no duplicate leg.

### 4.5 `wall_divider` variant

Old `blockstate` 0/1/2 → `wall_divider` / `wall_divider_right` / `wall_divider_left`
(new `variant` 1/3/2). All 18 dividers in this structure are end pieces, so each got an explicit
`facing` (already flipped) + `variant` in `position_fixes`.

## 5. Validation

* Every `fnafmw:` id used was checked against `new_blockstates.txt` **and** against the new jar's
  blockstate JSON. All properties (`face`, `layout`, `rotation`, `modular_type`, `lit`, `facing`,
  `wall`, `variant`, `half`, `hinge`, `part`) and all values exist in the jar.
* No `management_wanted:*` and no `fnafmw:legacy_placeholder` end up in the result
  (`management_wanted:*` only appears in `old`/rule keys).
* Door cell count in the converted grid: 15 (big entrance) + 4 (parts/service, 2 leaves × 2 halves)
  + 4 (backstage) = 23 blocks, exactly one door per opening.
* Re-ran the converter on the NBT: 66 distinct result blocks, nothing unmapped except deliberate air.

## 6. Preview

Rendered with Python + PIL (not in game):

* `fnaf2-preview.png` — isometric view of the converted structure (roof on).
* `fnaf2-preview-cutaway.png` — same, everything at y ≥ 6 removed so the interior floor is visible.
* `fnaf2-plan.png` — top-down plans of the converted y = 1 and y = 2 layers with the three door
  openings outlined and labelled, plus a legend.

Colours are averaged block textures from the new jar (vanilla blocks use flat colours), so the images
are a *schematic* of block placement, not a faithful render. They confirm that the three openings are
open, that each opening has exactly one door with both halves, and that the windows sit in the wall
lines. They cannot show model rotations, so the facing work above is verified against the blockstate
JSON, not the pictures.

## 7. Remaining visual risks

1. **Door frame trim is chunky.** `retro_wall_blue_black_trim` is a full cube, so the old 1-pixel
   door-frame strips become a solid wall block with a black band. Placement is safe (they replace
   solid wall blocks, so no opening is created or blocked), but the thin frame look is gone.
2. **Double-flip risk.** If the runtime also applies the mod's `FLIPPED_FACING` rules, the flips for
   `locker`, `retro_monitor`, `wall_wires`, `children_drawings_2`, `children_drawings_3` would be
   applied twice and cancel out (block faces the wall). Use the JSON rules **or** the mod rules, not
   both.
3. **3-tall opening, 2-tall doors.** The parts/service and backstage openings are 3 high; standard
   doors only fill 2. The top row stays open (transom). If a full-height door is wanted, the new
   `fnafmw:big_parts_service_door` / `fnafmw:big_employee_door` (2 wide × 3 tall, 12 blocks) are the
   exact successors of these old doors, but the JSON schema only has `single`/`double`/`big_entrance`
   kinds, so this was not used.
4. **East windows are plain glass.** They are the correct orientation but lose the new window model.
5. **`shingle_stair` → full `cyan_shingle`.** 110 roof stairs become full cubes, so the eaves are
   chunkier than the old stepped roof.
6. **`wall_divider` left/right** is set per instance; if the new `_left`/`_right` models are mirrored
   relative to the old ones the 1-pixel plate lands on the wrong edge of its block.
7. **`speaker` rotation** is the lowest-confidence mapping (the model is nearly symmetric, so the
   chosen value only affects which face the cone points at).
8. Nothing was tested in game; the JSON is derived from the NBT, the old/new jars and the new mod
   source only.
