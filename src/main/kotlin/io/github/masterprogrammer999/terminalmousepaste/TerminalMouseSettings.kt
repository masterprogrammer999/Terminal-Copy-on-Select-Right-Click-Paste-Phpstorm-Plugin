package io.github.masterprogrammer999.terminalmousepaste

import com.intellij.openapi.components.*

@Service(Service.Level.APP)
@State(name = "TerminalMouseSettings", storages = [Storage("terminalMousePaste.xml")])
class TerminalMouseSettings : SimplePersistentStateComponent<TerminalMouseSettings.State>(State()) {
    class State : BaseState() {
        var copyOnSelect by property(true)
        var pasteOnRightClick by property(true)
    }

    companion object {
        fun getInstance(): TerminalMouseSettings = service()
    }
}
