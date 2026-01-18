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
        getLogger().info("ItemScanner enabled");
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
        if (world == null) {
            sender.sendMessage("§cWorld not found.");
            return true;
        }

        Material mat = Material.matchMaterial(args[1].toUpperCase());
        if (mat == null) {
            sender.sendMessage("§cInvalid item.");
            return true;
        }

        sender.sendMessage("§aScanning world §e" + world.getName() + " §afor §e" + mat);

        Bukkit.getScheduler().runTaskAsynchronously(this, () -> {

            int found = 0;

            for (Chunk chunk : world.getLoadedChunks()) {

                // scan dropped items
                for (Entity e : chunk.getEntities()) {
                    if (e instanceof Item item) {
                        if (item.getItemStack().getType() == mat) {
                            sender.sendMessage("§6Dropped at " + loc(item.getLocation()));
                            found++;
                        }
                    }
                }

                // scan containers
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

            // scan players in that world
            for (Player p : world.getPlayers()) {
                for (ItemStack it : p.getInventory().getContents()) {
                    if (it != null && it.getType() == mat) {
                        sender.sendMessage("§dPlayer " + p.getName() + " has " + mat);
                        found++;
                        break;
                    }
                }
            }

            sender.sendMessage("§aScan complete. Found: §e" + found);

        });

        return true;
    }

    private String loc(Location l) {
        return l.getWorld().getName() + ":" +
                l.getBlockX() + "," +
                l.getBlockY() + "," +
                l.getBlockZ();
    }
                              }
