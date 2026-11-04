package com.fork.fnafmw.pizzeria;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.ovdr.fnafmw.chunk.ChunkAllocator;
import net.ovdr.fnafmw.chunk.ChunkSector;
import net.ovdr.fnafmw.legacy.LegacyIds;
import net.ovdr.fnafmw.legacy.LegacyNbtRewriter;
import net.ovdr.fnafmw.level.block.BigDoorBlock;
import net.ovdr.fnafmw.level.block.BigEntranceDoorBlock;
import net.ovdr.fnafmw.registry.MWBlocks;
import org.slf4j.Logger;

public final class DeedPlacement {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static Map<String, String> legacyBlockMap;

   private DeedPlacement() {
   }

   public static boolean place(ServerLevel level, BlockPos clickedPos, PizzeriaDeedType.Placement placement, PizzeriaDeedType type, Player player) {
      if (player != null) {
         ChunkAllocator allocator = ChunkAllocator.get(level);
         if (allocator != null) {
            ChunkSector existing = allocator.getSector(new ChunkPos(clickedPos.getX() >> 4, clickedPos.getZ() >> 4));
            if (existing != null && existing.getPlayerOwner() != null && !existing.getPlayerOwner().equals(player.getUUID())) {
               return false;
            }
         }
      }
      CompoundTag tag = read(type);
      if (tag == null) {
         return false;
      }

      remapPalette(tag);
      LegacyNbtRewriter.rewrite(tag);
      sanitizePalette(tag);
      sanitizeBlockEntities(tag);
      sanitizeEntities(tag);

      StructureTemplate template = new StructureTemplate();
      try {
         template.load(level.holderLookup(Registries.BLOCK), tag);
      } catch (Throwable throwable) {
         LOGGER.error("Could not load converted pizzeria structure {}", type.structure(), throwable);
         return false;
      }

      BlockPos origin = clickedPos.offset(placement.dx(), 0, placement.dz());
      StructurePlaceSettings settings = new StructurePlaceSettings()
         .setRotation(placement.rotation())
         .setMirror(Mirror.NONE)
         .setIgnoreEntities(false);
      BoundingBox box = template.getBoundingBox(settings, origin);
      if (player != null && overlapsForeignClaim(level, box, player)) {
         return false;
      }
      boolean placed;
      try {
         placed = template.placeInWorld(level, origin, origin, settings, level.getRandom(), 3);
      } catch (Throwable throwable) {
         LOGGER.error("Could not place pizzeria structure {}", type.structure(), throwable);
         return false;
      }
      if (!placed) {
         return false;
      }

      rotateNonRotatingBlocks(level, template, origin, settings);
      fixMultiblocks(level, template, origin, settings);
      if (player != null) {
         claimChunks(level, clickedPos, box, player);
      }

      if (player != null) {
         ChunkAllocator allocator = ChunkAllocator.get(level);
         if (allocator != null) {
            ChunkSector sector = allocator.getSector(new ChunkPos(clickedPos.getX() >> 4, clickedPos.getZ() >> 4));
            if (sector != null) {
               PizzeriaManager.requestRescan(level, sector);
            }
         }
      }
      return true;
   }

   private static void fixMultiblocks(ServerLevel level, StructureTemplate template, BlockPos origin, StructurePlaceSettings settings) {
      for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(origin, settings, MWBlocks.BIG_ENTRANCE_DOOR.get())) {
         BlockState state = level.getBlockState(info.pos());
         if (!(state.getBlock() instanceof BigEntranceDoorBlock door) || state.getValue(BigEntranceDoorBlock.PART) != 0) {
            continue;
         }
         BlockPos[] parts = door.subParts(info.pos(), state);
         for (int part = 0; part < parts.length; part++) {
            level.setBlock(parts[part], state.setValue(BigEntranceDoorBlock.PART, part + 1), 3);
         }
      }

      for (Block doorBlock : List.of(MWBlocks.BIG_EMPLOYEE_DOOR.get(), MWBlocks.BIG_PARTS_SERVICE_DOOR.get())) {
         for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(origin, settings, doorBlock)) {
            BlockState state = level.getBlockState(info.pos());
            if (state.getBlock() instanceof BigDoorBlock door) {
               door.setPlacedBy(level, info.pos(), state, null, ItemStack.EMPTY);
            }
         }
      }

      for (Block doorBlock : List.of(MWBlocks.EMPLOYEE_DOOR.get(), MWBlocks.PARTS_SERVICE_DOOR.get(), MWBlocks.ENTRANCE_DOOR.get())) {
         for (StructureTemplate.StructureBlockInfo info : template.filterBlocks(origin, settings, doorBlock)) {
            BlockState state = level.getBlockState(info.pos());
            if (state.getValue(DoorBlock.HALF) != DoubleBlockHalf.LOWER) {
               continue;
            }
            level.setBlock(info.pos(), state, 2);
            level.setBlock(info.pos().above(), state.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), 2);
         }
      }
   }

   private static void rotateNonRotatingBlocks(ServerLevel level, StructureTemplate template, BlockPos origin, StructurePlaceSettings settings) {
      Rotation rotation = settings.getRotation();
      if (rotation == Rotation.NONE) {
         return;
      }

      BoundingBox box = template.getBoundingBox(settings, origin);
      Set<Block> candidates = new HashSet<>();
      for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
         BlockState state = level.getBlockState(pos);
         if (hasDirectional(state) && state.rotate(rotation).equals(state)) {
            candidates.add(state.getBlock());
         }
      }

      for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
         BlockState state = level.getBlockState(pos);
         if (!candidates.contains(state.getBlock())) {
            continue;
         }
         BlockState rotated = rotateState(state, rotation);
         if (rotated != state) {
            level.setBlock(pos, rotated, 2);
         }
      }
   }

   private static boolean hasDirectional(BlockState state) {
      for (Property<?> property : state.getProperties()) {
         String name = property.getName();
         if (name.equals("facing") || name.equals("axis") || name.equals("rotation") || name.equals("north")
            || name.equals("east") || name.equals("south") || name.equals("west")) {
            return true;
         }
      }
      return false;
   }

   private static BlockState rotateState(BlockState state, Rotation rotation) {
      BlockState result = state;
      Property<?> facing = property(state, "facing");
      if (facing != null && state.getValue(facing) instanceof Direction direction) {
         result = setRaw(result, facing, rotation.rotate(direction));
      }

      Property<?> axis = property(state, "axis");
      if (axis != null && state.getValue(axis) instanceof Direction.Axis oldAxis) {
         Direction.Axis newAxis = oldAxis;
         if (rotation == Rotation.CLOCKWISE_90 || rotation == Rotation.COUNTERCLOCKWISE_90) {
            newAxis = oldAxis == Direction.Axis.X ? Direction.Axis.Z : oldAxis == Direction.Axis.Z ? Direction.Axis.X : oldAxis;
         }
         result = setRaw(result, axis, newAxis);
      }

      Property<?> rotationProperty = property(state, "rotation");
      if (rotationProperty != null && state.getValue(rotationProperty) instanceof Integer value) {
         int steps = rotation == Rotation.CLOCKWISE_90 ? 4 : rotation == Rotation.CLOCKWISE_180 ? 8 : 12;
         result = setRaw(result, rotationProperty, (value + steps) & 15);
      }

      for (Direction from : Direction.Plane.HORIZONTAL) {
         result = copyBool(result, state, from.getName(), rotation.rotate(from).getName());
      }
      return result;
   }

   private static Property<?> property(BlockState state, String name) {
      for (Property<?> property : state.getProperties()) {
         if (property.getName().equals(name)) {
            return property;
         }
      }
      return null;
   }

   @SuppressWarnings({"rawtypes", "unchecked"})
   private static BlockState setRaw(BlockState state, Property property, Comparable value) {
      return state.setValue(property, value);
   }

   private static BlockState copyBool(BlockState result, BlockState original, String from, String to) {
      Property<?> source = property(original, from);
      Property<?> target = property(result, to);
      if (source != null && target != null && original.getValue(source) instanceof Boolean value) {
         return setRaw(result, target, value);
      }
      return result;
   }

   private static CompoundTag read(PizzeriaDeedType type) {
      try (InputStream stream = DeedPlacement.class.getResourceAsStream("/data/fnafmwpizzeria/structure/pizzerias/" + type.structure() + ".nbt")) {
         if (stream == null) {
            LOGGER.error("Missing bundled pizzeria structure {}", type.structure());
            return null;
         }
         return NbtIo.readCompressed(stream, NbtAccounter.unlimitedHeap());
      } catch (IOException exception) {
         LOGGER.error("Could not read bundled pizzeria structure {}", type.structure(), exception);
         return null;
      }
   }

   private static boolean overlapsForeignClaim(ServerLevel level, BoundingBox box, Player player) {
      ChunkAllocator allocator = ChunkAllocator.get(level);
      if (allocator == null) {
         return false;
      }

      for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
         for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) {
            ChunkSector sector = allocator.getSector(new ChunkPos(x, z));
            if (sector != null && sector.getPlayerOwner() != null && !sector.getPlayerOwner().equals(player.getUUID())) {
               return true;
            }
         }
      }
      return false;
   }

   private static void claimChunks(ServerLevel level, BlockPos clickedPos, BoundingBox box, Player player) {
      ChunkAllocator allocator = ChunkAllocator.get(level);
      if (allocator == null) {
         return;
      }

      for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
         for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) {
            allocator.tryClaimFor(player.getUUID(), new BlockPos(x << 4, clickedPos.getY(), z << 4));
         }
      }
   }

   private static void remapPalette(CompoundTag tag) {
      Map<String, String> map = legacyBlockMap();
      if (map.isEmpty()) {
         return;
      }

      ListTag palette = tag.getList("palette", 10);
      for (int i = 0; i < palette.size(); i++) {
         CompoundTag entry = palette.getCompound(i);
         String oldName = entry.getString("Name");
         String mapped = oldName.equals("minecraft:grass") ? "minecraft:short_grass" : map.get(oldName);
         if (mapped == null) {
            continue;
         }

         entry.putString("Name", mapped);
         if (mapped.equals("minecraft:air")) {
            entry.remove("Properties");
         } else if (mapped.equals("fnafmw:party_table")) {
            String modularType = tableModularType(oldName);
            if (modularType != null) {
               CompoundTag properties = entry.getCompound("Properties");
               properties.putString("modular_type", modularType);
               entry.put("Properties", properties);
            }
         }
      }
   }

   private static String tableModularType(String oldName) {
      String path = oldName.substring(oldName.indexOf(':') + 1);
      if (path.equals("party_table_middle")) {
         return "15";
      }
      if (path.equals("backstage_table_left")) {
         return "2";
      }
      if (path.equals("backstage_table_middle")) {
         return "10";
      }
      if (path.equals("backstage_table_right")) {
         return "8";
      }

      String[][] suffixes = {
         {"vertical_bottom", "1"},
         {"horizontal_left", "2"},
         {"bottom_left", "3"},
         {"vertical_top", "4"},
         {"vertical_middle", "5"},
         {"top_left", "6"},
         {"middle_left", "7"},
         {"horizontal_right", "8"},
         {"bottom_right", "9"},
         {"horizontal_middle", "10"},
         {"bottom_middle", "11"},
         {"top_right", "12"},
         {"middle_right", "13"},
         {"top_middle", "14"}
      };
      for (String[] suffix : suffixes) {
         if (path.endsWith("_" + suffix[0])) {
            return suffix[1];
         }
      }
      if (path.endsWith("_left")) {
         return "2";
      }
      if (path.endsWith("_right")) {
         return "8";
      }
      if (path.endsWith("_middle")) {
         return "10";
      }
      return null;
   }

   private static Map<String, String> legacyBlockMap() {
      if (legacyBlockMap != null) {
         return legacyBlockMap;
      }

      Map<String, String> map = new HashMap<>();
      try (InputStream stream = DeedPlacement.class.getResourceAsStream("/data/fnafmwpizzeria/legacy_block_map.json")) {
         if (stream == null) {
            LOGGER.error("Missing legacy block map resource");
         } else {
            JsonObject object = JsonParser.parseReader(new InputStreamReader(stream, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String key : object.keySet()) {
               map.put(key, object.get(key).getAsString());
            }
         }
      } catch (Throwable throwable) {
         LOGGER.error("Could not read legacy block map", throwable);
      }
      legacyBlockMap = map;
      return legacyBlockMap;
   }

   private static void sanitizePalette(CompoundTag tag) {
      ListTag palette = tag.getList("palette", 10);
      for (int i = 0; i < palette.size(); i++) {
         CompoundTag entry = palette.getCompound(i);
         String name = entry.getString("Name");
         ResourceLocation id = ResourceLocation.tryParse(name);
         if (id == null || id.equals(LegacyIds.PLACEHOLDER_BLOCK_ID)) {
            entry.putString("Name", "minecraft:air");
            entry.remove("Properties");
            LOGGER.warn("Unmapped legacy pizzeria block, removing: {}", name);
            continue;
         }

         Block block = BuiltInRegistries.BLOCK.get(id);
         if (block == Blocks.AIR && !id.equals(ResourceLocation.withDefaultNamespace("air"))) {
            entry.putString("Name", "minecraft:air");
            entry.remove("Properties");
            LOGGER.warn("Unknown pizzeria block, removing: {}", name);
            continue;
         }

         StateDefinition<Block, BlockState> definition = block.getStateDefinition();
         CompoundTag properties = entry.getCompound("Properties");
         for (String key : List.copyOf(properties.getAllKeys())) {
            Property<?> property = definition.getProperty(key);
            if (property == null || property.getValue(properties.getString(key)).isEmpty()) {
               properties.remove(key);
            }
         }
         if (properties.isEmpty()) {
            entry.remove("Properties");
         }
      }
   }

   private static void sanitizeBlockEntities(CompoundTag tag) {
      ListTag palette = tag.getList("palette", 10);
      ListTag blocks = tag.getList("blocks", 10);
      for (int i = 0; i < blocks.size(); i++) {
         CompoundTag block = blocks.getCompound(i);
         if (!(block.get("nbt") instanceof CompoundTag nbt)) {
            continue;
         }

         int state = block.getInt("state");
         String name = state >= 0 && state < palette.size() ? palette.getCompound(state).getString("Name") : "minecraft:air";
         if (name.equals("minecraft:air")) {
            block.remove("nbt");
            continue;
         }

         ResourceLocation blockId = ResourceLocation.tryParse(name);
         if (blockId == null) {
            block.remove("nbt");
            continue;
         }

         ResourceLocation blockEntityType = LegacyIds.blockEntityIdFor(blockId);
         if (blockEntityType != null) {
            nbt.putString("id", blockEntityType.toString());
         } else {
            block.remove("nbt");
         }
      }
   }

   private static void sanitizeEntities(CompoundTag tag) {
      ListTag entities = tag.getList("entities", 10);
      for (int i = entities.size() - 1; i >= 0; i--) {
         CompoundTag entity = entities.getCompound(i);
         CompoundTag nbt = entity.getCompound("nbt");
         ResourceLocation id = ResourceLocation.tryParse(nbt.getString("id"));
         if (id != null && id.equals(LegacyIds.PLACEHOLDER_ENTITY_ID)) {
            entities.remove(i);
            continue;
         }
         if (id == null || !LegacyIds.isLegacyNamespace(id.getNamespace())) {
            continue;
         }
         String resolved = LegacyIds.resolveEntity(id.getPath());
         if (resolved == null) {
            entities.remove(i);
         } else {
            nbt.putString("id", resolved);
         }
      }
   }
}
