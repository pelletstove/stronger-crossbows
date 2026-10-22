package pelletstove.strongercrossbows.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import pelletstove.strongercrossbows.BuffedArrow;
import pelletstove.strongercrossbows.StrongerCrossbows;

@Mixin(AbstractArrow.class)
public class AbstractArrowMixin implements BuffedArrow {
	@Unique
	private boolean strongerCrossbows$buffed;

	@Override
	public boolean strongerCrossbows$isBuffed() {
		return strongerCrossbows$buffed;
	}

	@Override
	public void strongerCrossbows$setBuffed(boolean buffed) {
		strongerCrossbows$buffed = buffed;
	}

	@WrapOperation(method = "onHitEntity", at = @At(value = "INVOKE",
			target = "Lnet/minecraft/world/item/enchantment/EnchantmentHelper;modifyDamage(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/damagesource/DamageSource;F)F"))
	private float strongerCrossbows$bonus(ServerLevel level, ItemStack weapon, Entity victim, DamageSource source, float damage, Operation<Float> original) {
		float result = original.call(level, weapon, victim, source, damage);
		return strongerCrossbows$buffed ? result + StrongerCrossbows.BONUS_DAMAGE : result;
	}
}
