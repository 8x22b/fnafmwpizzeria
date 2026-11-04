package com.fork.fnafmw.pizzeria;

import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.context.UseOnContext;

public class PizzeriaDeedItem extends Item {
   private final PizzeriaDeedType type;

   public PizzeriaDeedItem(PizzeriaDeedType type) {
      super(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
      this.type = type;
   }

   @Override
   public InteractionResult useOn(UseOnContext context) {
      if (!(context.getLevel() instanceof ServerLevel level) || context.getPlayer() == null) {
         return InteractionResult.SUCCESS;
      }
      PizzeriaDeedType.Placement placement = this.type.placement(context.getPlayer().getDirection());
      boolean placed = DeedPlacement.place(
         level,
         context.getClickedPos(),
         placement,
         this.type,
         context.getPlayer()
      );
      context.getPlayer().displayClientMessage(
         Component.translatable(placed ? "pizzeria.command.place_success" : "pizzeria.command.place_fail"), false
      );
      if (placed && !context.getPlayer().isCreative()) {
         context.getItemInHand().shrink(1);
      }
      return placed ? InteractionResult.SUCCESS : InteractionResult.FAIL;
   }

   @Override
   public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
      tooltip.add(Component.translatable("tooltip.fnafmwpizzeria.deed").withStyle(ChatFormatting.GRAY));
   }
}

