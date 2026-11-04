import os
import nbtlib

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "src", "main", "resources", "data", "fnafmwpizzeria")
FILES = []
for name in ["discountdiner", "fredbears", "fnaf1", "fnaf2", "jeffspizza"]:
    FILES.append(os.path.join(BASE, "structure", "pizzerias", name + ".nbt"))
for name in ["franchiseeoffice", "pinwheel_largetent", "pinwheel_carnival"]:
    FILES.append(os.path.join(BASE, "structure", "worldgen_extras", name + ".nbt"))

legacy_prefixes = ("management_wanted:", "management_wanted_2:", "management_wanted_fixes:")
removed = 0
for path in FILES:
    if not os.path.exists(path):
        continue
    f = nbtlib.load(path)
    changed = False
    for b in f["blocks"]:
        nbt = b.get("nbt")
        if nbt is None:
            continue
        items = nbt.get("Items")
        if items is None:
            continue
        kept = [it for it in items if not str(it.get("id", "")).startswith(legacy_prefixes)]
        removed += len(items) - len(kept)
        if len(kept) != len(items):
            nbt["Items"] = nbtlib.List[type(items[0])](kept) if kept else nbtlib.List[type(items[0])]([])
            changed = True
    if changed:
        f.save(path, gzipped=True)
        print("cleaned", os.path.basename(os.path.dirname(path)) + "/" + os.path.basename(path))
print("removed legacy items", removed)
