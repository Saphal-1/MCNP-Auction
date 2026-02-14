package saphalthapaliya.mcnpauction.database;

import org.bukkit.inventory.ItemStack;
import saphalthapaliya.mcnpauction.models.Auction;

import java.util.List;
import java.util.UUID;

public interface Database {
    
    /**
     * Connect to database
     */
    void connect();
    
    /**
     * Disconnect from database
     */
    void disconnect();
    
    /**
     * Create necessary tables
     */
    void createTables();
    
    /**
     * Create a new auction
     */
    int createAuction(UUID sellerId, String sellerName, ItemStack item, double price, long expiryTime, long listedTime);
    
    /**
     * Load all active auctions
     */
    List<Auction> loadAuctions();
    
    /**
     * Delete an auction
     */
    void deleteAuction(int auctionId);
    
    /**
     * Add pending return item
     */
    void addPendingReturn(UUID playerId, ItemStack item);
    
    /**
     * Get pending returns for player
     */
    List<ItemStack> getPendingReturns(UUID playerId);
    
    /**
     * Clear pending returns for player
     */
    void clearPendingReturns(UUID playerId);
}
