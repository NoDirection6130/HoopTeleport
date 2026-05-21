package pl.nodirection6130.hoop;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoopManager {

    private final HoopPlugin plugin;
    private final Map<UUID, PlayerHoop> hoops = new HashMap<>();
    private File dataFile;
    private FileConfiguration dataConfig;

    public HoopManager(HoopPlugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public PlayerHoop getHoop(UUID uuid) {
        return hoops.computeIfAbsent(uuid, PlayerHoop::new);
    }

    public void saveData() {
        dataConfig.set("hoops", null);
        for (Map.Entry<UUID, PlayerHoop> entry : hoops.entrySet()) {
            String base = "hoops." + entry.getKey() + ".";
            PlayerHoop ph = entry.getValue();
            for (int i = 0; i < 3; i++) {
                HoopSlot slot = ph.getSlot(i);
                String sp = base + "slot" + i + ".";
                dataConfig.set(sp + "tier", slot.getTier().name());
                if (!slot.isEmpty() && slot.getLocation() != null) {
                    Location loc = slot.getLocation();
                    dataConfig.set(sp + "world", loc.getWorld().getName());
                    dataConfig.set(sp + "x", loc.getX());
                    dataConfig.set(sp + "y", loc.getY());
                    dataConfig.set(sp + "z", loc.getZ());
                    dataConfig.set(sp + "yaw", (double) loc.getYaw());
                    dataConfig.set(sp + "pitch", (double) loc.getPitch());
                }
            }
        }
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "hoops.yml");
        if (!dataFile.exists()) {
            plugin.getDataFolder().mkdirs();
            try { dataFile.createNewFile(); } catch (IOException e) { e.printStackTrace(); }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);

        if (!dataConfig.contains("hoops")) return;

        for (String uuidStr : dataConfig.getConfigurationSection("hoops").getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(uuidStr);
                PlayerHoop ph = new PlayerHoop(uuid);
                for (int i = 0; i < 3; i++) {
                    String sp = "hoops." + uuidStr + ".slot" + i + ".";
                    if (!dataConfig.contains(sp + "tier")) continue;
                    HoopSlot.Tier tier = HoopSlot.Tier.valueOf(dataConfig.getString(sp + "tier", "EMPTY"));
                    if (tier == HoopSlot.Tier.EMPTY) {
                        ph.setSlot(i, new HoopSlot());
                        continue;
                    }
                    String worldName = dataConfig.getString(sp + "world");
                    World world = Bukkit.getWorld(worldName);
                    if (world == null) {
                        ph.setSlot(i, new HoopSlot());
                        continue;
                    }
                    double x = dataConfig.getDouble(sp + "x");
                    double y = dataConfig.getDouble(sp + "y");
                    double z = dataConfig.getDouble(sp + "z");
                    float yaw = (float) dataConfig.getDouble(sp + "yaw");
                    float pitch = (float) dataConfig.getDouble(sp + "pitch");
                    Location loc = new Location(world, x, y, z, yaw, pitch);
                    ph.setSlot(i, new HoopSlot(tier, loc));
                }
                hoops.put(uuid, ph);
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to load hoop for " + uuidStr + ": " + e.getMessage());
            }
        }
        plugin.getLogger().info("Loaded " + hoops.size() + " player hoops.");
    }
}
