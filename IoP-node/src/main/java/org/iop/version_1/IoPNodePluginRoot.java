package org.iop.version_1;

import com.bitdubai.fermat_api.CantStartPluginException;
import com.bitdubai.fermat_api.layer.all_definition.common.system.abstract_classes.AbstractPlugin;
import com.bitdubai.fermat_api.layer.all_definition.common.system.annotations.NeededAddonReference;
import com.bitdubai.fermat_api.layer.all_definition.common.system.interfaces.error_manager.enums.UnexpectedPluginExceptionSeverity;
import com.bitdubai.fermat_api.layer.all_definition.common.system.utils.PluginVersionReference;
import com.bitdubai.fermat_api.layer.all_definition.crypto.asymmetric.ECCKeyPair;
import com.bitdubai.fermat_api.layer.all_definition.enums.Addons;
import com.bitdubai.fermat_api.layer.all_definition.enums.Layers;
import com.bitdubai.fermat_api.layer.all_definition.enums.Platforms;
import com.bitdubai.fermat_api.layer.all_definition.util.Version;
import com.bitdubai.fermat_api.layer.all_definition.util.ip_address.IPAddressHelper;
import com.bitdubai.fermat_api.layer.osa_android.file_system.FileLifeSpan;
import com.bitdubai.fermat_api.layer.osa_android.file_system.FilePrivacy;
import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginFileSystem;
import com.bitdubai.fermat_api.layer.osa_android.file_system.PluginTextFile;
import com.bitdubai.fermat_api.layer.osa_android.file_system.exceptions.CantCreateFileException;
import com.bitdubai.fermat_api.layer.osa_android.file_system.exceptions.FileNotFoundException;
import com.bitdubai.fermat_api.layer.osa_android.location_system.Location;
import com.bitdubai.fermat_api.layer.osa_android.location_system.LocationManager;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.NetworkNodeManager;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.profiles.NodeProfile;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.JettyEmbeddedAppServer;
import org.iop.version_1.structure.conf.EmbeddedNodeServerConf;
import org.iop.version_1.structure.context.NodeContext;
import org.iop.version_1.structure.context.NodeContextItem;
import org.iop.version_1.structure.database.jpa.DatabaseManager;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;
import org.iop.version_1.structure.database.jpa.entities.GeoLocation;
import org.iop.version_1.structure.util.ConfigurationManager;
import org.iop.version_1.structure.util.UPNPService;

import java.io.IOException;
import java.util.UUID;



/**
 * Created by mati on 11/08/16.
 */
public class IoPNodePluginRoot extends AbstractPlugin implements NetworkNodeManager {

    /**
     * Represent the LOG
     */
    private static final Logger LOG = Logger.getLogger("debugLogger");

    static final Logger resultLog = Logger.getLogger("reportsLogger");

    @NeededAddonReference(platform = Platforms.OPERATIVE_SYSTEM_API, layer = Layers.SYSTEM, addon = Addons.DEVICE_LOCATION)
    private LocationManager locationManager;

    private PluginFileSystem pluginFileSystem;

    /**
     * Represent the node identity
     */
    private ECCKeyPair identity;

    /**
     * Represent the nodeProfile
     */
    private NodeProfile nodeProfile;


    public IoPNodePluginRoot() {
        super(new PluginVersionReference(new Version()));
        //id hardcoded
        super.setId(UUID.fromString("6e933c4e-6fd8-4c2c-a583-39edc2f78065"));
    }


    /**
     * (non-javadoc)
     *
     * @see AbstractPlugin#start()
     */
    @Override
    public void start() throws CantStartPluginException {

        LOG.info("Calling method - start()...");
        LOG.info("pluginId = " + pluginId);
        /*
         * Validate required resources
         */
//        validateInjectedResources();

        try {

            DatabaseManager.start();

            /*
             * Clean tables at start
             */
            cleanTables();

            /*
             * Initialize the identity of the node
             */
            initializeIdentity();

            /*
             * Initialize the configuration file
             */
            initializeConfigurationFile();

            /*
             * Get the server ip
             */
//            generateNodePublicIp();

            /*
             * Generate the profile of the node
             */
            generateNodeProfile();


            LOG.info("Add references to the node context...");

            /*
             * Add references to the node context
             */
            NodeContext.add(NodeContextItem.PLUGIN_ROOT, this);

            /*
             * Process the node catalog
             */
//            initializeNodeCatalog();

            /*
             * Initialize propagate catalog agents
             */
//            LOG.info("Initializing propagate catalog agents ...");
//            this.propagateCatalogAgent = new PropagateCatalogAgent(this);
//            this.propagateCatalogAgent.start();

            /*
             * Try to forwarding port
             */
            UPNPService.portForwarding(15400, ConfigurationManager.getValue(ConfigurationManager.NODE_NAME));

            /*
             Represent the fermatEmbeddedNodeServer instance
            */
            JettyEmbeddedAppServer fermatEmbeddedNodeServer = JettyEmbeddedAppServer.getInstance();
            fermatEmbeddedNodeServer.start();


        } catch (Exception exception) {

            exception.printStackTrace();

            String context = "Plugin ID: " + pluginId;
            String possibleCause = "The Network Node Service triggered an unexpected problem that wasn't able to solve by itself";
            CantStartPluginException pluginStartException = new CantStartPluginException(CantStartPluginException.DEFAULT_MESSAGE, exception, context, possibleCause);

            super.reportError(UnexpectedPluginExceptionSeverity.DISABLES_THIS_PLUGIN, pluginStartException);
            throw pluginStartException;
        }
    }


    /**
     * Initialize the identity of this plugin
     */
    private void initializeIdentity() throws Exception {

        System.out.println("Calling the method - initializeIdentity() ");

        try {

            System.out.println("Loading identity");

         /*
          * Load the file with the identity
          */
            PluginTextFile pluginTextFile = pluginFileSystem.getTextFile(pluginId, "DIR", "FILE_NAME", FilePrivacy.PRIVATE, FileLifeSpan.PERMANENT);
            String content = pluginTextFile.getContent();

            System.out.println("content = " + content);

            identity = new ECCKeyPair(content);

        } catch (FileNotFoundException e) {

            /*
             * The file no exist may be the first time the plugin is running on this device,
             * We need to create the new identity
             */
            try {

                System.out.println("No previous identity found - Proceed to create new one");

                /*
                 * Create the new identity
                 */
                identity = new ECCKeyPair();

                System.out.println("identity.getPrivateKey() = " + identity.getPrivateKey());
                System.out.println("identity.getPublicKey() = " + identity.getPublicKey());

                /*
                 * save into the file
                 */
                PluginTextFile pluginTextFile = pluginFileSystem.createTextFile(pluginId, "DIR", "FILE_NAME", FilePrivacy.PRIVATE, FileLifeSpan.PERMANENT);
                pluginTextFile.setContent(identity.getPrivateKey());
                pluginTextFile.persistToMedia();

            } catch (Exception exception) {
                /*
                 * The file cannot be created. I can not handle this situation.
                 */
                throw new Exception(exception.getLocalizedMessage());
            }

        } catch (CantCreateFileException cantCreateFileException) {

            /*
             * The file cannot be load. I can not handle this situation.
             */
            throw new Exception(cantCreateFileException.getLocalizedMessage());

        }

    }

    /**
     * Initializes the configuration file
     */
    private void initializeConfigurationFile() throws ConfigurationException, IOException {
        LOG.info("Starting initializeConfigurationFile()...");
        if(ConfigurationManager.isExist()){
            LOG.info("Configuration file exist. Loading...");
            ConfigurationManager.load();
        }else {
            LOG.info("Configuration file doesn't exist. Creating...");
            ConfigurationManager.create(identity.getPublicKey());
            ConfigurationManager.load();
        }
    }

    /**
     * Generate the node profile of this node
     */
    private void generateNodeProfile() {

        LOG.info("Generating Node Profile...");

        nodeProfile = new NodeProfile();
        nodeProfile.setIdentityPublicKey(identity.getPublicKey());
        nodeProfile.setIp(generateNodePublicIp() );
        nodeProfile.setDefaultPort(Integer.valueOf(ConfigurationManager.getValue(ConfigurationManager.PORT)));
        nodeProfile.setName(ConfigurationManager.getValue(ConfigurationManager.NODE_NAME));
        nodeProfile.setLocation(generateNodeLocation());

        LOG.info("Node Profile = "+nodeProfile);

    }


    /**
     * Generate de node public ip
     */
    private String generateNodePublicIp() {
        String publicIp;
        try {
            if (ConfigurationManager.getValue(ConfigurationManager.PUBLIC_IP).equals(EmbeddedNodeServerConf.DEFAULT_IP)){
                publicIp = IPAddressHelper.getCurrentPublicIPAddress();
                LOG.info(">>>> Server public ip: " + publicIp + " get by online service");
                ConfigurationManager.updateValue(ConfigurationManager.PUBLIC_IP, publicIp);
            }else {
                publicIp = ConfigurationManager.getValue(ConfigurationManager.PUBLIC_IP);
                LOG.info(">>>> Server public ip: " + publicIp + " get from configuration file");
            }
        }catch (Exception e){

            LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            LOG.warn("! Could not get the external ip with the online service, it must be configured manually in the configuration file !");
            LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            publicIp = ConfigurationManager.getValue(ConfigurationManager.PUBLIC_IP);
        }
        return publicIp;
    }


    /**
     * Generate the node location
     * @return Location
     */
    private Location generateNodeLocation(){
        Location location;
        try {
            if (ConfigurationManager.getValue(ConfigurationManager.LATITUDE).equals("0.0") && ConfigurationManager.getValue(ConfigurationManager.LONGITUDE).equals("0.0")){
                LOG.info(">>>> Trying to get the location of the node...");
                location = locationManager.getLocation();
                ConfigurationManager.updateValue(ConfigurationManager.LATITUDE, location.getLatitude().toString());
                ConfigurationManager.updateValue(ConfigurationManager.LONGITUDE, location.getLongitude().toString());
            }else {
                LOG.info(">>>> Getting the location from the configuration file");
                location = new GeoLocation(nodeProfile.getIdentityPublicKey(), new Double(ConfigurationManager.getValue(ConfigurationManager.LATITUDE)), new Double(ConfigurationManager.getValue(ConfigurationManager.LONGITUDE)));
            }
        }catch (Exception e){
            LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            LOG.warn("! Could not get the location with the online service, it must be configured manually in the configuration file !");
            LOG.warn("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            location = new GeoLocation(nodeProfile.getIdentityPublicKey(), new Double(ConfigurationManager.getValue(ConfigurationManager.LATITUDE)), new Double(ConfigurationManager.getValue(ConfigurationManager.LONGITUDE)));
        }
        return location;
    }


    /**
     * This method clean all data from de check in tables.
     *  - CLIENT
     *  - NETWORK_SERVICE
     */
    private void cleanTables() {

        try {

            LOG.info("Deleting older session and his associate entities");

            JPADaoFactory.getClientDao().delete();
            JPADaoFactory.getClientDao().deleteAllClientGeolocation();
            JPADaoFactory.getNetworkServiceDao().delete();
            JPADaoFactory.getActorCatalogDao().setSessionsToNull();

        }catch (Exception e){
            LOG.error("Can't Deleting older session and his associate entities, maybe is first time to run the node and the tables no exist: "+e.getMessage());
        }

    }


    public ECCKeyPair getIdentity() {
        return identity;
    }

    public NodeProfile getNodeProfile() {
        return nodeProfile;
    }

    public void setLocationManager(LocationManager locationManager) {
        this.locationManager = locationManager;
    }

    public void setPluginFileSystem(PluginFileSystem pluginFileSystem) {
        this.pluginFileSystem = pluginFileSystem;
    }
}
