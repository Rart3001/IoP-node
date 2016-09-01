package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_api.layer.all_definition.location_system.NetworkNodeCommunicationDeviceLocation;
import com.bitdubai.fermat_api.layer.all_definition.network_service.enums.NetworkServiceType;
import com.bitdubai.fermat_api.layer.osa_android.location_system.Location;
import com.bitdubai.fermat_api.layer.osa_android.location_system.LocationSource;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.NetworkNodePluginRoot;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContext;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContextItem;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.NetworkServiceDao;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ActorCatalog;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ClientSession;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.NodeCatalog;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ClassUtils;
import org.jboss.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.NetworkData</code>
 * <p/>
 * Created by Hendry Rodriguez - (elnegroevaristo@gmail.com) on 16/06/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@Path("/rest/api/v1/network")
public class NetworkData {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(NetworkData.class));

    /*
     * Represent the pluginRoot
     */
    private final NetworkNodePluginRoot pluginRoot;

    /**
     * Constructor
     */
    public NetworkData() {
        pluginRoot = (NetworkNodePluginRoot) NodeContext.get(NodeContextItem.PLUGIN_ROOT);
    }


    @GET
    @Path("/catalog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNodes() {

        try {
            /*
             * Get the node catalog list
             */
            List<NodeCatalog> nodesCatalogs = JPADaoFactory.getNodeCatalogDao().list();
            List<String> nodes = new ArrayList<>();

            if (nodesCatalogs != null) {
                for (NodeCatalog node : nodesCatalogs) {
                    nodes.add(node.getIp());
                }
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("nodes", GsonProvider.getGson().toJson(nodes));

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        } catch (Exception e) {

            LOG.error("Error trying to list nodes.", e);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("code", e.hashCode());
            jsonObject.addProperty("message", e.getMessage());
            jsonObject.addProperty("details", GsonProvider.getGson().toJson(e.getCause()));


            JsonObject jsonObjectError = new JsonObject();
            jsonObjectError.addProperty("error", GsonProvider.getGson().toJson(jsonObject));

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObjectError)).build();

        }


    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getServerData() {

        try {

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("hash", pluginRoot.getIdentity().getPublicKey());

            if (pluginRoot.getNodeProfile() != null && pluginRoot.getNodeProfile().getLocation() != null) {
                jsonObject.addProperty("location", GsonProvider.getGson().toJson(pluginRoot.getNodeProfile().getLocation()));
            } else {

                Location location = new NetworkNodeCommunicationDeviceLocation(
                        0.0,
                        0.0,
                        0.0,
                        0,
                        0.0,
                        System.currentTimeMillis(),
                        LocationSource.UNKNOWN
                );

                jsonObject.addProperty("location", GsonProvider.getGson().toJson(location));
            }

            /*
             * Count Network Services
             */
            NetworkServiceDao networkServiceDao = JPADaoFactory.getNetworkServiceDao();
            Map<NetworkServiceType, Long> networkServiceData = new HashMap<>();
            List<Object[]> result = networkServiceDao.countOnLineByType();

            for (Object[] values:result) {
                networkServiceData.put(((NetworkServiceType) values[0]), ((Long) values[1]));
            }

            jsonObject.addProperty("os", "");
            jsonObject.addProperty("networkServices", GsonProvider.getGson().toJson(networkServiceData));

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        } catch (Exception e) {

            LOG.error("Error trying to get server data.", e);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("code", e.hashCode());
            jsonObject.addProperty("message", e.getMessage());
            jsonObject.addProperty("details", GsonProvider.getGson().toJson(e.getCause()));


            JsonObject jsonObjectError = new JsonObject();
            jsonObjectError.addProperty("error", GsonProvider.getGson().toJson(jsonObject));

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObjectError)).build();

        }

    }

    @GET
    @Path("/clients")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getClients() {

        try {

            List<String> listOfClients = new ArrayList<>();

            List<ClientSession> clientSessions = JPADaoFactory.getClientSessionDao().list();

            if (clientSessions != null) {
                for (ClientSession clientCheckIn : clientSessions) {

                    JsonObject jsonObjectClient = new JsonObject();
                    jsonObjectClient.addProperty("hash", clientCheckIn.getClient().getId());
                    jsonObjectClient.addProperty("location", GsonProvider.getGson().toJson(clientCheckIn.getClient().getLocation()));
                    jsonObjectClient.addProperty("networkServices", GsonProvider.getGson().toJson(JPADaoFactory.getNetworkServiceDao().listTypeOnLineByClient(clientCheckIn.getClient().getId())));

                    listOfClients.add(GsonProvider.getGson().toJson(jsonObjectClient));

                }
            }

            return Response.status(200).entity(GsonProvider.getGson().toJson(listOfClients)).build();

        } catch (Exception e) {
            LOG.error("Error trying to list clients.", e);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("code", e.hashCode());
            jsonObject.addProperty("message", e.getMessage());
            jsonObject.addProperty("details", GsonProvider.getGson().toJson(e.getCause()));


            JsonObject jsonObjectError = new JsonObject();
            jsonObjectError.addProperty("error", GsonProvider.getGson().toJson(jsonObject));

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObjectError)).build();
        }
    }

    @GET
    @Path("/actors")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActors() {

        try {

            List<String> actors = new ArrayList<>();

            List<ActorCatalog> listOfCheckedInActor = JPADaoFactory.getActorCatalogDao().listOnline();

            for (ActorCatalog actorCatalog : listOfCheckedInActor) {

                JsonObject jsonObjectActor = new JsonObject();
                jsonObjectActor.addProperty("hash", actorCatalog.getId());
                jsonObjectActor.addProperty("type", actorCatalog.getActorType());
                jsonObjectActor.addProperty("links", GsonProvider.getGson().toJson(new ArrayList<>()));

                jsonObjectActor.addProperty("location", GsonProvider.getGson().toJson(actorCatalog.getLocation()));

                JsonObject jsonObjectActorProfile = new JsonObject();
                jsonObjectActorProfile.addProperty("phrase", "There is not Phrase");
                jsonObjectActorProfile.addProperty("picture", Base64.encodeBase64String(actorCatalog.getPhoto()));
                jsonObjectActorProfile.addProperty("name", actorCatalog.getName());

                jsonObjectActor.addProperty("profile", GsonProvider.getGson().toJson(jsonObjectActorProfile));

                actors.add(GsonProvider.getGson().toJson(jsonObjectActor));
            }

            return Response.status(200).entity(GsonProvider.getGson().toJson(actors)).build();

        } catch (Exception e) {

            LOG.error("Error trying to list actors.", e);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("code", e.hashCode());
            jsonObject.addProperty("message", e.getMessage());
            jsonObject.addProperty("details", GsonProvider.getGson().toJson(e.getCause()));


            JsonObject jsonObjectError = new JsonObject();
            jsonObjectError.addProperty("error", GsonProvider.getGson().toJson(jsonObject));

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObjectError)).build();
        }

    }

}
