package com.fork.fnafmw.pizzeria.mixin;

import com.fork.fnafmw.pizzeria.PizzeriaManager;
import net.ovdr.fnafmw.level.entity.customer.Customer;
import net.ovdr.fnafmw.level.entity.customer.payment.PaymentCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PaymentCalculator.class)
public class PaymentCalculatorMixin {
   @Inject(method = "applyToOwner(Lnet/ovdr/fnafmw/level/entity/customer/Customer;F)V", at = @At("HEAD"))
   private static void fnafmwpizzeria$recordIncome(Customer customer, float amount, CallbackInfo callback) {
      PizzeriaManager.recordIncomeSafe(customer, amount);
   }
}
