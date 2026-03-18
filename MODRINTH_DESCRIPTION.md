# Seamless Crafting

Seamless Crafting lets you craft with items stored in nearby chests and other nearby inventories instead of moving every ingredient into your player inventory first.

Open a crafting table or your inventory recipe book, and the mod will scan nearby storage, count available ingredients, and update recipe availability to match what you can actually craft.

## Features

- Craft using ingredients from nearby inventories while keeping the vanilla crafting flow.
- Updates the recipe book so recipes become craftable when nearby storage contains the missing items.
- Adds a nearby-items panel with item counts, search, scrolling, and sorted entries.
- Click an item in the nearby panel to locate the storage that contains it.
- Highlights chests in the world, including double chests.
- Optional distance labels and locate-trail effects for faster chest finding.
- Includes a cancel button that returns nearby-sourced ingredients back to storage.
- Supports a quick spacebar shortcut to fill one set from the selected recipe.
- Configurable refresh timing, scan radius, highlight color, opacity, duration, and panel defaults.

## How It Works

1. Open a crafting table or your inventory recipe book.
2. The mod scans nearby inventories within the configured radius.
3. Nearby items appear in the side panel and count toward recipe availability.
4. When you craft, ingredients can be pulled from nearby storage.
5. If you need to undo a fill, use the cancel button to send nearby items back.

## Dependencies

Required:

- Minecraft 1.21.11
- Fabric Loader 0.18.4 or newer
- Fabric API 0.141.1+1.21.11

Optional:

- Mod Menu for an in-game config screen

## Configuration

If Mod Menu is installed, you can configure:

- Nearby scan radius
- Highlight color
- Highlight opacity
- Highlight duration
- Distance labels
- Highlighter toggle
- Snap aim to chest
- Locate-trail toggle and particle style
- Nearby panel default open state
- Auto-refresh interval

On singleplayer, your local config controls the scan radius. On a dedicated server, the server config controls the actual nearby-inventory range.

## Notes

- The mod is built around normal vanilla crafting screens rather than adding a global shared inventory.
- Chest highlighting is supported explicitly, including double chests.
- Nearby item access depends on loaded chunks inside the configured radius.

## Bugs and Support

If you find a bug, please open an issue on GitHub:

- Issues: https://github.com/DerkOttersberg/seamless-crafting/issues
- Source: https://github.com/DerkOttersberg/seamless-crafting

Bug reports are much easier to fix if you include:

- Minecraft version
- Fabric Loader version
- Fabric API version
- Mod version
- Steps to reproduce the problem
- Latest log or crash report, if available

## Why Use It

Seamless Crafting keeps crafting fast and vanilla-friendly. You still use the normal crafting UI, but the ingredients around you finally matter.
