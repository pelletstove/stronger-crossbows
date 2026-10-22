package pelletstove.strongercrossbows;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StrongerCrossbows implements ModInitializer {
	public static final String MOD_ID = "stronger-crossbows";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	/** Added to base damage before the speed multiplier. Power IV on a bow adds 1.0 + 0.5 * 3 damage to an arrow hit (see data/minecraft/enchantment/power.json). */
	public static final float BONUS_DAMAGE = 2.5F;

	@Override
	public void onInitialize() {
		LOGGER.info("Stronger Crossbows loaded: player crossbow arrows get +{} base damage (Power IV)", BONUS_DAMAGE);
	}
}
