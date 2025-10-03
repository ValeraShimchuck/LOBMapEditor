Map Editor for Lines Of Battle!

<img width="1911" height="952" alt="image" src="https://github.com/user-attachments/assets/fece431b-1a73-48f1-a9dc-afaaf215f2a3" />

# How to install
1. Head towards releases section [here](https://github.com/ValeraShimchuck/LOBMapEditor/releases).
2. Find your installer depending on your operating system(.exe and .msi for windows, dmg for macos, etc). Or use .jar(you need java 21 in order to run) file of the editor.
3. Install and enjoy the application.

# Features
It has almost all features from the original map editor. Also we added a lot of new ones.

## Better editing experience
Create your high-quality maps in no time.

## Better perfomance
The application uses OpenGL and a lot of heavy tile blending calculations are shifted to GPU. You can even create the biggest available map(200x152 tiles) and it will only have a slight perfomance drop at the beginning.

## Almost identical render
The editor was created with this idea in mind, so what you see in the editor will be the same in the game.

## Improved grid visibility
In the map editor the grid(even thin one) will render correctly whereas official map editor has some grid glitches.

## Open-source
Make your forks or contribute to the project directly heling the community and the project.

## Keybindings
- Ctrl + z and ctrl + shift + z - undo/redo.
- Delete - delets selected units or objective.
- Ctrl + d - duplicates selected units or objective.
- Shift + LMB on a unit - add the clicked unit to selected ones.
- Ctrl + LMB on a selected unit - exclude the clicked unit from the selection.
- LMB drag - selects units within the selection box.
- LMB drag on selected units or objective - Moves selected objects.
- LMB drag on blue arrow - changes the selected units rotation.
- MMB drag - move camera.
- RMB - use current tool.

## A lot of tools
There are 8 tools, in order to use one of them - just click on it.

### Configure Players
Select player you want to edit using `Current player` drop down. You can set players team and amount of ammo, also you can add a new player, duplicate the current one, or delete the current player.

### Height
Select the brush size, height and the brush type(cirle or square) and using RMB draw height onto the map.

### Terrain
Select the brush size, brush type and terrain type and draw terrain onto the map.

### Terrain pick
Click RMB using this tool to pick a terrain type for terrain brush.

### Place unit
Select the unit owner, its name, type and rotation and plate onto the map using RMB.

### Place objective
Select the objective owner, its name and type and place it onto the map using RMB.

### Grid
You can toggle grid, select its thickness, size, offset and even color.

### Reference image
Put a background image on your map as a reference. Toggle it, select its transperency. Also you can change its rotation, size and offset. Use `Import reference` button to add background image or replace the existing one.

## Export/Import
Import your map from json. Or export the game compatible json type.


# Techstack
- OpenGL - API for low-level rendering, gives insane boost in perfomance.
- JOGL - binding for OpenGL for java.
- JOML - Linear algebra library designed for OpenGL.
- Kotlin compose - Modern UI framework for android and desktop applications.
- Jewel UI toolkit - jetbrain-like UI toolkit.
- Kodein - DI library that allow us to wire everything easily.

# Contribution/Building from the source

## In order to initialize the project run:

`./gradlew :composeApp:generateResourceAccessorsForCommonMain`

`./gradlew :composeApp:generateResourceAccessorsForJvmMain`

Also, you might want to run some init tasks like `./gradlew init`.

## In order to run execute:

`./gradlew run`

P.s. If you are using windows then you should use `gradlew.bat` instead of `gradlew`

