package com.fork.fnafmw.pizzeria;

import com.mojang.logging.LogUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.phys.AABB;
import net.ovdr.fnafmw.chunk.ChunkAllocator;
import net.ovdr.fnafmw.chunk.ChunkSector;
import net.ovdr.fnafmw.level.block.block.OpenNeonSignBlock;
import net.ovdr.fnafmw.level.blockentity.WelcomeMatBlockEntity;
import net.ovdr.fnafmw.level.entity.customer.Customer;
import net.ovdr.fnafmw.registry.MWBlocks;
import net.ovdr.fnafmw.registry.MWPois;
import net.ovdr.fnafmw.registry.customer.ChunkPoiType;
import net.ovdr.fnafmw.services.WorldTime;
import org.slf4j.Logger;

public final class PizzeriaManager {
   private static final Logger LOGGER = LogUtils.getLogger();
   private static final Map<StatsKey, PizzeriaStats> STATS = new HashMap<>();
   private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();
   private static Map<BlockState, ChunkPoiType> poiStates;
   private static final int SCAN_BUDGET = 24;
   private static final int RESCAN_COOLDOWN = 1200;
   public static final int OPEN_HOUR = 6;
   public static final int CLOSE_HOUR = 22;

   private PizzeriaManager() {
   }

   public static boolean isOpen() {
      int hour = WorldTime.getHour();
      return hour >= OPEN_HOUR && hour < CLOSE_HOUR;
   }

   public static PizzeriaStats stats(Level level, ChunkSector sector) {
      return STATS.computeIfAbsent(new StatsKey(level.dimension(), sector.sectorID), key -> new PizzeriaStats());
   }

   public static void clear() {
      STATS.clear();
      BARS.clear();
      poiStates = null;
   }

   public static void tickLevel(ServerLevel level) {
      ChunkAllocator allocator = ChunkAllocator.get(level);
      if (allocator == null) {
         return;
      }

      long now = level.getGameTime();
      boolean open = isOpen();
      int scanBudget = SCAN_BUDGET;
      for (ChunkSector sector : allocator.getSectors()) {
         try {
            scanBudget = tickSector(level, sector, now, open, scanBudget);
         } catch (Throwable throwable) {
            LOGGER.error("Failed to tick pizzeria sector {}", sector.sectorID, throwable);
         }
      }
   }

   private static int tickSector(ServerLevel level, ChunkSector sector, long now, boolean open, int scanBudget) {
      if (!sector.hasAnyChunk()) {
         return scanBudget;
      }
      if (level.getPlayerByUUID(sector.getPlayerOwner()) == null) {
         return scanBudget;
      }

      PizzeriaStats stats = stats(level, sector);
      long day = level.getServer().overworld().getDayTime() / 24000L;
      if (stats.lastResetDay != day) {
         stats.lastResetDay = day;
         stats.incomeToday = 0.0F;
         stats.visitorsToday = 0;
      }

      Set<ChunkPos> chunks = sector.getChunks();
      if (!stats.scanning) {
         if (!stats.initialScanDone && now >= stats.nextScanRetry) {
            startScan(level, sector, stats, true);
         } else if (stats.initialScanDone && !chunks.equals(stats.knownChunks)) {
            startScan(level, sector, stats, false);
         }
      }
      if (stats.scanning && scanBudget > 0) {
         scanBudget -= processRescan(level, sector, stats, scanBudget, now);
      }
      if (stats.scanning || !stats.initialScanDone) {
         if (!open) {
            forceLeave(level, sector);
            stats.closed = true;
         }
         return scanBudget;
      }

      if (stats.quality == null || now - stats.qualityTick >= 100L) {
         PizzeriaQuality quality = PizzeriaQuality.compute(level, sector);
         stats.quality = quality.withEntrance(isEntranceReady(level, sector));
         stats.qualityTick = now;
      }

      if (!open) {
         forceLeave(level, sector);
         stats.closed = true;
         return scanBudget;
      }
      stats.closed = false;

      PizzeriaQuality quality = stats.quality;
      int actual = countCustomers(level, sector);
      int effective = reconcileCount(level, sector, actual);
      int cap = PizzeriaQuality.cap(quality.score(), sector.getChunks().size());
      int interval = PizzeriaQuality.interval(quality.score());

      if (effective < cap && now - sector.lastCustomerSpawnTick >= interval) {
         sector.lastCustomerSpawnTick = now;
         if (isEntranceReady(level, sector)) {
            try {
               sector.spawnCustomer();
            } catch (Throwable throwable) {
               LOGGER.error("Failed to spawn a customer in sector {}", sector.sectorID, throwable);
            }
         }
      }
      return scanBudget;
   }

   public static void tickBars(ServerLevel level) {
      ChunkAllocator allocator = ChunkAllocator.get(level);
      if (allocator == null) {
         return;
      }

      for (ServerPlayer player : level.players()) {
         try {
            tickBar(level, allocator, player);
         } catch (Throwable throwable) {
            LOGGER.error("Failed to update pizzeria bar for {}", player.getUUID(), throwable);
         }
      }
   }

   private static void tickBar(ServerLevel level, ChunkAllocator allocator, ServerPlayer player) {
      ChunkSector sector = allocator.getSector(player.chunkPosition());
      if (sector == null || !sector.getPlayerOwner().equals(player.getUUID())) {
         hideBar(player);
         return;
      }

      PizzeriaStats stats = stats(level, sector);
      boolean scanning = stats.scanning || !stats.initialScanDone;
      int customers = countCustomers(level, sector);
      boolean open = isOpen();
      Component name;
      float progress;
      BossEvent.BossBarColor color;

      PizzeriaQuality quality = stats.quality;
      if (scanning || quality == null) {
         name = Component.translatable(scanning ? "pizzeria.bar.scanning" : "pizzeria.bar.updating");
         progress = 0.05F;
         color = BossEvent.BossBarColor.BLUE;
      } else {
         int cap = PizzeriaQuality.cap(quality.score(), sector.getChunks().size());
         int interval = PizzeriaQuality.interval(quality.score());
         long now = level.getGameTime();
         long next = Math.max(0L, sector.lastCustomerSpawnTick + interval - now);
         boolean entrance = isEntranceReady(level, sector);
         boolean full = customers >= cap;
         Component status = !open
            ? Component.translatable("pizzeria.bar.closed")
            : !entrance
               ? Component.translatable("pizzeria.bar.no_entrance")
               : full ? Component.translatable("pizzeria.bar.full") : Component.translatable("pizzeria.bar.next", String.valueOf(next / 20L));
         name = Component.translatable(
            "pizzeria.bar.main",
            String.format(Locale.ROOT, "%.1f", quality.score() * 5.0F),
            String.valueOf(customers),
            String.valueOf(cap),
            String.format(Locale.ROOT, "%.1f", stats.incomeToday),
            status
         );
         progress = !open || !entrance ? 0.0F : quality.score();
         color = !open || !entrance ? BossEvent.BossBarColor.RED : full ? BossEvent.BossBarColor.YELLOW : quality.score() >= 0.6F ? BossEvent.BossBarColor.GREEN : BossEvent.BossBarColor.YELLOW;
      }

      ServerBossEvent bar = BARS.computeIfAbsent(
         player.getUUID(),
         id -> new ServerBossEvent(name, BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.PROGRESS)
      );
      bar.setName(name);
      bar.setProgress(progress);
      bar.setColor(color);
      if (!bar.getPlayers().contains(player)) {
         bar.addPlayer(player);
      }
   }

   public static void hideBar(ServerPlayer player) {
      ServerBossEvent bar = BARS.remove(player.getUUID());
      if (bar != null) {
         bar.removePlayer(player);
      }
   }

   public static void recordIncomeSafe(Customer customer, float amount) {
      try {
         recordIncome(customer, amount);
      } catch (Throwable throwable) {
         LOGGER.error("Failed to record pizzeria income", throwable);
      }
   }

   public static void recordIncome(Customer customer, float amount) {
      if (customer.level().isClientSide) {
         return;
      }
      ChunkSector sector = customer.getSector();
      if (sector == null) {
         return;
      }
      if (customer.level().getPlayerByUUID(sector.getPlayerOwner()) == null) {
         return;
      }
      PizzeriaStats stats = stats(customer.level(), sector);
      stats.incomeToday += amount;
      stats.visitorsToday++;
   }

   public static float qualityScore(Customer customer) {
      if (customer.level().isClientSide) {
         return 0.5F;
      }
      ChunkSector sector = customer.getSector();
      if (sector == null) {
         return 0.5F;
      }
      PizzeriaStats stats = STATS.get(new StatsKey(customer.level().dimension(), sector.sectorID));
      return stats == null || stats.quality == null ? 0.5F : stats.quality.score();
   }

   public static List<Component> statusLines(ServerLevel level, ChunkSector sector) {
      PizzeriaStats stats = stats(level, sector);
      boolean scanning = stats.scanning || !stats.initialScanDone;
      PizzeriaQuality quality = stats.quality;
      int customers = countCustomers(level, sector);
      boolean open = isOpen();
      boolean entrance = !scanning && isEntranceReady(level, sector);

      List<Component> lines = new ArrayList<>();
      if (scanning || quality == null) {
         lines.add(Component.translatable("pizzeria.status.scanning").withStyle(ChatFormatting.YELLOW));
      } else {
         lines.add(
            Component.translatable(
               "pizzeria.status.rating",
               String.format(Locale.ROOT, "%.1f", quality.score() * 5.0F),
               String.format(Locale.ROOT, "%.0f", quality.score() * 100.0F)
            ).withStyle(ChatFormatting.GOLD)
         );
      }

      int cap = quality == null ? 0 : PizzeriaQuality.cap(quality.score(), sector.getChunks().size());
      int interval = quality == null ? 0 : PizzeriaQuality.interval(quality.score());
      long now = level.getGameTime();
      long next = quality == null ? 0L : Math.max(0L, sector.lastCustomerSpawnTick + interval - now);

      lines.add(
         Component.translatable(
            "pizzeria.status.visitors",
            String.valueOf(customers),
            String.valueOf(cap),
            String.valueOf(stats.visitorsToday),
            String.format(Locale.ROOT, "%.2f", stats.incomeToday)
         )
      );
      String time = String.format(Locale.ROOT, "%02d:%02d", WorldTime.getHour(), WorldTime.getMins());
      lines.add(Component.translatable(open ? "pizzeria.status.open" : "pizzeria.status.closed", time));
      lines.add(
         (scanning
            ? Component.translatable("pizzeria.status.entrance_scanning")
            : entrance
               ? Component.translatable("pizzeria.status.entrance_ok", String.valueOf(next / 20L))
               : Component.translatable("pizzeria.status.entrance_missing"))
            .withStyle(scanning ? ChatFormatting.YELLOW : entrance ? ChatFormatting.GREEN : ChatFormatting.RED)
      );

      if (quality != null) {
         lines.add(
            Component.translatable(
               "pizzeria.status.breakdown",
               String.valueOf(quality.arcades()),
               String.valueOf(quality.seats()),
               String.valueOf(quality.vending()),
               String.valueOf(quality.toilets()),
               String.valueOf(quality.animatronics()),
               String.valueOf(quality.trash()),
               String.valueOf(quality.blood()),
               String.valueOf(quality.trashCans()),
               String.valueOf(quality.chunks())
            )
         );
      }
      return lines;
   }

   public static void requestRescan(ServerLevel level, ChunkSector sector) {
      PizzeriaStats stats = stats(level, sector);
      startScan(level, sector, stats, true);
   }

   public static boolean canRescan(ServerLevel level, ChunkSector sector, long now) {
      PizzeriaStats stats = stats(level, sector);
      return !stats.scanning && now - stats.lastRescanTick >= RESCAN_COOLDOWN;
   }

   public static void markRescan(ServerLevel level, ChunkSector sector, long now) {
      stats(level, sector).lastRescanTick = now;
   }

   private static void startScan(ServerLevel level, ChunkSector sector, PizzeriaStats stats, boolean clearPois) {
      stats.scanning = true;
      stats.initialScanDone = false;
      stats.scanSkipped = false;
      stats.scanChunks.clear();
      stats.scanChunks.addAll(sector.getChunks());
      stats.scanChunkIndex = 0;
      stats.scanSectionIndex = 0;
      stats.quality = null;
      if (clearPois) {
         clearPois(sector);
      }
   }

   private static int processRescan(ServerLevel level, ChunkSector sector, PizzeriaStats stats, int budget, long now) {
      int processed = 0;
      while (stats.scanning && processed < budget) {
         if (stats.scanChunkIndex >= stats.scanChunks.size()) {
            stats.scanning = false;
            stats.initialScanDone = !stats.scanSkipped;
            stats.quality = null;
            if (stats.scanSkipped) {
               stats.scanSkipped = false;
               stats.nextScanRetry = now + 200L;
            } else {
               stats.knownChunks.clear();
               stats.knownChunks.addAll(stats.scanChunks);
            }
            return processed;
         }

         ChunkPos chunkPos = stats.scanChunks.get(stats.scanChunkIndex);
         LevelChunk chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
         if (chunk == null) {
            stats.scanSkipped = true;
            stats.scanChunkIndex++;
            stats.scanSectionIndex = 0;
            processed++;
            continue;
         }

         LevelChunkSection[] sections = chunk.getSections();
         if (stats.scanSectionIndex >= sections.length) {
            stats.scanChunkIndex++;
            stats.scanSectionIndex = 0;
            continue;
         }

         LevelChunkSection section = sections[stats.scanSectionIndex];
         int sectionY = level.getMinSection() + stats.scanSectionIndex;
         if (!section.hasOnlyAir()) {
            scanSection(sector, chunkPos, section, sectionY);
         }
         stats.scanSectionIndex++;
         processed++;
      }
      return processed;
   }

   private static void scanSection(ChunkSector sector, ChunkPos chunkPos, LevelChunkSection section, int sectionY) {
      int baseX = chunkPos.getMinBlockX();
      int baseY = sectionY << 4;
      int baseZ = chunkPos.getMinBlockZ();
      Map<BlockState, ChunkPoiType> states = poiStates();

      for (int y = 0; y < 16; y++) {
         for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
               BlockState state = section.getBlockState(x, y, z);
               if (state.isAir()) {
                  continue;
               }
               ChunkPoiType type = states.get(state);
               if (type != null) {
                  sector.poiManager.addPoi(new BlockPos(baseX + x, baseY + y, baseZ + z), type);
               }
            }
         }
      }
   }

   private static Map<BlockState, ChunkPoiType> poiStates() {
      if (poiStates == null) {
         Map<BlockState, ChunkPoiType> states = new HashMap<>();
         for (ChunkPoiType type : MWPois.types()) {
            for (BlockState state : type.validStates()) {
               states.put(state, type);
            }
         }
         states.put(Blocks.BROWN_CARPET.defaultBlockState(), MWPois.TRASH);
         states.put(Blocks.RED_CARPET.defaultBlockState(), MWPois.BLOOD);
         poiStates = states;
      }
      return poiStates;
   }

   private static void clearPois(ChunkSector sector) {
      Set<ChunkPoiType> types = new HashSet<>(MWPois.types());
      types.add(MWPois.TRASH);
      types.add(MWPois.BLOOD);
      for (ChunkPoiType type : types) {
         for (BlockPos pos : new ArrayList<>(sector.poiManager.get(type).validPositions(true))) {
            sector.poiManager.removePoi(pos, type);
         }
      }
   }

   public static boolean isEntranceReady(ServerLevel level, ChunkSector sector) {
      Set<BlockPos> mats = sector.poiManager.get(MWPois.WELCOME_MAT).validPositions(true);
      if (!mats.isEmpty()) {
         boolean open = false;
         boolean safe = false;
         boolean unknown = false;
         boolean unsafe = false;
         for (BlockPos pos : mats) {
            if (!sector.hasChunk(new ChunkPos(pos))) {
               unsafe = true;
               continue;
            }
            if (!level.isLoaded(pos)) {
               unknown = true;
               continue;
            }
            if (!(level.getBlockEntity(pos) instanceof WelcomeMatBlockEntity mat) || !mat.isOpen()) {
               continue;
            }
            open = true;
            if (hasFloor(level, pos)) {
               safe = true;
            } else {
               unsafe = true;
            }
         }
         return open && safe && !unknown && !unsafe;
      }

      boolean safe = false;
      boolean unknown = false;
      boolean unsafe = false;
      for (BlockPos pos : sector.poiManager.get(MWPois.ENTRANCE_SIGN).validPositions(true)) {
         if (!sector.hasChunk(new ChunkPos(pos))) {
            unsafe = true;
            continue;
         }
         if (!level.isLoaded(pos)) {
            unknown = true;
            continue;
         }
         BlockState state = level.getBlockState(pos);
         if (!state.is(MWBlocks.OPEN_NEON_SIGN.get()) || !state.getValue(OpenNeonSignBlock.LIT.get())) {
            unsafe = true;
            continue;
         }
         if (hasFloor(level, pos)) {
            safe = true;
         } else {
            unsafe = true;
         }
      }
      return safe && !unknown && !unsafe;
   }

   private static boolean hasFloor(ServerLevel level, BlockPos pos) {
      for (int i = 1; i <= 64; i++) {
         BlockPos below = pos.below(i);
         if (!level.isLoaded(below)) {
            return false;
         }
         if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
            return true;
         }
      }
      return false;
   }

   private static void forceLeave(ServerLevel level, ChunkSector sector) {
      for (Customer customer : customers(level, sector)) {
         customer.extendStay(-1000000);
      }
   }

   public static void refreshCount(ServerLevel level, ChunkSector sector) {
      reconcileCount(level, sector, countCustomers(level, sector));
   }

   private static int reconcileCount(ServerLevel level, ChunkSector sector, int actual) {
      if (allChunksLoaded(level, sector)) {
         sector.customerCount = actual;
         return actual;
      }
      return Math.max(actual, sector.customerCount);
   }

   private static boolean allChunksLoaded(ServerLevel level, ChunkSector sector) {
      for (ChunkPos chunk : sector.getChunks()) {
         if (!level.isLoaded(new BlockPos(chunk.getMinBlockX(), level.getMinBuildHeight(), chunk.getMinBlockZ()))) {
            return false;
         }
      }
      return true;
   }

   private static int countCustomers(ServerLevel level, ChunkSector sector) {
      return customers(level, sector).size();
   }

   private static List<Customer> customers(ServerLevel level, ChunkSector sector) {
      Set<ChunkPos> chunks = sector.getChunks();
      if (chunks.isEmpty()) {
         return List.of();
      }
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
      AABB box = new AABB(
         minX * 16.0,
         level.getMinBuildHeight(),
         minZ * 16.0,
         (maxX + 1) * 16.0,
         level.getMaxBuildHeight(),
         (maxZ + 1) * 16.0
      );
      return level.getEntitiesOfClass(Customer.class, box, customer -> customer.getSector() == sector);
   }

   private record StatsKey(ResourceKey<Level> dimension, UUID sectorID) {
   }
}
