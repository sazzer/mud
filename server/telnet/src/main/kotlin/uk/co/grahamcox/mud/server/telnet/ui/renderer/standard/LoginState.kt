package uk.co.grahamcox.mud.server.telnet.ui.renderer.standard

import io.netty.channel.socket.SocketChannel
import kotlin.properties.Delegates

/**
 * Renderer State to handle authentication
 */
@StateName("login")
class LoginState(private val channel: SocketChannel) : RendererState() {
    /**
     * Enumeration of the current input state that we are in
     */
    private enum class InputState {
        /** We are expecting a Username */
        USERNAME,
        /** We are expecting a Password */
        PASSWORD
    }

    /** The current input state that we are in. Note that changing this will send a prompt to the client */
    private var inputState: InputState by Delegates.observable(InputState.USERNAME) { prop, old, new -> showInputPrompt() }

    /** The username that was entered */
    private var username: String? = null

    /** The password that was entered */
    private var password: String? = null

    init {
        showInputPrompt()
    }

    /**
     * Handle receiving a command from the user. What we do with this depends on what field we are next expecting to
     * receive
     * @param command The command to receive
     */
    override fun handleCommand(command: String) {
        if (command.isEmpty()) {
            showInputPrompt()
        } else {
            when (inputState) {
                InputState.USERNAME -> {
                    username = command
                    inputState = InputState.PASSWORD
                }
                else -> {
                    password = command
                    inputState = InputState.USERNAME
                }
            }
        }
    }

    /**
     * Show the input prompt for the state that we are currently in
     */
    private fun showInputPrompt() {
        val prompt = when(inputState) {
            InputState.USERNAME -> "Username: "
            InputState.PASSWORD -> "Password: "
        }

        channel.writeAndFlush(prompt)
    }
}
