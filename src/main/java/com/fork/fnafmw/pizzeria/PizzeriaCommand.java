package com.fork.fnafmw.pizzeria;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.ovdr.fnafmw.chunk.ChunkAllocator;
import net.ovdr.fnafmw.chunk.ChunkSector;
import net.ovdr.fnafmw.legacy.LegacyIds;
import net.ovdr.fnafmw.level.blockentity.LockerBlockEntity;

public final class PizzeriaCommand {
   private PizzeriaCommand() {
   }

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      dispatcher.register(
         Commands.literal("pizzeria")
            .executes(PizzeriaCommand::status)
            .then(Commands.literal("quality").executes(PizzeriaCommand::status))
            .then(Commands.literal("rescan").executes(PizzeriaCommand::rescan))
            .then(Commands.literal("claim").requires(source -> source.hasPermission(2)).executes(PizzeriaCommand::claim))
            .then(Commands.literal("spawn").requires(source -> source.hasPermission(2)).executes(PizzeriaCommand::spawn))
            .then(Commands.literal("cleanup").requires(source -> source.hasPermission(2)).executes(PizzeriaCommand::cleanup))
            .then(Commands.literal("loot").requires(source -> source.hasPermission(2)).executes(PizzeriaCommand::loot))
            .then(
               Commands.literal("place")
                  .requires(source -> source.hasPermission(2))
                  .then(
                     Commands.argument("deed", StringArgumentType.word())
                        .executes(context -> place(context, StringArgumentType.getString(context, "deed")))
                  )
            )
      );
   }

   private static int status(CommandContext<CommandSourceStack> context) {
      CommandSourceStack source = context.getSource();
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.translatable("pizzeria.command.player_only"));
         return 0;
      }
      ChunkSector sector = sector(player);
      if (sector == null) {
         source.sendFailure(Component.translatable("pizzeria.command.not_in_sector"));
         return 0;
      }
      if (!sector.getPlayerOwner().equals(player.getUUID()) && !source.hasPermission(2)) {
         source.sendFailure(Component.translatable("pizzeria.command.not_owner"));
         return 0;
      }
      for (Component line : PizzeriaManager.statusLines((ServerLevel) player.level(), sector)) {
         source.sendSuccess(() -> line, false);
      }
      return 1;
   }

   private static int rescan(CommandContext<CommandSourceStack> context) {
      CommandSourceStack source = context.getSource();
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.translatable("pizzeria.command.player_only"));
         return 0;
      }
      ChunkSector sector = sector(player);
      if (sector == null) {
         source.sendFailure(Component.translatable("pizzeria.command.not_in_sector"));
         return 0;
      }
      if (!sector.getPlayerOwner().equals(player.getUUID()) && !source.hasPermission(2)) {
         source.sendFailure(Component.translatable("pizzeria.command.not_owner"));
         return 0;
      }
      ServerLevel level = (ServerLevel) player.level();
      long now = level.getGameTime();
      if (!PizzeriaManager.canRescan(level, sector, now)) {
         source.sendFailure(Component.translatable("pizzeria.command.rescan_busy"));
         return 0;
      }
      PizzeriaManager.markRescan(level, sector, now);
      PizzeriaManager.requestRescan(level, sector);
      source.sendSuccess(() -> Component.translatable("pizzeria.command.rescan_started"), false);
      return 1;
   }

   private static int claim(CommandContext<CommandSourceStack> context) {
      CommandSourceStack source = context.getSource();
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.translatable("pizzeria.command.player_only"));
         return 0;
      }
      ChunkAllocator allocator = ChunkAllocator.get(player.level());
      boolean claimed = allocator != null && allocator.tryClaimFor(player.getUUID(), player.blockPosition());
      source.sendSuccess(
         () -> Component.translatable(claimed ? "pizzeria.command.claim_success" : "pizzeria.command.claim_fail"), true
      );
      return claimed ? 1 : 0;
   }

   private static int spawn(CommandContext<CommandSourceStack> context) {
      CommandSourceStack source = context.getSource();
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.translatable("pizzeria.command.player_only"));
         return 0;
      }
      ChunkSector sector = sector(player);
      if (sector == null) {
         source.sendFailure(Component.translatable("pizzeria.command.not_in_sector"));
         return 0;
      }
      ServerLevel level = (ServerLevel) player.level();
      if (!PizzeriaManager.isEntranceReady(level, sector)) {
         source.sendFailure(Component.translatable("pizzeria.command.spawn_no_entrance"));
         return 0;
      }
      PizzeriaManager.refreshCount(level, sector);
      sector.lastCustomerSpawnTick = level.getGameTime();
      boolean spawned = sector.spawnCustomer();
      source.sendSuccess(
         () -> Component.translatable(spawned ? "pizzeria.command.spawn_success" : "pizzeria.command.spawn_fail"), true
      );
      return spawned ? 1 : 0;
   }

   private static int cleanup(CommandContext<CommandSourceStack> context) {
      ServerPlayer player = context.getSource().getPlayer();
      if (player == null) {
         context.getSource().sendFailure(Component.translatable("pizzeria.command.player_only"));
         return 0;
      }
      ServerLevel level = (ServerLevel) player.level();
      AABB area = player.getBoundingBox().inflate(64.0);
      int removed = 0;
      for (Entity entity : level.getEntitiesOfClass(Entity.class, area)) {
         ResourceLocation entityType = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
         if (entityType == null || !entityType.equals(LegacyIds.PLACEHOLDER_ENTITY_ID)) {
            continue;
         }
         String name = entity.getCustomName() == null ? "" : entity.getCustomName().getString();
         BlockPos pos = entity.blockPosition();
         if (name.contains("office_chair_entity")) {
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("fnafmw:classic_chair")).defaultBlockState());
         } else if (name.contains("freddy_cutout_entity")) {
            level.setBlockAndUpdate(pos, BuiltInRegistries.BLOCK.get(ResourceLocation.parse("fnafmw:freddy_stand")).defaultBlockState());
         }
         entity.discard();
         removed++;
      }
      int count = removed;
      context.getSource().sendSuccess(() -> Component.literal("Removed " + count + " legacy entities"), true);
      return removed;
   }

   private static int loot(CommandContext<CommandSourceStack> context) {
      CommandSourceStack source = context.getSource();
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.translatable("pizzeria.command.player_only"));
         return 0;
      }

      ServerLevel level = player.serverLevel();
      BlockPos center = player.blockPosition();
      int radius = 40;
      int lockers = 0;
      int containers = 0;
      int chunkRadius = (radius >> 4) + 1;
      ChunkPos centerChunk = player.chunkPosition();
      for (int cx = centerChunk.x - chunkRadius; cx <= centerChunk.x + chunkRadius; cx++) {
         for (int cz = centerChunk.z - chunkRadius; cz <= centerChunk.z + chunkRadius; cz++) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
            if (chunk == null) {
               continue;
            }

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
               BlockPos pos = blockEntity.getBlockPos();
               if (Math.abs(pos.getX() - center.getX()) > radius
                  || Math.abs(pos.getY() - center.getY()) > 32
                  || Math.abs(pos.getZ() - center.getZ()) > radius) {
                  continue;
               }

               if (blockEntity instanceof LockerBlockEntity locker) {
                  if (locker.isEmpty()) {
                     PizzeriaLoot.fillLocker(locker, level.random);
                     lockers++;
                  }
               } else if (blockEntity instanceof RandomizableContainerBlockEntity container) {
                  if (container.isEmpty() && container.getLootTable() == null) {
                     container.setLootTable(
                        ResourceKey.create(Registries.LOOT_TABLE, ResourceLocation.parse("fnafmwpizzeria:chests/loot"))
                     );
                     container.setChanged();
                     containers++;
                  }
               }
            }
         }
      }

      int filledLockers = lockers;
      int filledContainers = containers;
      source.sendSuccess(
         () -> Component.translatable("pizzeria.command.loot_success", filledLockers, filledContainers), true
      );
      return lockers + containers;
   }

   private static int place(CommandContext<CommandSourceStack> context, String deed) {
      CommandSourceStack source = context.getSource();
      ServerPlayer player = source.getPlayer();
      if (player == null) {
         source.sendFailure(Component.translatable("pizzeria.command.player_only"));
         return 0;
      }
      PizzeriaDeedType type = null;
      for (PizzeriaDeedType value : PizzeriaDeedType.values()) {
         if (value.itemId().equals(deed)) {
            type = value;
            break;
         }
      }
      if (type == null) {
         source.sendFailure(Component.translatable("pizzeria.command.place_fail"));
         return 0;
      }
      boolean placed = DeedPlacement.place(
         (ServerLevel) player.level(),
         player.blockPosition(),
         type.placement(player.getDirection()),
         type,
         player
      );
      source.sendSuccess(
         () -> Component.translatable(placed ? "pizzeria.command.place_success" : "pizzeria.command.place_fail"), true
      );
      return placed ? 1 : 0;
   }

   private static ChunkSector sector(ServerPlayer player) {
      ChunkAllocator allocator = ChunkAllocator.get(player.level());
      return allocator == null ? null : allocator.getSector(player.chunkPosition());
   }
}
