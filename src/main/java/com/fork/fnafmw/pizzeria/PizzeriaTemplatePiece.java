package com.fork.fnafmw.pizzeria;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class PizzeriaTemplatePiece extends StructurePiece {
   private static final String TEMPLATE_TAG = "Template";
   private static final String X_TAG = "TemplateX";
   private static final String Y_TAG = "TemplateY";
   private static final String Z_TAG = "TemplateZ";
   private final ResourceLocation template;
   private final BlockPos templatePosition;
   private final StructureTemplateManager manager;
   private StructureTemplate templateCache;

   public PizzeriaTemplatePiece(ResourceLocation template, BlockPos position, StructureTemplateManager manager) {
      super(PizzeriaMod.TEMPLATE_PIECE.get(), 0, makeBoundingBox(position, manager.getOrCreate(template).getSize()));
      this.template = template;
      this.templatePosition = position;
      this.manager = manager;
   }

   public PizzeriaTemplatePiece(CompoundTag tag, StructureTemplateManager manager) {
      super(PizzeriaMod.TEMPLATE_PIECE.get(), tag);
      this.template = ResourceLocation.parse(tag.getString(TEMPLATE_TAG));
      this.templatePosition = new BlockPos(tag.getInt(X_TAG), tag.getInt(Y_TAG), tag.getInt(Z_TAG));
      this.manager = manager;
   }

   private static BoundingBox makeBoundingBox(BlockPos position, Vec3i size) {
      return BoundingBox.fromCorners(position, position.offset(size.getX() - 1, size.getY() - 1, size.getZ() - 1));
   }

   @Override
   protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
      tag.putString(TEMPLATE_TAG, this.template.toString());
      tag.putInt(X_TAG, this.templatePosition.getX());
      tag.putInt(Y_TAG, this.templatePosition.getY());
      tag.putInt(Z_TAG, this.templatePosition.getZ());
   }

   @Override
   public void postProcess(
      WorldGenLevel level,
      StructureManager structureManager,
      ChunkGenerator generator,
      RandomSource random,
      BoundingBox box,
      ChunkPos chunkPos,
      BlockPos pos
   ) {
      StructureTemplate template = this.templateCache;
      if (template == null) {
         template = this.manager.getOrCreate(this.template);
         this.templateCache = template;
      }
      StructurePlaceSettings settings = new StructurePlaceSettings()
         .setRotation(Rotation.NONE)
         .setMirror(Mirror.NONE)
         .setIgnoreEntities(false)
         .setBoundingBox(box);
      template.placeInWorld(level, this.templatePosition, this.templatePosition, settings, random, 2);
   }
}
