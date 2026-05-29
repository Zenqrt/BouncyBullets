# BouncyBullets
 A FPS-style minigame revolving around bullet ricochet mechanics.

 ## Gameplay
[![Watch the video](https://img.youtube.com/vi/K7K-QCUrioY/hqdefault.jpg)](https://www.youtube.com/embed/K7K-QCUrioY)

## Features

### Unique Classes
- **5+ unique classes** with different weapons and abilities
- Different playstyles to suit different player preferences

### Player Stats
- Persistent player stats such as kills, deaths, and wins
- Supports **MongoDB** for data storage

### Map Creation
- Create your own maps with JSON configuration
- _In-game map setup coming soon..._

### Multiple Game Support
- Support for multiple concurrent games with different maps and settings
- Temporary world creation and cleanup for each game instance

## Commands
`/bb lobby` (bouncybullets.commands.lobby) - Join the lobby

`/bb stats` (bouncybullets.commands.stats) - View your stats

`/bb game create <map> <game_time> <min_players> <max_players>` (bouncybullets.commands.game.create) - Create a game with the specified game settings

`/bb game join` (bouncybullets.commands.game.join) - Opens a menu to join a game

`/bb game info [game_id]` (bouncybullets.commands.game.info) - Get info about the game

`/bb game state <next/prev> [game_id]` (bouncybullets.commands.game.state) - Go forward or backward in the game's state sequence

`/bb game list` (bouncybullets.commands.game.list) - List all active games

`/bb map list` (bouncybullets.commands.map.list) - List all maps

`/bb map reload` (bouncybullets.commands.map.reload) - Reload maps from config
