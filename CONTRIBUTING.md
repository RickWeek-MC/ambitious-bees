# Contributing

Thanks for wanting to help! A few rules keep the project healthy.

## Clean room: only your own work
Ambitious Bees is a **clean-room** project. Its functionality is based on public descriptions only.
Please **do not submit code, textures, text or other assets copied or derived from other mods**, in
particular from Career Bees or its addons (decompiled code, extracted textures, translated or
reworded source, ...). Pull requests that contain such material will be closed.
By contributing you confirm that your work is your own and that you license it under the project's
[MIT license](LICENSE).

## Reporting bugs
Open an issue with the bug template. Always include:
- Minecraft, NeoForge, Forestry CE and Ambitious Bees versions;
- `latest.log` (on a crash, the crash report) as a file or a paste link;
- the bees, effects and blocks involved, and what you expected to happen.

## Building
1. JDK 21.
2. Put the Forestry CE and Patchouli jars into `libs/` (see `libs/README.txt`; they are git-ignored).
3. `./gradlew build` (jar in `build/libs/`) or run `runClient` / `runServer` from your IDE.

## Code style
- Java 21, tabs for indentation, one top-level class per file.
- Prefer the Forestry API (`forestry.api.*`). Internal Forestry classes are used in a few places;
  keep new uses to a minimum and mention them in the pull request.
- Bee effects extend `TerritoryBeeEffect` or `PedestalBeeEffect` so that they also work with the
  Bee-Bee Gun and respect the config.
- Species and effects are data (`src/main/resources/data/ambitious_bees/`); every species needs
  lang keys and, if it has a custom overlay, an item model per life stage.
