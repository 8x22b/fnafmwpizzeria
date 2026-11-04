# fnaf1 – signs & cardboard boxes report

## Big entrance

| item | value |
|---|---|
| `fnafmw:big_entrance_door` part=0 (origin) | **[12, 1, 32]** |
| door facing | **south** (outward) |
| opening | x=11..13, y=1..3 (3 wide × 3 tall) |
| facade wall plane | z=32 (interior is z<32, outside is z=33) |
| outside cell above the door | [12, 4, 33] – occupied by the cornice |

The door's three side-columns extend north to z=31; the walkway through the opening (x=11..13) is clear.

## OPEN sign

### What was wrong

The baked NBT had exactly **one** `fnafmw:open_neon_sign`, at **[12, 4, 32]** — the facade wall cell itself. It was embedded in the wall (a wall block had been overwritten by the sign), and the `exit_sign` at [12, 4, 31] was mounted on the sign's back instead of on a wall. Facing was `south`.

### What was done

| pos | action | block | properties |
|---|---|---|---|
| [12, 4, 32] | **replace** (removes the old sign) | `fnafmw:tan_facade_bricks` | — (no properties; matches neighbours [11,4,32], [13,4,32], [12,5,32]) |
| [12, 5, 33] | **add** | `fnafmw:open_neon_sign` | `facing=north, alt=false, lit=true, powered=false` |

Result: one OPEN sign, one block outside the entrance wall (z=33), centered over the door (x=12), with the wall block left in place behind it at z=32. The old sign block is gone and the facade is continuous again.

### Why y=5 instead of y=4

`origin.y + 3` = y=4. The outboard y=4 cell **[12, 4, 33]** is not free: it is `fnafmw:caramel_facade_wall_slab` (`type=top`) — the projecting cornice/awning over the entrance (x=10..14, z=33, y=4). Replacing it would carve a 1-block notch out of the awning and leave the sign recessed at the back of the notch. The nearest valid wall cell above is **[12, 5, 33]**: air, with `fnafmw:tan_facade_bricks` directly behind at [12, 5, 32]. The sign is still centered over the entrance, one row higher than the ideal.

If the reviewer prefers the literal y=4, change the addition to `[12,4,33]` as a *replace* of the cornice slab; everything else stays valid.

### Why `facing=north` (not `south`)

The mod's sign model is authored with the board on the side named by `facing`, and the readable artwork on the opposite face:

* `OpenNeonSignBlock` shape = `wallShapes(0, 0, 0.93625, 1, 1, 1.06125)`, mapped to `Direction.SOUTH` → the board occupies the **south** edge of the cell for `facing=south`. The model plane is at `z = 15.98`, also the south edge.
* `base_open_neon_sign` texture: the **north** face uses the unmirrored `OPEN` half (UV `[0,0,8,8]`), the **south** face uses the mirrored half (UV `[8,0,16,8]`). So at `y=0` (`facing=south`) the sign reads correctly from the **north**.

Therefore, to mount the sign on a wall to its **north** (facade at z=32) and have it read **outward to the south**, the value must be `facing=north` (blockstate `y=180`). This is the same convention the `fnaf2` / `fredbears` sign-fix structures use (sign one block outside, `facing` = toward the wall). `facing=south` here would put the mirrored back to the street and float the board one block off the wall.

## Other signs (audited, no change)

| pos | block | facing / props | supporting wall | verdict |
|---|---|---|---|---|
| [12, 2, 12] | `fnafmw:hazard_sign` | `facing=east` | x=13 `classic_pizzeria_wall_red_trim` | OK – board on the facing side, reads west into the room |
| [23, 2, 2] | `fnafmw:hazard_sign` | `facing=north` | z=1 `tan_facade_bricks_trim` | OK |
| [30, 2, 31] | `fnafmw:hazard_sign` | `facing=south` | z=32 `tan_facade_bricks_trim` | OK (stands on the small box at [30,1,31], but is wall-mounted, not floating) |
| [12, 4, 31] | `fnafmw:exit_sign` | `rotation=0, wall=true` | z=32 (now the restored wall) | OK – south-wall mount, reads north |
| [19, 1, 27] | `fnafmw:out_of_order_sign` | `wall=false, rotation=0` | floor `black_white_tiles` | OK – free-standing sign |
| [25..27, 2..3, 33] | `minecraft:oak_wall_sign` ×6 | `facing=south` | z=32 `fnafmw:window` | left as-is – original storefront boards, 1 block outside the window plane, blank text |

The `facing_map` N↔S / E↔W flips already applied during conversion kept the hazard-sign boards on the correct wall face; they only needed auditing, not fixing. There are no pizza or restroom signs in this structure.

## Cardboard boxes

136 boxes audited (81 small, 55 large, 0 medium). Every box was checked for a block below it; stacked boxes are supported by the box under them.

### Removed (floating)

| pos | block | problem |
|---|---|---|
| **[8, 3, 29]** | small | nothing below (`air`); it floated in the old NBT too |
| **[8, 4, 28]** | small | nothing below; its old support `metal_shelf_small` at [8,3,28] was dropped in the conversion |
| **[34, 4, 26]** | small | nothing below; 1-block gap above the locker top at [34,2,26] |

All three were isolated random clutter in backstage rooms, so removal was preferred over inventing a support block.

### Kept after audit

* **136 − 3 = 133 boxes remain.** All now have solid support below (tiles, smooth stone, stage, party table, or another full-size box).
* Stacks are 1–3 high and always small/large on **large** (the large model is a full 16×16 block). No large box rests on a 6 px small box, and no box floats on a gap.
* [10,1,31] and [14,1,31] flank the entrance side columns but are outside the x=11..13 walkway — not blocking passage.
* No box occupies a door cell, locker cell, or blocks a doorway.
* [17,2,31] and [21,2,31] stand on the show stage and [16,2,23] on a party table. They are supported and were placed deliberately in the old map, so they were left as decor rather than deleted.

## Remaining risk

1. **Sign height.** The OPEN sign is at y=5 instead of the nominal y=4 because the y=4 outside cell is the awning slab. If a runtime hard-checks `origin.y + 3`, the fallback is the cornice replace described above.
2. **`facing` convention.** I used the model/shape/texture evidence (`facing` = wall side). If a tool instead interprets `facing` as the outward direction, the value would need to be `south`. The sign would then render mirrored and 1 block off the wall under the real block model.
3. **Oak storefront signs.** Their support is `fnafmw:window` at z=32, not a wall block. They are standard vanilla wall signs; if the window block is not considered solid they could pop on a block update. They are original to the old map and blank, so no change was made.
4. **Box variants.** The baked palette has no `variant`/`open` values for the cardboard boxes, so all boxes fall back to the default model (`variant=1`, `open=false`) and look uniform. That was outside this task's scope but explains any remaining "sameness".
5. **No 3D render was produced** for this pass; placements were verified against block coordinates, the jar's blockstates/models/shapes, and the old NBT.
