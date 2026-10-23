# Player Mods — Minecraft 1.21.4 Fabric

Client-side Fabric mod. Press **Right Shift** to open the interface.

## Build
Use Java 21 and run `gradle build`. The installable JAR is created in `build/libs/`; do not use the `-sources.jar`.

## Important limitation
Minecraft does not normally disclose each remote player's complete client mod folder. This initial version lists mods installed locally and connected player names. A complete remote mod list requires an explicit sharing protocol supported by the other players or server; the mod does not silently read files from other computers. Downloads of other players' private files are not possible without their explicit hosting/sharing.
