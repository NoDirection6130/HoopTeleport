package pl.nodirection6130.hoop;

import org.bukkit.Location;

public class HoopSlot {

    public enum Tier {
        EMPTY(0, 0),
        IRON(1, 500),
        GOLD(2, 1000),
        DIAMOND(3, 2000);

        private final int level;
        private final int maxDistance;

        Tier(int level, int maxDistance) {
            this.level = level;
            this.maxDistance = maxDistance;
        }

        public int getLevel() { return level; }
        public int getMaxDistance() { return maxDistance; }
    }

    private Tier tier;
    private Location location; // null if EMPTY
    private String worldName;  // stored separately for cross-world checks

    public HoopSlot() {
        this.tier = Tier.EMPTY;
        this.location = null;
        this.worldName = null;
    }

    public HoopSlot(Tier tier, Location location) {
        this.tier = tier;
        this.location = location;
        this.worldName = location != null ? location.getWorld().getName() : null;
    }

    public Tier getTier() { return tier; }
    public void setTier(Tier tier) { this.tier = tier; }
    public Location getLocation() { return location; }
    public void setLocation(Location location) {
        this.location = location;
        this.worldName = location != null ? location.getWorld().getName() : null;
    }
    public String getWorldName() { return worldName; }

    public boolean isEmpty() { return tier == Tier.EMPTY; }

    /**
     * Returns cost in XP levels to purchase or upgrade this slot.
     * EMPTY -> IRON: 20 lvl
     * IRON -> GOLD: 20 lvl
     * GOLD -> DIAMOND: 30 lvl
     */
    public int getUpgradeCost() {
        return switch (tier) {
            case EMPTY -> 20;
            case IRON  -> 20;
            case GOLD  -> 30;
            default    -> 0;
        };
    }

    public Tier getNextTier() {
        return switch (tier) {
            case EMPTY   -> Tier.IRON;
            case IRON    -> Tier.GOLD;
            case GOLD    -> Tier.DIAMOND;
            case DIAMOND -> Tier.DIAMOND;
        };
    }

    /**
     * Returns a display label for the sign - e.g. "Overworld 100 60 100"
     */
    public String getLabel() {
        if (location == null) return "Empty";
        String dim = switch (worldName) {
            case "world_nether" -> "Nether";
            case "world_the_end" -> "The End";
            default -> "Overworld";
        };
        return dim + " " + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ();
    }
}
