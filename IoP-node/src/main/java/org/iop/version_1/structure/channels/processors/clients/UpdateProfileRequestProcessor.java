package org.iop.version_1.structure.channels.processors.clients;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.request.UpdateActorProfileMsgRequest;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ACKRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.IsActorOnlineMsgRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.profiles.ActorProfile;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.HeadersAttName;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.IoPNodePluginRoot;
import org.iop.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint;
import org.iop.version_1.structure.channels.processors.PackageProcessor;
import org.iop.version_1.structure.context.NodeContext;
import org.iop.version_1.structure.context.NodeContextItem;
import org.iop.version_1.structure.database.jpa.daos.ActorCatalogDao;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;
import org.iop.version_1.structure.database.jpa.entities.ActorCatalog;
import org.iop.version_1.structure.database.jpa.entities.NodeCatalog;

import javax.websocket.Session;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 * Created by Manuel Perez P. (darkpriestrelative@gmail.com) on 16/08/16.
 *
 */
public class UpdateProfileRequestProcessor extends PackageProcessor {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(UpdateProfileRequestProcessor.class));

    /**
     * Default constructor
     */
    public UpdateProfileRequestProcessor() {
        super(PackageType.UPDATE_ACTOR_PROFILE_REQUEST);
    }

    /**
     * This method process the request message
     * @param session          that send the package
     * @param packageReceived  to process
     * @param channel          in which we are processing the package.
     *
     * @return a package instance as response.
     *
     * @throws IOException if something goes wrong.
     */
    @Override
    public Package processingPackage(final Session                        session        ,
                                     final Package                        packageReceived,
                                     final FermatWebSocketChannelEndpoint channel        ) throws IOException {

        LOG.info("Processing new package received: " + packageReceived.getPackageType());

        //Represents the requester pk
        String destinationIdentityPublicKey = (String) session.getUserProperties().get(HeadersAttName.CPKI_ATT_HEADER_NAME);

        //Parsing the json String
        UpdateActorProfileMsgRequest updateActorProfileMsgRequest = UpdateActorProfileMsgRequest.parseContent(packageReceived.getContent());

        //Profile requested
        ActorProfile actorProfile = (ActorProfile) updateActorProfileMsgRequest.getProfileToUpdate();

        try{
            //Get the profile from node database
            ActorCatalogDao actorCatalogDao = JPADaoFactory.getActorCatalogDao();

            ActorCatalog actorsCatalogToUpdate = actorCatalogDao.findById(actorProfile.getIdentityPublicKey());

            ACKRespond isActorOnlineMsgRespond;

            if (actorsCatalogToUpdate != null) {

                boolean hasChanges = false;

                if (!actorProfile.getName().equals(actorsCatalogToUpdate.getName())) {
                    actorsCatalogToUpdate.setName(actorProfile.getName());
                    hasChanges = true;
                }

                if (!actorProfile.getAlias().equals(actorsCatalogToUpdate.getAlias())) {
                    actorsCatalogToUpdate.setAlias(actorProfile.getAlias());
                    hasChanges = true;
                }

                if (!Arrays.equals(actorProfile.getPhoto(), actorsCatalogToUpdate.getPhoto())) {
                    actorsCatalogToUpdate.setPhoto(actorProfile.getPhoto());
                    hasChanges = true;
                }

                if (!getNetworkNodePluginRoot().getNodeProfile().getIdentityPublicKey().equals(actorsCatalogToUpdate.getHomeNode().getId())) {
                    actorsCatalogToUpdate.setHomeNode(new NodeCatalog(getNetworkNodePluginRoot().getNodeProfile().getIdentityPublicKey()));
                    hasChanges = true;
                }

                if (actorProfile.getLocation() != null && actorsCatalogToUpdate.getLocation() != null && !actorProfile.getLocation().equals(actorsCatalogToUpdate.getLocation())) {
                    actorsCatalogToUpdate.setLocation(actorProfile.getLocation().getLatitude(), actorProfile.getLocation().getLongitude());
                    hasChanges = true;
                }

                LOG.info("hasChanges = " + hasChanges);

                if (hasChanges) {

                    Timestamp currentMillis = new Timestamp(System.currentTimeMillis());

                    actorsCatalogToUpdate.setLastConnection(currentMillis);
                    actorsCatalogToUpdate.setLastUpdateTime(currentMillis);
                    actorsCatalogToUpdate.setVersion(actorsCatalogToUpdate.getVersion() + 1);
                    actorsCatalogToUpdate.setTriedToPropagateTimes(0);

                    LOG.info("Updating profile");

                    actorCatalogDao.update(actorsCatalogToUpdate);
                }

                //Respond the request
                isActorOnlineMsgRespond = new ACKRespond(packageReceived.getPackageId(), ACKRespond.STATUS.SUCCESS, ACKRespond.STATUS.SUCCESS.toString());
            } else {

                isActorOnlineMsgRespond = new ACKRespond(packageReceived.getPackageId(), ACKRespond.STATUS.FAIL, "An actor with that public key does not exist.");
            }

            //Create instance
            if (session.isOpen()) {

                return Package.createInstance(
                        isActorOnlineMsgRespond.toJson()                      ,
                        PackageType.ACK                         ,
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
                ACKRespond actorListMsgRespond = new ACKRespond(packageReceived.getPackageId(), IsActorOnlineMsgRespond.STATUS.FAIL, exception.getLocalizedMessage());

                return Package.createInstance(
                        actorListMsgRespond.toJson()                      ,
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

    private IoPNodePluginRoot pluginRoot;

    private IoPNodePluginRoot getNetworkNodePluginRoot() {

        if (pluginRoot == null)
            pluginRoot = (IoPNodePluginRoot) NodeContext.get(NodeContextItem.PLUGIN_ROOT);

        return pluginRoot;
    }
}
