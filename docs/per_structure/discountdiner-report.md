# discountdiner — per-structure conversion report

Old NBT: `fnafmwpizzeria/structures/discountdiner.nbt` (management_wanted 1.3.3)
New mod: `fnafmw-alpha-1.0.0-snapshot-1`

## 1. Structure

| | |
|---|---|
| Size | **29 x 10 x 30** (x, y, z); runtime rotation not applied |
| Floor | y=0 — smooth stone outside, oak planks inside, polished andesite threshold at the front door |
| Walls | y=1..6 brown facade bricks, interior partitions of stripped spruce logs |
| Ceiling | y=6 ceiling tiles (27 lighter `normal_light_on` panels), roof slabs at y=7 |
| Rooms | dining hall z=13..26; stage 5x3 at x=9..13, z=13..15; backstage/kitchen z=1..11 north of the z=12 log wall; east corridor x=21..26 behind the x=20 log wall; store room z=0..5 |

## 2. Doors — 7 openings, 11 leaves

`pos` = **lower** half (floor level, y=1); every standard door also gets its upper half at y+1.
Included in the JSON `doors` array.

| # | kind | old block(s) | new leaves (lower) | facing | hinge |
|---|---|---|---|---|---|
| 1 | single | staff_door (5,1,12)+(5,2,12) | (5,1,12) | north | right (old value kept) |
| 2 | double | backstage_door (18,2,6) | (17,1,6) r / (18,1,6) l | north | mirrored pair |
| 3 | double | backstage_door (15,2,9) | (15,1,8) l / (15,1,9) r | east | mirrored pair |
| 4 | double | backstage_door (20,2,8) | (20,1,8) r / (20,1,9) l | west | mirrored pair |
| 5 | double | backstage_door (20,2,15) | (20,1,15) r / (20,1,16) l | west | mirrored pair |
| 6 | double | backstage_door (20,2,24) | (20,1,24) r / (20,1,25) l | west | mirrored pair |
| 7 | big_entrance | big_entrance_door (6,2,27) | origin (6,1,27) | south | 15 parts, runtime-expanded |

Decisions:

* **Old custom doors were 2 wide x 3 tall, one block placed off-centre in the opening.** Each backstage
  opening is exactly 2 wide x 3 tall (verified column by column), so each becomes a **double standard
  door** (2 leaves, 2 tall) — the width is preserved exactly, nothing is left half-open.
* The 3rd (top) row of every backstage opening is filled with the surrounding wall material
  (`brown_facade_bricks` at (17..18,3,6), `stripped_spruce_log` elsewhere) so the 2-tall door does not
  leave a 2x1 hole. 10 `position_fixes`.
* **The old door blocks are removed** (`staff_door`, `backstage_door`, `big_entrance_door` → air) and
  all doors come from the `doors` list, so there is exactly one door per opening and no orphan halves.
* **The big entrance origin is y=1, not the old block's y=2.** The old single block was centred on the
  3-tall model (y=1..3 in world space); `fnafmw:big_entrance_door` part 0 is the *bottom-centre* block
  and its frame spans y 0..48 relative to the origin (`PARTS`/`FRAME` in `BigEntranceDoorBlock`).
  Origin (6,1,27) makes the frame cover exactly x=5..7, y=1..3. Its second (back) layer lands at
  z=26, x=5 and x=7 — those cells are air in the old NBT, so nothing is destroyed. The old position
  (6,2,27) is named in the door's `reason` string.
* **Brief vs NBT:** the task said "2 standard staff doors"; the palette has two `staff_door` entries
  (lower + upper) but they are the same door at (5,1,12)/(5,2,12). There is **one** standard door.
* Hinge: `fnafmw:employee_door` uses the same vanilla door blockstate table as `staff_door`
  (`north→y270`, `south→y90`, …), so the old facing and hinge carry over unchanged. Only the *single*
  door's hinge comes from the old data; for the new double doors the two leaves are given opposite
  hinges (hinge on the outer jamb, handles meeting in the middle). If the swing direction is wrong in
  game, swap `left`/`right` for that pair — it does not affect the shape of the opening.

## 3. Changes relative to the global block map

| old block | global map | this structure | why |
|---|---|---|---|
| `backstage_shelf` | `fnafmw:locker` | **air** | 4 *wall-mounted* shelves at y=2; `fnafmw:locker` is a floor-standing 2-block-tall block → they would float and clip the wall |
| `locker_void` | `fnafmw:locker_part` | **air** | `fnafmw:locker` is already 2 blocks tall as a single block; keeping the upper half would duplicate/stack the model |
| `staff_door`, `backstage_door`, `big_entrance_door` | `fnafmw:employee_door` / `fnafmw:big_entrance_door` | **air** | rebuilt from the `doors` array (avoids half doors, wrong widths, double placement) |
| everything else | — | unchanged | |

Removed as clutter (already air in the global map, confirmed): 36 `trash`, 11 `metal_shelf`,
7 `paper`, 1 `utility_shelf_small`, 1 `storage_shelf`, 1 `mop`, 2 `pipes`, 2 `party_hat_red`,
1 `digital_clock`, 1 `paperdoll_bunting`.

## 4. Multi-block furniture (explicitly positioned)

### Dining tables — old 4x2 `wood_table_*` → 3x3 modular `party_table`
The new party table has no 4x2 variant (its 16 types are single / 3-long / 3x3), so each old table
becomes a 3x3 table centred on the old one. Type numbers were derived from the models: `top` = south
edge, `bottom` = north edge, `left` = east edge, `right` = west edge.

Table A x=7..9, z=18..20 and table B x=13..15, z=18..20 (same pattern):

| | z=18 (north) | z=19 | z=20 (south) |
|---|---|---|---|
| x = west | 9 `bottom_right` | 13 `middle_right` | 12 `top_right` |
| x = middle | 11 `bottom_middle` | 15 `middle` | 14 `top_middle` |
| x = east | 3 `bottom_left` | 7 `middle_left` | 6 `top_left` |

Old columns (6,19-20) and (16,19-20) become air; the z=18 row is new (`action: replace` on cells whose
old block is air). Net effect: two 3x3 tables instead of two 4x2 tables, no hole in a tabletop.

### Backstage counter — old 3x1 `backstage_table_*` → 3-long `party_table`
(21,1,11)=8 `horizontal_right` (west end), (22,1,11)=10 `horizontal_middle`, (23,1,11)=2 `horizontal_left`
(east end). Fits exactly between the log pillars at x=20 and x=24.

### Lockers
Old `locker` + `locker_void` pair at (24,1..2,8) and (24,1..2,9) → **one** `fnafmw:locker` per column
at y=1, `facing=east` (back against the x=25 wall).

## 5. Facing / rotation corrections

The two mods do **not** share a facing convention. For most new directional blocks the blockstate is
`facing=south → y=0` (the model is authored with its **back** on the +z side, so `facing` names the
wall side), while the old mod is `facing=north → y=0` (facing names the **front**). Verified on the
models themselves, not on names: e.g. `desk_fan` y=0 geometry matches `fan_on` y=0 (score 0.60 vs 0.14
for a 180° match) while its blockstate table is rotated 180°; `thin_wall_wires`, `large_cardboard_box`
and `locker` show the same.

`facing_map` = `{north↔south, east↔west}` (inverted) for:

`locker`, `classic_monitor`, `desk_fan`, `hazard_sign`, `open_neon_sign`,
`fredbears_advertisement_poster`, `crying_child_poster`, `classic/rebrand/fright_childrens_drawings`,
`thin_wall_wires`, `light_bulb`, `large_cardboard_box`, `small_cardboard_box`.

Identity (no change) for:
* `employee_door` — same vanilla door table as `staff_door`.
* `terminal` — blockstate is `north=0`, same as the old `laptop`.

Blocks that needed explicit property values beyond the plain facing map:

| block | pos | correction |
|---|---|---|
| `fnafmw:out_of_order_sign` | (11,1,16) | `rotation=8` (= y180, text faces south), `wall=false` |
| `fnafmw:exit_sign` | (6,4,26) | `rotation=0` (= y0, faces north into the room), `wall=true` |
| `fnafmw:classic_speaker` | (8,1,14) | `rotation=0`; the new model is a plain box, rotation does not change it |
| `fnafmw:light_bulb` | 3x | `face=ceiling`, `lit=true`, facing inverted (old never used `face`) |
| `fnafmw:open_neon_sign` | (8,3,26) | `lit=true`, `powered=true`, `alt=false`, facing inverted |
| `fnafmw:desk_fan` / `terminal` | (23,2,11)/(21,2,11) | `lit=true` |
| `fnafmw:basic_light_warm` | 27x | `lit=true` (new block also has six connection booleans; left at default, isolated panels) |

### Windows — old `window_*` → `fnafmw:window`
`fnafmw:window` has **no facing**, only `layout` (0..46), and its glass planes are parallel to the x axis
— which is the correct orientation for this east-west front wall. Layouts 0..15 cover every combination
of frames on the four edges; the 6x2 window only gets a frame on its outer edge:

| | x=12 | x=13..16 | x=17 |
|---|---|---|---|
| y=3 (top) | 12 (top+west) | 14 (top only) | 6 (top+east) |
| y=2 (bottom) | 9 (bottom+west) | 11 (bottom only) | 3 (bottom+east) |

## 6. Validation

`ps_dd_validate.py` replays the whole spec (overrides → property rules → position fixes → doors) and
checks every resulting block id and property against the new jar's blockstate files:

* 0 unknown block ids, 0 invalid properties.
* 0 remaining `management_wanted:*` blocks, no `legacy_placeholder`.
* 22 `fnafmw:employee_door` blocks = 1 single (2) + 5 doubles (20); both halves present in every leaf,
  no third block above any leaf.
* Result: 674 brown facade bricks, 650 ceiling tiles, 21 party tables, 12 windows, 2 lockers,
  27 ceiling lights, 51 wall wires, 15 stage blocks, 3 cauldrons, 3 light bulbs.

## 7. Remaining visual risks

1. **Big entrance depth.** The new 15-part door is 2 blocks deep; its back layer sits inside the room
   at z=26, x=5/7. That is how the model is built, but it makes a small 2-block-deep vestibule that
   the old 1-deep door did not have.
2. **Big entrance origin.** If the runtime insists on the *old* block position (6,2,27) as origin
   instead of the `pos` in the JSON, the door will be one block too high and its top row will replace
   the brick row at y=4. `pos` is deliberately (6,1,27); the old position is named in its `reason`.
3. **Door height.** Backstage openings are 3 tall; standard doors are 2 tall. The top row is filled,
   so the doors read as 2-tall doors in a 3-tall wall — a slight proportion change from the old
   (visually 3-tall) custom doors.
4. **Double-door hinge side** is a guess (mirrored pair, hinges on the outer jambs). If they swing the
   wrong way, flip left/right per pair.
5. **Party table shape.** Old tables were 4x2, new are 3x3 — one block narrower, one deeper. The extra
   north row is a `replace` whose old block is `minecraft:air` (assumes the runtime can set a block into
   an empty cell, like the big-door expansion does); if it cannot, the tables will be missing their
   north row and the `modular_type` 9/11/3 cells should be dropped.
6. **No shelf/counter block exists in the new mod**, so all metal shelves, backstage shelves and the
   small utility shelf are gone; the store room and backstage are emptier than before.
7. **Window `layout` may be recomputed** by the game on a neighbour update; if it is, the frames will
   re-derive from the 6x2 grid and should still look right.
8. `minecraft:cauldron` is still used for the three old trashcans (global map choice) — a cauldron is
   not a great visual trashcan, but the new mod has no equivalent.
9. Structure-level rotation is not modelled here: if the build is rotated 90°, `fnafmw:window`
   (no facing) will keep its north-south pane orientation and frames would land in the wrong walls.

## 8. Preview

Rendered with Python + PIL (schematic isometric/plan projections, flat colours per block id — **no**
block models or textures, so furniture silhouettes are cubes):

* `discountdiner-iso.png` — whole structure, y=0..7
* `discountdiner-iso-interior.png` — y=1..3, shows the two 3x3 party tables, stage, window strip and the blue door leaves
* `discountdiner-plan.png` — floor plan at y=1/2 with coordinates; blue = `employee_door`, cyan = window, magenta = big entrance opening (x=5..7, z=27)
* `discountdiner-front.png` — front elevation looking north; 6-wide window at x=12..17, entrance at x=5..7

The big entrance itself is not drawn (it only exists as 15 runtime-expanded parts), so the preview
shows a hole at x=5..7 in the front wall.
