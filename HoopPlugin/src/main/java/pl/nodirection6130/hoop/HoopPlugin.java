package pl.nodirection6130.hoop;

import org.bukkit.plugin.java.JavaPlugin;

public class HoopPlugin extends JavaPlugin {

    private HoopManager manager;
    private HoopListener hoopListener;

    @Override
    public void onEnable() {
        manager = new HoopManager(this);
        hoopListener = new HoopListener(this, manager);
        getServer().getPluginManager().registerEvents(hoopListener, this);
        getCommand("hoop").setExecutor(new HoopCommand(hoopListener));
        getLogger().info("Hoop enabled.");
    }

    @Override
    public void onDisable() {
        if (manager != null) manager.saveData();
        getLogger().info("Hoop disabled.");
    }
}
