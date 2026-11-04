# fredbears — per-structure conversion report

## Structure

- Old NBT: `fnafmwpizzeria/structures/fredbears.nbt`
- Size: **29 x 10 x 26** (x east, y up, z south)
- Single storey: floor at y=0, interior y=1..5, ceiling at y=6. The east office/backstage strip (x=19..23) has its own lower ceiling at y=5.
- Main room is roughly x=1..22, z=1..18, with a south extension x=8..23, z=19..24. The 3-wide entrance is in the south facade (z=24), the stage/curtain is on the north wall (z=1).

## Doors and entrances

All four custom small doors are one block in the NBT but **2 wide and 3 tall visually** (old collision/geo span `y-16..y+32` from the block at y=2, i.e. world y=1..3, standing on the carpet at y=0). The new `parts_service_door` and `employee_door` are normal 2-tall doors, so each leaf is placed at **y=1..2** and the old third row (y=3) is closed with a matching `fnafmw:diner_wall` header. That keeps the doors on the floor and leaves no hole above them.

| # | kind | block | facing | opening | leaves (bottom half) | hinge |
|---|------|-------|--------|---------|----------------------|-------|
| 1 | double | `fnafmw:parts_service_door` | south | x=1..2, z=10, y=1..2 (+y=3 header) | (1,1,10), (2,1,10) | left / right |
| 2 | double | `fnafmw:parts_service_door` | west | x=18, z=1..2, y=1..2 (+y=3 header) | (18,1,1), (18,1,2) | left / right |
| 3 | double | `fnafmw:parts_service_door` | west | x=18, z=15..16, y=1..2 (+y=3 header) | (18,1,15), (18,1,16) | left / right |
| 4 | double | `fnafmw:employee_door` (backstage) | west | x=18, z=9..10, y=1..2 (+y=3 header) | (18,1,9), (18,1,10) | left / right |
| 5 | big_entrance | `fnafmw:big_entrance_door` | south | x=11..13, z=23..24, y=1..3 | origin (12,1,24) | — |

- Exactly one door per real opening; no duplicate door blocks.
- Every small door leaf has both `half=lower` and `half=upper` states; all 16 door blocks (4 openings x 2 leaves x 2 halves) are present.
- `open=false` everywhere.
- Hinges are on the outer edges of each pair: `hinge=left` on the leaf on the left when looking at the door from its facing side, `hinge=right` on the other leaf.
- Big entrance: all 15 parts are written explicitly (part 0..14) so the door is complete even if the runtime does not expand it. The old block at (12,2,24) is part **4** (centre middle), not the origin; the new origin is (12,1,24), one block lower, because the old 3-tall model starts at y=1. The 15 parts exactly fill x=11..13, y=1..3, z=23..24, all of which were air in the old NBT except the old block.
- Note: the literal task wording says "use the old block position as the origin". The old block is at y=2; using that as the origin would place the new 3-tall door at y=2..4, one block above the floor and overlapping the y=4 facade. The JSON uses y=1 and documents the one-block offset.

## Changes relative to the global map

**Furniture / rotations**
- `backstage_shelf` → `fnafmw:locker`: facing is flipped (old facing = visible/door side, new locker model's door is on the opposite side). All 8 lockers now have their doors facing into the room and their backs against the wall.
- `desk_horizontal_middle` → `fnafmw:classic_desk`: facing is flipped 180°. The new desk model has its user side opposite the old desk; y=180 puts the modesty panel against the z=12 wall and the user side south, matching the old desk and the existing office-chair entity.
- `monitor` / `retro_monitor` → `fnafmw:classic_monitor` / `fnafmw:retro_monitor`: facing is flipped 180°. The new CRT screen face is on the opposite side of the model from the old one. `monitors=1`, `alt=1` (`top_alt=1` for retro).
- `fnaf_1_chair` → `fnafmw:classic_chair`: model is byte-identical to the old chair, facing unchanged.
- `stool` → `fnafmw:classic_chair`: set `facing=south` (seats face away from the counter wall at z=13).
- Wood party tables: `modular_type` set per old part name (2/10/8 horizontal, 3/7/6 bottom-left, 9/13/12 bottom-right, etc.). Geometry matches the old parts, no rotation.
- `backstage_table_*` → `fnafmw:party_table`: old rows face east (run along z); the new modular table has no facing, so `rotation_y: 90` is applied. Right end at low z, left end at high z, as in the old build.
- `desk_horizontal_left/right`, `metal_shelf*`, `storage_shelf` → air (global map) — the new desk/shelves replace them.

**Overlooked-in-global-map props**
- `window_*` → `fnafmw:window`: `layout=0` (standalone fully framed pane), old `facing` dropped. All 18 old window parts are on north/south-facing walls, so no model rotation is needed. Result is a 3x2 grid of framed panes per old window.
- `fredbear_wall_tile` → `fnafmw:diner_wall_trim`: `face=floor` selects the texture with the blue band on top, which matches the old `fredbear_wall_tile_trim`; `face=wall` uses the vertically flipped `trim2` texture.
- `normal_light_on` → `fnafmw:basic_light_warm`: all six direction flags true (full glowing cube, like the old light block), `lit=true`, `powered=false`. `powered=true` would be cleared by `BaseModularLightBlock.neighborChanged` on the next update without redstone.
- `hanging_lamp` / `light_bulb`: `face=ceiling`, `facing=north`, `lit=true`, `powered=false` (both hang from the y=5 office ceiling).
- `open_neon_sign`: facing flipped north→south so the flat sign sits on the south wall (block z=23, plane at z≈24) instead of floating one block into the room; `lit=true`, `powered=false`, `alt=false`.
- `nebraska_poster`: facing flipped east→west so the poster plane is flush with the east wall at x=25; old `face=wall` dropped.
- `out_of_order_sign`: `wall=false`, `rotation=0` (standing sign), old facing dropped.
- `cardboard_box` → `fnafmw:large_cardboard_box`: `open=false`, `variant=1`, old `blockstate` dropped.
- `small_cardboard_box`: `variant=1`.
- `purple_curtain_pane`: old connection booleans kept, `waterlogged` dropped; new multipart recomputes the pane shapes.
- `puppet_box`, `trashcan`: stale old properties dropped (`trashcan` → cauldron has no facing).
- `fredbear_carpet` / `ruined_white_black_tiles`: stale `facing` / `blockstate` dropped.

**Removals / decluttering**
- Wall wires (`wall_wires`, `wall_wires_thin`, `wall_wires_curved_*`, `hanging_wires_alt`, `hanging_wall_wires_thin`), `paper`, `trash`: removed. The new wall decorations sit on the *facing* face of their block, while the old ones were placed one block away from the wall (back face); converted as-is they would float or be mirrored. Removing them is cleaner than wrong placement.
- `fan` and `laptop`: removed. They sat at y=2 on the old 1-tall desk; the new `classic_desk` is 2 blocks tall with a built-in hutch/monitor cluster, and these props would clip it.
- `small_cardboard_box` at (22,1,13): removed — inside the new 3-wide desk.
- `monitor` at (23,3,15): removed — it sat on an open shelf; the new locker is a closed 2-tall body and the monitor would be inside it.
- Furniture counts after conversion: 1 desk, 8 lockers, 31 party tables, 7 chairs, 2 classic monitors, 2 retro monitors, 18 windows, 1 puppet box, 5 cauldrons (trash cans), 1 poster, 1 neon sign, 1 out-of-order sign.

**Ceiling light toggles**
- Old `minecraft:daylight_detector` blocks were light toggles: 10 sat in the y=6 ceiling layer and 32 sat at y=7 above the ceiling. The y=6 ones are replaced with `fnafmw:ceiling_tile_gray` (so the ceiling has no holes); the y=7 ones are removed (they were invisible bumps above the ceiling).

## Rotations corrected (summary)

| Block | Correction |
|-------|-----------|
| backstage tables | `rotation_y: 90` (row along z, right end north) |
| classic_desk | facing flipped 180° |
| locker (backstage_shelf) | facing flipped 180° |
| classic/retro monitors | facing flipped 180° |
| wall signs / poster | facing flipped to the wall side |
| windows | `layout=0`, no facing |
| doors | explicit facing + hinge per leaf; old door block is an upper half (or big-door part 4) |

## Validation

A Python validator applied the JSON to the old NBT and checked every final block state against the new jar's blockstate JSON:

- 2400 final non-air blocks.
- **0 invalid block IDs, 0 invalid property names/values, 0 invalid full states.**
- 16 small-door blocks (4 openings x 2 leaves x 2 halves), all with matching facing/hinge and correct halves.
- 15 big-entrance parts, exactly one `part=0` origin.
- No `management_wanted:*` or `fnafmw:legacy_placeholder` remains.
- Old-position guards on all `position_fixes` match the NBT (including air guards).
- The 15 big-door parts all land inside the old opening (x=11..13, y=1..3, z=23..24); every cell was air except the old door block.

## Preview

Rendered `fredbears-preview.png` in this folder: a 2D top-down plan at y=2 (left) and the south elevation at z=24 (right), with door leaves in red, the big entrance outlined in orange and windows in blue. It is a **2D map render, not a 3D model render** — I did not fake a 3D preview.

## Remaining visual risks

1. **Hinge convention**: left/right were assigned assuming "left = the leaf's left as seen from the front". If the runtime uses the opposite convention, the pairs would open toward each other; swap `hinge` on both leaves if that is seen in game.
2. **Big entrance origin**: intentionally at y=1, one block below the old block position, to sit on the floor. If a consumer insists on using the old block y=2 as the origin without adjustment, the door will be one block high and overlap the y=4 facade.
3. **180° flips for desk/locker/monitors** are inferred from model geometry (user side, locker door, CRT screen face) and are the main judgement calls. They are correct for this snapshot's models; re-check if the assets change.
4. **Big entrance / big custom door models are empty** in this jar snapshot (`big_entrance_door.json` has no elements, the template is empty), as are the `big_*` single-block doors. If those blocks render through a block entity in a later build they are fine; in this snapshot they may be invisible. That is a mod asset issue, not a mapping choice.
5. **New desk is 2 blocks tall** and 3 wide, so the office silhouette is taller than the old 1-tall desk. Its top reaches y=2 and its hutch reaches about y=3; the removed fan/laptop/box are the only old props that would have clipped it.
6. **Windows are individual framed panes** (`layout=0`) rather than a seamless 3x2 sheet. This is the safe standalone layout; using connection layouts would need the mod's layout lookup table, which is not exposed in the blockstate JSON.
7. **Wall wires/papers were dropped**; the room loses some grime/detail.
8. **Light emission**: the new light blocks are set `lit=true, powered=false` to stay on, but actual dynamic light may still depend on the mod's light-link system. If the room is dark, the lights may need linking in game.
9. Old entities (`wetfloor`, `office_chair`) are outside this JSON's scope and are handled by the runtime's entity ID mapping.
10. **Puppet box**: the new `fnafmw:puppet_box` is a block-entity/geo model with no `facing` property, so its old west-facing orientation cannot be encoded. It keeps the default model orientation at (22,1,20); if it faces the wall in game, replace it with a plain box or remove it.
11. **Door height**: the new standard doors are 2 tall while the old custom doors were 3 tall. The third row is closed with `diner_wall`, so the doorways are now 2 tall with a solid header. If the intent is to keep the full 3-tall opening, a 3-tall custom door model is needed (the `big_*` door models in this snapshot have no facing/rotation).
