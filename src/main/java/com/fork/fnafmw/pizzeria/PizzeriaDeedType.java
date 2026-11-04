package com.fork.fnafmw.pizzeria;

import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Rotation;

public enum PizzeriaDeedType {
   DISCOUNT_DINER(
      "discount_diner_deed",
      "discountdiner",
      new Placement(-14, -29, Rotation.NONE),
      new Placement(29, -14, Rotation.CLOCKWISE_90),
      new Placement(14, 29, Rotation.CLOCKWISE_180),
      new Placement(-29, 14, Rotation.COUNTERCLOCKWISE_90)
   ),
   FREDBEARS_FAMILY_DINER(
      "fredbears_family_diner_deed",
      "fredbears",
      new Placement(-14, -25, Rotation.NONE),
      new Placement(25, -14, Rotation.CLOCKWISE_90),
      new Placement(14, 25, Rotation.CLOCKWISE_180),
      new Placement(-25, 14, Rotation.COUNTERCLOCKWISE_90)
   ),
   JEFFS_PIZZA(
      "jeffs_pizza_deed",
      "jeffspizza",
      new Placement(21, -32, Rotation.CLOCKWISE_90),
      new Placement(32, 21, Rotation.CLOCKWISE_180),
      new Placement(-21, 32, Rotation.COUNTERCLOCKWISE_90),
      new Placement(-32, -21, Rotation.NONE)
   ),
   FNAF_ONE(
      "freddy_fazbears_pizza_deed",
      "fnaf1",
      new Placement(-18, -33, Rotation.NONE),
      new Placement(33, -18, Rotation.CLOCKWISE_90),
      new Placement(18, 33, Rotation.CLOCKWISE_180),
      new Placement(-33, 18, Rotation.COUNTERCLOCKWISE_90)
   ),
   FNAF_TWO(
      "grand_reopening_deed",
      "fnaf2",
      new Placement(-15, -39, Rotation.NONE),
      new Placement(39, -15, Rotation.CLOCKWISE_90),
      new Placement(15, 39, Rotation.CLOCKWISE_180),
      new Placement(-39, 15, Rotation.COUNTERCLOCKWISE_90)
   );

   private final String itemId;
   private final String structure;
   private final Map<Direction, Placement> placements;

   PizzeriaDeedType(String itemId, String structure, Placement north, Placement east, Placement south, Placement west) {
      this.itemId = itemId;
      this.structure = structure;
      this.placements = Map.of(Direction.NORTH, north, Direction.EAST, east, Direction.SOUTH, south, Direction.WEST, west);
   }

   public String itemId() {
      return this.itemId;
   }

   public String structure() {
      return this.structure;
   }

   public Placement placement(Direction direction) {
      return this.placements.getOrDefault(direction, this.placements.get(Direction.NORTH));
   }

   public record Placement(int dx, int dz, Rotation rotation) {
   }
}
