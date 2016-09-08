package org.iop.version_1.structure.channels.processors.clients;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ACKRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.STATUS;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import org.iop.version_1.structure.channels.processors.PackageProcessor;
import org.iop.version_1.structure.context.SessionManager;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;
import org.iop.version_1.structure.util.logger.ReportLogger;

import javax.websocket.Session;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.processors.clients.MessageTransmitProcessor</code>
 * process all packages received the type <code>PackageType.MESSAGE_TRANSMIT</code><p/>
 *
 * Created by Roberto Requena - (rart3001@gmail.com) on 30/04/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class MessageTransmitProcessor extends PackageProcessor {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger("debugLogger");

    /**
     * Constructor
     */
    public MessageTransmitProcessor() {
        super(PackageType.MESSAGE_TRANSMIT);
    }

    /**
     * (non-javadoc)
     * @see PackageProcessor#processingPackage(Session, Package, FermatWebSocketChannelEndpoint)
     */
    @Override
    public Package processingPackage(final Session session, final Package packageReceived, final FermatWebSocketChannelEndpoint channel) {

        LOG.info("Processing new package received "+packageReceived.getPackageType());

        final String destinationIdentityPublicKey = packageReceived.getDestinationPublicKey();
        LOG.info("Package destinationIdentityPublicKey =  "+destinationIdentityPublicKey);
        ACKRespond messageTransmitRespond = null;

        try {

            /*
             * Get the connection to the destination
             */
            String actorSessionId = JPADaoFactory.getActorCatalogDao().findValueById(destinationIdentityPublicKey, String.class, "sessionId");

            LOG.info("Actor session id = "+actorSessionId);

            Session sessionDestination = SessionManager.get(actorSessionId);
            LOG.info("Is a Active session = "+(sessionDestination != null ? true : false));

            if (sessionDestination != null) {

                Future<Void> futureResult = null;
                try{

                    futureResult = sessionDestination.getAsyncRemote().sendObject(packageReceived);
                    // wait for completion max 6 seconds
                    futureResult.get(6, TimeUnit.SECONDS);

                    messageTransmitRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.SUCCESS, STATUS.SUCCESS.toString());
                    LOG.info("Message transmit successfully");

                    /**
                     * Report Logger
                     */
                    ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.SUCCESS,packageReceived.toString());

                }catch (TimeoutException | ExecutionException | InterruptedException e){

                    LOG.error("Message cannot be transmitted: ", e);
                    LOG.error("packageReceived  = " + packageReceived);

                    if (e instanceof  TimeoutException){

                        if (futureResult != null){
                            // cancel the message
                            futureResult.cancel(true);
                        }
                        /**
                         * Report Logger
                         */
                        ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.FAIL,packageReceived.toString(),e);

                        return Package.createInstance(
                                packageReceived.getPackageId(),
                                messageTransmitRespond.toJson()                      ,
                                PackageType.ACK                         ,
                                channel.getChannelIdentity().getPrivateKey(),
                                destinationIdentityPublicKey
                        );
                    }

                    messageTransmitRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.FAIL, "Can't send message to destination, error details: "+e.getMessage());

                }

            } else {

                /*
                 * Remove old session
                 */
                if ((actorSessionId != null && !actorSessionId.isEmpty()) && sessionDestination == null){
                    LOG.info("Setting to null the old session in not active");
                    JPADaoFactory.getActorCatalogDao().setSessionToNull(destinationIdentityPublicKey);
                }

                /*
                 * Notify to de sender the message can not transmit
                 */
                messageTransmitRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.FAIL, "The destination is not more available");

                /**
                 * Report Logger
                 */
                ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.FAIL,"The destination is not more available, "+packageReceived.toString());


                LOG.info("The destination is not more available, Message not transmitted");

            }

            /**
             * Report Logger
             */
            ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.FAIL,"The destination is not more available, "+packageReceived.toString());


            LOG.info("------------------ Processing finish ------------------");

            return Package.createInstance(
                    packageReceived.getPackageId(),
                    messageTransmitRespond.toJson(),
                    PackageType.ACK,
                    channel.getChannelIdentity().getPrivateKey(),
                    destinationIdentityPublicKey
            );

        } catch (Exception exception){

            try {

                LOG.error(exception);
                messageTransmitRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.FAIL, exception.getMessage());

                LOG.info("------------------ Processing finish ------------------");

                return Package.createInstance(
                        packageReceived.getPackageId(),
                        messageTransmitRespond.toJson(),
                        PackageType.ACK,
                        channel.getChannelIdentity().getPrivateKey(),
                        destinationIdentityPublicKey
                );

            } catch (Exception e) {
                LOG.error(e);
            }
        }

        LOG.info("------------------ Processing finish ------------------");
        return null;
    }

}
