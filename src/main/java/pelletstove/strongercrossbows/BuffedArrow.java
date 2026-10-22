package pelletstove.strongercrossbows;

/** Implemented by AbstractArrow through a mixin: marks an arrow that a player fired from a crossbow. */
public interface BuffedArrow {
	boolean strongerCrossbows$isBuffed();

	void strongerCrossbows$setBuffed(boolean buffed);
}
