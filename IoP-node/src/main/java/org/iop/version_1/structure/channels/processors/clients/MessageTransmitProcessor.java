package org.iop.version_1.structure.channels.processors.clients;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ACKRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.MsgRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.STATUS;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.commons.lang.ClassUtils;
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
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(MessageTransmitProcessor.class));

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

        try {

            /*
             * Get the connection to the destination
             */
            String actorSessionId = JPADaoFactory.getActorCatalogDao().findValueById(destinationIdentityPublicKey, String.class, "sessionId");

            LOG.info("ACTOR SESSION ID = "+actorSessionId);
            if (actorSessionId!=null) {
                Session clientDestination = SessionManager.get(actorSessionId);
                LOG.info("CLIENT DESTINATION = " + (clientDestination != null ? clientDestination.getId() : null));
                if (clientDestination != null) {
                    Future<Void> futureResult = null;
                    try {
                        futureResult = clientDestination.getAsyncRemote().sendObject(packageReceived);
                        // wait for completion max 4 seconds
                        futureResult.get(4, TimeUnit.SECONDS);
                        LOG.info("Message transmit successfully");
                        ACKRespond ackRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.SUCCESS, STATUS.SUCCESS.toString());

                        /**
                         * Report Logger
                         */
                        ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.SUCCESS,packageReceived.toString());

                        return Package.createInstance(
                                packageReceived.getPackageId(),
                                ackRespond.toJson()                      ,
                                PackageType.ACK                         ,
                                channel.getChannelIdentity().getPrivateKey(),
                                destinationIdentityPublicKey
                                );
                    } catch (TimeoutException | ExecutionException | InterruptedException e) {
                        LOG.error("Message cannot be transmitted");
                        LOG.error("Package trasmitted fail: " + packageReceived.toString());
                        LOG.error(e);
                        ACKRespond messageTransmitRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.FAIL, "Can't send message to destination, error details: " + e.getMessage());
                        LOG.info("Message cannot be transmitted");
                        if (e instanceof TimeoutException) {
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
                } else {

                /*
                 * Remove old session
                 */
                    if (!actorSessionId.isEmpty()) {
                        JPADaoFactory.getActorCatalogDao().setSessionToNull(destinationIdentityPublicKey);
                    }

                    /*
                     * Notify to de sender the message can not transmit
                     */
                    ACKRespond ackRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.FAIL, "The destination is not more available");

                    LOG.info("The destination is not more available,destination session null. Message not transmitted");

                    /**
                     * Report Logger
                     */
                    ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.FAIL,"The destination is not more available, "+packageReceived.toString());

                    return Package.createInstance(
                            packageReceived.getPackageId(),
                            ackRespond.toJson(),
                            PackageType.ACK,
                            channel.getChannelIdentity().getPrivateKey(),
                            destinationIdentityPublicKey
                    );

                }
            }else {
                /*
                 * Notify to de sender the message can not transmit
                 */
                ACKRespond ackRespond = new ACKRespond(packageReceived.getPackageId(), STATUS.FAIL, "The destination is not more available");

                LOG.info("The destination is not more available, actorSession null. Message not transmitted.");

                /**
                 * Report Logger
                 */
                ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.FAIL,"The destination is not more available, "+packageReceived.toString());

                return Package.createInstance(
                        packageReceived.getPackageId(),
                        ackRespond.toJson(),
                        PackageType.ACK,
                        channel.getChannelIdentity().getPrivateKey(),
                        destinationIdentityPublicKey
                );
            }


//            LOG.info("------------------ Processing finish ------------------");

        } catch (Exception exception){

            try {
                exception.printStackTrace();
                LOG.error(exception);

                ACKRespond ackRespond = new ACKRespond(packageReceived.getPackageId(),STATUS.FAIL, exception.getMessage());
                return Package.createInstance(
                        packageReceived.getPackageId(),
                        ackRespond.toJson(),
                        PackageType.ACK,
                        channel.getChannelIdentity().getPrivateKey(),
                        destinationIdentityPublicKey
                );

            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e);
            }
        }
        return null;
    }

}
