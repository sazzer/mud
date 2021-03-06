package uk.co.grahamcox.mud.server.telnet.netty

import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import uk.co.grahamcox.mud.server.telnet.spring.ConnectionScope

/**
 * Channel Initializer for the MUD
 * @param connectionScope The connection scope, to make active as we register a new channel
 */
class MudServerInitializer(private val connectionScope: ConnectionScope) :
        ChannelInitializer<SocketChannel>(), ApplicationContextAware {
    
    /** The logger to use */
    private val LOG = LoggerFactory.getLogger(MudServerInitializer::class.java)

    /** The actual Spring Application Context */
    private lateinit var springApplicationContext: ApplicationContext

    /**
     * Set the Application Context so that we can get the Channel Handlers at channel registration time
     * @param applicationContext The application context to use
     */
    override fun setApplicationContext(applicationContext: ApplicationContext) {
        this.springApplicationContext = applicationContext
    }

    /**
     * Initialize the provided channel
     * @param channel The channel to initialize
     */
    override fun initChannel(channel: SocketChannel) {
        LOG.info("Received a new connection from {}", channel)
        connectionScope.setActiveConnection(channel)

        val handlers = springApplicationContext.getBean("channelHandlers", List::class.java) as List<ChannelHandler>
        handlers.forEach { handler -> channel.pipeline().addLast(handler) }

        connectionScope.clearActiveConnection()
    }
}
