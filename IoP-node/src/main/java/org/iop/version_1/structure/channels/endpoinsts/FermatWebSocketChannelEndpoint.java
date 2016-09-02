package org.iop.version_1.structure.channels.endpoinsts;

import com.bitdubai.fermat_api.layer.all_definition.crypto.asymmetric.ECCKeyPair;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.Package;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.PackageType;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.exception.PackageTypeNotSupportedException;
import org.iop.version_1.IoPNodePluginRoot;
import org.iop.version_1.structure.channels.processors.PackageProcessor;
import org.iop.version_1.structure.context.NodeContext;
import org.iop.version_1.structure.context.NodeContextItem;

import javax.websocket.EncodeException;
import javax.websocket.Session;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.channels.endpoinsts.FermatWebSocketChannelEndpoint</code>
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 06/12/15.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public abstract class FermatWebSocketChannelEndpoint {

    /**
     * Represent the MAX_MESSAGE_SIZE
     */
    public static final int MAX_MESSAGE_SIZE = 3000000;

    /**
     * Represent the MAX_IDLE_TIMEOUT
     */
    public static final int MAX_IDLE_TIMEOUT = 60000;

    /**
     * Represent the channelIdentity
     */
    private ECCKeyPair channelIdentity;

    /**
     * Processors
     */
    private Map<String,PackageProcessor> packageProcessors;

    /**
     * Constructor
     */
    public FermatWebSocketChannelEndpoint(){
        super();
        this.channelIdentity = ((IoPNodePluginRoot) NodeContext.get(NodeContextItem.PLUGIN_ROOT)).getIdentity(); //new ECCKeyPair(); //
        packageProcessors = getPackageProcessors();
    }

    /**
     * Gets the value of channelIdentity and returns
     *
     * @return channelIdentity
     */
    public ECCKeyPair getChannelIdentity() {
        return channelIdentity;
    }

    /**
     * Sets the channelIdentity
     *
     * @param channelIdentity to set
     */
    protected void setChannelIdentity(ECCKeyPair channelIdentity) {
        this.channelIdentity = channelIdentity;
    }

    /**
     * Method that process a new message received
     *
     * @param packageReceived
     * @param session
     */
    protected Package processMessage(Package packageReceived, Session session) throws PackageTypeNotSupportedException {
        try {
        /*
         * Validate if can process the message
         */
            if (!packageProcessors.isEmpty()) {

            /*
             * process message and return package to the other side
             */
                return packageProcessors.get(packageReceived.getPackageType().name()).processingPackage(session, packageReceived, this);

            } else {

                throw new PackageTypeNotSupportedException("The package type: " + packageReceived.getPackageType() + " is not supported");
            }
        }catch (IOException e) {
            //todo: ver que pasa cuando la session está caida, quizás no deba hacer anda acá
            e.printStackTrace();
            return null;
        }
    }


    public synchronized final void sendPackage(Package p,Session session) throws IOException, EncodeException {
        if (session.isOpen()) {
            session.getBasicRemote().sendObject(p);
        } else {
            throw new IOException("connection is not opened.");
        }
    }

    public void sendPackage(Session session, UUID packageId, String packageContent, PackageType packageType, String destinationIdentityPublicKey) throws IOException, EncodeException, IllegalArgumentException {
        if (session==null) throw new IllegalArgumentException("Session can't be null");
        if (session.isOpen()) {
            Package packageRespond = Package.createInstance(
                    packageId,
                    packageContent                      ,
                    packageType                         ,
                    getChannelIdentity().getPrivateKey(),
                    destinationIdentityPublicKey
            );

            session.getBasicRemote().sendObject(packageRespond);
        } else {
            throw new IOException("connection is not opened.");
        }
    }



    /**
     * Gets the value of packageProcessors and returns
     *
     * @return packageProcessors
     */
    protected abstract Map<String,PackageProcessor> getPackageProcessors();


}
