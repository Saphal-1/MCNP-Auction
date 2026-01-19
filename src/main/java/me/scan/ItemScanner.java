package me.scan;

import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.inventory.*;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemScanner extends JavaPlugin {

    @Override
    public void onEnable() {
        getCommand("finditem").setExecutor(this::onCommand);
    }

    private boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("itemscanner.use")) {
            sender.sendMessage("§cNo permission.");
            return true;
        }

        if (args.length != 2) {
            sender.sendMessage("§cUsage: /finditem <world> <item>");
            return true;
        }

        World world = Bukkit.getWorld(args[0]);
        Material mat = Material.matchMaterial(args[1].toUpperCase());

        if (world == null || mat == null) {
            sender.sendMessage("§cInvalid world or item.");
            return true;
        }

        // Running async to prevent lag, though scanning chunks should be done carefully
        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
            int found = 0;
            for (Chunk chunk : world.getLoadedChunks()) {
                for (Entity e : chunk.getEntities()) {
                    if (e instanceof Item item && item.getItemStack().getType() == mat) {
                        sender.sendMessage("§6Dropped at " + loc(item.getLocation()));
                        found++;
                    }
                }
                for (BlockState state : chunk.getTileEntities()) {
                    if (state instanceof InventoryHolder inv) {
                        for (ItemStack it : inv.getInventory().getContents()) {
                            if (it != null && it.getType() == mat) {
                                sender.sendMessage("§bContainer at " + loc(state.getLocation()));
                                found++;
                                break;
                            }
                        }
                    }
                }
            }
            sender.sendMessage("§aScan complete. Found: §e" + found);
        });
        return true;
    }

    private String loc(Location l) {
        return l.getWorld().getName() + ":" + l.getBlockX() + "," + l.getBlockY() + "," + l.getBlockZ();
    }
}
