package org.iop.version_1.structure.channels.endpoinsts.clients;

import com.bitdubai.fermat_api.layer.all_definition.crypto.asymmetric.ECCKeyPair;
import com.bitdubai.fermat_api.layer.all_definition.network_service.enums.NetworkServiceType;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ServerHandshakeRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.HeadersAttName;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.eclipse.jetty.websocket.api.MessageTooLargeException;
import org.iop.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import org.iop.version_1.structure.channels.endpoinsts.clients.conf.ClientChannelConfigurator;
import org.iop.version_1.structure.channels.processors.NodesPackageProcessorFactory;
import org.iop.version_1.structure.channels.processors.PackageProcessor;
import org.iop.version_1.structure.context.SessionManager;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;
import org.iop.version_1.structure.util.PackageDecoder;
import org.iop.version_1.structure.util.PackageEncoder;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.endpoinsts.clients.FermatWebSocketClientChannelServerEndpoint</code> this
 * is a especial channel to manage all the communication between the clients and the node
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 12/11/15.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@ServerEndpoint(
        value = "/ws/client-channel",
        configurator = ClientChannelConfigurator.class,
        encoders = {PackageEncoder.class},
        decoders = {PackageDecoder.class}
)
public class FermatWebSocketClientChannelServerEndpoint extends FermatWebSocketChannelEndpoint {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(FermatWebSocketClientChannelServerEndpoint.class));

    /**
     * Constructor
     */
    public FermatWebSocketClientChannelServerEndpoint(){
        super();
    }

    /**
     * (non-javadoc)
     *
     * @see FermatWebSocketChannelEndpoint#getPackageProcessors(PackageType)
     */
    @Override
    protected PackageProcessor getPackageProcessors(PackageType packageType){
        return NodesPackageProcessorFactory.getClientPackageProcessorsByPackageType(packageType);
    }

    /**
     *  Method called to handle a new connection
     *
     * @param session connected
     * @param endpointConfig created
     * @throws IOException
     */
    @OnOpen
    public void onConnect(Session session, EndpointConfig endpointConfig) throws IOException {

        LOG.info(" New connection stablished: " + session.getId());
        LOG.info(" Open sessions: " + session.getOpenSessions().size());

        try {

            /*
             * Get the node identity
             */
            setChannelIdentity((ECCKeyPair) endpointConfig.getUserProperties().get(HeadersAttName.REMOTE_NPKI_ATT_HEADER_NAME));

            /*
             * Get the client public key identity
             */
            String cpki = (String) endpointConfig.getUserProperties().get(HeadersAttName.CPKI_ATT_HEADER_NAME);

          /*  String oldSessionId = JPADaoFactory.getClientDao().getSessionId(cpki);

            if (oldSessionId != null && !oldSessionId.isEmpty()) {

                LOG.warn("oldSessionId found: = " + oldSessionId);

                if (SessionManager.exist(oldSessionId)){
                    Session previousSession = SessionManager.get(oldSessionId);
                    if (previousSession.isOpen()) {
                        previousSession.close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, "Closing a Previous Session"));
                    }
                }
            } */

            SessionManager.add(session);

            /*
             * Construct packet SERVER_HANDSHAKE_RESPONSE
             * the only respond with packageId null is this one
             */
            ServerHandshakeRespond serverHandshakeRespond = new ServerHandshakeRespond(null,ServerHandshakeRespond.STATUS.SUCCESS, ServerHandshakeRespond.STATUS.SUCCESS.toString(), cpki);
            Package packageRespond = Package.createInstance(serverHandshakeRespond.toJson(), PackageType.SERVER_HANDSHAKE_RESPONSE, getChannelIdentity().getPrivateKey(), cpki);

            /*
             * Send the respond
             */
            session.getAsyncRemote().sendObject(packageRespond);


        }catch (Exception e){
            e.printStackTrace();
            LOG.error(e);
            session.close(new CloseReason(CloseReason.CloseCodes.PROTOCOL_ERROR, e.getMessage()));
        }

    }

    /**
     * Method called to handle a new message received
     *
     * @param packageReceived new
     * @param session sender
     */
    @OnMessage
    public Package newPackageReceived(Package packageReceived, Session session) {

        try {

            /*
             * Process the new package received
             */
            return processMessage(packageReceived,session);

        }catch (Exception p){
            LOG.warn("Session: "+session.getId(),p);
        }
        return null;
    }

    @OnMessage
    public void onPongMessage(PongMessage message) {
        LOG.debug("Pong message receive from server = " + message);
    }

    /**
     * Method called to handle a connection close
     *
     * @param closeReason message with the details.
     * @param session     closed session.
     */
    @OnClose
    public void onClose(final CloseReason closeReason,
                        final Session     session    ) {

        LOG.info("Closed session : " + session.getId() + " Code: (" + closeReason.getCloseCode() + ") - reason: " + closeReason.getReasonPhrase());

        try {

            LOG.info(" Open sessions: " + session.getOpenSessions().size());
            LOG.info("Removing session and associate entities");
            SessionManager.remove(session);
            JPADaoFactory.getClientDao().checkOut(session.getId());
            //Este checkout deberia ser más controlado
//            List<String> listActorsCheckingOut = JPADaoFactory.getActorCatalogDao().checkOutAndGet(session.getId());

            //remover eventos que está escuchando la session ya que no va a recibir más.
//            try {
//                JPADaoFactory.getEventListenerDao().removeEventListenersFromSessionId(session.getId());
//            }catch (Exception e){
//                e.printStackTrace();
//            }

            //subscribers
//            try {
//                List<EventListener> listEvent = JPADaoFactory.getEventListenerDao().getEventsForCodeAndConditions(EventOp.EVENT_OP_IS_PROFILE_ONLINE, listActorsCheckingOut);
//                // todo: hacerlo mejor
//                listEvent.forEach(e -> {
//                    try {
//                        EventPublishRespond eventPublishRespond = new EventPublishRespond(true);
//
//                        Session listenerSession = SessionManager.get(e.getSessionId());
//                        LOG.info("Sending event happen to session: "+e.getSessionId());
//                        if (listenerSession != null) {
//                            sendPackage(
//                                    listenerSession,
//                                    UUID.fromString(e.getId()),
//                                    eventPublishRespond.toJson(),
//                                    null,
//                                    PackageType.EVENT_PUBLISH,
//                                    null
//                            );
//                        }else LOG.info("Tenemos un problema en la db, hay eventListener que no se estan borrando cuando su session se cierra y quedan escuchando..");
//                    } catch (IOException e1) {
//                        e1.printStackTrace();
//                    } catch (EncodeException e1) {
//                        e1.printStackTrace();
//                    } catch (Exception e2){
//                        e2.printStackTrace();
//                    }
//                });
//            }catch (Exception e){
//                e.printStackTrace();
//            }

            session.getOpenSessions().remove(session);
            LOG.info(" Open sessions: " + session.getOpenSessions().size());

        } catch (Exception exception) {

            exception.printStackTrace();
        }
    }

    /**
     * Create a new row into the table ProfileRegistrationHistory
     * Method  called to handle a error
     * @param session
     * @param throwable
     */
    @OnError
    public void onError(Session session, Throwable throwable){
        LOG.error("@OnError - Unhandled exception catch");
        throwable.printStackTrace();
        LOG.error(throwable);

        try {

            if (throwable instanceof MessageTooLargeException){
                LOG.warn("No voy a cerrar el canal acá...");
            }else {

                if (session.isOpen()) {
                    LOG.warn("session is open, try to close");
                    session.close(new CloseReason(CloseReason.CloseCodes.UNEXPECTED_CONDITION, throwable.getMessage()));
                } else {
                    LOG.error("The session already close, no try to close");
                }
            }
            SessionManager.remove(session);
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e);
        }
    }


}
