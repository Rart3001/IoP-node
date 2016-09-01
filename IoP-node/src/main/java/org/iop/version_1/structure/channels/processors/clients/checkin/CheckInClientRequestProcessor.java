package org.iop.version_1.structure.channels.processors.clients.checkin;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.request.CheckInProfileMsgRequest;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ACKRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.STATUS;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.checkin.ClientCheckInRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.profiles.ClientProfile;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.HeadersAttName;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.IoPNodePluginRoot;
import org.iop.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import org.iop.version_1.structure.channels.processors.PackageProcessor;
import org.iop.version_1.structure.context.NodeContext;
import org.iop.version_1.structure.context.NodeContextItem;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;
import org.iop.version_1.structure.database.jpa.entities.Client;
import org.iop.version_1.structure.util.logger.ReportLogger;

import javax.websocket.Session;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.processors.clients.CheckInClientRequestProcessor</code>
 * process all packages received the type <code>PackageType.CHECK_IN_CLIENT_REQUEST</code><p/>
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 06/12/15.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class CheckInClientRequestProcessor extends PackageProcessor {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(CheckInClientRequestProcessor.class));

    /**
     * Constructor
     */
    public CheckInClientRequestProcessor() {
        super(PackageType.CHECK_IN_CLIENT_REQUEST);
    }

    /**
     * (non-javadoc)
     *
     * @see PackageProcessor#processingPackage(Session, Package, FermatWebSocketChannelEndpoint)
     */
    @Override
    public Package processingPackage(Session session, Package packageReceived, FermatWebSocketChannelEndpoint channel) {

        LOG.info("Processing new package received: "+packageReceived.getPackageType());
        System.out.println("CheckInProcessor");
        String destinationIdentityPublicKey = (String) session.getUserProperties().get(HeadersAttName.CPKI_ATT_HEADER_NAME);
        ClientProfile clientProfile;

        try {

            CheckInProfileMsgRequest messageContent = CheckInProfileMsgRequest.parseContent(packageReceived.getContent());

            /*
             * Obtain the profile of the client
             */
            clientProfile = (ClientProfile) messageContent.getProfileToRegister();

            /*
             * Delete all previous or old session
             */
            //JPADaoFactory.getClientSessionDao().deleteAll(clientProfile);

            /*
             * Save the client
             */
            Client client = new Client(clientProfile);
            client.setSessionId(session.getId());
            JPADaoFactory.getClientDao().save(client);

            /*
             * If all ok, respond whit success message
             */
            ClientCheckInRespond respondProfileCheckInMsj = new ClientCheckInRespond(packageReceived.getPackageId(), STATUS.SUCCESS, STATUS.SUCCESS.toString());
            IoPNodePluginRoot ioPNodePluginRoot = (IoPNodePluginRoot) NodeContext.get(NodeContextItem.PLUGIN_ROOT);
            String uri = ioPNodePluginRoot.getNodeProfile().getIp()+":"+ioPNodePluginRoot.getNodeProfile().getDefaultPort();
            //todo: ver esto de la pk
            respondProfileCheckInMsj.setHomeNodePk(ioPNodePluginRoot.getIdentity().getPrivateKey());
            respondProfileCheckInMsj.setNodeUri(uri);

            /**
             * Report Logger
             */
            ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.SUCCESS,packageReceived.toString());

            return Package.createInstance(
                    respondProfileCheckInMsj.toJson(),
                    PackageType.CHECK_IN_CLIENT_RESPOND,
                    channel.getChannelIdentity().getPrivateKey(),
                    destinationIdentityPublicKey
                );

        } catch (Exception exception) {

            try {

                LOG.error(exception);

                /*
                 * Respond whit fail message
                 */
                ACKRespond respondProfileCheckInMsj = new ACKRespond(
                        packageReceived.getPackageId(),
                        STATUS.FAIL,
                        exception.getLocalizedMessage()
                );

                /**
                 * Report logger
                 */
                ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.FAIL,packageReceived.toString(),exception);

                return Package.createInstance(
                        respondProfileCheckInMsj.toJson(),
                        PackageType.CHECK_IN_CLIENT_RESPOND,
                        channel.getChannelIdentity().getPrivateKey(),
                        destinationIdentityPublicKey
                );

            } catch (Exception e) {
                e.printStackTrace();
                LOG.error(e);
                return null;
            }
        }
    }

}
