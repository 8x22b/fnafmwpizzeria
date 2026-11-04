# fnaf1 – visual conversion report

## Structure

- Source: `fnafmwpizzeria/structures/fnaf1.nbt` (old 1.20.1 mod).
- Size: **37 × 48 × 34** (x, y, z); 60,384 block records, 207 palette states.
- Floor is y=0; interior walls start at y=1.
- Old custom doors (`backstage_door`, `parts_service_door`, `big_entrance_door`) are single blocks at **y=2** whose models overflow one block down and two blocks up, so their real opening is y=1..3. Standard doors (`staff_door`, `kitchen_door`) already have lower/upper halves at y=1/2.

### Door openings found (old coordinates)

| Opening | Old block | Real opening | Type |
|---|---|---|---|
| Staff door | (5, 1..2, 13) facing west | 1 wide × 2 tall in x=5 wall | single |
| Kitchen door | (11, 1..2, 15) facing north | 1 wide × 2 tall in z=15 wall | single |
| Parts/service | (8, 2, 9) facing south | 2 wide (x=7..8) × 3 tall in z=9 wall | double |
| Backstage A | (21, 2, 4) facing east | 2 wide (z=3..4) × 3 tall in x=21 wall | double |
| Backstage B | (25, 2, 3) facing west | 2 wide (z=3..4) × 3 tall in x=25 wall | double |
| Backstage C | (5, 2, 19) facing east | 2 wide (z=18..19) × 3 tall in x=5 wall | double |
| Backstage D | (5, 2, 23) facing east | 2 wide (z=22..23) × 3 tall in x=5 wall | double |
| Backstage E | (29, 2, 28) facing west | 2 wide (z=28..29) × 3 tall in x=29 wall | double |
| Main entrance | (12, 2, 32) facing south | 3 wide (x=11..13) × 3 tall in z=32 facade | big_entrance |

Two further door-*frame* openings exist without any door in the old NBT: **(x=23, z=15)** and **(x=9, z=22)**, both 1 wide × 3 tall. They are left open as archways; adding doors there would be inventing openings.

## Changes relative to the global map

`block_overrides` (structure-only):

- `management_wanted:kitchen_door` → **`fnafmw:white_entrance_door`** instead of `fnafmw:big_employee_door`.
  `big_employee_door` is a 1-block controller with an empty model (rendered by a block entity); pasted from a structure it shows nothing and has no `half`/`facing` support, so the old lower+upper states were also invalid. `white_entrance_door` is the closest new standard white door to the old white kitchen door texture and is a real 2-block door.
- `management_wanted:backstage_door` → **air** (5 old blocks). The global map turned each single block into one 1-wide, 2-tall `employee_door` leaf at y=2, i.e. a floating half-door with no lower half. They are rebuilt from the `doors` list as 2-wide double leaves at y=1/2.
- `management_wanted:parts_service_door` → **air** (1 old block), same reason; rebuilt as two `parts_service_door` leaves.
- `management_wanted:big_entrance_door` → **air** (1 old block); rebuilt as the 15-part `big_entrance_door` from the `doors` list.

Other per-structure fixes (not block-name changes):

- **Window**: the global map drops the old `facing` and every block would use the default `layout=0`. The 3×2 storefront window at x=25..27, y=2..3, z=32 is given the correct connection layout per cell (3/6/9/11/12/14), derived from the new `window_N` model element patterns.
- **Party tables**: the global map loses the part identity and defaults `modular_type=0` (single). Every old table part is assigned its correct modular type (0–15 numbering read from `party_table.json` and the per-part models).
- **Facing/rotation** corrections are listed below.

## Doors (explicit)

All standard doors are placed with the lower half at floor level **y=1** and are 1 wide × 2 tall. Backstage and parts/service leaves are 2-wide double doors. Hinge is chosen so each leaf hinges on its outer jamb.

| pos (lower) | kind | facing | hinge | block | note |
|---|---|---|---|---|---|
| [5, 1, 13] | single | west | right | `fnafmw:employee_door` | staff door |
| [11, 1, 15] | single | north | right | `fnafmw:white_entrance_door` | kitchen, replaces wrong mapping |
| [7, 1, 9] | double | south | left | `fnafmw:parts_service_door` | west leaf |
| [8, 1, 9] | double | south | right | `fnafmw:parts_service_door` | east leaf |
| [21, 1, 3] | double | east | right | `fnafmw:employee_door` | backstage A, south leaf |
| [21, 1, 4] | double | east | left | `fnafmw:employee_door` | backstage A, north leaf |
| [25, 1, 3] | double | west | left | `fnafmw:employee_door` | backstage B, north leaf |
| [25, 1, 4] | double | west | right | `fnafmw:employee_door` | backstage B, south leaf |
| [5, 1, 18] | double | east | right | `fnafmw:employee_door` | backstage C, south leaf |
| [5, 1, 19] | double | east | left | `fnafmw:employee_door` | backstage C, north leaf |
| [5, 1, 22] | double | east | right | `fnafmw:employee_door` | backstage D, south leaf |
| [5, 1, 23] | double | east | left | `fnafmw:employee_door` | backstage D, north leaf |
| [29, 1, 28] | double | west | left | `fnafmw:employee_door` | backstage E, north leaf |
| [29, 1, 29] | double | west | right | `fnafmw:employee_door` | backstage E, south leaf |
| [12, 1, 32] | big_entrance | south | — | `fnafmw:big_entrance_door` | 3×3 origin, bottom-centre |

**Big entrance origin correction:** the old block is at y=2 (the middle row of the 3-tall opening). The new 15-part door expands from part 0, which is the **bottom-centre** cell, so the origin must be **(12, 1, 32)**, not the literal old block position. Using y=2 would make the door float one block and clip the facade lintel at y=4. The opening is x=11..13, y=1..3, so the 3×3 door fills it exactly; its side columns extend north into z=31, which is air.

Door entries also carry `block` (intended new door block) and `hinge`; the printed schema only showed `pos`/`kind`/`facing`/`reason`, so these are extra descriptive fields for runtimes that honour them.

## Rotations / facings corrected

New model bases are frequently authored facing south while the old ones face north. For every old block where the new blockstate's `facing=north` variant is rotated 180° relative to the old one, `facing_map` flips N↔S and E↔W:

- lockers/shelves: `locker`, `backstage_shelf`, `file_cabinet_black`
- wall art: `fnaf_1_poster_freddy_jaw`, `celebrate_poster_fnaf1`, `children_drawings_2`, `safety_rules`, `paper_pal`, `nebraska_poster`
- `desk_horizontal_middle` (new `classic_desk`), `fan` (new `desk_fan`)
- `cardboard_box`, `small_cardboard_box`
- all wire variants: `wall_wires`, `wall_wires_thin`, `wall_wires_curved`, `wall_wires_curved_thin`, `wall_wires_curved_long`, `wall_wires_dangling`, `hanging_wires`, `hanging_wires_alt`, `hanging_wall_wires`, `hanging_wall_wires_thin`
- `open_neon_sign`
- `shingle_stair`

Not flipped (new blockstate matches old): `fnaf_1_chair` (new `classic_chair`), `laptop` (new `terminal`).

No block-state facing exists on these new blocks; they were handled explicitly:

- `out_of_order_sign` → `rotation=0`, **`wall=false`** (the old model is a free-standing sign with a post on the floor at (19,1,27), not a wall sign).
- `exit_sign` → `rotation=0`, `wall=true`, `centered=false`, `black=false` (wall-mounted high on the south wall at (12,4,31); new wall shape index 0 is the south-wall mount).
- `speaker` → `rotation=0` (floor speaker; new model reuses the old orientation).
- `light_bulb` (2×) and `hanging_lamp` (1×) → `face=ceiling`, `facing=down`, `lit=true`, `powered=true`.
- `door_frame_*` → old `facing` dropped; new black wall trim uses `face=wall`.
- `wall_divider` → old `blockstate` dropped, `variant=1` (centred 4-px band). Old east/west facing is kept; in the new blockstate this puts the band on the opening-facing jamb face (east face of x=14, west face of x=16), which is the intended trim around the x=15 archway.
- Standard doors: old `powered` dropped (new `employee_door` / `white_entrance_door` have no `powered`).

## Party table modular types

| old part | `modular_type` |
|---|---|
| wood_table_bottom_left | 3 |
| wood_table_bottom_right | 9 |
| wood_table_middle_left | 7 |
| wood_table_middle_right | 13 |
| wood_table_top_left | 6 |
| wood_table_top_right | 12 |
| backstage_table_left (north end) | 1 (vertical_bottom) |
| backstage_table_middle | 5 (vertical_middle) |
| backstage_table_right (south end) | 4 (vertical_top) |

The new table has no `facing`; its wood-table part names line up directly with the old geometry (left = east, bottom = north), and the old backstage table runs north–south, so it uses the vertical set.

## Remaining visual risks

1. **Transom gaps.** Backstage and parts/service openings are 3 high, but the best available new doors are 2 high. Each double therefore leaves a 1-block gap above. The old custom doors were 3 tall; a 2×3 standard door does not exist. (New `big_employee_door`/`big_parts_service_door` are 2×3 but are block-entity controllers with empty block models and cannot be safely expanded from a position fix.)
2. **Door frames.** Old `door_frame_*` thin pieces become full `classic_pizzeria_wall_black_trim` cubes. They do not block any opening, but the jambs/lintels are chunky black blocks rather than thin trim.
3. **Roofline.** The global map maps 112 `shingle_stair` blocks to full `cyan_shingle` blocks. Facing is flipped to keep the texture direction, but the stair shape is lost.
4. **Doors list dependency.** `backstage_door`, `parts_service_door` and `big_entrance_door` are set to air, so those openings depend on the `doors` list actually placing the new blocks. If a runtime ignores the `block` field, parts/service and backstage may be rebuilt with the default standard door rather than the intended door texture.
5. **Big entrance origin.** Correct at (12,1,32). A runtime that insists on the literal old block position (y=2) will float the door and overlap the y=4 lintel.
6. **Window layouts.** Values were derived from the new model element sets, not from code; verify the 3×2 window in-game.
7. **Box variants.** Old `blockstate` on `cardboard_box` and the many new `variant` models on both boxes are dropped/defaulted, so some boxes change size/style.
8. **Nebraska poster.** The old model is a flat, nearly-horizontal decal; it is mapped to a normal wall poster.
9. **Wall divider.** The old left/right half-bands are simplified to the centred band variant; still reads as a jamb pilaster.
10. **No 3D render.** Two schematic 2D previews were generated from the NBT plus this JSON: `fnaf1_plan.png` (top-down plan with door markers and facing arrows) and `fnaf1_south_facade.png` (south elevation showing the 3×3 entrance opening and 3×2 window). No isometric block renderer was built, so these are placement checks, not texture previews.
