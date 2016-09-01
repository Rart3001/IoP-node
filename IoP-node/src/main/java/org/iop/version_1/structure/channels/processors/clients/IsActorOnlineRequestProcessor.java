package org.iop.version_1.structure.channels.processors.clients;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.request.IsActorOnlineMsgRequest;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ACKRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.STATUS;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.enums.ProfileStatus;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.HeadersAttName;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import org.iop.version_1.structure.channels.processors.PackageProcessor;
import org.iop.version_1.structure.context.SessionManager;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;
import org.iop.version_1.structure.util.logger.ReportLogger;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by Manuel Perez P. (darkpriestrelative@gmail.com) on 16/08/16.
 */
public class IsActorOnlineRequestProcessor extends PackageProcessor {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(IsActorOnlineRequestProcessor.class));

    /**
     * Default constructor
     */
    public IsActorOnlineRequestProcessor() {
        super(PackageType.IS_ACTOR_ONLINE);
    }

    /**
     * This method process the request message
     * @param session that send the package
     * @param packageReceived to process
     * @param channel
     * @return
     * @throws IOException
     */
    @Override
    public Package processingPackage(
            Session session,
            Package packageReceived,
            FermatWebSocketChannelEndpoint channel) throws IOException {

        LOG.info("Processing new package received: " + packageReceived.getPackageType());

        //Represents the requester pk
        String destinationIdentityPublicKey = (String) session
                .getUserProperties()
                .get(HeadersAttName.CPKI_ATT_HEADER_NAME);

        //Parsing the json String
        IsActorOnlineMsgRequest isActorOnlineMsgRequest = IsActorOnlineMsgRequest
                .parseContent(packageReceived.getContent());

        //Profile requested
        String actorProfilePublicKey = isActorOnlineMsgRequest.getRequestedProfilePublicKey();

        try{

            ProfileStatus profileStatus = ProfileStatus.OFFLINE;

            /*
             * Get the actorSessionId
             */
            String actorSessionId = JPADaoFactory.getActorCatalogDao().findValueById(destinationIdentityPublicKey, String.class, "sessionId");

            /*
             * Validate the session
             */
            if(actorSessionId != null &&
                    !actorSessionId.isEmpty() &&
                        SessionManager.exist(actorSessionId)){

                profileStatus = ProfileStatus.ONLINE;
            }

            //Respond the request
            ACKRespond isActorOnlineMsgRespond = new ACKRespond(packageReceived.getPackageId(),
                                                                STATUS.SUCCESS,
                                                                STATUS.SUCCESS.toString());

            //Create instance
            if (session.isOpen()) {

                /**
                 * Report Logger
                 */
                ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.SUCCESS,packageReceived.toString());

                return Package.createInstance(
                        isActorOnlineMsgRespond.toJson(),
                        PackageType.IS_ACTOR_ONLINE,
                        channel.getChannelIdentity().getPrivateKey(),
                        destinationIdentityPublicKey
                );

            } else {
                throw new IOException("connection is not opened.");
            }

        } catch(Exception exception){
            try {
                exception.printStackTrace();
                LOG.error(exception.getMessage());
                /*
                 * Respond whit fail message
                 */
                ACKRespond actorListMsgRespond = new ACKRespond(
                        packageReceived.getPackageId(),
                        STATUS.FAIL,
                        exception.getLocalizedMessage());

                /**
                 * Report Logger
                 */
                ReportLogger.infoProcessor(getClass(),packageReceived.getPackageType(),STATUS.FAIL,packageReceived.toString(),exception);

                return Package.createInstance(
                        actorListMsgRespond.toJson()                      ,
                        PackageType.IS_ACTOR_ONLINE                         ,
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
