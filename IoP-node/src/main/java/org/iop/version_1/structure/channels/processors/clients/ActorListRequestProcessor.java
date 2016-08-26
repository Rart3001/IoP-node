package org.iop.version_1.structure.channels.processors.clients;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.DiscoveryQueryParameters;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.request.ActorListMsgRequest;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ActorCallMsgRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.client.respond.ActorListMsgRespond;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.enums.ProfileStatus;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantReadRecordDataBaseException;
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
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;
import org.iop.version_1.structure.database.jpa.entities.ActorCatalog;

import javax.websocket.Session;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.processors.clients.ActorListRequestProcessor</code>
 * process all packages received the type <code>MessageType.ACTOR_LIST_REQUEST</code><p/>
 *
 * Created by Leon Acosta - (laion.cj91@gmail.com) on 24/06/2016.
 *
 * @author  lnacosta
 * @version 1.0
 * @since   Java JDK 1.7
 */
public class ActorListRequestProcessor extends PackageProcessor {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(ActorListRequestProcessor.class));

    /**
     * Constructor
     */
    public ActorListRequestProcessor() {
        super(PackageType.ACTOR_LIST_REQUEST);
    }

    /**
     * (non-javadoc)
     * @see PackageProcessor#processingPackage(Session, Package, FermatWebSocketChannelEndpoint)
     */
    @Override
    public Package processingPackage(Session session, Package packageReceived, FermatWebSocketChannelEndpoint channel) throws IOException{

        LOG.info("Processing new package received " + packageReceived.getPackageType());

        String destinationIdentityPublicKey = (String) session.getUserProperties().get(HeadersAttName.CPKI_ATT_HEADER_NAME);
        ActorListMsgRequest messageContent = ActorListMsgRequest.parseContent(packageReceived.getContent());

        try {

            /*
             * Create the method call history
             */
            List<ActorProfile> actorsList = filterActors(messageContent.getParameters(), messageContent.getRequesterPublicKey());

            /*
             * If all ok, respond whit success message
             */
            ActorListMsgRespond actorListMsgRespond = new ActorListMsgRespond(packageReceived.getPackageId(),ActorCallMsgRespond.STATUS.SUCCESS, ActorCallMsgRespond.STATUS.SUCCESS.toString(), actorsList);

            if (session.isOpen()) {

                return Package.createInstance(
                        packageReceived.getPackageId(),
                        actorListMsgRespond.toJson(),
                        packageReceived.getNetworkServiceTypeSource(),
                        PackageType.ACTOR_LIST_REQUEST,
                        channel.getChannelIdentity().getPrivateKey(),
                        destinationIdentityPublicKey
                );

            } else {
                throw new IOException("connection is not opened.");
            }

        } catch (Exception exception){

            try {

                LOG.error(exception);

                /*
                 * Respond whit fail message
                 */
                ActorListMsgRespond actorListMsgRespond = new ActorListMsgRespond(
                        packageReceived.getPackageId(),
                        ActorListMsgRespond.STATUS.FAIL,
                        exception.getLocalizedMessage(),
                        null);

                return Package.createInstance(
                        packageReceived.getPackageId(),
                        actorListMsgRespond.toJson(),
                        packageReceived.getNetworkServiceTypeSource(),
                        PackageType.ACTOR_LIST_REQUEST,
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

    /**
     * Filter all actor component profiles from database that match with the given parameters.
     *
     * @param discoveryQueryParameters parameters of the discovery done by the user.
     *
     * @return a list of actor profiles.
     */
    private List<ActorProfile> filterActors(final DiscoveryQueryParameters discoveryQueryParameters, String requesterPublicKey) throws CantReadRecordDataBaseException {

        int max    = 10;
        int offset =  0;

        if (discoveryQueryParameters.getMax() != null && discoveryQueryParameters.getMax() > 0)
            max = (discoveryQueryParameters.getMax() > 100) ? 100 : discoveryQueryParameters.getMax();

        if (discoveryQueryParameters.getOffset() != null && discoveryQueryParameters.getOffset() >= 0)
            offset = discoveryQueryParameters.getOffset();

        List<ActorProfile> resultList = null;
        List<ActorCatalog> actorsList = JPADaoFactory.getActorCatalogDao().findAll(discoveryQueryParameters, requesterPublicKey, max, offset);

        if (actorsList != null && !actorsList.isEmpty()) {
            resultList = new ArrayList<>();
            for (ActorCatalog actorCatalog: actorsList) {
                resultList.add(buildActorProfileFromActorCatalogAndSetStatus(actorCatalog));
            }
        }

        return resultList;
    }

    /**
     * Build an Actor Profile from an Actor Catalog record and set its status.
     */
    private ActorProfile buildActorProfileFromActorCatalogAndSetStatus(final ActorCatalog actor){

        ActorProfile actorProfile = actor.getActorProfile();

        if (actorProfile.getStatus().equals(ProfileStatus.UNKNOWN) ){
            actorProfile.setStatus(isActorOnline(actor));
        }

        return actorProfile;
    }

    /**
     * Through this method we're going to determine a status for the actor profile.
     * First we'll check if the actor belongs to this node:
     *   if it belongs we'll check directly if he is online in the check-ins table
     *   if not we'll call to the other node.
     *
     * @param actorsCatalog  the record of the profile from the actors catalog table.
     *
     * @return an element of the ProfileStatus enum.
     */
    private ProfileStatus isActorOnline(ActorCatalog actorsCatalog) {


        try {

            if((actorsCatalog.getHomeNode() != null) &&
                    (actorsCatalog.getHomeNode().getId().equals(getNetworkNodePluginRoot().getIdentity().getPublicKey()))) {

                if (actorsCatalog.getSessionId() != null)
                    return ProfileStatus.ONLINE;
                else
                    return ProfileStatus.OFFLINE;

            }

        } catch (Exception e) {
            LOG.error(e);
            return ProfileStatus.UNKNOWN;
        }
        return ProfileStatus.OFFLINE;
    }



    private IoPNodePluginRoot pluginRoot;

    private IoPNodePluginRoot getNetworkNodePluginRoot() {

        if (pluginRoot == null)
            pluginRoot = (IoPNodePluginRoot) NodeContext.get(NodeContextItem.PLUGIN_ROOT);

        return pluginRoot;
    }
}
