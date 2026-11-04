# jeffspizza — signs & cardboard boxes

Structure size 33x17x43. Only `fnafmw:` signs and cardboard boxes were touched; no other block changes.

## Big entrance

| item | value |
|---|---|
| block | `fnafmw:big_entrance_door` |
| origin (part=0) | **[30, 1, 37]** |
| facing | **east** (outward is +x; facade wall plane is x=30) |
| size | 3 wide (z=36..38) x 3 tall (y=1..3) |
| model block | part=4 at [30,2,37] |
| part 9..14 | x=29 (inside the wall thickness) |

The OPEN sign target is one block above the door top and in the middle of the opening: origin y 1 + 3 = 4, z center = 37, x = 31 (first cell outside the x=30 wall).

## OPEN sign

Before: exactly one `fnafmw:open_neon_sign`, at **[30,4,37]** — it sat *in* the wall plane (a facade block was replaced by the sign, with air behind it to the west), so the wall had a 1-block hole.

Changes:

| pos | action | block | properties |
|---|---|---|---|
| [30, 4, 37] | replace | `fnafmw:brown_facade_bricks` | — (restores the wall the sign was occupying; neighbours at [30,4,36]/[30,4,38]/[30,5,37] are the same block) |
| [31, 4, 37] | add | `fnafmw:open_neon_sign` | `facing=east, alt=false, lit=true, powered=false` |

Result: wall solid at x=30, single OPEN sign on the outside face above the door, facing east like the door. No second neon sign anywhere in the structure.

Model note (verified in the jar): `base_open_neon_sign` is a single 16x16 quad at z=15.98, i.e. on the *facing* face (`facing=south` = y=0). With `facing=east` that quad lands on the +x face of the sign cell, so the panel reads as a neon sign standing one block proud of the facade. `facing=west` in the same cell would place the quad exactly on the wall face (flush), but then the state value points into the wall, so the outward-facing variant was used as specified. This is the one visual trade-off of the change.

## Freddy pizza sign

`fnafmw:freddy_fazbear_pizza_sign` at **[31, 8, 21]**, `facing=west, lit=true, powered=false` — **initially audited, unchanged**.
It is one block outside the x=30 facade with the wall directly west of it. The geo model (`assets/fnafmw/geo/block/freddy_fazbear_pizza_sign.geo.json`) is a 78x66 plane at z=+7.95, i.e. on the facing face, so `facing=west` puts the billboard flush on the outer facade skin. It was converted from `management_wanted:jeffs_pizza_sign` (old `facing=east`) using the map's 180-degree facing rule, so the visible direction still matches the old map: outward, over the east front.

## Other signs audited (no changes)

| pos | block | state | wall opposite/behind | verdict |
|---|---|---|---|---|
| [8, 2, 12] | `fnafmw:hazard_sign` | facing=south | [8,2,13] `brown_facade_bricks_trim` | plate on facing face, flush, not embedded |
| [17, 2, 8] | `fnafmw:hazard_sign` | facing=north | [17,2,7] `brown_facade_bricks_trim` | flush |
| [24, 3, 31] | `fnafmw:hazard_sign` | facing=south | [24,3,32] `brown_facade_bricks` | flush |
| [7, 4, 16] | `fnafmw:exit_sign` | rotation=4, wall=true | [6,4,16] `brown_facade_bricks` west | plate on west face, flush on the wall |
| [24, 1, 29] | `fnafmw:out_of_order_sign` | rotation=4, wall=false | — (floor sign) | stand correct on the y=0 floor |

No restrooms / prizes / JRS / arcade neon signs exist in this structure.

## Cardboard boxes

71 boxes before (large, small; no medium). Six were **floating** (air directly below) and were removed:

| pos | block | why |
|---|---|---|
| [21, 4, 3] | large | 3 blocks above the office floor, bolted to the x=20 wall |
| [21, 4, 5] | small | 2 blocks above the top of the locker at [21,1,5]-[21,2,5] |
| [28, 4, 2] | small | top of the storage corner, nothing under it |
| [29, 4, 3] | small | same pile, air below |
| [12, 4, 7] | small | above/behind the twin lockers at z=6 and z=8 |
| [25, 5, 2] | small | stuck under the ceiling tile at y=6 |

65 boxes kept. Every kept box has solid support:

* y=1 boxes stand on the y=0 floor layer (fright_wall / facade bricks / smooth stone);
* y>=2 boxes stand on another box, on `classic_stage` / `red_sturdy_stage` (full cubes) or on `party_table` (table top is 13..16, so the box rests exactly on the block top);
* no box occupies a door cell (checked all 11 openings, including the 3x3 big entrance at x=30, z=36..38);
* no locker front is blocked: [12,1,6] and [12,1,8] face east into the x=13 wall, [27,1,2] faces north, [29,1,3] faces east into the x=30 wall.

Kept piles worth knowing about: two 3-high stacks of large boxes on the stage edge at x=21 (z=10) and x=29 (z=10-11), and the exterior stack at [24,1,39]/[24,2,39]/[25,1,39]/[25,1,40]/[26,1,40] in the east side yard. All are supported and read as storage clutter, not floating.

## Remaining risk

1. **OPEN sign standoff** — with `facing=east` the neon quad sits on the outer face of [31,4,37], one block in front of the wall skin. If a flush panel is preferred, use `facing=west` at the same cell (the model is double-sided, so the visible image is identical).
2. The removed boxes are gone; if the intent was to keep some of that clutter, only [12,4,7] and [25,5,2] could be kept with a support block under them (they are in walkable rooms), the other four sit in dead corners where support would look arbitrary.
3. Stale data that this deliverable cannot change: the Freddy sign block still carries the old `management_wanted:jeffs_pizza_sign` nbt id, and [29,3,35] is air carrying an old `management_wanted:open_neon_sign` block entity (the old sign was deleted by the conversion, its nbt was not). Same kind of leftover exists on many other converted blocks in this structure, so it is a global conversion issue, not sign-specific.
4. The Freddy billboard and the neon sign are block entities / BER / full-quad models, so only the blockstate could be verified here, not the final render.
