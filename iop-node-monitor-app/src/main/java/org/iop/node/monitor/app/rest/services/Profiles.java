package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.DiscoveryQueryParameters;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.enums.ProfileStatus;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.profiles.ActorProfile;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.NetworkNodePluginRoot;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContext;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.context.NodeContextItem;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ActorCatalog;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.exceptions.CantReadRecordDataBaseException;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.RestFulServices;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ClassUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.Profiles</code>
 * <p/>
 * Created by Leon Acosta - (laion.cj91@gmail.com) on 18/05/2016.
 *
 * @author  lnacosta
 * @version 1.0
 * @since   Java JDK 1.7
 */
@Path("/rest/api/v1/profiles")
public class Profiles implements RestFulServices {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(Profiles.class));

    @GET
    @GZIP
    @Path("/actor/photo/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getFullPhoto(@PathParam("id") String actorIdentityPublicKey){

        LOG.info(" --------------------------------------------------------------------- ");
        LOG.info("Profiles - Starting listActors");
        JsonObject jsonObjectRespond = new JsonObject();

        try{

            LOG.info("actorIdentityPublicKey  = " + actorIdentityPublicKey);
            byte[] photo = JPADaoFactory.getActorCatalogDao().getPhoto(actorIdentityPublicKey);

            /*
             * Create the respond
             */
            if (photo != null && photo.length > 0) {
                jsonObjectRespond.addProperty("photo", Base64.encodeBase64String(photo));
                jsonObjectRespond.addProperty("success", Boolean.TRUE);
            }else {
                jsonObjectRespond.addProperty("success", Boolean.FALSE);
                jsonObjectRespond.addProperty("failure", "Actor photo is not available");
            }

        } catch (Exception e){

            e.printStackTrace();

            LOG.warn("Actor photo is not available, or actor no exist");
            jsonObjectRespond.addProperty("success", Boolean.FALSE);
            jsonObjectRespond.addProperty("failure", "Actor photo is not available, or actor no exist");
        }

        String jsonString = GsonProvider.getGson().toJson(jsonObjectRespond);
        return Response.status(200).entity(jsonString).build();

    }

    @POST
    @GZIP
    @Path("/actors")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getList(@FormParam("client_public_key") String clientIdentityPublicKey, @FormParam("discovery_params") String discoveryParam){

        LOG.info(" --------------------------------------------------------------------- ");
        LOG.info("Profiles - Starting listActors");
        JsonObject jsonObjectRespond = new JsonObject();

        try{

            /*
             * Construct the json object
             */
            DiscoveryQueryParameters discoveryQueryParameters = DiscoveryQueryParameters.parseContent(discoveryParam);

            LOG.info(" ClientIdentityPublicKey  = " + clientIdentityPublicKey);
            LOG.info(" DiscoveryQueryParameters = " + discoveryQueryParameters.toJson());

            /*
             * hold the result list
             */
            List<ActorProfile> filteredLis = filterActors(discoveryQueryParameters, clientIdentityPublicKey);

            LOG.info("FilteredLis.size() =" + (filteredLis != null ? filteredLis.size() : 0));

            /*
             * Convert the list to json representation
             */
            String jsonListRepresentation = GsonProvider.getGson().toJson(filteredLis, new TypeToken<List<ActorProfile>>() {
            }.getType());

            /*
             * Create the respond
             */
            jsonObjectRespond.addProperty("data", jsonListRepresentation);


        } catch (Exception e){

            LOG.error("Requested list is not available.", e);
            jsonObjectRespond.addProperty("failure", "Requested list is not available");
        }

        String jsonString = GsonProvider.getGson().toJson(jsonObjectRespond);

        LOG.debug("jsonString.length() = " + jsonString.length());

        return Response.status(200).entity(jsonString).build();

    }

    /**
     * Filter all actor component profiles from database that match with the given parameters.
     * We'll use the @clientIdentityPublicKey param to filter the actors who belongs to the client asking.
     *
     * @param discoveryQueryParameters parameters of the discovery done by the user.
     *
     * @return a list of actor profiles.
     */
    private List<ActorProfile> filterActors(final DiscoveryQueryParameters discoveryQueryParameters,
                                            final String                   clientIdentityPublicKey ) throws CantReadRecordDataBaseException {

        int max    = 10;
        int offset =  0;

        if (discoveryQueryParameters.getMax() != null && discoveryQueryParameters.getMax() > 0)
            max = (discoveryQueryParameters.getMax() > 100) ? 100 : discoveryQueryParameters.getMax();

        if (discoveryQueryParameters.getOffset() != null && discoveryQueryParameters.getOffset() >= 0)
            offset = discoveryQueryParameters.getOffset();

        List<ActorProfile> resultList = null;
        List<ActorCatalog> actorsList = JPADaoFactory.getActorCatalogDao().findAll(discoveryQueryParameters, clientIdentityPublicKey, max, offset);

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
    private ActorProfile buildActorProfileFromActorCatalogAndSetStatus(final ActorCatalog actorCatalog){

        ActorProfile actorProfile = actorCatalog.getActorProfile();
        if (actorProfile.getStatus() == ProfileStatus.UNKNOWN){
            actorProfile.setStatus(isActorOnline(actorCatalog));
        }

        return actorProfile;
    }

    /**
     * Through this method we're going to determine a status for the actor profile.
     * First we'll check if the actor belongs to this node:
     *   if it belongs we'll check directly if he is online in the check-ins table
     *   if not we'll call to the other node.
     *
     * @param actorsCatalog  the record of the profile from the actors catalog table
     * @return an element of the ProfileStatus enum.
     */
    private ProfileStatus isActorOnline(ActorCatalog actorsCatalog) {

        try {

            if(actorsCatalog.getHomeNode()!= null && actorsCatalog.getHomeNode().getId().equals(getPluginRoot().getIdentity().getPublicKey())) {
               return ProfileStatus.OFFLINE;
            } else {
               return isActorOnlineInOtherNode(actorsCatalog);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ProfileStatus.UNKNOWN;
        }
    }

    /**
     * Through this method we're going to determine a status for the actor profile calling another node.
     *
     * @param actorsCatalog  the record of the profile from the actors catalog table.
     *
     * @return an element of the ProfileStatus enum.
     */
    private ProfileStatus isActorOnlineInOtherNode(final ActorCatalog actorsCatalog) {

        try {

            if (actorsCatalog.getHomeNode() != null){

                String nodeUrl = actorsCatalog.getHomeNode().getIp()+":"+actorsCatalog.getHomeNode().getDefaultPort();

                URL url = new URL("http://" + nodeUrl + "/fermat/rest/api/v1/online/component/actor/" + actorsCatalog.getId());

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");

                BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String respond = reader.readLine();

                if (conn.getResponseCode() == 200 && respond != null && respond.contains("success")) {

                    JsonObject respondJsonObject = (JsonObject) GsonProvider.getJsonParser().parse(respond.trim());
                    return respondJsonObject.get("isOnline").getAsBoolean() ? ProfileStatus.ONLINE : ProfileStatus.OFFLINE;

                } else {
                    return ProfileStatus.UNKNOWN;
                }

            }else {
                return ProfileStatus.UNKNOWN;
            }

        } catch (Exception e) {
            e.printStackTrace();
            return ProfileStatus.UNKNOWN;
        }
    }

    /**
     * Through this method we'll get the plugin root.
     *
     * @return a plugin root object.
     */
    private NetworkNodePluginRoot getPluginRoot() {
        return (NetworkNodePluginRoot) NodeContext.get(NodeContextItem.PLUGIN_ROOT);
    }

}
