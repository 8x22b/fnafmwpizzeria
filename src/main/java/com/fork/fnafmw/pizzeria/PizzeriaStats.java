package com.fork.fnafmw.pizzeria;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.world.level.ChunkPos;

public final class PizzeriaStats {
   public PizzeriaQuality quality;
   public long qualityTick = Long.MIN_VALUE;
   public long lastResetDay = Long.MIN_VALUE;
   public float incomeToday;
   public int visitorsToday;
   public boolean closed;
   public boolean initialScanDone;
   public boolean scanning;
   public final List<ChunkPos> scanChunks = new ArrayList<>();
   public final Set<ChunkPos> knownChunks = new HashSet<>();
   public int scanChunkIndex;
   public int scanSectionIndex;
   public boolean scanSkipped;
   public long nextScanRetry;
   public long lastRescanTick;
}
