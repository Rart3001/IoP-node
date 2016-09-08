package org.iop.version_1.structure.channels.processors.clients;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.request.UnSubscribeMsgRequest;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ACKRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.base.STATUS;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.HeadersAttName;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import org.iop.version_1.structure.channels.processors.PackageProcessor;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;

import javax.websocket.Session;
import java.io.IOException;

/**
 * Created by Manuel Perez P. (darkpriestrelative@gmail.com) on 16/08/16.
 */
public class UnSubscribeRequestProcessor extends PackageProcessor {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger("debugLogger");

    /**
     * Default constructor
     */
    public UnSubscribeRequestProcessor() {
        super(PackageType.EVENT_UNSUBSCRIBER);
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
        UnSubscribeMsgRequest unSubscribeMsgRequest = UnSubscribeMsgRequest
                .parseContent(packageReceived.getContent());


        try{


            JPADaoFactory.getEventListenerDao().removeEvent(unSubscribeMsgRequest.getUnSubscribeEventId());

            //Respond the request
            ACKRespond ackRespond = new ACKRespond(packageReceived.getPackageId(),
                    STATUS.SUCCESS,
                    STATUS.SUCCESS.toString());

            //Create instance
            if (session.isOpen()) {

                return Package.createInstance(
                        ackRespond.toJson(),
                        PackageType.ACK,
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
                ACKRespond ackRespond = new ACKRespond(
                        packageReceived.getPackageId(),
                        STATUS.FAIL,
                        exception.getLocalizedMessage()
                );

                return Package.createInstance(
                        ackRespond.toJson()                      ,
                        PackageType.ACK                         ,
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
