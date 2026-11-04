import os, random, shutil, zlib
import nbtlib
from nbtlib.tag import Byte, Compound, Int, String

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "src", "main", "resources", "data", "fnafmwpizzeria")
BACKUP = os.path.join(ROOT, "provenance", "loot_before")
os.makedirs(BACKUP, exist_ok=True)

FILES = []
for name in ["discountdiner", "fredbears", "fnaf1", "fnaf2", "jeffspizza"]:
    FILES.append(os.path.join(BASE, "structure", "pizzerias", name + ".nbt"))
for name in ["franchiseeoffice", "pinwheel_largetent", "pinwheel_carnival"]:
    FILES.append(os.path.join(BASE, "structure", "worldgen_extras", name + ".nbt"))

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

def pick(rng):
    total = sum(w for _, w, _, _ in POOL)
    n = rng.randrange(total)
    for item, weight, lo, hi in POOL:
        if n < weight:
            return item, rng.randint(lo, hi)
        n -= weight
    return POOL[0][0], 1

def make_items(rng):
    entries = []
    slots = rng.sample(range(27), rng.randint(3, 6))
    for slot in slots:
        item, count = pick(rng)
        entries.append(Compound({
            "Slot": Byte(slot),
            "id": String(item),
            "count": Int(count),
        }))
    return nbtlib.List[Compound](entries)

for path in FILES:
    if not os.path.exists(path):
        continue
    shutil.copy2(path, os.path.join(BACKUP, os.path.basename(path)))
    f = nbtlib.load(path)
    rng = random.Random(zlib.crc32((os.path.basename(path) + ":loot").encode("utf-8")))
    lockers = 0
    containers = 0
    for b in f["blocks"]:
        nbt = b.get("nbt")
        if nbt is None:
            continue
        beid = str(nbt.get("id"))
        if beid == "fnafmw:locker":
            nbt["Items"] = make_items(rng)
            nbt.pop("LootTable", None)
            nbt.pop("LootTableSeed", None)
            lockers += 1
        elif beid in ("minecraft:chest", "minecraft:barrel"):
            nbt["LootTable"] = String("fnafmwpizzeria:chests/loot")
            nbt.pop("Items", None)
            nbt.pop("LootTableSeed", None)
            containers += 1
    f.save(path, gzipped=True)
    print(os.path.basename(path), "lockers", lockers, "loot-chests", containers)
