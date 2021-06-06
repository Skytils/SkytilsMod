# SkytilsMod
***
<p align="center">
  <a href="https://github.com/Skytils/SkytilsMod/releases" target="_blank">
    <img alt="downloads" src="https://img.shields.io/github/v/release/Skytils/SkytilsMod?color=4166f5&style=flat-square" />
  </a>
  <a href="https://github.com/Skytils/SkytilsMod/releases" target="_blank">
    <img alt="downloads" src="https://img.shields.io/github/downloads/Skytils/SkytilsMod/total?color=4166f5&style=flat-square" />
  </a>
  <a href="https://github.com/Skytils/SkytilsMod/blob/main/LICENSE" target="_blank">
    <img alt="license" src="https://img.shields.io/github/license/Skytils/SkytilsMod?color=4166f5&style=flat-square" />
  </a>
  <a href="https://discord.gg/skytils" target="_blank">
    <img alt="discord" src="https://img.shields.io/discord/807302538558308352?color=4166f5&label=discord&style=flat-square" />
  </a>
</p>
A Hypixel Skyblock Utilities mod.


## Features
<details>
  <summary>General</summary>

### General
 - Client Side Custom Armor Colors (with animated rainbow)
 - Custom Command Aliases
 - Griffin Burrow Locator and Waypoints
 - Track Mythological Event drops
 - Track Gaia Construct Hits
 - Reparty Command Which Yields to Other Mods
 - Auto Accept Reparty
 - ~~Trick or Treat Chest Alert~~
 - Custom Key Shortcuts
 - Better Auction House Price Input
 - Spam Hider for
     - Profile messages
     - Mort messages
     - Boss messages
     - Oruo (Trivia puzzle) messages
     - Autopet messages
     - Ability messages
          - Implosion
          - Midas Staff
          - Spirit Sceptre
          - Giant Sword
          - Livid Dagger
          - Staff of the Rising Sun
     - Cooldown messages
     - Mana messages
     - Blocks in the way messages
     - Dungeon blessings
     - Wither & blood key pickups
     - Superboom TNT pickups
     - Revive stone pickups
     - Combo messages
     - Blessing enchant and bait messages
     - Wither and Undead Essence unlock messages
</details>
<details>
  <summary>Dungeons</summary>

#### Dungeons
 - ~~Show Hidden Fels, Shadow Assassins, and blood room mobs~~
 - Blaze Solver Which Replaces The Skin Textures
 - Boulder Puzzle Solver [WIP]
 - Simon Says Solver [WIP]
 - Trivia Solver (updated with the latest answers)
 - Three Weirdo Solver
 - Spirit Leap Names
 - Click in Order Terminal Solver
 - Ice Path Solver
 - Select All Color Solver
 - Stop Dropping, Salvaging, and Selling Starred Dungeon Items
 - Bigger Bat Rendering and Bat Hitbox Display
 - Giant, Sadan, and Necron HP Display
 - Better Sadan Interest Timer (Terracotta Phase)
 - Score Calculation
 - Dungeon Timer
 - Necron Phase Timer
 - Dungeon reroll Confirmation
 - Skeleton Master Boxes
 - Correct Livid Finder (with M5 support)
 - Dungeon Chest Profit
</details>
<details>
  <summary>Farming</summary>

### Farming
 - ~~Block Math Hoe Recipe Viewer~~
 - ~~Block Breaking Farms~~
 - Hungry Hiker solver
 - Treasure Hunter Solver
</details>
<details>
  <summary>Mining</summary>

### Mining 
 - ~~Show Ghosts in the Mist (Also their health)~~
 - ~~Disable Pickaxe Ability on Private Island~~
 - Fetchur Solver
 - Puzzler Solver
 - Raffle Waypoint and Warning
 - Show hidden sneaky creepers
 - Dark Mode Mist
 - More Visible Ghosts
 - Recolor Carpets
 - Highlight Completed Comissions
</details>
<details>
  <summary>Items</summary>
  
### Items
 - Soul Eater Bonus Strength
 - ~~Block Useless Zombie Sword Uses~~
 - ~~Prioritize Item Abilities~~
 - Pet Item Confirmation
 - Highlight Active & Favorite Pets
 - Hide Implosion Particles
 - Hide Midas Staff Gold Blocks
 - Big Item Drops
 - Larger Heads
 - Show Enchanted Book, Potion, and Minion Tiers
 - Show Pet Candies
 - Only Collect Enchanted Items
 - Dungeon Potion Lock
 - Power Orb Lock
 - Prevent Placing Spirit Sceptre and Flower of Truth
 - Transparent Head Layer
 - Show NPC Sell Values
 - ~~Hide Wither Veil Creepers Near NPCs~~
 - ~~Customizable Block Item Ability~~
 - Show Price of Items in the Experimentation Table
 - Jerry-chine Gun Sound Hider
 - Show Enchanted Book Abbreviation
</details>
<details>
  <summary>Miscellaneous</summary>

## Miscellaneous
 - Hide Witherborn Boss Bars
 - Hide Fire and Lightning
 - Custom Damage Splash
 - Legion and Dolphin player displays
 - Alerts for Hidden Jerry spawns
 - Relic and Rare Uber Relic waypoints
 - Stop other mods from cancelling terminal clicks
 - Slayer miniboss spawn alert
 - Hide fishing hooks from other players
 - Placed Summoning Eye Display
 - Spider's Den Rain Timer
 - Stop Dropping Valuable Items (customizable BIN value)
</details>

And more to come!
<details>
  <summary>Commands</summary>

## Commands
- /skytils - Opens the main GUI. (Alias is /st)
- /skytils config - Opens the GUI to edit the config.
- /skytils help - Displays the various commands and their usages
- /skytils setkey <apikey> - Sets your api key (will also grab it from /api new).
- /skytils reload <aliases/data> - Forces Skytils to re-fetch your command aliases or solutions from the data repository.
- /skytils fetchur - Displays the current Fetchur item for the day.
- /skytils griffin refresh - Forces a refresh for the Griffin burrow waypoints.
- /skytils swaphub - Quickly leaves the hub and goes to the forge, then back to the hub. Useful for Diana event.
- /skytils aliases - Opens the GUI to edit command aliases.
- /skytils editlocations (/skytils editlocation, gui, loc) - Opens the GUI to modify HUD element locations.
- /skytils shortcuts - Opens the GUI to modify keybind shortcuts.
- /armorcolor <set/clear/clearall> - Changes the color of an armor piece to the hexcode or decimal color provided. (can also be accessed by /skytils armorcolor)
- ~~/blockability [clearall] - Block using the ability on the currently held item~~
- /reparty (/rp) - Disbands and sends a party invite to everyone who was in your party.
- /glintcustomize override <on/off/clear/clearall> - Change the visibility of enchantment glints for the item.
- /glintcustomize color <set/clear/clearall> - Change the enchant glint color for the item.
</details>

### Credits to Open Source Software
***
Skytils would not be possible without other open source projects.

[For more information, click here](https://github.com/Skytils/SkytilsMod/blob/main/OPEN_SOURCE_SOFTWARE.md "Credits")
