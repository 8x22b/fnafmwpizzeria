package com.fork.fnafmw.pizzeria;

import com.mojang.logging.LogUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.slf4j.Logger;

public final class PizzeriaEvents {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static int ticks;

   private PizzeriaEvents() {
   }

   @SubscribeEvent
   public static void onServerTick(ServerTickEvent.Post event) {
      ticks++;
      if (ticks % 10 != 0) {
         return;
      }
      MinecraftServer server = event.getServer();
      if (server == null) {
         return;
      }
      for (ServerLevel level : server.getAllLevels()) {
         try {
            PizzeriaManager.tickLevel(level);
         } catch (Throwable throwable) {
            LOGGER.error("Pizzeria tick failed for level {}", level.dimension().location(), throwable);
         }
         if (ticks % 20 == 0) {
            try {
               PizzeriaManager.tickBars(level);
            } catch (Throwable throwable) {
               LOGGER.error("Pizzeria bar update failed for level {}", level.dimension().location(), throwable);
            }
         }
      }
   }

   @SubscribeEvent
   public static void onRegisterCommands(RegisterCommandsEvent event) {
      PizzeriaCommand.register(event.getDispatcher());
   }

   @SubscribeEvent
   public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
      if (event.getEntity() instanceof ServerPlayer player) {
         PizzeriaManager.hideBar(player);
      }
   }

   @SubscribeEvent
   public static void onServerStopping(ServerStoppingEvent event) {
      PizzeriaManager.clear();
   }
}
