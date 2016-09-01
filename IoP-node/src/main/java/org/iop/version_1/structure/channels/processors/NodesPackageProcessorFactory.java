package org.iop.version_1.structure.channels.processors;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import org.iop.version_1.structure.channels.processors.clients.*;
import org.iop.version_1.structure.channels.processors.clients.checkin.CheckInActorRequestProcessor;
import org.iop.version_1.structure.channels.processors.clients.checkin.CheckInClientRequestProcessor;
import org.iop.version_1.structure.channels.processors.clients.checkin.CheckInNetworkServiceRequestProcessor;

import java.util.HashMap;
import java.util.Map;

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


    private static final NodesPackageProcessorFactory instance = new NodesPackageProcessorFactory();
    private Map<String,PackageProcessor> packageProcessors;


    private NodesPackageProcessorFactory() {
        packageProcessors = new HashMap<>();
        packageProcessors.put(PackageType.ACTOR_LIST_REQUEST.name(),new ActorListRequestProcessor());
        packageProcessors.put(PackageType.CHECK_IN_CLIENT_REQUEST.name(),new CheckInClientRequestProcessor());
        packageProcessors.put(PackageType.CHECK_IN_NETWORK_SERVICE_REQUEST.name(),new CheckInNetworkServiceRequestProcessor());
        packageProcessors.put(PackageType.CHECK_IN_ACTOR_REQUEST.name(),new CheckInActorRequestProcessor());
        packageProcessors.put(PackageType.MESSAGE_TRANSMIT.name(),new MessageTransmitProcessor());
        packageProcessors.put(PackageType.IS_ACTOR_ONLINE.name(),new IsActorOnlineRequestProcessor());
        packageProcessors.put(PackageType.UPDATE_ACTOR_PROFILE_REQUEST.name(),new UpdateProfileRequestProcessor());
        packageProcessors.put(PackageType.EVENT_SUBSCRIBER.name(),new SubscribersRequestProcessor());
        packageProcessors.put(PackageType.EVENT_UNSUBSCRIBER.name(),new UnSubscribeRequestProcessor());
    }

    public Map<String,PackageProcessor> getClientPackageProcessorsByPackageType() {
        return packageProcessors;
    }

    public static NodesPackageProcessorFactory getInstance() {
        return instance;
    }
}
