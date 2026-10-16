package io.github.masterprogrammer999.terminalmousepaste

import com.intellij.ide.DataManager
import com.intellij.openapi.Disposable
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.ActionUiKind
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ex.ActionUtil
import com.intellij.openapi.components.Service
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.EditorMouseEvent
import com.intellij.openapi.editor.event.EditorMouseEventArea
import com.intellij.openapi.editor.event.EditorMouseListener
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.ProjectActivity
import java.awt.datatransfer.StringSelection
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.SwingUtilities

class TerminalMouseStartup : ProjectActivity {
    override suspend fun execute(project: Project) {
        service<TerminalMouseService>().install()
    }
}

/**
 * Registers one app-wide listener. Registering per project would paste once per open project.
 */
@Service(Service.Level.APP)
class TerminalMouseService : Disposable {
    private val installed = AtomicBoolean(false)

    fun install() {
        if (!installed.compareAndSet(false, true)) return
        // The multicaster is attached to every editor at creation, before the terminal adds its own
        // mouse listeners, so this listener sees the event first.
        EditorFactory.getInstance().eventMulticaster.addEditorMouseListener(Listener(), this)
    }

    override fun dispose() = Unit

    private class Listener : EditorMouseListener {
        override fun mousePressed(event: EditorMouseEvent) {
            if (!isPasteClick(event)) return
            val paste = terminalPasteEvent(event) ?: return
            // EditorImpl skips caret/selection handling and the context menu when the event is consumed
            // (see EditorImpl.runMousePressedCommand / invokePopupIfNeeded). On Linux the popup trigger is the press.
            event.consume()
            // The terminal's own Paste action: bracketed paste, scroll to cursor, prompt handling.
            ActionUtil.performAction(paste.first, paste.second)
            // Consuming the press also skips the click's usual selection reset. A leftover selection keeps
            // Terminal.CopySelectedText enabled, and that action owns Ctrl+C, so ^C would never reach the shell.
            event.editor.selectionModel.removeSelection()
        }

        override fun mouseReleased(event: EditorMouseEvent) {
            val mouse = event.mouseEvent
            when {
                // Windows shows the popup on release; swallow it there too.
                SwingUtilities.isRightMouseButton(mouse) ->
                    if (isPasteClick(event) && terminalPasteEvent(event) != null) event.consume()
                // Copy once when the drag / double-click / triple-click finishes, not on every selection change.
                SwingUtilities.isLeftMouseButton(mouse) ->
                    if (settings().copyOnSelect && terminalPasteEvent(event) != null) copySelection(event.editor)
            }
        }

        /** Plain right-click. Shift+right-click falls through to the normal context menu. */
        private fun isPasteClick(event: EditorMouseEvent): Boolean =
            settings().pasteOnRightClick &&
                SwingUtilities.isRightMouseButton(event.mouseEvent) &&
                !event.mouseEvent.isShiftDown &&
                event.area == EditorMouseEventArea.EDITING_AREA

        /**
         * The terminal's Paste action and an event for it, or null when [event] is not in a Reworked terminal.
         *
         * Terminal.Paste enables itself only in Reworked terminal editors, so asking it is how we detect the
         * terminal without depending on the terminal plugin's internal or experimental API.
         */
        private fun terminalPasteEvent(event: EditorMouseEvent): Pair<AnAction, AnActionEvent>? {
            val action = ActionManager.getInstance().getAction("Terminal.Paste") ?: return null
            val actionEvent = AnActionEvent.createEvent(
                DataManager.getInstance().getDataContext(event.editor.contentComponent),
                action.templatePresentation.clone(),
                ActionPlaces.MOUSE_SHORTCUT,
                ActionUiKind.NONE,
                event.mouseEvent,
            )
            ActionUtil.updateAction(action, actionEvent)
            return if (actionEvent.presentation.isEnabledAndVisible) action to actionEvent else null
        }

        private fun copySelection(editor: Editor) {
            val text = editor.selectionModel.getSelectedText(true)?.takeIf { it.isNotEmpty() } ?: return
            // CLIPBOARD (Ctrl+V), not PRIMARY. The IDE's own handler still fills PRIMARY on Linux.
            CopyPasteManager.getInstance().setContents(StringSelection(text))
        }

        private fun settings(): TerminalMouseSettings.State = TerminalMouseSettings.getInstance().state
    }
}
