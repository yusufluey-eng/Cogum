# LAST ZONE — Godot 4 prototype

This is a **new Godot 4 3D prototype** in the `last-zone-godot` branch, separate from the original Java Android shooter. It is not yet a built APK.

Open `godot/project.godot` in Godot 4.3+ with the Compatibility renderer and run the scene.

Implemented: 3D world, first-person camera, basic rifle mesh, raycast shooting, five capsule enemies, HUD, touch joystick, right-side swipe look, fire and reload controls.

Not implemented: final cinematic icon asset, enemy AI, voice acting, production graphics, online multiplayer, Android build/signing, device testing. The HUD is currently laid out for a 1280×720 landscape canvas and needs responsive mobile tuning.

Android export requires Godot headless/editor with matching export templates and Android SDK in CI; the existing Gradle workflow builds only the legacy Java app.
