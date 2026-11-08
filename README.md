# POH Loading Screen

A RuneLite plugin that replaces the broken-down house picture you see while your player-owned house loads. Instead you get an original OSRS meme card, a quick tip or a "Did you know?" fact, in the game's own font and colours.

![Loading screen preview](docs/screenshot.png)

## Features
- **100 original meme cards**: OSRS item sprites drawn by your own RuneLite client, with original captions. No images are downloaded or bundled.
- **148 tips and facts**, checked against the [OSRS Wiki](https://oldschool.runescape.wiki). Both are dealt like a shuffled deck, so you won't see a repeat until you've seen them all.
- Choose memes, tips or a mix. Type `::pohmeme` in chat to preview a meme card at any time.
- Text shrinks automatically to fit both fixed and resizable mode.

## Settings
| Setting | Default | What it does |
|---|---|---|
| Show | Meme cards | Meme cards, tips & facts, or a mix |
| Show quick tips | On | Useful gameplay tips |
| Show 'Did you know?' facts | On | Trivia and history |
| Text size | 24 | 16 is the normal in-game size |
| Also post tip in chat | Off | Posts the tip as a game message |

## Adding tips or memes
- Tips: edit `src/main/resources/com/pohloadingtips/tips.txt`, one per line, as `TIP|Category|Text` or `FACT|Category|Text`.
- Memes: edit `scripts/memes-source.txt` (`Item name | TOP TEXT | BOTTOM TEXT`), then run `node scripts/resolve-memes.mjs` to look up the item ids.

## Credits
Tips and facts are written from information on the [Old School RuneScape Wiki](https://oldschool.runescape.wiki). Meme captions are original.

## Running
You need JDK 11 or newer. Run `./gradlew run` (or `gradlew.bat run` on Windows) to launch RuneLite in developer mode with the plugin loaded.

## Disclaimer
Created using intellectual property belonging to Jagex Limited under the terms of Jagex's Fan Content Policy. This content is not endorsed by or affiliated with Jagex.
