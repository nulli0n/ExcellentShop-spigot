# About
<div align="center">
  <img src="https://nightexpressdev.com/excellentshop/header.png">

<a href="https://discord.gg/EwNFGsnGaW"><img src="https://img.shields.io/discord/903053383475277844?style=for-the-badge&label=Discord&color=%2333a8ff"></a>
<a href="https://ko-fi.com/nightexpress"><img src="https://img.shields.io/badge/donate-%E2%9D%A4%EF%B8%8F_to_support-dff33?style=for-the-badge"></a>
<a href="https://nightexpressdev.com/excellentshop/"><img src="https://img.shields.io/badge/wiki-documentation-ff9c33?style=for-the-badge"></a>

![](https://repo.nightexpressdev.com/api/badge/latest/releases/su/nightexpress/excellentshop/ExcellentShop-spigot?color=40c14a&name=ExcellentShop&prefix=v)

**ExcellentShop** is a lightweight and modern 4 in 1 shop plugin that includes **GUI Shop** (with Rotations), **Chest Shop** and **Auction House**.

<img src="https://nightexpressdev.com/img/excellentshop/central.gif"><img src="https://nightexpressdev.com/img/excellentshop/editor.gif">
<img src="https://nightexpressdev.com/img/excellentshop/cart.gif"><img src="https://nightexpressdev.com/img/excellentshop/sellall2.gif">
<img src="https://nightexpressdev.com/img/excellentshop/cshoptype.gif"><img src="https://nightexpressdev.com/img/excellentshop/cshopcust.gif">

</div>

# Features
- **Database**. Works with SQLite and MySQL!
- **Synchronization**. Sync your products data across multiple servers!
- **Modularized**. Fully disable any part of the plugin you don't like!
- [**Multi-Currency**](https://nightexpressdev.com/excellentshop/features/multi-currency). Use different currencies in shop items and auction listings!
- [**4 Price Types**](https://nightexpressdev.com/excellentshop/features/price-types). Use different price systems for in virtual and chest shops!
- [**Purchase GUI**](https://nightexpressdev.com/excellentshop/features/purchase-gui). Simple, intuitive and fully customizable GUI for buying and selling items!
- [**Virtual Shop**](https://nightexpressdev.com/excellentshop/virtual/overview). Create fully customizable admin GUI shops with a lot of options available!
    - **In-Game Editor**. Create and manage your shops directly in-game with simple and intuitive GUIs!
    - [**Main GUI**](https://nightexpressdev.com/excellentshop/virtual/main-gui). Quick access shops with the fully customizable main shops GUI!
    - [**Stocks & Limits**](https://nightexpressdev.com/excellentshop/virtual/stocks-limits). Limit amount of available products to buy and sell on global and per-player basis!
    - [**Items & Commands**](https://nightexpressdev.com/excellentshop/virtual/product-types). Sell items with full NBT support, custom item plugin integrations, and commands with PlaceholderAPI support!
    - [**Product Requirements**](https://nightexpressdev.com/excellentshop/virtual/product-requirements). Set rank and permission requirements for the shop items!
    - [**Shop Layouts**](https://nightexpressdev.com/excellentshop/virtual/shop-layouts). Create fully custom GUI configurations for your shops with per-page layout support!
    - [**Shop Requirements**](https://nightexpressdev.com/excellentshop/virtual/shop-requirements). Restrict access to all or specific shops based on certain conditions!
    - [**Shop Rotations**](https://nightexpressdev.com/excellentshop/virtual/shop-rotations). Create dynamic offers in your shops that changes with over time!
    - [**Shop Shortcuts**](https://nightexpressdev.com/excellentshop/virtual/shop-shortcuts). Create custom commands for quick access to shops!
    - [**Sell Features**](https://nightexpressdev.com/excellentshop/virtual/sell-features). Quickly sell the whole inventory, item in hand, or specific items using the GUI!
    - [**Sell Multipliers**](https://nightexpressdev.com/excellentshop/virtual/sell-multipliers). Boost sell prices for players based on their rank or permissions!
- [**Chest Shop**](https://nightexpressdev.com/excellentshop/chest/overview). Create chest shops at any container block in the world!
    - **In-Game Editor**. Create and manage your shops with simple and intuitive GUIs!
    - **Holographic Displays**. Display shop info with packet-based clientside holograms!
    - **Shop Bank**. Split your pocket and shop balances with the bank feature!
    - **Shop List**. Browse your own or other player's shops in GUI!
    - **Shop Search**. Search for shops that contains a specific item!
    - **Shop Amount**. Set how many shops players can create based on their rank or permissions!
    - **Shop Blocks**. Create shops by placing special shop blocks!
    - **Product Amount**. Set how many products per-shop players can create based on their rank or permissions!
    - **Fees**. Take fees from players for creating and removing shops!
    - [**Admin Shops**](https://nightexpressdev.com/excellentshop/chest/admin-shops). Make chest shops to be admin shops with unlimited stocks and money!
    - [**Shop Renting**](https://nightexpressdev.com/excellentshop/chest/shop-renting). Rent shops created by other players for a price and time set by the shop owner!
    - [**Shop Types**](https://nightexpressdev.com/excellentshop/chest/shop-types). Set which blocks are allowed to be chest shops! Includes **Shulker boxes**, **Barrels**, and more!
    - [**Claim Integrations**](https://nightexpressdev.com/excellentshop/chest/claim-integrations). Restrict shop creation outside of player claims!
    - [**Infinite Storage**](https://nightexpressdev.com/excellentshop/chest/infinite-storage). Bypass block's inventory capacity limits with virtual item storage.
    - **Item Blacklist**. Prevent items with specific name, lore and type from being added to shops!
- [**Auction**](https://nightexpressdev.com/excellentshop/auction/overview). Allow players to trade items on the global server marketplace!
    - **Categories**. Filter items on the auction with fully customizable item categories!
    - **Sorting**. Sort items on the auction by their price, date, owner, type and name!
    - **Notifications**. Notify players about sold, expired and unclaimed listings!
    - **Announcements**. Broadcast a message when new listing is added on the auction!
    - **Fees**. Take fees from players for adding items on the auction!
    - **Container Preview**. Preview content of shulker boxes and chests before purchase!
    - [**Listings Amount**](https://nightexpressdev.com/excellentshop/auction/listings-amount). Set how many listings players can add on the auction based on their rank or permissions!
    - [**Price Limits**](https://nightexpressdev.com/excellentshop/auction/price-limits). Limit listing prices for certain items and currencies!
- **Transaction Logs**. Log all shop transactions in a dedicated log file!
- [**PlaceholderAPI Support**](https://nightexpressdev.com/excellentshop/hooks/placeholder-api).

# Compatibility
- **Java Version:** 21 or higher.
- **Server Software:** Spigot / Paper / Purpur
- **Server Version:** 1.21.4 or higher.

# Dependencies
**REQUIRED:**
- [nightcore](https://nightexpressdev.com/nightcore/) - Plugin engine.
- [EconomyBridge](https://nightexpressdev.com/economy-bridge/) - Economy, currency and custom item compatibilities.

**OPTIONAL:**
- [ProtocolLib](https://ci.dmulloy2.net/job/ProtocolLib/) or [PacketEvents](https://spigotmc.org/resources/80279/) - For chest shop holograms.

# Downloads & Documentation
- [SpigotMC](https://spigotmc.org/resources/50696/)
- [BuiltByBit](https://builtbybit.com/resources/46692/)
- [Documentation](https://nightexpressdev.com/excellentshop/)

# Donate
If you like my work or enjoy using my plugins, feel free to [Buy me a coffee](https://ko-fi.com/nightexpress) :) Thank you! 🧡