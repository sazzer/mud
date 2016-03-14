package uk.co.grahamcox.mud.server.telnet.ui.renderer.standard

import org.slf4j.LoggerFactory

/**
 * Mechanism to receive input bytes and optionally return commands to perform
 */
class InputHandler {
    /** The logger to use */
    private val LOG = LoggerFactory.getLogger(InputHandler::class.java)

    /** The byte that represents a Carriage Return */
    private val CR_BYTE = 0x0d.toByte()

    /** The byte that represents a Line Feed */
    private val LF_BYTE = 0x0a.toByte()

    /** Indication of the state that we're in as far as receiving messages */
    private enum class State {
        /** No special state */
        NONE,
        /** The last byte was a CR */
        CR,
        /** The last byte was an LF */
        LF
    }

    /** The message that we've so far received */
    private val message: MutableList<Byte> = arrayListOf()

    /** The current state that we're in for receiving input */
    private var state: State = State.NONE

    /**
     * Handle when we've received an actual byte from the client
     * @param byte The byte that was received
     */
    fun receiveByte(byte: Byte) {
        when (state) {
            State.NONE -> {
                when (byte) {
                    CR_BYTE -> state = State.CR
                    LF_BYTE -> state = State.LF
                    else -> message.add(byte)
                }
            }
            State.CR -> {
                state = State.NONE
                when (byte) {
                    LF_BYTE -> {
                        handleCommand(message)
                        message.clear()
                    }
                    else -> {
                        message.add(byte)
                    }
                }
            }
            State.LF -> {
                state = State.NONE
                when (byte) {
                    CR_BYTE -> {
                        handleCommand(message)
                        message.clear()
                    }
                    else -> {
                        message.add(byte)
                    }
                }
            }
        }
    }

    /**
     * Handle a fully received command
     * @param command The fully received command
     */
    private fun handleCommand(command: List<Byte>) {
        LOG.info("Received command: {}", command)
    }
}
