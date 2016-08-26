package org.iop.version_1.structure.channels.processors;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.iop.version_1.structure.channels.processors.clients.*;
import org.iop.version_1.structure.channels.processors.clients.checkin.CheckInActorRequestProcessor;
import org.iop.version_1.structure.channels.processors.clients.checkin.CheckInClientRequestProcessor;
import org.iop.version_1.structure.channels.processors.clients.checkin.CheckInNetworkServiceRequestProcessor;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.processors.NodesPackageProcessorFactory</code>
 * <p/>
 * Created by Leon Acosta - (laion.cj91@gmail.com) on 05/08/2016.
 *
 * @author  lnacosta
 * @version 1.0
 * @since   Java JDK 1.7
 */
public class NodesPackageProcessorFactory {

    /**
     * Get a new instance the PackageProcessor for the packageType
     * @param packageType
     * @return packageProcessor instance
     */
    public static PackageProcessor getClientPackageProcessorsByPackageType(PackageType packageType) {

        switch (packageType) {
            case ACTOR_LIST_REQUEST:
                return new ActorListRequestProcessor();

            case CHECK_IN_CLIENT_REQUEST:
                return new CheckInClientRequestProcessor();

            case CHECK_IN_NETWORK_SERVICE_REQUEST:
                return new CheckInNetworkServiceRequestProcessor();

            case CHECK_IN_ACTOR_REQUEST:
                return new CheckInActorRequestProcessor();

            case MESSAGE_TRANSMIT:
                return new MessageTransmitProcessor();

            case IS_ACTOR_ONLINE:
                return new IsActorOnlineRequestProcessor();

            case UPDATE_ACTOR_PROFILE_REQUEST:
                return new UpdateProfileRequestProcessor();

            case EVENT_SUBSCRIBER:
                return new SubscribersRequestProcessor();

            case EVENT_UNSUBSCRIBER:
                return new UnSubscribeRequestProcessor();

        }

        return null;

    }

}
