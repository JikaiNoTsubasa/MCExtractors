# MCExtractors

A Minecraft Forge mod for 1.20.1 that adds specialized extraction machines.

## Features

### Iron Extractor

The Iron Extractor is a machine that processes cobblestone and cobbled deepslate to extract iron nuggets.

**Crafting Recipe:**
```
Iron    Iron    Iron
Iron    Furnace Iron
Iron    Piston  Iron
```

**How it works:**
- **Input:** Cobblestone or Cobbled Deepslate
- **Fuel:** Redstone Dust
- **Output:** Iron Nuggets (90% success rate)
- **Processing Time:** 10 seconds per item (200 ticks)
- **Fuel Duration:** 1 redstone dust = 80 seconds (1600 ticks)

**Features:**
- 3D custom model with animated states (on/off)
- Light emission when active (level 13)
- Animated particles (redstone dust and smoke)
- Directional block (rotates when placed)
- Hopper compatible:
  - **Sides:** Insert cobblestone and redstone
  - **Bottom:** Extract iron nuggets
  - **Top:** No interaction

**Stats:**
- 90% chance to extract an iron nugget
- 10% chance to get nothing
- Can store up to 3 output slots (192 iron nuggets max)

## Compatibility

### Create Mod
The Iron Extractor is fully compatible with the Create mod's funnels and mechanical automation:
- **Funnels can insert:** Cobblestone and redstone from the sides
- **Funnels can extract:** Iron nuggets from the bottom
- Works with mechanical belts, chutes, and other Create logistics

## Technical Details

- **Minecraft Version:** 1.20.1
- **Forge Version:** 47.2.0
- **Java Version:** 17
- **Mod Version:** 1.0.0

## Building

```bash
./gradlew build
```

The compiled JAR will be in `build/libs/mcextractors-1.0.0.jar`

## Installation

1. Install Minecraft Forge 1.20.1 (version 47.2.0 or higher)
2. Download the mod JAR file
3. Place it in your `mods` folder
4. Launch Minecraft

## Credits

- **Author:** Jikai
- **Model:** Created with Blockbench
- **License:** MIT

## Development

This mod was created using Minecraft Forge's deferred register system and follows best practices for mod development.

### Project Structure
```
src/main/java/com/mcextractors/
├── MCExtractors.java          # Main mod class
├── blocks/                    # Block classes
│   └── IronExtractorBlock.java
├── blockentities/             # Block entity logic
│   └── IronExtractorBlockEntity.java
├── menu/                      # Container menus
│   └── IronExtractorMenu.java
├── client/                    # Client-side code
│   ├── ClientSetup.java
│   └── screen/
│       └── IronExtractorScreen.java
└── init/                      # Registration classes
    ├── ModBlocks.java
    ├── ModBlockEntities.java
    ├── ModMenuTypes.java
    └── ModItems.java
```
