package com.fork.fnafmw.pizzeria;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class PizzeriaLoot {
   private static final List<LootEntry> POOL = List.of(
      new LootEntry("fnafmw:fazcoin", 20, 2, 6),
      new LootEntry("fnafmw:fazbuck", 8, 1, 2),
      new LootEntry("minecraft:cobweb", 15, 1, 3),
      new LootEntry("minecraft:string", 15, 1, 4),
      new LootEntry("minecraft:paper", 12, 1, 4),
      new LootEntry("minecraft:iron_nugget", 10, 2, 8),
      new LootEntry("minecraft:gold_nugget", 8, 1, 4),
      new LootEntry("minecraft:emerald", 6, 1, 2),
      new LootEntry("minecraft:diamond", 2, 1, 1),
      new LootEntry("minecraft:name_tag", 2, 1, 1),
      new LootEntry("fnafmw:freddy_head", 1, 1, 1),
      new LootEntry("fnafmw:bonnie_mask", 1, 1, 1)
   );

   private PizzeriaLoot() {
   }

   public static void fillLocker(Container locker, RandomSource random) {
      int count = 3 + random.nextInt(4);
      Set<Integer> slots = new HashSet<>();
      for (int i = 0; i < count; i++) {
         int slot;
         do {
            slot = random.nextInt(27);
         } while (!slots.add(slot));

         LootEntry entry = pick(random);
         Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(entry.id));
         if (item != Items.AIR) {
            int amount = entry.min + random.nextInt(entry.max - entry.min + 1);
            locker.setItem(slot, new ItemStack(item, amount));
         }
      }
      locker.setChanged();
   }

   private static LootEntry pick(RandomSource random) {
      int total = 0;
      for (LootEntry entry : POOL) {
         total += entry.weight;
      }
      int roll = random.nextInt(total);
      for (LootEntry entry : POOL) {
         if (roll < entry.weight) {
            return entry;
         }
         roll -= entry.weight;
      }
      return POOL.get(0);
   }

   private record LootEntry(String id, int weight, int min, int max) {
   }
}
