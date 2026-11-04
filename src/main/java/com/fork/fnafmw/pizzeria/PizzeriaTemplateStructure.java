package com.fork.fnafmw.pizzeria;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.ovdr.fnafmw.level.block.BigDoorBlock;
import net.ovdr.fnafmw.level.blockentity.BigDoorBlockEntity;
import net.ovdr.fnafmw.level.blockentity.BigDoorPartBlockEntity;
import net.ovdr.fnafmw.registry.MWBlocks;

public class PizzeriaTemplateStructure extends Structure {
   public static final MapCodec<PizzeriaTemplateStructure> CODEC = RecordCodecBuilder.mapCodec(
      instance -> instance.group(
            settingsCodec(instance),
            ResourceLocation.CODEC.listOf().fieldOf("templates").forGetter(structure -> structure.templates)
         )
         .apply(instance, PizzeriaTemplateStructure::new)
   );

   private final List<ResourceLocation> templates;

   public PizzeriaTemplateStructure(StructureSettings settings, List<ResourceLocation> templates) {
      super(settings);
      this.templates = templates;
   }

   @Override
   protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
      if (this.templates.isEmpty()) {
         return Optional.empty();
      }

      ChunkPos chunkPos = context.chunkPos();
      int x = chunkPos.getMiddleBlockX();
      int z = chunkPos.getMiddleBlockZ();
      Holder<Biome> biome = context.biomeSource()
         .getNoiseBiome(x >> 2, 64 >> 2, z >> 2, context.randomState().sampler());
      if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)) {
         return Optional.empty();
      }

      int y = context.chunkGenerator()
         .getFirstFreeHeight(x, z, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, context.heightAccessor(), context.randomState());
      ResourceLocation template = this.templates.get(context.random().nextInt(this.templates.size()));
      BlockPos position = new BlockPos(x, y, z);
      return Optional.of(
         new GenerationStub(
            position,
            builder -> builder.addPiece(new PizzeriaTemplatePiece(template, position, context.structureTemplateManager()))
         )
      );
   }

   @Override
   public void afterPlace(
      WorldGenLevel level,
      StructureManager structureManager,
      ChunkGenerator generator,
      RandomSource random,
      BoundingBox box,
      ChunkPos chunkPos,
      PiecesContainer pieces
   ) {
      BoundingBox full = pieces.calculateBoundingBox();
      int minX = Math.max(box.minX(), full.minX());
      int minY = Math.max(box.minY(), full.minY());
      int minZ = Math.max(box.minZ(), full.minZ());
      int maxX = Math.min(box.maxX(), full.maxX());
      int maxY = Math.min(box.maxY(), full.maxY());
      int maxZ = Math.min(box.maxZ(), full.maxZ());
      if (minX > maxX || minY > maxY || minZ > maxZ) {
         return;
      }

      for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
         BlockState state = level.getBlockState(pos);
         Block block = state.getBlock();
         if (block instanceof BigDoorBlock) {
            if (level.getBlockEntity(pos) instanceof BigDoorBlockEntity door) {
               for (BlockPos partPos : door.getParts()) {
                  if (level.getBlockEntity(partPos) instanceof BigDoorPartBlockEntity part) {
                     part.setController(pos.immutable());
                  }
               }
            }
         } else if (isBigDoorPart(block) && level.getBlockEntity(pos) instanceof BigDoorPartBlockEntity part) {
            BlockPos controller = findController(level, pos);
            if (controller != null) {
               part.setController(controller);
            }
         }
      }
   }

   private static boolean isBigDoorPart(Block block) {
      return block == MWBlocks.BIG_EMPLOYEE_DOOR_PART.get()
         || block == MWBlocks.BIG_PARTS_SERVICE_DOOR_PART.get()
         || block == MWBlocks.BIG_BEDROOM_DOOR_PART.get();
   }

   private static BlockPos findController(WorldGenLevel level, BlockPos partPos) {
      for (BlockPos candidate : BlockPos.betweenClosed(partPos.offset(-3, -3, -3), partPos.offset(3, 3, 3))) {
         if (level.getBlockState(candidate).getBlock() instanceof BigDoorBlock
            && level.getBlockEntity(candidate) instanceof BigDoorBlockEntity door) {
            for (BlockPos part : door.getParts()) {
               if (part.equals(partPos)) {
                  return candidate.immutable();
               }
            }
         }
      }
      return null;
   }

   @Override
   public StructureType<?> type() {
      return PizzeriaMod.TEMPLATE_STRUCTURE.get();
   }
}
