# Signs & boxes — `discountdiner`

Input: `fnafmw-companion/.../structures/discountdiner.nbt` (already converted, size 29 x 10 x 30, 8700 blocks).
Nothing in the NBT, the addon source or shared files was modified; only the two deliverable files were written.

## Big entrance

| item | value |
|---|---|
| door block | `fnafmw:big_entrance_door`, `part=0` at **`(6, 1, 27)`** |
| facing | **south** (outward, toward +z; street side) |
| opening | 3 wide x 3 tall, `x=5..7`, `y=1..3`, two cells deep (`z=26` back layer, `z=27` front layer) |
| facade wall | 1 block thick at `z=27`; the air cell outside is `z=28` |
| sign slot | `origin + 1` outward, `origin.y + 3` = **`(6, 4, 28)`** |

## OPEN sign changes

The baked NBT had exactly one `fnafmw:open_neon_sign`, at **`(6,4,27)`** — i.e. *in* the
facade wall cell, so the sign block had replaced a `brown_facade_bricks` block. That is the
"embedded in the wall" case the task asks to remove.

| pos | action | block | properties |
|---|---|---|---|
| `(6,4,27)` | replace | `fnafmw:brown_facade_bricks` | `{}` — restores the wall block that stays behind the sign |
| `(6,4,28)` | add | `fnafmw:open_neon_sign` | `facing=north`, `alt=false`, `lit=true`, `powered=false` |

`facing=north` is deliberate and is what the updated `validate_signfix.py` expects
(`expected_facing = opposite(door facing)`). Reason, checked against the new jar:

* `assets/fnafmw/models/block/base_open_neon_sign.json` is a single **double-sided, zero-thickness
  panel** at `z = 15.98` (`box 0..16, 0..16, 15.98..15.98`), and
  `OpenNeonSignBlock.SHAPES = wallShapes(0, 0, 0.93625, 1, 1, 1.06125)` — the panel straddles the
  block edge **in the `facing` direction**.
* `blockstates/open_neon_sign.json`: `facing=south -> y=0` (panel on the south edge),
  `facing=north -> y=180` (panel on the north edge), 48 variants = `facing` x `lit` x `powered` x `alt`.
* Therefore a sign placed in the outside cell `(6,4,28)` with `facing=south` would hang its panel at
  `z≈29.0`, a full block in front of the facade (floating). With `facing=north` the panel lands at
  `z=28.0`, exactly on the wall's outer face, protruding only ~0.06 — a mounted sign. The texture
  (`open_neon_sign.png`, left = "OPEN", right = pre-mirrored) is readable from the street either way.
* `lit=true, powered=false, alt=false` all exist in the blockstate.

## Other signs audited (no changes)

| pos | block | state | verdict |
|---|---|---|---|
| `(6,4,26)` | `fnafmw:exit_sign` | `wall=true, rotation=0, black=false, centered=false` | correct: panel on the *inside* face of the front wall (`z=27` behind it), reads into the room |
| `(11,1,16)` | `fnafmw:out_of_order_sign` | `wall=false, rotation=8` | standing sign, correctly not wall-mounted |
| `(24,2,10)` | `fnafmw:hazard_sign` | `facing=west` | **not flush**: panel hangs ~0.94 blocks in front of the east log wall (see risk 1) |

There are no `pizza` / `restroom` / other neon signs in this structure. Nothing else was changed:
the hazard sign and the decorative decals are not neon/pizza/restroom signs and a correct fix for
them is a map-wide facing pass, not a single-block patch (see risk 1).

## Cardboard boxes audited (no changes)

26 boxes total: 12 `small_cardboard_box`, 14 `large_cardboard_box`, 0 medium.

Every box has a non-air support directly below, and every position matches the old NBT
(the clutter is original map content, not conversion junk):

* 24 boxes stand on `minecraft:oak_planks`
* `(14,2,19)` small box stands on the `fnafmw:party_table` at `(14,1,19)` (table block is solid)
* `(26,2,20)` large box stands on the `fnafmw:large_cardboard_box` at `(26,1,20)`. The large box
  model spans `y=0..16.1`, i.e. a full block, so the 2-high stack is physically sound; it exists
  in the old map as well.

No box is inside a locker, inside a door opening, blocking a passage, or stacked impossibly, so
nothing was removed, replaced or added. Box count was left at the original level.

## Verification

Applied the JSON to a copy of the baked NBT and ran the repo's `validate_signfix.py`:

```
open signs [((6, 4, 28), ('fnafmw:open_neon_sign', {'alt':'false','facing':'north','lit':'true','powered':'false'}))]
big entrances [((6, 1, 27), ('fnafmw:big_entrance_door', {'facing':'south','open':'false','part':'0'}))]
issues 0
```

## Remaining risk

1. **Every flat wall decal in this baked map is one cell off its wall — the facing is reversed.**
   Posters, children's drawings, `thin_wall_wires` and `hazard_sign` all have `facing` pointing
   *away* from the wall they hang on (e.g. `fredbears_advertisement_poster` at `(11,3,3)`,
   `facing=north`, wall at `z=4`; `hazard_sign` at `(24,2,10)`, `facing=west`, wall at `x=25`).
   Because these models are edge-pinned panels, their art therefore sits ~0.8–0.95 blocks in front
   of the wall. In `jeffspizza` the same blocks have `facing` pointing *into* the wall and are flush,
   so this map's decals are the reversed ones. Fixing it means inverting the facing of about 40
   blocks; that is outside this task's sign/box scope, so nothing there was changed, but it should
   be a single deliberate pass, not a per-structure patch (a later global pass would otherwise
   double-flip anything patched here).
2. The OPEN sign is a 1x1 panel of a 3-wide doorway, so it reads as a single-letter-wide "OPEN"
   plate rather than a word across the whole entrance; the mod has no wider variant.
3. The sign is placed at `y=4`, one block above the door frame brick course at `y=3`; if the runtime
   renders the big-entrance door with a frame that also occupies `y=4` outside the wall plane, the
   panel could visually clip the frame. Not verifiable from the NBT (the door parts use an
   entity-rendered model).
