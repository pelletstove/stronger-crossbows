# Stronger Crossbows

Replacement for the deprecated Stronger Crossbows datapack, as a Fabric mod for Minecraft 26.2.

Arrows a **player** fires from a crossbow get +2.5 base damage, exactly what Power IV gives a bow arrow
(`1.0 + 0.5 * 3`, see `data/minecraft/enchantment/power.json`). As with Power, the bonus is added to the arrow's base damage (2.0 -> 4.5)
before the speed multiplier, so a full-speed crossbow arrow hits about 8 harder (7 -> 15 on an unarmoured target). Mobs with crossbows (pillagers) are not buffed.
Works alongside real Power enchantments. Fireworks are not affected.

Two mixins: `CrossbowItem.createProjectile` marks the arrow, `AbstractArrow.onHitEntity` adds the bonus to the result of
`EnchantmentHelper.modifyDamage`, i.e. next to where Power applies. The mark is not saved, so an arrow in flight when the world is saved loses the bonus.
