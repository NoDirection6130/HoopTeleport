package pl.nodirection6130.hoop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;
import java.util.UUID;

public class HoopGUI {

    // Slot positions in a 9-slot inventory
    // Slots 0,1,2 = teleport slots, slot 4 = barrier/skull
    public static final int[] TELEPORT_SLOTS = {0, 1, 2};
    public static final int BARRIER_SLOT = 4;

    public static final String MAIN_GUI_TITLE = "Hoop";
    public static final String UPGRADE_GUI_TITLE = "Hoop - Upgrade?";

    private HoopGUI() {}

    /**
     * Build the main 9-slot GUI for a player.
     * deleteMode: if true, barrier is replaced with skull.
     */
    public static Inventory buildMainGUI(PlayerHoop ph, boolean deleteMode) {
        Inventory inv = Bukkit.createInventory(null, 9, MAIN_GUI_TITLE);

        for (int i = 0; i < 3; i++) {
            HoopSlot slot = ph.getSlot(i);
            inv.setItem(TELEPORT_SLOTS[i], buildSlotItem(slot, deleteMode));
        }

        // Barrier / Skull
        if (deleteMode) {
            ItemStack skull = new ItemStack(Material.SKELETON_SKULL);
            ItemMeta meta = skull.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Delete Mode ON");
            meta.setLore(List.of(ChatColor.GRAY + "Click a teleport to remove it.", ChatColor.GRAY + "Close GUI to cancel."));
            skull.setItemMeta(meta);
            inv.setItem(BARRIER_SLOT, skull);
        } else {
            ItemStack barrier = new ItemStack(Material.BARRIER);
            ItemMeta meta = barrier.getItemMeta();
            meta.setDisplayName(ChatColor.RED + "Delete Teleport");
            meta.setLore(List.of(ChatColor.GRAY + "Click to enter delete mode."));
            barrier.setItemMeta(meta);
            inv.setItem(BARRIER_SLOT, barrier);
        }

        return inv;
    }

    private static ItemStack buildSlotItem(HoopSlot slot, boolean deleteMode) {
        if (slot.isEmpty()) {
            ItemStack hopper = new ItemStack(Material.HOPPER);
            ItemMeta meta = hopper.getItemMeta();
            meta.setDisplayName(ChatColor.YELLOW + "Empty Slot");
            meta.setLore(List.of(
                    ChatColor.GRAY + "Right-click to buy a teleport here.",
                    ChatColor.GRAY + "Cost: " + ChatColor.GOLD + "20 XP levels"
            ));
            hopper.setItemMeta(meta);
            return hopper;
        }

        Material mat = switch (slot.getTier()) {
            case IRON    -> Material.IRON_BLOCK;
            case GOLD    -> Material.GOLD_BLOCK;
            case DIAMOND -> Material.DIAMOND_BLOCK;
            default      -> Material.HOPPER;
        };

        String tierName = switch (slot.getTier()) {
            case IRON    -> ChatColor.WHITE + "Iron Teleport";
            case GOLD    -> ChatColor.GOLD + "Gold Teleport";
            case DIAMOND -> ChatColor.AQUA + "Diamond Teleport";
            default      -> ChatColor.GRAY + "Empty";
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(tierName);

        if (deleteMode) {
            meta.setLore(List.of(
                    ChatColor.GREEN + slot.getLabel(),
                    ChatColor.RED + "Click to DELETE this teleport.",
                    ChatColor.GRAY + "No refund."
            ));
        } else {
            String rangeLine = ChatColor.GRAY + "Range: " + ChatColor.WHITE + slot.getTier().getMaxDistance() + " blocks";
            String coordLine = ChatColor.GREEN + slot.getLabel();

            if (slot.getTier() == HoopSlot.Tier.DIAMOND) {
                meta.setLore(List.of(coordLine, rangeLine, ChatColor.GRAY + "Max tier reached."));
            } else {
                int cost = slot.getUpgradeCost();
                String nextTier = switch (slot.getNextTier()) {
                    case GOLD    -> "Gold";
                    case DIAMOND -> "Diamond";
                    default      -> "?";
                };
                meta.setLore(List.of(
                        coordLine,
                        rangeLine,
                        ChatColor.GRAY + "Left-click to teleport.",
                        ChatColor.YELLOW + "Right-click to upgrade to " + nextTier + " (" + cost + " XP levels)"
                ));
            }
            if (slot.getTier() == HoopSlot.Tier.DIAMOND) {
                // Lore already set above, just add teleport hint
                List<String> lore = new java.util.ArrayList<>(meta.getLore());
                lore.add(ChatColor.GRAY + "Left-click to teleport.");
                meta.setLore(lore);
            }
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Build the upgrade confirmation GUI.
     */
    public static Inventory buildUpgradeGUI(HoopSlot slot) {
        Inventory inv = Bukkit.createInventory(null, 9, UPGRADE_GUI_TITLE);

        // Green wool - Yes
        ItemStack yes = new ItemStack(Material.GREEN_WOOL);
        ItemMeta yesMeta = yes.getItemMeta();
        yesMeta.setDisplayName(ChatColor.GREEN + "Yes");
        String nextTierName = switch (slot.getNextTier()) {
            case GOLD    -> "Gold";
            case DIAMOND -> "Diamond";
            default      -> "?";
        };
        yesMeta.setLore(List.of(
                ChatColor.GRAY + "Upgrade to " + nextTierName + " Teleport",
                ChatColor.GRAY + "Cost: " + ChatColor.GOLD + slot.getUpgradeCost() + " XP levels"
        ));
        yes.setItemMeta(yesMeta);
        inv.setItem(2, yes);

        // Red wool - No
        ItemStack no = new ItemStack(Material.RED_WOOL);
        ItemMeta noMeta = no.getItemMeta();
        noMeta.setDisplayName(ChatColor.RED + "No");
        noMeta.setLore(List.of(ChatColor.GRAY + "Cancel upgrade"));
        no.setItemMeta(noMeta);
        inv.setItem(6, no);

        return inv;
    }
}
