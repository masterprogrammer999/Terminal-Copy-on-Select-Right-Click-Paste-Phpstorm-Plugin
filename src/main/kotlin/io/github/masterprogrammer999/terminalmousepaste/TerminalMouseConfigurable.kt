package io.github.masterprogrammer999.terminalmousepaste

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindSelected
import com.intellij.ui.dsl.builder.panel

class TerminalMouseConfigurable : BoundConfigurable("Terminal Copy on Select & Right-Click Paste") {
    override fun createPanel(): DialogPanel = panel {
        val state = TerminalMouseSettings.getInstance().state
        row {
            checkBox("Copy selected text to the clipboard")
                .bindSelected(state::copyOnSelect)
        }
        row {
            checkBox("Paste with right-click")
                .bindSelected(state::pasteOnRightClick)
                .comment("Shift+right-click still opens the terminal's context menu.")
        }
    }
}
