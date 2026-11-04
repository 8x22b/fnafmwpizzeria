package com.fork.fnafmw.pizzeria;

import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.ovdr.fnafmw.chunk.ChunkSector;
import net.ovdr.fnafmw.level.entity.generic.AnimatronicEntity;
import net.ovdr.fnafmw.registry.MWPois;
import net.ovdr.fnafmw.registry.customer.ChunkPoiType;

public record PizzeriaQuality(
   float score,
   int arcades,
   int seats,
   int vending,
   int toilets,
   int trashCans,
   int blood,
   int trash,
   int animatronics,
   int chunks
) {
   public static PizzeriaQuality compute(ServerLevel level, ChunkSector sector) {
      int arcades = count(level, sector, MWPois.ARCADE);
      int seats = count(level, sector, MWPois.SITTABLE);
      int vending = count(level, sector, MWPois.VENDING_MACHINE);
      int toilets = count(level, sector, MWPois.TOILET);
      int trashCans = count(level, sector, MWPois.TRASH_CAN);
      int blood = count(level, sector, MWPois.BLOOD);
      int trash = countTrash(level, sector);
      int animatronics = countAnimatronics(level, sector);
      int chunks = sector.getChunks().size();

      float arcadeScore = Math.min(arcades / 8.0F, 1.0F);
      float seatScore = Math.min(seats / 16.0F, 1.0F);
      float drinkScore = Math.min(vending / 3.0F, 1.0F);
      float toiletScore = Math.min(toilets / 3.0F, 1.0F);
      float animatronicScore = Math.min(animatronics / 2.0F, 1.0F);
      float hygiene = Math.max(0.0F, 1.0F - Math.min((blood + trash * 0.5F) / 8.0F, 1.0F));
      float cleanliness = hygiene * 0.7F + Math.min(trashCans / 4.0F, 1.0F) * 0.3F;

      float score = 0.22F * arcadeScore
         + 0.18F * seatScore
         + 0.12F * drinkScore
         + 0.13F * toiletScore
         + 0.20F * animatronicScore
         + 0.15F * cleanliness;
      return new PizzeriaQuality(clamp(score), arcades, seats, vending, toilets, trashCans, blood, trash, animatronics, chunks);
   }

   public PizzeriaQuality withEntrance(boolean entrance) {
      return entrance ? this : new PizzeriaQuality(
         clamp(score * 0.35F), arcades, seats, vending, toilets, trashCans, blood, trash, animatronics, chunks
      );
   }

   private static int count(ServerLevel level, ChunkSector sector, ChunkPoiType type) {
      int count = 0;
      for (BlockPos pos : sector.poiManager.get(type).validPositions(true)) {
         if (!sector.hasChunk(new ChunkPos(pos))) {
            continue;
         }
         if (level.isLoaded(pos)) {
            BlockState state = level.getBlockState(pos);
            if (type == MWPois.BLOOD) {
               if (!state.is(Blocks.RED_CARPET)) {
                  continue;
               }
            } else if (type == MWPois.TRASH) {
               if (!state.is(Blocks.BROWN_CARPET)) {
                  continue;
               }
            } else if (MWPois.getType(state) != type) {
               continue;
            }
         }
         count++;
      }
      return count;
   }

   private static int countTrash(ServerLevel level, ChunkSector sector) {
      int count = 0;
      for (BlockPos pos : sector.poiManager.get(MWPois.TRASH).validPositions(true)) {
         if (!sector.hasChunk(new ChunkPos(pos))) {
            continue;
         }
         if (level.isLoaded(pos) && !level.getBlockState(pos).is(Blocks.BROWN_CARPET)) {
            continue;
         }
         count++;
      }
      return count;
   }

   private static int countAnimatronics(ServerLevel level, ChunkSector sector) {
      Set<ChunkPos> chunks = sector.getChunks();
      if (chunks.isEmpty()) {
         return 0;
      }
      AABB box = box(level, chunks);
      List<AnimatronicEntity> list = level.getEntitiesOfClass(
         AnimatronicEntity.class,
         box,
         animatronic -> animatronic.isPerforming() && sector.hasChunk(new ChunkPos(animatronic.blockPosition()))
      );
      return list.size();
   }

   private static AABB box(ServerLevel level, Set<ChunkPos> chunks) {
      int minX = Integer.MAX_VALUE;
      int minZ = Integer.MAX_VALUE;
      int maxX = Integer.MIN_VALUE;
      int maxZ = Integer.MIN_VALUE;
      for (ChunkPos chunk : chunks) {
         minX = Math.min(minX, chunk.x);
         minZ = Math.min(minZ, chunk.z);
         maxX = Math.max(maxX, chunk.x);
         maxZ = Math.max(maxZ, chunk.z);
      }
      return new AABB(
         minX * 16.0,
         level.getMinBuildHeight(),
         minZ * 16.0,
         (maxX + 1) * 16.0,
         level.getMaxBuildHeight(),
         (maxZ + 1) * 16.0
      );
   }

   public static int interval(float score) {
      return Math.max(180, Math.min(600, Math.round(600.0F - 420.0F * score)));
   }

   public static int cap(float score, int chunks) {
      return Math.max(4, Math.min(20, 3 + chunks / 2 + Math.round(score * 10.0F)));
   }

   public static float clamp(float value) {
      return Math.max(0.0F, Math.min(1.0F, value));
   }
}
