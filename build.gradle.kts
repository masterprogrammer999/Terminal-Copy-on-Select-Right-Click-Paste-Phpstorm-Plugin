import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.2.20"
    id("org.jetbrains.intellij.platform") version "2.19.0"
}

group = "io.github.masterprogrammer999"
version = "1.0.0"

repositories {
    mavenCentral()
    intellijPlatform { defaultRepositories() }
}

kotlin { jvmToolchain(21) }

dependencies {
    intellijPlatform {
        // Compile against the oldest supported release. 2025.2 is the first with the Reworked terminal
        // and its Terminal.Paste action, which is all the plugin relies on.
        phpstorm("2025.2.6.2")
        bundledPlugin("org.jetbrains.plugins.terminal")
    }
}

intellijPlatform {
    pluginConfiguration {
        description = """
            <p>Copy on select and right-click paste for the built-in terminal, like PuTTY and most Linux terminals:</p>
            <ul>
              <li><b>Select to copy</b>: text you select with the mouse (drag, double-click, triple-click) goes straight to the clipboard.</li>
              <li><b>Right-click to paste</b>: right-click pastes the clipboard instead of opening the context menu.</li>
              <li><b>Shift+right-click</b> still opens the normal context menu.</li>
            </ul>
            <p>Each behaviour can be turned off under <i>Settings | Tools | Terminal Copy on Select &amp; Right-Click Paste</i>.
            Works in any JetBrains IDE with the Terminal plugin, 2025.2 or newer. The Classic terminal is not affected.</p>
            <p>This plugin was written with AI assistance (Claude by Anthropic).</p>
        """.trimIndent()
        changeNotes = """
            <p>1.0.0: first public release.</p>
            <ul>
              <li>Copy on select and paste on right-click in the Reworked terminal.</li>
              <li>Separate settings for each behaviour.</li>
              <li>Shift+right-click opens the context menu.</li>
              <li>Pasting clears the selection, so Ctrl+C right after a paste interrupts the shell instead of copying again.</li>
            </ul>
        """.trimIndent()
        ideaVersion {
            sinceBuild = "252"
            // No upper bound: the plugin uses only a few small APIs. Re-run verifyPlugin for each new IDE release.
            untilBuild = provider { null }
        }
    }

    pluginVerification {
        ides {
            create(IntelliJPlatformType.PhpStorm, "2025.2.6.2")
            create(IntelliJPlatformType.PhpStorm, "2025.3")
            create(IntelliJPlatformType.PhpStorm, "2026.2.3")
        }
    }

    publishing {
        // Create a token at https://plugins.jetbrains.com/author/me/tokens, then:
        // PUBLISH_TOKEN=... ./gradlew publishPlugin
        token = providers.environmentVariable("PUBLISH_TOKEN")
    }
}
