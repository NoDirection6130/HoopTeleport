package pl.nodirection6130.hoop;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class HoopCommand implements CommandExecutor {

    private final HoopListener listener;

    public HoopCommand(HoopListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("[Hoop] Only players can use this command.");
            return true;
        }

        if (!player.hasPermission("hoop.use")) {
            player.sendMessage(ChatColor.RED + "[Hoop] You don't have permission to use /hoop.");
            return true;
        }

        // /hoop <player> - admin opens another player's GUI
        if (args.length >= 1) {
            if (!player.hasPermission("hoop.admin")) {
                player.sendMessage(ChatColor.RED + "[Hoop] You don't have permission to manage other players' hoops.");
                return true;
            }
            Player target = Bukkit.getPlayerExact(args[0]);
            if (target == null) {
                player.sendMessage(ChatColor.RED + "[Hoop] Player not found or not online: " + args[0]);
                return true;
            }
            player.sendMessage(ChatColor.YELLOW + "[Hoop] Opening " + target.getName() + "'s hoop GUI.");
            listener.openHoopGUI(player, target.getUniqueId());
            return true;
        }

        // /hoop - open own GUI
        listener.openHoopGUI(player, player.getUniqueId());
        return true;
    }
}
