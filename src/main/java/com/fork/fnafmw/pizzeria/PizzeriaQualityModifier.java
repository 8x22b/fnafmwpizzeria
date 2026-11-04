package com.fork.fnafmw.pizzeria;

import net.ovdr.fnafmw.level.entity.customer.Customer;
import net.ovdr.fnafmw.level.entity.customer.payment.CustomerPayment;
import net.ovdr.fnafmw.level.entity.customer.payment.PaymentCalculator;
import net.ovdr.fnafmw.level.entity.customer.payment.TipModifier;

public final class PizzeriaQualityModifier implements TipModifier {
   private static boolean registered;

   private PizzeriaQualityModifier() {
   }

   public static void register() {
      if (!registered) {
         registered = true;
         PaymentCalculator.registerModifier(new PizzeriaQualityModifier());
      }
   }

   @Override
   public float calculate(Customer customer) {
      return (PizzeriaManager.qualityScore(customer) - 0.5F) * 0.4F;
   }

   @Override
   public void handleBonus(CustomerPayment payment, float contribution) {
      payment.addMoodBonus(contribution);
   }

   @Override
   public String name() {
      return "Pizzeria Quality";
   }
}
