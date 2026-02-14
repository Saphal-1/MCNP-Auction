# MCNP-Auction

A premium auction house plugin for Minecraft Paper 1.21.x with advanced features and zero item loss.

## Features

### Core Functionality
- 🏪 **Full Auction System** - List items with `/auction sell <price> <time>`
- 💰 **Vault Economy Integration** - Seamless money transactions
- 📦 **Automatic Item Return** - Expired items returned automatically (even offline)
- 🎯 **Zero Item Loss** - Items saved to database if player is offline
- 🔄 **Direct Delivery** - Items go directly to inventory or drop if full

### Advanced Features
- 📄 **Multi-page GUI** - Smooth pagination with next/previous buttons
- 🔍 **Sorting System** - Sort by price (low/high) or time remaining
- 🎨 **Premium GUI Design** - Clean, modern interface with visual feedback
- 🎵 **Sound Effects** - Audio feedback for all interactions
- ⚡ **Performance Optimized** - Async database operations
- 🛡️ **Safe Shutdown** - No data loss on server stop

### Configuration
- 📊 **Tax System** - Configurable percentage-based auction tax
- ⏱️ **Cooldown System** - Prevent auction spam
- 🎭 **Auction Limits** - Per-player limits via permissions
- 🚫 **Blocked Items** - Prevent certain items from being auctioned
- ⏳ **Duration Limits** - Min/max auction durations
- 💾 **Database Choice** - SQLite or MySQL support

### Admin Features
- 🔧 `/auctionadmin reload` - Reload configuration
- ❌ `/auctionadmin remove <id>` - Force remove auction
- 📤 `/auctionadmin return` - Force return all expired auctions
- 📝 **Debug Mode** - Detailed logging for troubleshooting

## Commands

### Player Commands
- `/auction` - Open auction house GUI
- `/auction sell <price> [time]` - List item in hand
- `/auction help` - Show help menu

**Aliases:** `/ah`, `/auc`

### Admin Commands
- `/auctionadmin reload` - Reload configs
- `/auctionadmin remove <id>` - Remove auction
- `/auctionadmin return` - Return expired items

**Aliases:** `/ahadmin`, `/aucadmin`

## Permissions

### Basic Permissions
- `mcnpauction.use` - Access auction house (default: true)
- `mcnpauction.sell` - Sell items (default: true)
- `mcnpauction.buy` - Buy items (default: true)
- `mcnpauction.admin` - Admin commands (default: op)

### Special Permissions
- `mcnpauction.notax` - Bypass auction tax
- `mcnpauction.nocooldown` - Bypass sell cooldown

### Limit Permissions
- `mcnpauction.limit.5` - 5 active auctions (default)
- `mcnpauction.limit.10` - 10 active auctions
- `mcnpauction.limit.20` - 20 active auctions
- `mcnpauction.limit.unlimited` - Unlimited auctions

## Installation

1. Download the latest release
2. Place JAR in your `plugins/` folder
3. Install **Vault** and an economy plugin (e.g., EssentialsX)
4. Restart server
5. Configure `config.yml` and `messages.yml`
6. Enjoy!

## Configuration

### Database Settings
```yaml
database:
  type: SQLITE # or MYSQL
  mysql:
    host: localhost
    port: 3306
    database: mcnp_auction
    username: root
    password: password
```

### Auction Settings
```yaml
auction:
  tax-percentage: 0.05 # 5% tax
  min-price: 1.0
  max-price: 1000000.0
  default-auction-limit: 5
  sell-cooldown: 30 # seconds
```

### GUI Settings
```yaml
gui:
  sounds:
    enabled: true
    on-buy: ENTITY_PLAYER_LEVELUP
    on-sell: ENTITY_VILLAGER_YES
```

## How It Works

### Selling Items
1. Hold item in hand
2. Run `/auction sell <price> [time]`
3. Tax is deducted (if applicable)
4. Item is removed from inventory
5. Auction is listed in GUI

### Buying Items
1. Open auction GUI with `/auction`
2. Click item to purchase
3. Money is transferred
4. Item goes to inventory (or drops if full)
5. Seller is notified

### Expiration System
When an auction expires:
1. Item is removed from active listings
2. If seller is **online** → Item returned immediately
3. If seller is **offline** → Item saved to `pending_returns` table
4. On next login → Items automatically returned
5. Player receives notification

**No manual claiming needed!**

## Technical Details

### Database Schema
**auctions table:**
- id (PRIMARY KEY)
- seller_uuid
- seller_name
- item (BLOB - serialized)
- price
- expiry_time
- listed_time

**pending_returns table:**
- id (PRIMARY KEY)
- player_uuid
- item (BLOB - serialized)

### Performance
- ✅ Async database operations
- ✅ Cached auction data
- ✅ Efficient GUI rendering
- ✅ Optimized expiry checks
- ✅ Connection pooling ready

## Dependencies

- **Paper 1.21.x** (or compatible fork)
- **Vault** (required)
- **Economy Plugin** (EssentialsX, CMI, etc.)

## Building from Source

```bash
git clone https://github.com/yourusername/MCNP-Auction.git
cd MCNP-Auction
mvn clean package
```

JAR will be in `target/MCNP-Auction-1.0.0.jar`

## Support

For issues, suggestions, or contributions, please open an issue on GitHub.

## License

This plugin is provided as-is for use on Minecraft servers.

---

**Made with ❤️ for the Minecraft community**
