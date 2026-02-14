package saphalthapaliya.mcnpauction.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import saphalthapaliya.mcnpauction.MCNPAuction;
import saphalthapaliya.mcnpauction.models.Auction;
import saphalthapaliya.mcnpauction.utils.ItemUtils;
import saphalthapaliya.mcnpauction.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class AuctionGUI implements Listener {
    
    private final MCNPAuction plugin;
    private final Player player;
    private Inventory inventory;
    private int currentPage;
    private SortType sortType;
    private List<Auction> displayedAuctions;
    
    public enum SortType {
        PRICE_LOW_HIGH,
        PRICE_HIGH_LOW,
        TIME_REMAINING
    }
    
    public AuctionGUI(MCNPAuction plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        this.currentPage = 0;
        this.sortType = SortType.TIME_REMAINING;
        this.displayedAuctions = new ArrayList<>();
    }
    
    public void open() {
        String title = plugin.getConfigManager().getMessage("gui.title");
        inventory = Bukkit.createInventory(null, 54, title);
        
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        
        refresh();
        player.openInventory(inventory);
        playSound("on-click");
    }
    
    public void refresh() {
        inventory.clear();
        
        // Get and sort auctions
        displayedAuctions = plugin.getAuctionManager().getActiveAuctions();
        sortAuctions();
        
        // Calculate pagination
        int itemsPerPage = plugin.getConfig().getInt("auction.items-per-page", 28);
        int totalPages = (int) Math.ceil((double) displayedAuctions.size() / itemsPerPage);
        int startIndex = currentPage * itemsPerPage;
        int endIndex = Math.min(startIndex + itemsPerPage, displayedAuctions.size());
        
        // Display auctions
        List<Auction> pageAuctions = displayedAuctions.subList(startIndex, endIndex);
        int slot = 10;
        for (Auction auction : pageAuctions) {
            // Skip border slots
            if (slot % 9 == 0 || slot % 9 == 8) {
                slot += 2;
            }
            if (slot >= 44) break;
            
            inventory.setItem(slot, createAuctionItem(auction));
            slot++;
        }
        
        // Navigation buttons
        if (currentPage > 0) {
            inventory.setItem(45, createNavigationButton(Material.ARROW, "gui-previous-page"));
        }
        
        if (currentPage < totalPages - 1) {
            inventory.setItem(53, createNavigationButton(Material.ARROW, "gui-next-page"));
        }
        
        // Info and control buttons
        inventory.setItem(49, createInfoButton(totalPages));
        inventory.setItem(48, createSortButton());
        inventory.setItem(50, createRefreshButton());
        
        // Fill borders
        fillBorders();
    }
    
    private void sortAuctions() {
        switch (sortType) {
            case PRICE_LOW_HIGH:
                displayedAuctions.sort(Comparator.comparingDouble(Auction::getPrice));
                break;
            case PRICE_HIGH_LOW:
                displayedAuctions.sort(Comparator.comparingDouble(Auction::getPrice).reversed());
                break;
            case TIME_REMAINING:
                displayedAuctions.sort(Comparator.comparingLong(Auction::getTimeRemaining));
                break;
        }
    }
    
    private ItemStack createAuctionItem(Auction auction) {
        ItemStack displayItem = auction.getItem().clone();
        ItemMeta meta = displayItem.getItemMeta();
        
        List<String> lore = meta.hasLore() ? new ArrayList<>(meta.getLore()) : new ArrayList<>();
        lore.add("");
        lore.add(plugin.getConfigManager().getMessage("lore-seller", "{player}", auction.getSellerName()));
        lore.add(plugin.getConfigManager().getMessage("lore-price", "{price}", String.format("%.2f", auction.getPrice())));
        lore.add(plugin.getConfigManager().getMessage("lore-time-remaining", "{time}", TimeUtils.formatTime(auction.getTimeRemaining())));
        lore.add("");
        
        if (auction.getSellerId().equals(player.getUniqueId())) {
            lore.add(plugin.getConfigManager().getMessage("lore-your-item"));
        } else {
            lore.add(plugin.getConfigManager().getMessage("lore-click-to-buy"));
        }
        
        meta.setLore(lore);
        displayItem.setItemMeta(meta);
        
        return displayItem;
    }
    
    private ItemStack createNavigationButton(Material material, String messageKey) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().getMessage(messageKey));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createInfoButton(int totalPages) {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().getMessage("gui-current-page",
            "{current}", String.valueOf(currentPage + 1),
            "{total}", String.valueOf(Math.max(1, totalPages))));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Total Auctions: §e" + displayedAuctions.size());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createSortButton() {
        ItemStack item = new ItemStack(Material.HOPPER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().getMessage("gui-sort"));
        
        List<String> lore = new ArrayList<>();
        lore.add("§7Current: §e" + getSortTypeName());
        lore.add("");
        lore.add("§eClick to change sort");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createRefreshButton() {
        ItemStack item = new ItemStack(Material.LIME_DYE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(plugin.getConfigManager().getMessage("gui-refresh"));
        item.setItemMeta(meta);
        return item;
    }
    
    private void fillBorders() {
        ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = border.getItemMeta();
        meta.setDisplayName("§r");
        border.setItemMeta(meta);
        
        // Top and bottom rows
        for (int i = 0; i < 9; i++) {
            inventory.setItem(i, border);
            inventory.setItem(i + 45, border);
        }
        
        // Left and right columns
        for (int i = 9; i < 45; i += 9) {
            inventory.setItem(i, border);
            inventory.setItem(i + 8, border);
        }
    }
    
    private String getSortTypeName() {
        switch (sortType) {
            case PRICE_LOW_HIGH: return "Price (Low → High)";
            case PRICE_HIGH_LOW: return "Price (High → Low)";
            case TIME_REMAINING: return "Time Remaining";
            default: return "Unknown";
        }
    }
    
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        if (!event.getInventory().equals(inventory)) return;
        
        event.setCancelled(true);
        
        Player clicker = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        
        if (clicked == null || clicked.getType() == Material.AIR) return;
        
        int slot = event.getSlot();
        
        // Previous page
        if (slot == 45 && currentPage > 0) {
            currentPage--;
            refresh();
            playSound("on-click");
            return;
        }
        
        // Next page
        if (slot == 53) {
            int itemsPerPage = plugin.getConfig().getInt("auction.items-per-page", 28);
            int totalPages = (int) Math.ceil((double) displayedAuctions.size() / itemsPerPage);
            if (currentPage < totalPages - 1) {
                currentPage++;
                refresh();
                playSound("on-click");
            }
            return;
        }
        
        // Sort button
        if (slot == 48) {
            cycleSortType();
            currentPage = 0;
            refresh();
            playSound("on-click");
            return;
        }
        
        // Refresh button
        if (slot == 50) {
            refresh();
            playSound("on-click");
            return;
        }
        
        // Auction item click
        int itemsPerPage = plugin.getConfig().getInt("auction.items-per-page", 28);
        int startIndex = currentPage * itemsPerPage;
        
        // Calculate which auction was clicked
        int relativeSlot = getRelativeSlot(slot);
        if (relativeSlot >= 0 && relativeSlot < itemsPerPage) {
            int auctionIndex = startIndex + relativeSlot;
            if (auctionIndex < displayedAuctions.size()) {
                handleAuctionClick(clicker, displayedAuctions.get(auctionIndex));
            }
        }
    }
    
    private int getRelativeSlot(int slot) {
        // Convert inventory slot to relative position in auction display
        if (slot < 10 || slot > 43) return -1;
        
        int row = (slot / 9) - 1;
        int col = (slot % 9) - 1;
        
        if (col < 0 || col > 6) return -1;
        
        return (row * 7) + col;
    }
    
    private void handleAuctionClick(Player buyer, Auction auction) {
        // Check if buyer is trying to buy their own auction
        if (auction.getSellerId().equals(buyer.getUniqueId())) {
            buyer.sendMessage(plugin.getConfigManager().getMessageWithPrefix("buy-own-auction"));
            playSound("on-error");
            return;
        }
        
        // Check if auction expired
        if (auction.isExpired()) {
            buyer.sendMessage(plugin.getConfigManager().getMessageWithPrefix("buy-auction-expired"));
            refresh();
            playSound("on-error");
            return;
        }
        
        // Check if buyer has permission
        if (!buyer.hasPermission("mcnpauction.buy")) {
            buyer.sendMessage(plugin.getConfigManager().getMessageWithPrefix("no-permission"));
            playSound("on-error");
            return;
        }
        
        // Check if buyer has enough money
        if (!plugin.getEconomy().has(buyer, auction.getPrice())) {
            buyer.sendMessage(plugin.getConfigManager().getMessageWithPrefix("buy-insufficient-funds",
                "{price}", String.format("%.2f", auction.getPrice())));
            playSound("on-error");
            return;
        }
        
        // Process purchase
        plugin.getEconomy().withdrawPlayer(buyer, auction.getPrice());
        plugin.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(auction.getSellerId()), auction.getPrice());
        
        // Give item to buyer
        boolean inventoryFull = !ItemUtils.giveItem(buyer, auction.getItem());
        
        // Send messages
        buyer.sendMessage(plugin.getConfigManager().getMessageWithPrefix("buy-success",
            "{item}", ItemUtils.getItemName(auction.getItem()),
            "{amount}", String.valueOf(auction.getItem().getAmount()),
            "{price}", String.format("%.2f", auction.getPrice())));
        
        if (inventoryFull) {
            buyer.sendMessage(plugin.getConfigManager().getMessage("buy-inventory-full"));
        }
        
        // Notify seller if online
        Player seller = Bukkit.getPlayer(auction.getSellerId());
        if (seller != null && seller.isOnline()) {
            seller.sendMessage(plugin.getConfigManager().getMessageWithPrefix("seller-item-sold",
                "{item}", ItemUtils.getItemName(auction.getItem()),
                "{amount}", String.valueOf(auction.getItem().getAmount()),
                "{player}", buyer.getName(),
                "{price}", String.format("%.2f", auction.getPrice())));
            seller.sendMessage(plugin.getConfigManager().getMessage("seller-money-received",
                "{amount}", String.format("%.2f", auction.getPrice())));
        }
        
        // Remove auction
        plugin.getAuctionManager().purchaseAuction(buyer, auction.getId());
        
        // Refresh GUI
        refresh();
        playSound("on-buy");
    }
    
    private void cycleSortType() {
        switch (sortType) {
            case TIME_REMAINING:
                sortType = SortType.PRICE_LOW_HIGH;
                break;
            case PRICE_LOW_HIGH:
                sortType = SortType.PRICE_HIGH_LOW;
                break;
            case PRICE_HIGH_LOW:
                sortType = SortType.TIME_REMAINING;
                break;
        }
    }
    
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getInventory().equals(inventory)) return;
        HandlerList.unregisterAll(this);
    }
    
    private void playSound(String soundKey) {
        if (!plugin.getConfig().getBoolean("gui.sounds.enabled")) return;
        
        try {
            String soundName = plugin.getConfig().getString("gui.sounds." + soundKey);
            Sound sound = Sound.valueOf(soundName);
            player.playSound(player.getLocation(), sound, 1.0f, 1.0f);
        } catch (IllegalArgumentException ignored) {}
    }
              }
          
