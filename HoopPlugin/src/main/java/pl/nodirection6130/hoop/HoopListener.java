package pl.nodirection6130.hoop;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HoopListener implements Listener {

    private final HoopPlugin plugin;
    private final HoopManager manager;

    // Tracks which player is viewing whose GUI (viewer UUID -> owner UUID)
    // Persists across both main and upgrade GUI
    private final Map<UUID, UUID> openGUI = new HashMap<>();
    // Tracks delete mode per viewer
    private final Map<UUID, Boolean> deleteMode = new HashMap<>();
    // Tracks pending upgrade: viewer UUID -> slot index
    private final Map<UUID, Integer> pendingUpgrade = new HashMap<>();
    // Tracks teleport countdown tasks
    private final Map<UUID, BukkitTask> teleportTasks = new HashMap<>();
    // Tracks position at teleport start for movement cancel
    private final Map<UUID, Location> teleportStartPos = new HashMap<>();
    // Tracks destination for teleport
    private final Map<UUID, Location> teleportDest = new HashMap<>();
    // Tracks whether inventory close should be ignored (re-opening)
    private final Map<UUID, Boolean> suppressClose = new HashMap<>();

    public HoopListener(HoopPlugin plugin, HoopManager manager) {
        this.plugin = plugin;
        this.manager = manager;
    }

    // -----------------------------------------------------------------------
    // Open GUI
    // -----------------------------------------------------------------------
    public void openHoopGUI(Player viewer, UUID ownerUUID) {
        PlayerHoop ph = manager.getHoop(ownerUUID);
        boolean dm = deleteMode.getOrDefault(viewer.getUniqueId(), false);
        Inventory inv = HoopGUI.buildMainGUI(ph, dm);
        openGUI.put(viewer.getUniqueId(), ownerUUID);
        suppressClose.put(viewer.getUniqueId(), true);
        viewer.openInventory(inv);
    }

    private void openUpgradeGUI(Player viewer, HoopSlot slot, int slotIndex) {
        pendingUpgrade.put(viewer.getUniqueId(), slotIndex);
        suppressClose.put(viewer.getUniqueId(), true);
        viewer.openInventory(HoopGUI.buildUpgradeGUI(slot));
    }

    // -----------------------------------------------------------------------
    // Inventory click
    // -----------------------------------------------------------------------
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        UUID viewerUUID = player.getUniqueId();

        String title = event.getView().getTitle();

        // ---- Main GUI ----
        if (title.equals(HoopGUI.MAIN_GUI_TITLE) && openGUI.containsKey(viewerUUID)) {
            // Block ALL clicks including player inventory - prevent item theft
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;

            // Only process clicks in the top inventory (GUI), not player inventory
            if (event.getClickedInventory() == null) return;
            if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

            UUID ownerUUID = openGUI.get(viewerUUID);
            PlayerHoop ph = manager.getHoop(ownerUUID);
            int rawSlot = event.getRawSlot();
            boolean dm = deleteMode.getOrDefault(viewerUUID, false);

            // Barrier/Skull slot
            if (rawSlot == HoopGUI.BARRIER_SLOT) {
                boolean currentDM = deleteMode.getOrDefault(viewerUUID, false);
                deleteMode.put(viewerUUID, !currentDM);
                openHoopGUI(player, ownerUUID);
                if (!currentDM) {
                    player.sendMessage(ChatColor.RED + "[Hoop] Delete mode activated. Click a teleport to remove it. Close GUI to cancel.");
                } else {
                    player.sendMessage(ChatColor.YELLOW + "[Hoop] Delete mode deactivated.");
                }
                return;
            }

            // Teleport slot
            int slotIndex = -1;
            for (int i = 0; i < HoopGUI.TELEPORT_SLOTS.length; i++) {
                if (HoopGUI.TELEPORT_SLOTS[i] == rawSlot) { slotIndex = i; break; }
            }
            if (slotIndex == -1) return;

            HoopSlot slot = ph.getSlot(slotIndex);

            // Delete mode
            if (dm) {
                if (!slot.isEmpty()) {
                    ph.setSlot(slotIndex, new HoopSlot());
                    manager.saveData();
                    player.sendMessage(ChatColor.YELLOW + "[Hoop] Teleport removed.");
                } else {
                    player.sendMessage(ChatColor.GRAY + "[Hoop] This slot is already empty.");
                }
                deleteMode.put(viewerUUID, false);
                openHoopGUI(player, ownerUUID);
                return;
            }

            // Empty slot - buy (right click only)
            if (slot.isEmpty()) {
                if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
                    Player owner = Bukkit.getPlayer(ownerUUID);
                    if (owner == null) {
                        player.sendMessage(ChatColor.RED + "[Hoop] The owner must be online to set a teleport.");
                        return;
                    }
                    if (player.getLevel() < 20) {
                        player.sendMessage(ChatColor.RED + "[Hoop] You need at least 20 XP levels to buy a teleport slot. You have: " + player.getLevel() + " levels.");
                        return;
                    }
                    player.setLevel(player.getLevel() - 20);
                    Location loc = owner.getLocation().clone();
                    HoopSlot newSlot = new HoopSlot(HoopSlot.Tier.IRON, loc);
                    ph.setSlot(slotIndex, newSlot);
                    manager.saveData();
                    player.sendMessage(ChatColor.GREEN + "[Hoop] Iron Teleport purchased at " + newSlot.getLabel() + "!");
                    openHoopGUI(player, ownerUUID);
                }
                return;
            }

            // Occupied slot - left click = teleport, right click = upgrade
            if (event.getClick() == ClickType.LEFT || event.getClick() == ClickType.SHIFT_LEFT) {
                suppressClose.put(viewerUUID, true);
                player.closeInventory();
                startTeleport(player, slot);
            } else if (event.getClick() == ClickType.RIGHT || event.getClick() == ClickType.SHIFT_RIGHT) {
                if (slot.getTier() == HoopSlot.Tier.DIAMOND) {
                    player.sendMessage(ChatColor.YELLOW + "[Hoop] This teleport is already at max tier (Diamond).");
                    return;
                }
                openUpgradeGUI(player, slot, slotIndex);
            }
            return;
        }

        // ---- Upgrade GUI ----
        if (title.equals(HoopGUI.UPGRADE_GUI_TITLE) && pendingUpgrade.containsKey(viewerUUID)) {
            // Block ALL clicks
            event.setCancelled(true);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
            if (event.getClickedInventory() == null) return;
            if (!event.getClickedInventory().equals(event.getView().getTopInventory())) return;

            int rawSlot = event.getRawSlot();
            UUID ownerUUID = openGUI.get(viewerUUID);
            if (ownerUUID == null) {
                player.sendMessage(ChatColor.RED + "[Hoop] Something went wrong. Please reopen /hoop.");
                player.closeInventory();
                pendingUpgrade.remove(viewerUUID);
                return;
            }

            PlayerHoop ph = manager.getHoop(ownerUUID);
            int slotIndex = pendingUpgrade.get(viewerUUID);
            HoopSlot slot = ph.getSlot(slotIndex);

            if (rawSlot == 2) { // Green wool - Yes
                int cost = slot.getUpgradeCost();
                if (player.getLevel() < cost) {
                    player.sendMessage(ChatColor.RED + "[Hoop] Not enough XP levels to upgrade. You need: " + cost + " levels. You have: " + player.getLevel() + " levels.");
                    pendingUpgrade.remove(viewerUUID);
                    openHoopGUI(player, ownerUUID);
                    return;
                }
                HoopSlot.Tier nextTier = slot.getNextTier();
                String tierName = switch (nextTier) {
                    case GOLD    -> "Gold";
                    case DIAMOND -> "Diamond";
                    default      -> nextTier.name();
                };
                player.setLevel(player.getLevel() - cost);
                slot.setTier(nextTier);
                manager.saveData();
                player.sendMessage(ChatColor.GREEN + "[Hoop] Successfully upgraded to " + tierName + " Teleport!");
                pendingUpgrade.remove(viewerUUID);
                openHoopGUI(player, ownerUUID);
            } else if (rawSlot == 6) { // Red wool - No
                player.sendMessage(ChatColor.YELLOW + "[Hoop] Upgrade cancelled.");
                pendingUpgrade.remove(viewerUUID);
                openHoopGUI(player, ownerUUID);
            }
        }
    }

    // -----------------------------------------------------------------------
    // GUI close - reset delete mode only when truly closing (not re-opening)
    // -----------------------------------------------------------------------
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        UUID viewerUUID = player.getUniqueId();

        // If we suppressed this close (because we're re-opening), skip cleanup
        if (suppressClose.remove(viewerUUID) != null) return;

        String title = event.getView().getTitle();

        if (title.equals(HoopGUI.MAIN_GUI_TITLE) || title.equals(HoopGUI.UPGRADE_GUI_TITLE)) {
            deleteMode.remove(viewerUUID);
            openGUI.remove(viewerUUID);
            pendingUpgrade.remove(viewerUUID);
        }
    }

    // -----------------------------------------------------------------------
    // Teleport logic
    // -----------------------------------------------------------------------
    private void startTeleport(Player player, HoopSlot slot) {
        UUID uuid = player.getUniqueId();

        cancelTeleport(uuid);

        if (slot.getLocation() == null || slot.getWorldName() == null) {
            player.sendMessage(ChatColor.RED + "[Hoop] Invalid teleport location.");
            return;
        }

        if (!player.getWorld().getName().equals(slot.getWorldName())) {
            player.sendMessage(ChatColor.RED + "[Hoop] You can only teleport within the same dimension.");
            return;
        }

        double dist = player.getLocation().distance(slot.getLocation());
        if (dist > slot.getTier().getMaxDistance()) {
            player.sendMessage(ChatColor.RED + "[Hoop] You are too far away! Max range: " + slot.getTier().getMaxDistance() + " blocks. Your distance: " + (int) dist + " blocks.");
            return;
        }

        teleportStartPos.put(uuid, player.getLocation().clone());
        teleportDest.put(uuid, slot.getLocation().clone());

        player.sendMessage(ChatColor.YELLOW + "[Hoop] Teleporting in 10 seconds. Don't move!");

        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin, new Runnable() {
            int countdown = 10;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancelTeleport(uuid);
                    return;
                }

                Location current = player.getLocation();
                Location start = teleportStartPos.get(uuid);
                if (start != null && (Math.abs(current.getX() - start.getX()) > 0.15
                        || Math.abs(current.getZ() - start.getZ()) > 0.15)) {
                    player.sendMessage(ChatColor.RED + "[Hoop] Teleport cancelled - you moved!");
                    cancelTeleport(uuid);
                    return;
                }

                countdown--;
                if (countdown > 0) {
                    player.sendMessage(ChatColor.YELLOW + "[Hoop] Teleporting in " + countdown + "...");
                } else {
                    Location dest = teleportDest.get(uuid);
                    player.teleport(dest);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 2));
                    player.sendMessage(ChatColor.GREEN + "[Hoop] Teleported!");
                    cancelTeleport(uuid);
                }
            }
        }, 20L, 20L);

        teleportTasks.put(uuid, task);
    }

    private void cancelTeleport(UUID uuid) {
        BukkitTask task = teleportTasks.remove(uuid);
        if (task != null) task.cancel();
        teleportStartPos.remove(uuid);
        teleportDest.remove(uuid);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        cancelTeleport(uuid);
        openGUI.remove(uuid);
        deleteMode.remove(uuid);
        pendingUpgrade.remove(uuid);
        suppressClose.remove(uuid);
    }
}
