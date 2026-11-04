package com.fork.fnafmw.pizzeria;

import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.ovdr.fnafmw.server.TerminalData;

public final class PizzeriaShopIntegration {
   private static final List<String> SHOP_BLOCKS = List.of(
      "fnafmw:classic_chair",
      "fnafmw:restaurant_chair",
      "fnafmw:party_table",
      "fnafmw:classic_desk",
      "fnafmw:terminal",
      "fnafmw:locker",
      "fnafmw:dumpster",
      "fnafmw:freddy_arcade_cabinet",
      "fnafmw:bonnie_arcade_cabinet",
      "fnafmw:chica_arcade_cabinet",
      "fnafmw:foxy_arcade_cabinet",
      "fnafmw:classic_stage",
      "fnafmw:checkered_stage",
      "fnafmw:red_sturdy_stage",
      "fnafmw:black_white_tiles",
      "fnafmw:black_white_tiles_diagonal",
      "fnafmw:ceiling_tile_gray",
      "fnafmw:ceiling_tile_black",
      "fnafmw:classic_pizzeria_wall",
      "fnafmw:classic_pizzeria_wall_bottom",
      "fnafmw:classic_pizzeria_wall_top",
      "fnafmw:brown_facade_bricks",
      "fnafmw:tan_facade_bricks",
      "fnafmw:blue_facade_wall",
      "fnafmw:red_facade_wall",
      "fnafmw:black_facade_wall",
      "fnafmw:window",
      "fnafmw:employee_door",
      "fnafmw:parts_service_door",
      "fnafmw:entrance_door",
      "fnafmw:big_employee_door",
      "fnafmw:big_parts_service_door",
      "fnafmw:open_neon_sign",
      "fnafmw:restrooms_neon_sign",
      "fnafmw:classic_freddy_poster",
      "fnafmw:classic_bonnie_poster",
      "fnafmw:classic_chica_poster",
      "fnafmw:classic_foxy_poster",
      "fnafmw:freddy_fazbear_pizza_sign",
      "fnafmw:gumball_machine",
      "fnafmw:cola_vending_machine",
      "fnafmw:balloon_bundle"
   );

   private PizzeriaShopIntegration() {
   }

   public static void commonSetup(FMLCommonSetupEvent event) {
      event.enqueueWork(() -> {
         TerminalData.Category<Item> blocks = TerminalData.EXAMPLE_BLOCKS;
         if (blocks == null) {
            return;
         }

         blocks.addItem(() -> PizzeriaMod.DISCOUNT_DINER_DEED.get(), 5000, Component.translatable(PizzeriaMod.DISCOUNT_DINER_DEED.get().getDescriptionId()));
         blocks.addItem(() -> PizzeriaMod.FREDBEARS_DEED.get(), 7500, Component.translatable(PizzeriaMod.FREDBEARS_DEED.get().getDescriptionId()));
         blocks.addItem(() -> PizzeriaMod.JEFFS_PIZZA_DEED.get(), 10000, Component.translatable(PizzeriaMod.JEFFS_PIZZA_DEED.get().getDescriptionId()));
         blocks.addItem(() -> PizzeriaMod.FNAF_ONE_DEED.get(), 15000, Component.translatable(PizzeriaMod.FNAF_ONE_DEED.get().getDescriptionId()));
         blocks.addItem(() -> PizzeriaMod.FNAF_TWO_DEED.get(), 20000, Component.translatable(PizzeriaMod.FNAF_TWO_DEED.get().getDescriptionId()));

         for (String id : SHOP_BLOCKS) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(id));
            if (item != Items.AIR) {
               blocks.addItem(() -> item, 500, Component.translatable(item.getDescriptionId()));
            }
         }
      });
   }
}
