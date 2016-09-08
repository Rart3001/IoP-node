package org.iop.version_1.structure;

import org.apache.log4j.Logger;
import org.eclipse.jetty.annotations.ServletContainerInitializersStarter;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.iop.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import org.iop.version_1.structure.channels.endpoinsts.clients.FermatWebSocketClientChannelServerEndpoint;
import org.iop.version_1.structure.util.ConfigurationManager;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * The class <code>com.bitdubai.fermat_p2p_plugin.layer.ws.communications.cloud.server.developer.bitdubai.version_1.structure.jetty.JettyEmbeddedAppServer</code>
 * is the application webapp server to deploy the webapp socket server</p>
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 08/01/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */

public class JettyEmbeddedAppServer {

    /**
     * Represent the logger instance
     */
    private static Logger LOG = Logger.getLogger("debugLogger");

    /**
     * Represent the DEFAULT_CONTEXT_PATH value (/fermat)
     */
    public static final String DEFAULT_CONTEXT_PATH = "/iop-node";

    /**
     * Represent the DEFAULT_PORT number
     */
    public static final int DEFAULT_PORT = 15400;

    /**
     * Represent the DEFAULT_IP number
     */
    public static final String DEFAULT_IP = "0.0.0.0";

    /**
     * Represent the JettyEmbeddedAppServer instance
     */
    private static JettyEmbeddedAppServer instance;

    /**
     * Represent the server instance
     */
    private Server server;

    /**
     * Represent the webapp socket server container instance
     */
    private ServerContainer wsServerContainer;

    /**
     * Represent the ServletContextHandler instance
     */
    private ServletContextHandler servletContextHandler;

    /**
     * Represent the ServerConnector instance
     */
    private ServerConnector serverConnector;

    /**
     * Constructor
     */
    private JettyEmbeddedAppServer() {
        super();
    }

    /**
     * Initialize and configure the server instance
     *
     * @throws IOException
     * @throws DeploymentException
     * @throws ServletException
     */
    private void initialize() throws IOException, DeploymentException, ServletException, URISyntaxException {

        LOG.info("Initializing the internal Server");

        Log.setLog(new Slf4jLog(Server.class.getName()));

        /*
         * Create and configure the server
         */
        this.server = new Server();
        this.serverConnector = new ServerConnector(server);
        this.serverConnector.setReuseAddress(Boolean.TRUE);

        int port = ConfigurationManager.getValue(ConfigurationManager.PORT) != null ? new Integer(ConfigurationManager.getValue(ConfigurationManager.PORT).trim()) : DEFAULT_PORT;
        String ip = ConfigurationManager.getValue(ConfigurationManager.INTERNAL_IP) != null ? ConfigurationManager.getValue(ConfigurationManager.INTERNAL_IP) : DEFAULT_IP;

        LOG.info("Server configure ip = " + ip);
        LOG.info("Server configure port = " + port);

        this.serverConnector.setHost(ip);
        this.serverConnector.setPort(port);
        this.server.addConnector(serverConnector);

        /*
         * Setup the basic application "context" for this application at "/fermat"
         */
        this.servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        this.servletContextHandler.setContextPath(JettyEmbeddedAppServer.DEFAULT_CONTEXT_PATH);
        this.server.setHandler(servletContextHandler);

        /*
         * Initialize webapp layer
         */
        WebAppContext webAppContext = new WebAppContext();
        webAppContext.setContextPath(JettyEmbeddedAppServer.DEFAULT_CONTEXT_PATH);
        webAppContext.setDescriptor("./webapp/WEB_INF/webapp.xml");
        webAppContext.setResourceBase("./webapp");
        webAppContext.addBean(new ServletContainerInitializersStarter(webAppContext), true);
        webAppContext.setWelcomeFiles(new String[]{"index.html"});
//        webAppContext.addFilter(AdminRestApiSecurityFilter.class, "/rest/api/v1/admin/*", EnumSet.of(DispatcherType.REQUEST));
        webAppContext.setInitParameter("org.eclipse.jetty.servlet.Default.dirAllowed", "false");
        servletContextHandler.setHandler(webAppContext);
        server.setHandler(webAppContext);

        /*
         * Initialize javax.websocket layer
         */
        this.wsServerContainer = WebSocketServerContainerInitializer.configureContext(webAppContext);
        this.wsServerContainer.setDefaultMaxSessionIdleTimeout(FermatWebSocketChannelEndpoint.MAX_IDLE_TIMEOUT);

        LOG.info("WebSocketServerContainer Default Max Session Idle Timeout = "+wsServerContainer.getDefaultMaxSessionIdleTimeout() + " milliseconds");
        LOG.info("WebSocketServerContainer Default Max Binary Message Buffer Size = "+wsServerContainer.getDefaultMaxBinaryMessageBufferSize() + " Bytes");
        LOG.info("WebSocketServerContainer Default Max Text Message Buffer Size = "+wsServerContainer.getDefaultMaxTextMessageBufferSize() + " Bytes");

        /*
         * Add WebSocket endpoint to javax.websocket layer
         */
//        this.wsServerContainer.addEndpoint(FermatWebSocketNodeChannelServerEndpoint.class);
        this.wsServerContainer.addEndpoint(FermatWebSocketClientChannelServerEndpoint.class);
        this.server.dump(System.err);

    }

    /**
     * Start the server instance
     *
     * @throws Exception
     */
    public void start() throws Exception {

        this.initialize();
        LOG.info("Starting the internal server");
        this.server.start();
        LOG.info("Server URI = " + this.server.getURI());
        this.server.join();

    }


    /**
     * Get the instance value
     *
     * @return instance current value
     */
    public synchronized static JettyEmbeddedAppServer getInstance() {
        if (instance == null) {
            instance = new JettyEmbeddedAppServer();
        }
        return instance;
    }

}