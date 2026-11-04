import os
import nbtlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "src", "main", "resources", "data", "fnafmwpizzeria")
FILES = []
for name in ["discountdiner", "fredbears", "fnaf1", "fnaf2", "jeffspizza"]:
    FILES.append(os.path.join(BASE, "structure", "pizzerias", name + ".nbt"))
for name in ["franchiseeoffice", "pinwheel_largetent", "pinwheel_carnival"]:
    FILES.append(os.path.join(BASE, "structure", "worldgen_extras", name + ".nbt"))

removed = 0
for path in FILES:
    if not os.path.exists(path):
        continue
    f = nbtlib.load(path)
    pal = f["palette"]
    changed = False
    for b in f["blocks"]:
        if b.get("nbt") is not None and str(pal[int(b["state"])]["Name"]) == "minecraft:air":
            del b["nbt"]
            removed += 1
            changed = True
    if changed:
        f.save(path, gzipped=True)
        print("cleaned air BEs", os.path.basename(os.path.dirname(path)) + "/" + os.path.basename(path))
print("removed", removed)
