package saphalthapaliya.mcnpauction.database;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import saphalthapaliya.mcnpauction.MCNPAuction;
import saphalthapaliya.mcnpauction.models.Auction;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLDatabase implements Database {
    
    private final MCNPAuction plugin;
    private Connection connection;
    
    public MySQLDatabase(MCNPAuction plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public void connect() {
        try {
            String host = plugin.getConfig().getString("database.mysql.host");
            int port = plugin.getConfig().getInt("database.mysql.port");
            String database = plugin.getConfig().getString("database.mysql.database");
            String username = plugin.getConfig().getString("database.mysql.username");
            String password = plugin.getConfig().getString("database.mysql.password");
            boolean useSSL = plugin.getConfig().getBoolean("database.mysql.useSSL");
            
            String url = "jdbc:mysql://" + host + ":" + port + "/" + database + "?useSSL=" + useSSL;
            connection = DriverManager.getConnection(url, username, password);
            plugin.getLogger().info("MySQL database connected");
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to connect to MySQL database!");
            e.printStackTrace();
        }
    }
    
    @Override
    public void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                plugin.getLogger().info("MySQL database disconnected");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public void createTables() {
        String auctionsTable = "CREATE TABLE IF NOT EXISTS auctions ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "seller_uuid VARCHAR(36) NOT NULL,"
            + "seller_name VARCHAR(16) NOT NULL,"
            + "item BLOB NOT NULL,"
            + "price DOUBLE NOT NULL,"
            + "expiry_time BIGINT NOT NULL,"
            + "listed_time BIGINT NOT NULL"
            + ");";
        
        String pendingReturnsTable = "CREATE TABLE IF NOT EXISTS pending_returns ("
            + "id INT AUTO_INCREMENT PRIMARY KEY,"
            + "player_uuid VARCHAR(36) NOT NULL,"
            + "item BLOB NOT NULL"
            + ");";
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(auctionsTable);
            stmt.execute(pendingReturnsTable);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to create tables!");
            e.printStackTrace();
        }
    }
    
    @Override
    public int createAuction(UUID sellerId, String sellerName, ItemStack item, double price, long expiryTime, long listedTime) {
        String sql = "INSERT INTO auctions (seller_uuid, seller_name, item, price, expiry_time, listed_time) VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, sellerId.toString());
            stmt.setString(2, sellerName);
            stmt.setBytes(3, serializeItem(item));
            stmt.setDouble(4, price);
            stmt.setLong(5, expiryTime);
            stmt.setLong(6, listedTime);
            stmt.executeUpdate();
            
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to create auction!");
            e.printStackTrace();
        }
        return -1;
    }
    
    @Override
    public List<Auction> loadAuctions() {
        List<Auction> auctions = new ArrayList<>();
        String sql = "SELECT * FROM auctions";
        
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                int id = rs.getInt("id");
                UUID sellerId = UUID.fromString(rs.getString("seller_uuid"));
                String sellerName = rs.getString("seller_name");
                ItemStack item = deserializeItem(rs.getBytes("item"));
                double price = rs.getDouble("price");
                long expiryTime = rs.getLong("expiry_time");
                long listedTime = rs.getLong("listed_time");
                
                Auction auction = new Auction(id, sellerId, sellerName, item, price, expiryTime, listedTime);
                auctions.add(auction);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to load auctions!");
            e.printStackTrace();
        }
        return auctions;
    }
    
    @Override
    public void deleteAuction(int auctionId) {
        String sql = "DELETE FROM auctions WHERE id = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setInt(1, auctionId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to delete auction!");
            e.printStackTrace();
        }
    }
    
    @Override
    public void addPendingReturn(UUID playerId, ItemStack item) {
        String sql = "INSERT INTO pending_returns (player_uuid, item) VALUES (?, ?)";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.setBytes(2, serializeItem(item));
            stmt.executeUpdate();
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to add pending return!");
            e.printStackTrace();
        }
    }
    
    @Override
    public List<ItemStack> getPendingReturns(UUID playerId) {
        List<ItemStack> items = new ArrayList<>();
        String sql = "SELECT item FROM pending_returns WHERE player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                ItemStack item = deserializeItem(rs.getBytes("item"));
                items.add(item);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to get pending returns!");
            e.printStackTrace();
        }
        return items;
    }
    
    @Override
    public void clearPendingReturns(UUID playerId) {
        String sql = "DELETE FROM pending_returns WHERE player_uuid = ?";
        
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, playerId.toString());
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to clear pending returns!");
            e.printStackTrace();
        }
    }
    
    private byte[] serializeItem(ItemStack item) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeObject(item);
        dataOutput.close();
        return outputStream.toByteArray();
    }
    
    private ItemStack deserializeItem(byte[] data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack item = (ItemStack) dataInput.readObject();
        dataInput.close();
        return item;
    }
}

