import sys, zipfile, io, gzip, json, collections
import nbtlib

if len(sys.argv) != 2:
    raise SystemExit("usage: python validate_jar.py <fnafmwpizzeria.jar>")
jar = sys.argv[1]
z = zipfile.ZipFile(jar)
VALID_BE = {
    "fnafmw:basic_light_warm_block_entity", "fnafmw:basic_light_cold_block_entity",
    "fnafmw:deluxe_light_warm_block_entity", "fnafmw:deluxe_light_cold_block_entity",
    "fnafmw:recessed_light_block_entity", "fnafmw:elevator_light_block_entity",
    "fnafmw:utility_light_block_entity", "fnafmw:hanging_utility_light_block_entity",
    "fnafmw:flood_lamps_block_entity", "fnafmw:fright_light_block_entity",
    "fnafmw:stage_bulb_block_entity", "fnafmw:sconce_block_entity",
    "fnafmw:spotlight_small_block_entity", "fnafmw:hanging_lamp_block_entity",
    "fnafmw:hanging_chain_lamp_block_entity", "fnafmw:light_bulb_block_entity",
    "fnafmw:chain_light_bulb_block_entity", "fnafmw:stagelight_block_entity",
    "fnafmw:deluxe_stagelight_block_entity", "fnafmw:light_switch_block_entity",
    "fnafmw:light_button_block_entity", "fnafmw:big_door_block_entity",
    "fnafmw:big_door_part_block_entity", "fnafmw:carousel_block_entity",
    "fnafmw:terminal", "fnafmw:balloon_bundle", "fnafmw:locker", "fnafmw:dumpster",
    "fnafmw:fright_box", "fnafmw:freddy_fazbear_pizza_sign",
    "fnafmw:new_freddy_fazbear_pizza_sign", "fnafmw:bonnie_stand_block_entity",
    "fnafmw:freddy_stand_block_entity", "fnafmw:welcome_mat_block_entity",
    "fnafmw:emissive_glow_block_entity", "fnafmw:arcade_block_entity",
}
bad = []
legacy_items = []
air_bes = 0
entities = 0
for n in z.namelist():
    if not (n.startswith("data/fnafmwpizzeria/structure/") and n.endswith(".nbt")):
        continue
    f = nbtlib.File.parse(io.BytesIO(gzip.decompress(z.read(n))))
    pal = f["palette"]
    entities += len(f.get("entities", []))
    for b in f["blocks"]:
        bn = str(pal[int(b["state"])]["Name"])
        if bn.startswith("management_wanted:") or bn == "fnafmw:legacy_placeholder":
            bad.append((n, bn))
        nbt = b.get("nbt")
        if nbt is None:
            continue
        if bn == "minecraft:air":
            air_bes += 1
        beid = str(nbt.get("id"))
        if not beid.startswith("minecraft:") and beid not in VALID_BE:
            bad.append((n, bn, beid))
        items = nbt.get("Items")
        if items:
            for it in items:
                iid = str(it.get("id"))
                if iid.startswith("management_wanted:"):
                    legacy_items.append((n, iid))
print("bad", bad[:10], "count", len(bad))
print("legacy items", legacy_items[:10], "count", len(legacy_items))
print("air block entities", air_bes)
print("entities", entities)
print("worldgen JSONs", [n for n in z.namelist() if "/worldgen/" in n])
