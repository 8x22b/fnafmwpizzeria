package com.fork.fnafmw.pizzeria;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

@Mod(PizzeriaMod.MODID)
public final class PizzeriaMod {
   public static final String MODID = "fnafmwpizzeria";
   public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
   public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
   public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registries.STRUCTURE_TYPE, MODID);
   public static final DeferredHolder<StructureType<?>, StructureType<PizzeriaTemplateStructure>> TEMPLATE_STRUCTURE = STRUCTURE_TYPES.register(
      "templates", () -> () -> PizzeriaTemplateStructure.CODEC
   );
   public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registries.STRUCTURE_PIECE, MODID);
   public static final DeferredHolder<StructurePieceType, StructurePieceType> TEMPLATE_PIECE = PIECE_TYPES.register(
      "template", () -> (context, tag) -> new PizzeriaTemplatePiece(tag, context.structureTemplateManager())
   );
   public static final DeferredItem<Item> DISCOUNT_DINER_DEED = ITEMS.register(
      PizzeriaDeedType.DISCOUNT_DINER.itemId(), () -> new PizzeriaDeedItem(PizzeriaDeedType.DISCOUNT_DINER)
   );
   public static final DeferredItem<Item> FREDBEARS_DEED = ITEMS.register(
      PizzeriaDeedType.FREDBEARS_FAMILY_DINER.itemId(), () -> new PizzeriaDeedItem(PizzeriaDeedType.FREDBEARS_FAMILY_DINER)
   );
   public static final DeferredItem<Item> JEFFS_PIZZA_DEED = ITEMS.register(
      PizzeriaDeedType.JEFFS_PIZZA.itemId(), () -> new PizzeriaDeedItem(PizzeriaDeedType.JEFFS_PIZZA)
   );
   public static final DeferredItem<Item> FNAF_ONE_DEED = ITEMS.register(
      PizzeriaDeedType.FNAF_ONE.itemId(), () -> new PizzeriaDeedItem(PizzeriaDeedType.FNAF_ONE)
   );
   public static final DeferredItem<Item> FNAF_TWO_DEED = ITEMS.register(
      PizzeriaDeedType.FNAF_TWO.itemId(), () -> new PizzeriaDeedItem(PizzeriaDeedType.FNAF_TWO)
   );
   public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DEEDS_TAB = TABS.register(
      "deeds",
      () -> CreativeModeTab.builder()
         .title(Component.translatable("itemGroup.fnafmwpizzeria.deeds"))
         .icon(() -> new ItemStack(DISCOUNT_DINER_DEED.get()))
         .displayItems((parameters, output) -> {
            output.accept(DISCOUNT_DINER_DEED.get());
            output.accept(FREDBEARS_DEED.get());
            output.accept(JEFFS_PIZZA_DEED.get());
            output.accept(FNAF_ONE_DEED.get());
            output.accept(FNAF_TWO_DEED.get());
         })
         .build()
   );

   public PizzeriaMod(IEventBus modBus, ModContainer container) {
      ITEMS.register(modBus);
      TABS.register(modBus);
      STRUCTURE_TYPES.register(modBus);
      PIECE_TYPES.register(modBus);
      modBus.addListener(PizzeriaShopIntegration::commonSetup);
      NeoForge.EVENT_BUS.register(PizzeriaEvents.class);
      PizzeriaQualityModifier.register();
   }
}
