import os, random
import nbtlib
from nbtlib.tag import Byte, Compound, Int, String

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
path = os.path.join(ROOT, "src", "main", "resources", "data", "fnafmwpizzeria", "structure", "worldgen_extras", "franchiseeoffice.nbt")
f = nbtlib.load(path)
rng = random.Random(20261104)
POOL = [
    ("fnafmw:fazcoin", 20, 2, 6),
    ("fnafmw:fazbuck", 8, 1, 2),
    ("minecraft:cobweb", 15, 1, 3),
    ("minecraft:string", 15, 1, 4),
    ("minecraft:paper", 12, 1, 4),
    ("minecraft:iron_nugget", 10, 2, 8),
    ("minecraft:gold_nugget", 8, 1, 4),
    ("minecraft:emerald", 6, 1, 2),
    ("minecraft:diamond", 2, 1, 1),
    ("minecraft:name_tag", 2, 1, 1),
    ("fnafmw:freddy_head", 1, 1, 1),
    ("fnafmw:bonnie_mask", 1, 1, 1),
]

def pick():
    total = sum(w for _, w, _, _ in POOL)
    n = rng.randrange(total)
    for item, weight, lo, hi in POOL:
        if n < weight:
            return item, rng.randint(lo, hi)
        n -= weight
    return POOL[0][0], 1

def items():
    out = []
    for slot in rng.sample(range(27), rng.randint(3, 6)):
        item, count = pick()
        out.append(Compound({"Slot": Byte(slot), "id": String(item), "count": Int(count)}))
    return nbtlib.List[Compound](out)

for b in f["blocks"]:
    n = str(f["palette"][int(b["state"])]["Name"])
    if n == "fnafmw:locker":
        pos = [int(v) for v in b["pos"]]
        b["nbt"] = Compound({
            "id": String("fnafmw:locker"),
            "x": Int(pos[0]),
            "y": Int(pos[1]),
            "z": Int(pos[2]),
            "Items": items(),
        })
f.save(path, gzipped=True)
print("booth lockers looted")
