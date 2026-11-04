import os, shutil, collections
import nbtlib
from nbtlib.tag import Compound, String, Int

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
BASE = os.path.join(ROOT, "src", "main", "resources", "data", "fnafmwpizzeria")
BACKUP = os.path.join(ROOT, "provenance", "be_backup")
os.makedirs(BACKUP, exist_ok=True)

FILES = []
for name in ["discountdiner", "fredbears", "fnaf1", "fnaf2", "jeffspizza"]:
    FILES.append(os.path.join(BASE, "structure", "pizzerias", name + ".nbt"))
for name in ["franchiseeoffice", "pinwheel_largetent", "pinwheel_carnival"]:
    FILES.append(os.path.join(BASE, "structure", "worldgen_extras", name + ".nbt"))

BLOCK_BE = {
    "fnafmw:basic_light_warm": "fnafmw:basic_light_warm_block_entity",
    "fnafmw:basic_light_cold": "fnafmw:basic_light_cold_block_entity",
    "fnafmw:deluxe_light_warm": "fnafmw:deluxe_light_warm_block_entity",
    "fnafmw:deluxe_light_cold": "fnafmw:deluxe_light_cold_block_entity",
    "fnafmw:recessed_light": "fnafmw:recessed_light_block_entity",
    "fnafmw:elevator_light": "fnafmw:elevator_light_block_entity",
    "fnafmw:utility_light": "fnafmw:utility_light_block_entity",
    "fnafmw:hanging_utility_light": "fnafmw:hanging_utility_light_block_entity",
    "fnafmw:flood_lamps": "fnafmw:flood_lamps_block_entity",
    "fnafmw:fright_light": "fnafmw:fright_light_block_entity",
    "fnafmw:stage_bulb": "fnafmw:stage_bulb_block_entity",
    "fnafmw:sconce": "fnafmw:sconce_block_entity",
    "fnafmw:spotlight_small": "fnafmw:spotlight_small_block_entity",
    "fnafmw:hanging_lamp": "fnafmw:hanging_lamp_block_entity",
    "fnafmw:hanging_chain_lamp": "fnafmw:hanging_chain_lamp_block_entity",
    "fnafmw:light_bulb": "fnafmw:light_bulb_block_entity",
    "fnafmw:chain_light_bulb": "fnafmw:chain_light_bulb_block_entity",
    "fnafmw:stagelight": "fnafmw:stagelight_block_entity",
    "fnafmw:deluxe_stagelight": "fnafmw:deluxe_stagelight_block_entity",
    "fnafmw:light_switch": "fnafmw:light_switch_block_entity",
    "fnafmw:light_button": "fnafmw:light_button_block_entity",
    "fnafmw:big_employee_door": "fnafmw:big_door_block_entity",
    "fnafmw:big_parts_service_door": "fnafmw:big_door_block_entity",
    "fnafmw:big_bedroom_door": "fnafmw:big_door_block_entity",
    "fnafmw:big_employee_door_part": "fnafmw:big_door_part_block_entity",
    "fnafmw:big_parts_service_door_part": "fnafmw:big_door_part_block_entity",
    "fnafmw:big_bedroom_door_part": "fnafmw:big_door_part_block_entity",
    "fnafmw:carousel": "fnafmw:carousel_block_entity",
    "fnafmw:terminal": "fnafmw:terminal",
    "fnafmw:balloon_bundle": "fnafmw:balloon_bundle",
    "fnafmw:locker": "fnafmw:locker",
    "fnafmw:dumpster": "fnafmw:dumpster",
    "fnafmw:fright_box": "fnafmw:fright_box",
    "fnafmw:freddy_fazbear_pizza_sign": "fnafmw:freddy_fazbear_pizza_sign",
    "fnafmw:new_freddy_fazbear_pizza_sign": "fnafmw:new_freddy_fazbear_pizza_sign",
    "fnafmw:bonnie_stand": "fnafmw:bonnie_stand_block_entity",
    "fnafmw:freddy_stand": "fnafmw:freddy_stand_block_entity",
    "fnafmw:welcome_mat": "fnafmw:welcome_mat_block_entity",
    "fnafmw:brown_welcome_mat": "fnafmw:welcome_mat_block_entity",
    "fnafmw:open_neon_sign": "fnafmw:emissive_glow_block_entity",
    "fnafmw:restrooms_neon_sign": "fnafmw:emissive_glow_block_entity",
    "fnafmw:prizes_neon_sign": "fnafmw:emissive_glow_block_entity",
    "fnafmw:prizes_neon_yellow_sign": "fnafmw:emissive_glow_block_entity",
    "fnafmw:arcade_neon_sign": "fnafmw:emissive_glow_block_entity",
    "fnafmw:jrs_neon_sign": "fnafmw:emissive_glow_block_entity",
    "fnafmw:blank_arcade_cabinet_style_1": "fnafmw:arcade_block_entity",
    "fnafmw:blank_arcade_cabinet_style_2": "fnafmw:arcade_block_entity",
    "fnafmw:blank_arcade_cabinet_style_3": "fnafmw:arcade_block_entity",
    "fnafmw:freddy_arcade_cabinet": "fnafmw:arcade_block_entity",
    "fnafmw:bonnie_arcade_cabinet": "fnafmw:arcade_block_entity",
    "fnafmw:chica_arcade_cabinet": "fnafmw:arcade_block_entity",
    "fnafmw:foxy_arcade_cabinet": "fnafmw:arcade_block_entity",
    "fnafmw:candy_fishing_arcade_cabinet": "fnafmw:arcade_block_entity",
    "fnafmw:popgoes_adventure_arcade_cabinet": "fnafmw:arcade_block_entity",
    "fnafmw:cola_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:fiztime_popsoda_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:grape_fizzy_faz_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:moon_drop_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:orange_fizzy_faz_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:pink_lemonade_fizzy_faz_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:popsi_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:sodaroni_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:sour_lime_fizzy_faz_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:sour_punch_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:lemon_lime_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:sun_drop_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:orange_soda_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:water_vending_machine": "fnafmw:arcade_block_entity",
    "fnafmw:exotic_beverage_vending_machine": "fnafmw:arcade_block_entity",
}

ALWAYS = {
    "fnafmw:terminal",
    "fnafmw:open_neon_sign",
    "fnafmw:restrooms_neon_sign",
    "fnafmw:prizes_neon_sign",
    "fnafmw:prizes_neon_yellow_sign",
    "fnafmw:arcade_neon_sign",
    "fnafmw:jrs_neon_sign",
}

VALID = set(BLOCK_BE.values()) | {"fnafmw:big_door_block_entity", "fnafmw:big_door_part_block_entity"}

stats = collections.Counter()
for path in FILES:
    if not os.path.exists(path):
        print("MISSING", path)
        continue
    shutil.copy2(path, os.path.join(BACKUP, os.path.basename(path)))
    f = nbtlib.load(path)
    pal = f["palette"]
    for b in f["blocks"]:
        state = pal[int(b["state"])]
        name = str(state["Name"])
        pos = [int(v) for v in b["pos"]]
        mapped = BLOCK_BE.get(name)
        nbt = b.get("nbt")
        if nbt is not None:
            beid = str(nbt.get("id", ""))
            if mapped:
                nbt["id"] = String(mapped)
                nbt["x"] = Int(pos[0])
                nbt["y"] = Int(pos[1])
                nbt["z"] = Int(pos[2])
                stats["remapped"] += 1
            elif beid.startswith("minecraft:"):
                stats["kept_vanilla"] += 1
            else:
                del b["nbt"]
                stats["removed"] += 1
        elif name in ALWAYS:
            b["nbt"] = Compound({
                "id": String(BLOCK_BE[name]),
                "x": Int(pos[0]),
                "y": Int(pos[1]),
                "z": Int(pos[2]),
            })
            stats["added"] += 1
    if len(f.get("entities", [])):
        # Structures should not contain entities at all after conversion.
        stats["entities"] += len(f["entities"])
    f.save(path, gzipped=True)
    print("fixed", os.path.basename(os.path.dirname(path)) + "/" + os.path.basename(path))

print("stats", dict(stats))

# validation
bad = []
for path in FILES:
    if not os.path.exists(path):
        continue
    f = nbtlib.load(path)
    for b in f["blocks"]:
        nbt = b.get("nbt")
        if nbt is None:
            continue
        beid = str(nbt.get("id", ""))
        if beid.startswith("management_wanted:"):
            bad.append((path, beid))
        elif not beid.startswith("minecraft:") and beid not in VALID:
            bad.append((path, beid))
print("bad ids", bad[:20], "count", len(bad))
