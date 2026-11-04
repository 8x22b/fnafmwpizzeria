import os, shutil
import nbtlib
from nbtlib.tag import Compound, String, Int

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
path = os.path.join(ROOT, "src", "main", "resources", "data", "fnafmwpizzeria", "structure", "worldgen_extras", "pinwheel_largetent.nbt")
backup = os.path.join(ROOT, "provenance", "pinwheel_largetent_before_fixes.nbt")
os.makedirs(os.path.dirname(backup), exist_ok=True)
shutil.copy2(path, backup)

f = nbtlib.load(path)
palette = f["palette"]
blocks = list(f["blocks"])
pos_to_block = {tuple(int(v) for v in b["pos"]): b for b in blocks}

cache = {}
for i, p in enumerate(palette):
    props = {str(k): str(v) for k, v in dict(p.get("Properties", {})).items()}
    cache[(str(p["Name"]), tuple(sorted(props.items())))] = i

def state_id(name, props):
    key = (name, tuple(sorted((k, str(v)) for k, v in props.items())))
    if key in cache:
        return cache[key]
    tag = Compound({"Name": String(name)})
    if props:
        tag["Properties"] = Compound({k: String(str(v)) for k, v in props.items()})
    idx = len(palette)
    palette.append(tag)
    cache[key] = idx
    return idx

def set_props(pos, **props):
    b = pos_to_block[tuple(pos)]
    st = palette[int(b["state"])]
    name = str(st["Name"])
    current = {str(k): str(v) for k, v in dict(st.get("Properties", {})).items()}
    current.update({k: str(v) for k, v in props.items()})
    b["state"] = Int(state_id(name, current))

def remove(pos):
    key = tuple(pos)
    b = pos_to_block.pop(key, None)
    if b is not None:
        blocks.remove(b)

def put(pos, name, props):
    key = tuple(int(v) for v in pos)
    idx = state_id(name, props)
    b = pos_to_block.get(key)
    if b is None:
        b = Compound({"pos": nbtlib.List([Int(v) for v in key]), "state": Int(idx)})
        blocks.append(b)
        pos_to_block[key] = b
    else:
        b["state"] = Int(idx)
    b.pop("nbt", None)

# Lockers: the model faces the side opposite to `facing`; put the back to the wall.
set_props((12, 1, 14), facing="west")
set_props((15, 1, 22), facing="south")

# Move the floating poster from the roof/under the stair to the quartz wall inside.
remove((16, 4, 11))
put((15, 3, 11), "fnafmw:lets_party_poster", {"facing": "east", "variant": "0"})

# Prizes sign faces east; give it a solid support block on the west side.
put((17, 1, 1), "minecraft:stripped_oak_wood", {"axis": "y"})

f["palette"] = nbtlib.List(palette)
f["blocks"] = nbtlib.List(blocks)
f.save(path, gzipped=True)
print("tent fixed; blocks", len(blocks), "palette", len(palette))
