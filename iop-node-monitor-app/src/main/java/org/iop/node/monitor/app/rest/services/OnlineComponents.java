package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.JPANamedQuery;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.NetworkNodePluginRoot;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContext;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContextItem;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ClientSession;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.NodeCatalog;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.RestFulServices;
import com.google.gson.JsonObject;
import org.apache.commons.lang.ClassUtils;
import org.jboss.logging.Logger;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.OnlineComponents</code>
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 29/04/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@Path("/rest/api/v1/online/component")
public class OnlineComponents implements RestFulServices {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(OnlineComponents.class));

    private final NetworkNodePluginRoot pluginRoot;

    /**
     * Constructor
     */
    public OnlineComponents(){
        pluginRoot = (NetworkNodePluginRoot) NodeContext.get(NodeContextItem.PLUGIN_ROOT);
    }

    @GET
    @Path("/client/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isClientOnline(@PathParam("id") String identityPublicKey){

        LOG.info("Executing isClientOnline");
        LOG.info("identityPublicKey = "+identityPublicKey);

        try {
            HashMap<String,Object> filters = new HashMap<>();
            filters.put("id",identityPublicKey);
            Boolean online = JPADaoFactory.getClientSessionDao().executeNamedQuery(JPANamedQuery.IS_CLIENT_ONLINE,filters, false).size() > 0;

            LOG.info("Is online = " + online);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("isOnline",online);

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        } catch (Exception e) {

            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("isOnline", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        }

    }


    @GET
    @Path("/ns/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isNsOnline(@PathParam("id") String identityPublicKey){

        LOG.info("Executing isOnlineNs");
        LOG.info("identityPublicKey = "+identityPublicKey);

        try {
            HashMap<String,Object> filters = new HashMap<>();
            filters.put("id",identityPublicKey);
            Boolean online = JPADaoFactory.getNetworkServiceDao().getSessionId(identityPublicKey) != null ? Boolean.TRUE : Boolean.FALSE;

            LOG.info("Is online = " + online);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("isOnline",online);

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        } catch (Exception e) {

            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("isOnline", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        }

    }

    /**
     * We'll ask the node if an actor is online.
     *
     * We'll check if the actor is registered.
     *    - If it is registered We'll return: isOnline:TRUE, sameNode:TRUE
     * If the actor is not registered, I will check the Actor Catalog, then:
     *    - If it is persisted in the same node, then We'll return:  isOnline:FALSE, sameNode:TRUE
     * If it is persisted in other node, then We'll check in the other node, then we will return: isOnline:(isOnline), sameNode:FALSE
     *
     * If an exception occurs We'll return its message in the json, like "details".
     *
     * @param identityPublicKey of the actor
     *
     * @return a json response.
     */
    @GET
    @Path("/actor/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isActorOnline(@PathParam("id") String identityPublicKey) {

        LOG.info("Executing isActorOnline");
        LOG.info("identityPublicKey = " + identityPublicKey);

        try {

            JsonObject jsonObject = new JsonObject();
            String actorSessionId = JPADaoFactory.getActorCatalogDao().getSessionId(identityPublicKey);

            LOG.info("actorSessionId = " + actorSessionId);

            if (actorSessionId != null && !actorSessionId.isEmpty()) {

                jsonObject.addProperty("success", Boolean.TRUE);
                jsonObject.addProperty("isOnline", Boolean.TRUE);
                jsonObject.addProperty("sameNode", Boolean.TRUE);

            } else {

                NodeCatalog homeNode = JPADaoFactory.getActorCatalogDao().getHomeNode(identityPublicKey);

                LOG.info("homeNode = " + (homeNode != null ? homeNode.getId() : null));
                LOG.info("pluginRoot.getNodeProfile().getIdentityPublicKey() = " + pluginRoot.getNodeProfile().getIdentityPublicKey());
                LOG.info("equals = " + ((homeNode != null ? homeNode.getId().equals(pluginRoot.getNodeProfile().getIdentityPublicKey()) : null)));

                if ((homeNode != null) && (homeNode.getId().equals(pluginRoot.getNodeProfile().getIdentityPublicKey()))) {

                    jsonObject.addProperty("success", Boolean.TRUE);
                    jsonObject.addProperty("isOnline", Boolean.FALSE);
                    jsonObject.addProperty("sameNode", Boolean.TRUE);

                } else if (homeNode != null) {

                    String nodeUrl = homeNode.getIp() + ":" + homeNode.getDefaultPort();
                    Boolean isOnline = isActorOnline(identityPublicKey, nodeUrl);
                    jsonObject.addProperty("success", Boolean.TRUE);
                    jsonObject.addProperty("isOnline", isOnline);
                    jsonObject.addProperty("sameNode", Boolean.FALSE);

                } else {

                    jsonObject.addProperty("success", Boolean.FALSE);
                    jsonObject.addProperty("isOnline", Boolean.FALSE);
                    jsonObject.addProperty("sameNode", Boolean.FALSE);
                    jsonObject.addProperty("details", "Home node not found.");
                }
            }

            LOG.info("respond = " + jsonObject);
            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        } catch (Exception e) {

            LOG.info("Error = " + e.getMessage());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("isOnline", Boolean.FALSE);
            jsonObject.addProperty("sameNode", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        }

    }

    private Boolean isActorOnline(final String publicKey,
                                  final String nodeUrl  ) {

        try {
            URL url = new URL("http://" + nodeUrl + "/fermat/rest/api/v1/online/component/actor/" + publicKey);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String respond = reader.readLine();

            if (conn.getResponseCode() == 200 && respond != null && respond.contains("success")) {
                JsonObject respondJsonObject = (JsonObject) GsonProvider.getJsonParser().parse(respond.trim());
                return respondJsonObject.get("isOnline").getAsBoolean();

            } else {
                return false;
            }
        } catch (Exception e) {
            LOG.error("Error: "+e.getMessage());
            return false;
        }
    }

    @GET
    @Path("/sessions/clients")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveClientSessionData(){

        LOG.info("Executing getActiveClientSessionData");

        try {

            List<ClientSession> clientSessionList = JPADaoFactory.getClientSessionDao().list();
            List<JsonObject> sessions = new ArrayList<>();

            for (ClientSession clientSession : clientSessionList) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("publicKey", clientSession.getClient().getId());
                jsonObject.addProperty("sessionId", clientSession.getId());
                sessions.add(jsonObject);
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("sessions",GsonProvider.getGson().toJson(sessions));

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        } catch (Exception e) {

            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        }

    }

}
