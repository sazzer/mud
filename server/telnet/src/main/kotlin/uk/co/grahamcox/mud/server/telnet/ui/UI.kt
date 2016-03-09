package uk.co.grahamcox.mud.server.telnet.ui

import io.netty.channel.socket.SocketChannel
import org.slf4j.LoggerFactory
import uk.co.grahamcox.mud.server.telnet.TelnetMessage
import uk.co.grahamcox.mud.server.telnet.options.NAWSOption
import uk.co.grahamcox.mud.server.telnet.options.OptionManager
import uk.co.grahamcox.mud.server.telnet.options.TelnetOption
import uk.co.grahamcox.mud.server.telnet.options.TerminalTypeOption
import java.util.*

/**
 * The actual entrypoint for the user interface
 * @param configOptionList The list of UI Config Options to work with
 * @param channel The channel to send messages to
 */
class UI(val configOptionList: List<UIConfigOption>,
         private val channel: SocketChannel) {
    /** The logger to use */
    private val LOG = LoggerFactory.getLogger(UI::class.java)

    /** Collection of the Option Statuses that we consider to be terminals. I.e. we now know what we're doing with this option */
    private val TERMINAL_OPTION_STATUSES = EnumSet.of(UIConfigOption.OptionStatus.DISABLED,
            UIConfigOption.OptionStatus.CONFIGURED)

    /** Map of the configuration options that we are using */
    private val configOptions = configOptionList.map { option -> option.javaClass to option }
            .toMap()

    /** Enumeration of the possible configuration statuses */
    private enum class ConfigurationStatus {
        /** We're not yet configured */
        UNKNOWN,
        /** We've timed out waiting for configuration */
        TIMED_OUT,
        /** We're fully configured */
        CONFIGURED
    }

    /** The current configuration status */
    private var configurationStatus = ConfigurationStatus.UNKNOWN
        set(value) {
            // We can only change from UNKNOWN to a value
            if (field == ConfigurationStatus.UNKNOWN) {
                field = value
                selectRenderer()
            }
        }

    init {
        configOptions.values.forEach { option ->
            option.eventManager.registerListener(UIConfigOption.STATUS_CHANGED_EVENT) {
                this.handleOptionChanges()
            }
        }

        class TimedOutTask : TimerTask() {
            override fun run() {
                configurationStatus = ConfigurationStatus.TIMED_OUT
            }
        }

        Timer().schedule(TimedOutTask(), 5000)
    }

    /**
     * Handle when the options change.
     * This is primarially used to signal the fact that we know what we're talking about with regards to the terminal
     * and so we can get on with the processing of the game
     */
    private fun handleOptionChanges() {
        val unconfigured = configOptions.map { entry -> entry.key to entry.value.optionStatus }
            .filter { entry -> !TERMINAL_OPTION_STATUSES.contains(entry.second) }
        LOG.debug("Unconfigured UI Options: {}", unconfigured)

        if (unconfigured.isEmpty()) {
            configurationStatus = ConfigurationStatus.CONFIGURED
        }
    }

    /**
     * Select a renderer to use for this session, now that we have enough information to do so
     */
    private fun selectRenderer() {
        LOG.debug("Received full configuration of session: {}", configurationStatus)
        "Received full configuration of session: ${configurationStatus}\r\n".toByteArray()
            .map { b -> TelnetMessage.ByteMessage(b) }
            .forEach { msg -> channel.write(msg) }
        channel.flush()
    }
}
