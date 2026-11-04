# fredbears — signs & cardboard boxes

Input NBT: `fnafmw-companion/src/data/fnafmwpizzeria/structures/fredbears.nbt` (29x10x26, unchanged).
Plan file: `signs_boxes_fredbears.json`.

## Big entrance

| | |
|---|---|
| Block | `fnafmw:big_entrance_door`, 15 parts |
| Origin (part=0) | **(12, 1, 24)** |
| Footprint | x = 11..13, y = 1..3, z = 24 (3 wide × 3 tall, 2 deep) |
| `facing` | **south** (outward = +z) |
| Wall plane | z = 24 (light_blue_terracotta at y=4) |
| Outside | z = 25 (air above the gravel y=0 sidewalk) |

## OPEN sign

The existing `fnafmw:open_neon_sign` sat **inside** the entrance wall cell `(12,4,24)` — flush with the wall, not in front of it.

| Pos | Action | Result |
|---|---|---|
| (12,4,24) | remove sign, then add `minecraft:light_blue_terracotta` | wall restored behind the sign |
| (12,4,25) | add `fnafmw:open_neon_sign` | `facing=south`, `alt=false`, `lit=true`, `powered=false` |

- One block outside the wall, centred over the 3-wide door (x=12), at origin.y+3 = **y=4**.
- Facing south matches the door and the model's front (blockstate `facing=south` = y=0).
- Verified against the jar: variant `alt=false,facing=south,lit=true,powered=false` exists.
- Result after applying the plan: exactly **one** `fnafmw:open_neon_sign` in the structure.

## Other signs

| Pos | Block | Before | After | Why |
|---|---|---|---|---|
| (25,3,12) | `fnafmw:fredbears_advertisement_poster` | `facing=west` | **`facing=east`** | It hangs on the outside of the east wall (solid x=24, outdoors is x=25+). Facing west pointed the poster into the wall with its art plane flush at x=25.0 (embedded); the east side (x=26) was empty air. Facing east puts its back on the solid x=24 wall and the art toward the gravel strip. |
| (12,1,5) | `fnafmw:out_of_order_sign` | `wall=false, rotation=0` | unchanged | Standing A-frame sign (`wall=false` is correct, not a wall-mounted one); sits on the carpet floor at y=0 in front of the low tile partition. |

No pizza/restroom/neon signs exist in this structure, so there is nothing else to re-mount.

## Cardboard boxes

58 boxes present (small + large; no medium). A box is "floating" when `(x,y-1,z)` is air.

### Removed — floating (10)

| Pos | Box | Support below before |
|---|---|---|
| (3,2,1) | small | air (old tile floor, not a shelf) |
| (5,2,4) | small | air — old `metal_shelf` was converted to air |
| (5,2,8) | small | air |
| (5,3,1) | small | air, 3 blocks up |
| (6,4,11) | small | air, 4 blocks up in the dining room |
| (18,3,19) | small | air, 3 blocks up beside the x=18 door wall |
| (20,2,19) | small | air |
| (21,2,13) | small | air — old desk top; the new `classic_desk` moved to (20,1,13) |
| (23,4,6) | small | air, tucked under the office ceiling |
| (23,4,16) | small | air, tucked under the office ceiling |

All were random clutter, so they are deleted rather than propped up; no support blocks were added.

### Kept (48)

- Rest on the floor (black/white tiles, ruined tiles, confetti carpet) or on a large box below.
- Stack heights are at most 3 (the (7,1..3,4) pile in the back room) — every cell has a full block underneath.
- 4 small boxes rest on `party_table` block tops and 3 rest on the 1-high `classic_pizzeria_wall_bathroom_trim` ledge; these are solid supports, so they do not float and were left in place.
- No box is inside a `locker` / `locker_part` cell, inside a door opening, in front of a door, or on the entrance path (checked against all 5 door groups and the 8 locker cells).

## Validation

The plan was applied to a copy of the NBT palette in memory and re-checked:

- exactly 1 `open_neon_sign`, at (12,4,25), support (12,4,24) = light_blue_terracotta;
- 48 boxes left, **0** with air below;
- 0 boxes on locker or door cells;
- poster at (25,3,12) `facing=east` with solid (24,3,12) behind it;
- all `fnafmw:` IDs/properties matched against the new jar's blockstates.

## Remaining risk

- These flat sign models put the artwork on the far face of their own block, so the OPEN panel at (12,4,25) renders on the z≈26 face — i.e. it stands about one block proud of the wall, the same convention the mod's posters use. If the packer expects the art flush on the wall instead, the sign would have to sit in the wall cell (explicitly forbidden by the task).
- If the conversion pipeline re-runs its old `open_neon_sign` / poster `facing_map`, it could flip the poster back to west; this JSON is the source of truth and should be applied last.
- The 10 deleted boxes are gone with no replacement clutter; if that room needs to look "stored in", a couple of floor boxes already remain nearby.
