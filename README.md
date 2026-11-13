# POH Loading Screen

A RuneLite plugin that replaces the broken-down house picture you see while your player-owned house loads with an original OSRS meme card.

## Features
- **100 original meme cards**: OSRS item sprites drawn by your own RuneLite client, with original captions. No images are downloaded or bundled.
- Cards are dealt like a shuffled deck, so you won't see a repeat until you've seen them all.
- Type `::pohmeme` in chat to preview a meme card at any time.
- Text shrinks automatically to fit both fixed and resizable mode.

## Settings
| Setting | Default | What it does |
|---|---|---|
| Text size | 24 | Size of the meme captions |

## Adding memes
Edit `scripts/memes-source.txt` (`Item name | TOP TEXT | BOTTOM TEXT`), then run `node scripts/resolve-memes.mjs` to look up the item ids.

## Running
You need JDK 11 or newer. Run `./gradlew run` (or `gradlew.bat run` on Windows) to launch RuneLite in developer mode with the plugin loaded.

## Disclaimer
Created using intellectual property belonging to Jagex Limited under the terms of Jagex's Fan Content Policy. This content is not endorsed by or affiliated with Jagex.
