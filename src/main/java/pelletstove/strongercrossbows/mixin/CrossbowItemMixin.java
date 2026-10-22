package pelletstove.strongercrossbows.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import pelletstove.strongercrossbows.BuffedArrow;

@Mixin(CrossbowItem.class)
public class CrossbowItemMixin {
	@Inject(method = "createProjectile", at = @At("RETURN"))
	private void strongerCrossbows$markArrow(net.minecraft.world.level.Level level, LivingEntity shooter, net.minecraft.world.item.ItemStack weapon,
			net.minecraft.world.item.ItemStack ammo, boolean isCrit, CallbackInfoReturnable<Projectile> cir) {
		if (shooter instanceof Player && cir.getReturnValue() instanceof AbstractArrow arrow) {
			((BuffedArrow) arrow).strongerCrossbows$setBuffed(true);
		}
	}
}
