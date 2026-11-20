# FlowTrails

---------------------------------------------------------------

![GitHub Downloads](https://img.shields.io/github/downloads/mesoooo/FlowTrails/total?label=GitHub&color=blue)
![Minecraft](https://img.shields.io/badge/Minecraft-1.21.11-04b54b)
![Fabric](https://img.shields.io/badge/Loader-Fabric-dbb69b)
![License](https://img.shields.io/badge/License-MIT-b434eb)

Smooth, glowing motion trails that flow from players' hands and feet.

Client-side only, so it works on any server.

---------------------------------------------------------------

## What Version Should I Get?

| What You Have | What to get                                                                                  |
|:--------------|:---------------------------------------------------------------------------------------------|
| 1.21.11       | [FlowTrails-1.0.0.jar](https://github.com/mesoooo/FlowTrails/releases/latest) *recommended |
| ..lower       | not supported                                                                                |

## Features

- Trails from both hands and feet that follow walking, sprinting, crouching and attacks
- Smooth curves at any FPS with a soft, fading tail
- Glow, gradient and rainbow modes
- Custom colour, opacity, width and length
- Show trails on yourself, other players or both

### Keybindings

-----------------------------------------

| **Keybinding** | **Key Name** | **Description**      |
|----------------|--------------|:---------------------|
| K              | `K`          | Open trail settings  |
| UNBOUND        | -            | Toggle trails on/off |

### Settings

-----------------------------------------

|     **Setting**     |                **Description**                 |
|:-------------------:|:----------------------------------------------:|
|       Trails        |           Turn all trails on or off            |
|    Other Players    |          Show trails on other players          |
|      Yourself       |      Show your own trails in third person      |
| Self in 1st Person  |      Show your own trails in first person      |
|     Hand Trails     |          Emit trails from both hands           |
|     Foot Trails     |           Emit trails from both feet           |
|    Max Distance     |      How far away players can get trails       |
|       Opacity       |            How see-through it is               |
|        Width        |                Trail thickness                 |
|       Length        |        How long the trail lingers for          |
|        Glow         |           Soft glowing halo effect             |
|       Colour        |              Trail colour (RGB)                |
|       Rainbow       |          Cycle through every colour            |
|      Gradient       |   Fade into a second colour along the trail    |

## For this Mod You Will Need
```yml
Minecraft Java Edition: 1.21.11
Fabric Loader: 0.20.1 or higher
Fabric API: 1.21.11
Mod Menu: optional
```

## Newest Changes
```yml
Version: 1.0.0

Added:
  - hand and foot trails
  - glow, gradient and rainbow modes
  - in-game settings screen
  - mod menu support
```

## Building
```sh
git clone https://github.com/mesoooo/FlowTrails.git
cd FlowTrails
./gradlew build
```
The jar will be in `build/libs/`.

## Have A Good One!
- Thanks for downloading FlowTrails!
- Make sure to give it a star on [GitHub](https://github.com/mesoooo/FlowTrails)

## License
FlowTrails is licensed under the [MIT License](LICENSE).
