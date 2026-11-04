# fnaf2 - signs & cardboard boxes cleanup

Structure: `fnaf2.nbt` (31 x 48 x 40, playable volume y=0..6)

## Big entrance

- Door: `fnafmw:big_entrance_door` `part=0` (origin) at **[22, 1, 38]**, `facing=south`.
- Opening: 3 wide (x=21..23) x 3 tall (y=1..3) in the z=38 facade. Outside is +z (z=39), covered by the `fnafmw:cyan_shingle` roof eave at y=5..6.

## OPEN sign

| pos | before | after |
|---|---|---|
| [22, 4, 38] | `fnafmw:open_neon_sign` (embedded in the facade wall, replaced an old `light_facade_bricks` block) | `fnafmw:tan_facade_bricks` - wall block restored |
| [22, 4, 39] | air (one block outside the wall, under the eave) | `fnafmw:open_neon_sign`, `facing=south`, `alt=false`, `lit=true`, `powered=false` |

- This was the only `fnafmw:open_neon_sign` in the structure, so nothing else had to be removed.
- Height is `origin.y + 3 = 4`, centred on x=22, directly above the door; `facing` matches the door and points outward (south).
- The wall block stays behind the new sign, no cell of the facade is left open.

## Other signs (no changes needed)

- `fnafmw:restrooms_neon_sign` at [24, 3, 15] and [24, 3, 20], `facing=east`, `lit=true` - each is flush on the west face of the solid wall at x=25, not embedded. Correct.
- `fnafmw:hazard_sign` at [4, 2, 17] (south), [7, 4, 13] and [7, 4, 15] (east), [18, 2, 32] (west) - each has a solid wall on its mounting side. Correct.
- `fnafmw:exit_sign` at [22, 4, 37] (`rotation=0`, `wall=true`) - mounted on the inside face of the entrance wall. Correct.
- `fnafmw:out_of_order_sign` at [25, 1, 30] (`rotation=12`, `wall=true`) - mounted against the stage block at x=26. Correct.
- Note on facing: in this mod's models the flat plane sits on the cell edge in the `facing` direction, so `facing` points *at* the supporting wall (open_neon_sign `facing=south` renders at z=15.98; restrooms_neon_sign `facing=east` sits beside the wall at x=25). The signs were therefore left with the facing the converter produced; flipping them by the vanilla rule would detach them from their walls.

## Cardboard boxes

111 boxes audited (all `fnafmw:small/medium/large_cardboard_box`). 3 removed, 108 kept.

| pos | block | why removed |
|---|---|---|
| [14, 4, 36] | small | floated: air directly below at [14, 3, 36], 3 blocks above the floor (only a 1-tall large box at [14, 1, 36]); random clutter |
| [23, 1, 8] | large | inside the 2-wide parts/service opening (door block at [22, 1, 8]); conversion artifact from the old door leaf |
| [29, 1, 5] | large | inside the 2-wide employee/backstage opening (door block at [29, 1, 4]); conversion artifact from the old door leaf |

Kept boxes:

- All y=1 boxes stand on the y=0 floor (tiles, concrete, stone, stage) - verified per block.
- Stacked boxes (y=2/y=3) each have a box directly below, e.g. the 3-high piles at [5,1..3,27] and [12,1..3,24]; no impossible stack remains.
- Boxes at y=2 on party tables sit exactly on the tabletop (the table models span y=13..16 of the y=1 cell), so they are supported, not floating.
- No box overlaps a locker column (`locker`/`locker_part`), and none stands in a 1-wide passage except the two removed above.

## Remaining risk

- The OPEN sign plane sits on the outer edge of its cell, so it reads as mounted under the roof eave roughly one block proud of the facade. If that looks detached in game, use `facing=north` in the same cell [22, 4, 39]: the plane then lies flush on the wall's outer face (the texture is readable from both sides).
- The two removed boxes sat in door cells that the old NBT had as empty air (the doors are one-block BER blocks spanning 2 wide), so the cells are now open door space again - correct for the door, but the cells read as "hole" if the door geo does not cover them.
- If the runtime also applies the mod's `LegacyBlockRules.FLIPPED_FACING` list, the sign facing gets flipped twice and would face into the wall. Apply the per-structure facing map only, not both.
- Nothing was verified in game. Block ids/properties were validated against the new jar (`open_neon_sign`: alt/facing/lit/powered; boxes: facing + variant, large also open).
