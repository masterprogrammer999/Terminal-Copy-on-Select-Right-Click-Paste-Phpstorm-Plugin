# Terminal Copy on Select & Right-Click Paste

<img src="src/main/resources/META-INF/pluginIcon.svg" width="80" alt="Plugin icon">

A JetBrains IDE plugin that gives the built-in terminal the mouse behaviour of most Linux terminals and PuTTY:

- **Select to copy.** Text you select with the mouse (drag, double-click, triple-click) is copied to the clipboard straight away.
- **Right-click to paste.** Right-click pastes the clipboard instead of opening the context menu.
- **Shift+right-click** still opens the normal context menu.

You can turn each behaviour on or off under **Settings | Tools | Terminal Copy on Select & Right-Click Paste**.

> **This project is AI-assisted.** It was written with the help of Claude (an AI model by Anthropic), then tested and published by the maintainer. Please report bugs through [GitHub issues](https://github.com/masterprogrammer999/Terminal-Copy-on-Select-Right-Click-Paste-Phpstorm-Plugin/issues).

## Compatibility

- Any JetBrains IDE with the bundled Terminal plugin: PhpStorm, IntelliJ IDEA, WebStorm, PyCharm, GoLand, and others.
- Version 2025.2 or newer.
- Only the **Reworked** terminal (Settings | Tools | Terminal | Terminal engine). The Classic terminal is not changed.
- Checked with the JetBrains Plugin Verifier against PhpStorm 2025.2.6.2, 2025.3 and 2026.2.3.

The plugin uses only the stable platform API. It relies on the terminal's *Paste* action (`Terminal.Paste`), so if a future IDE release renames that action the plugin stops doing anything; please open an issue if that happens.

## Install

From the JetBrains Marketplace: **Settings | Plugins | Marketplace**, search for *Terminal Copy on Select & Right-Click Paste*.

From a zip file: download it from the [Releases page](https://github.com/masterprogrammer999/Terminal-Copy-on-Select-Right-Click-Paste-Phpstorm-Plugin/releases) (or build it yourself, see below), then go to **Settings | Plugins | ⚙ | Install Plugin from Disk…** and pick the zip.

## Building from source

You need JDK 21. Nothing else needs to be installed: the Gradle wrapper downloads Gradle, and the build downloads the IDE SDK.

```bash
./gradlew buildPlugin     # builds build/distributions/terminal-mouse-paste-<version>.zip
./gradlew runIde          # opens a test PhpStorm 2025.2 with the plugin installed (first run downloads ~1 GB)
./gradlew verifyPlugin    # checks compatibility with the IDE versions listed in build.gradle.kts
```

If `java` on your PATH is not version 21, put `JAVA_HOME=/path/to/jdk-21` in front of the command.

### What is `gradlew`?

[Gradle](https://gradle.org) is the build tool: it compiles the Kotlin code, downloads dependencies and packages the plugin zip. `gradlew` ("Gradle wrapper") is a small script committed to the repository. The first time you run it, it downloads the exact Gradle version this project needs (set in `gradle/wrapper/gradle-wrapper.properties`) into `~/.gradle`, then runs it. That way nobody has to install Gradle, and everyone builds with the same version. Use `./gradlew` on Linux and macOS and `gradlew.bat` on Windows.

## Publishing a new version (maintainer notes)

1. Bump `version` in `build.gradle.kts` and update `changeNotes`.
2. Run `./gradlew verifyPlugin`. When a new IDE version is out, add it to `pluginVerification` first.
3. Create a Marketplace token at <https://plugins.jetbrains.com/author/me/tokens> (do this once, and keep it secret).
4. Run `PUBLISH_TOKEN=<your token> ./gradlew publishPlugin`.

The very first release has to be uploaded by hand on <https://plugins.jetbrains.com>. Later versions can use `publishPlugin`.

## How it works

The plugin adds one app-wide editor mouse listener. The Reworked terminal is drawn by an IDE editor, so the listener sees terminal clicks before the terminal does.

- When the left button is released in a terminal, it copies the current selection to the clipboard.
- On a right-click, it consumes the event, so the context menu doesn't open, and runs the terminal's own *Paste* action. That keeps bracketed-paste handling and scrolling the same as Ctrl+Shift+V. It then clears the selection: while text is selected, the terminal treats Ctrl+C as *Copy* instead of sending ^C.
- To tell a terminal apart from a normal code editor, it asks the terminal's *Paste* action whether it is enabled at the click position. That action only enables itself inside the Reworked terminal.

## License

[GNU General Public License v3.0](LICENSE). You may use, change and share this plugin freely. If you distribute a modified version, it must also be under the GPL with its source available.
