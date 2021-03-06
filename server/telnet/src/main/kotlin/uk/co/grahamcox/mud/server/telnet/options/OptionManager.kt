package uk.co.grahamcox.mud.server.telnet.options

/**
 * System by which we can manage the states of all of the Telnet Options for a single connection
 * @property clientOptions The list of all options that we want to support that are requested by the client
 * @property serverOptions The list of all options that we want to support that are requested by the server
 */
class OptionManager(val clientOptions: Map<TelnetOption, InitialStatus>,
                    val serverOptions: Map<TelnetOption, InitialStatus>) {
    /** Enumeration of the initial status to request for the options */
    enum class InitialStatus {
        ENABLED,
        DISABLED
    }

    /** The map of Client Options, so that we can look them up by ID */
    private val clientOptionsIdMap = clientOptions.keys.map { option -> option.optionId to option }.toMap()
    /** The map of Server Options, os that we can look them up by ID */
    private val serverOptionsIdMap = serverOptions.keys.map { option -> option.optionId to option }.toMap()

    /** The map of Client Options, so that we can look them up by type */
    private val clientOptionsMap = clientOptions.keys.map { option -> option.javaClass to option }.toMap()
    /** The map of Server Options, so that we can look them up by type */
    private val serverOptionsMap = serverOptions.keys.map { option -> option.javaClass to option }.toMap()

    /**
     * Get the Client Option that has the given Option ID. If none is known then null is returned
     * @param optionId The ID of the option
     * @return the option, or null if not known
     */
    fun getClientOption(optionId: Byte) = clientOptionsIdMap.get(optionId)

    /**
     * Get the Client Option that has the given type. If none is known then an error is thrown
     * @param optionClass The class of the option
     * @return the option, or null if not known
     */
    fun getClientOption(optionClass: Class<*>) = clientOptionsMap.get(optionClass) ?: throw IllegalArgumentException("No option of that type is known: ${optionClass}")

    /**
     * Get the Server Option that has the given Option ID. If none is known then null is returned
     * @param optionId The ID of the option
     * @return the option, or null if not known
     */
    fun getServerOption(optionId: Byte) = serverOptionsIdMap.get(optionId)

    /**
     * Get the Server Option that has the given type. If none is known then an error is thrown
     * @param optionClass The class of the option
     * @return the option, or null if not known
     */
    fun getServerOption(optionClass: Class<*>) = serverOptionsMap.get(optionClass) ?: throw IllegalArgumentException("No option of that type is known: ${optionClass}")
}
