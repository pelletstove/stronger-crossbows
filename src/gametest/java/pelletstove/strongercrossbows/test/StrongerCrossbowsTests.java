package pelletstove.strongercrossbows.test;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import pelletstove.strongercrossbows.BuffedArrow;

import java.util.List;
import java.util.Set;

@SuppressWarnings("removal")
public class StrongerCrossbowsTests {
	private static final float SPEED = 3.15F;

	private static void floor(GameTestHelper helper) {
		// the test area can straddle chunks and entities only tick in ticking chunks, so force-load every chunk it touches
		for (int x = -4; x <= 12; x += 16) {
			for (int z = -6; z <= 6; z += 12) {
				var pos = helper.absolutePos(new BlockPos(x, 1, z));
				helper.getLevel().setChunkForced(pos.getX() >> 4, pos.getZ() >> 4, true);
			}
		}
		for (int x = 0; x < 10; x++) {
			for (int z = -3; z <= 3; z++) {
				helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
			}
		}
	}

	private static ItemStack loadedCrossbow() {
		ItemStack crossbow = new ItemStack(Items.CROSSBOW);
		crossbow.set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new net.minecraft.world.item.ItemStackTemplate(Items.ARROW)));
		return crossbow;
	}

	/** Faces +x (yaw -90) at the given relative position. */
	private static ServerPlayer playerAt(GameTestHelper helper, double x, double y, double z) {
		ServerPlayer player = helper.makeMockServerPlayerInLevel();
		Vec3 at = helper.absoluteVec(new Vec3(x, y, z));
		player.teleportTo(helper.getLevel(), at.x, at.y, at.z, Set.of(), -90f, 0f, true);
		return player;
	}

	private static Mob mobAt(GameTestHelper helper, EntityType<? extends Mob> type, double x, double y, double z) {
		Mob mob = (Mob) helper.spawnWithNoFreeWill(type, new Vec3(x, y, z));
		mob.setYRot(-90f);
		mob.setYHeadRot(-90f);
		return mob;
	}

	private static void fire(GameTestHelper helper, LivingEntity shooter) {
		((CrossbowItem) Items.CROSSBOW).performShooting(helper.getLevel(), shooter, InteractionHand.MAIN_HAND, loadedCrossbow(), SPEED, 0f, null);
	}

	private static List<AbstractArrow> arrowsNear(GameTestHelper helper, Vec3 relative, double radius) {
		Vec3 at = helper.absoluteVec(relative);
		return helper.getLevel().getEntitiesOfClass(AbstractArrow.class, new AABB(at, at).inflate(radius));
	}

	@GameTest(padding = 8)
	public void playerCrossbowArrowIsBuffed(GameTestHelper helper) {
		floor(helper);
		ServerPlayer player = playerAt(helper, 1.5, 2, 0.5);
		fire(helper, player);
		var arrows = arrowsNear(helper, new Vec3(1.5, 3.5, 0.5), 3);
		helper.assertTrue(arrows.size() == 1, "expected one arrow, found " + arrows.size());
		helper.assertTrue(((BuffedArrow) arrows.get(0)).strongerCrossbows$isBuffed(), "a player's crossbow arrow should be buffed");
		helper.succeed();
	}

	@GameTest(padding = 8)
	public void mobCrossbowArrowIsNotBuffed(GameTestHelper helper) {
		floor(helper);
		Mob pillager = mobAt(helper, EntityTypes.PILLAGER, 1.5, 2, 0.5);
		fire(helper, pillager);
		var arrows = arrowsNear(helper, new Vec3(1.5, 3.5, 0.5), 3);
		helper.assertTrue(arrows.size() == 1, "expected one arrow, found " + arrows.size());
		helper.assertFalse(((BuffedArrow) arrows.get(0)).strongerCrossbows$isBuffed(), "a pillager's arrow must not be buffed");
		helper.succeed();
	}

	@GameTest(padding = 8)
	public void bowArrowIsNotBuffed(GameTestHelper helper) {
		floor(helper);
		var arrow = EntityTypes.ARROW.create(helper.getLevel(), net.minecraft.world.entity.EntitySpawnReason.COMMAND);
		helper.assertTrue(arrow != null, "arrow should be creatable");
		helper.assertFalse(((BuffedArrow) arrow).strongerCrossbows$isBuffed(), "a fresh arrow must not be buffed");
		helper.succeed();
	}

	/**
	 * Same shot, same distance, same victim type, one arrow with the mark removed. Like Power IV the bonus is added to the
	 * arrow's base damage (2.0 -> 4.5) before the speed multiplier (about 3.15), so the hit is about 7.9 harder.
	 */
	@GameTest(padding = 8, maxTicks = 200)
	public void buffedArrowHurtsMore(GameTestHelper helper) {
		floor(helper);
		ServerPlayer player = playerAt(helper, 1.5, 2, -1.5);
		ServerPlayer controlShooter = playerAt(helper, 1.5, 2, 1.5);
		Mob targetA = mobAt(helper, EntityTypes.HUSK, 6.5, 2, -1.5);
		Mob targetB = mobAt(helper, EntityTypes.HUSK, 6.5, 2, 1.5);
		for (Mob m : List.of(targetA, targetB)) {
			m.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200);
			m.setHealth(200);
		}
		// aim at body height
		player.setXRot(0f);
		helper.runAfterDelay(2, () -> {
			fire(helper, player);
			fire(helper, controlShooter);
			// fully charged crossbow arrows are crits, which add a random amount; turn that off so the damage is exact
			for (AbstractArrow arrow : arrowsNear(helper, new Vec3(1.5, 3, 0), 6)) {
				arrow.setCritArrow(false);
			}
			// the control arrow: same shot, but with the mod's mark removed
			var control = arrowsNear(helper, new Vec3(1.5, 3, 1.5), 1);
			helper.assertTrue(control.size() == 1, "expected one control arrow, found " + control.size());
			((BuffedArrow) control.get(0)).strongerCrossbows$setBuffed(false);
		});
		helper.succeedWhen(() -> {
			float lostA = 200 - targetA.getHealth();
			float lostB = 200 - targetB.getHealth();
			helper.assertTrue(lostA > 0 && lostB > 0, "both zombies should be hit (lost " + lostA + " and " + lostB + ")");
			helper.assertTrue(lostA - lostB >= 7f, "buffed arrow should do about 7.9 more damage: " + lostA + " vs " + lostB);
			helper.assertTrue(lostA - lostB <= 9f, "bonus should be about 7.9, not more: " + lostA + " vs " + lostB);
		});
	}
}
