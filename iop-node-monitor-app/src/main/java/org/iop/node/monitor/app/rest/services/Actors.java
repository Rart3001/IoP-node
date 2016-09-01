package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_api.FermatException;
import com.bitdubai.fermat_api.layer.all_definition.location_system.NetworkNodeCommunicationDeviceLocation;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.enums.JPANamedQuery;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.ActorCatalogDao;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ActorCatalog;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.RestFulServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.ClassUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.Actors</code>
 * <p/>
 *
 * Created by Roberto Requena - (rart3001@gmail.com) on 29/06/2016.
 *
 * @author  rrequena
 * @version 1.0
 * @since   Java JDK 1.7
 */
@Path("/rest/api/v1/admin/actors")
public class Actors implements RestFulServices {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(Actors.class));

    /**
     * Represent the gson
     */
    private final Gson gson;

    /**
     * Constructor
     */
    public Actors(){
        super();
        this.gson = GsonProvider.getGson();
    }

    /**
     * Get all check in actors in the  node
     * @return Response
     */
    @GZIP
    @GET
    @Path("/types")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActorsTypes(){

        LOG.info("Executing getActorsTypes");

        try {

            Map<com.bitdubai.fermat_api.layer.all_definition.enums.Actors, String> types = new HashMap<>();
            for (com.bitdubai.fermat_api.layer.all_definition.enums.Actors actorsType : com.bitdubai.fermat_api.layer.all_definition.enums.Actors.values()) {
                types.put(actorsType, actorsType.getCode());
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("types", gson.toJson(types));

            return Response.status(200).entity(gson.toJson(jsonObject)).build();

        } catch (Exception e) {

            LOG.info(FermatException.wrapException(e).toString());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(gson.toJson(jsonObject)).build();

        }

    }

    /**
     * Get all check in actors in the  node
     * @param offSet
     * @param max
     * @return Response
     */
    @GZIP
    @GET
    @Path("/check_in")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCheckInActors(@QueryParam("actorType") String actorType, @QueryParam("offSet") Integer offSet, @QueryParam("max") Integer max){

        LOG.info("Executing getCheckInActors");
        LOG.info("actorType = "+actorType+" offset = "+offSet+" max = "+max);

        try {

            ActorCatalogDao actorCatalogDao = JPADaoFactory.getActorCatalogDao();

            long total;
            List<String> actorProfilesRegistered = new ArrayList<>();
            List<ActorCatalog> actorList;
            HashMap<String,Object> filters = new HashMap<>();
            filters.put("max",max);
            filters.put("offset", offSet);

            if(actorType != null && !actorType.isEmpty()) {

                filters.put("type",actorType);
                actorList = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ALL_CHECKED_IN_ACTORS_BY_ACTOR_TYPE, filters, false);
                filters.clear();
                filters.put("type",actorType);
                total = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ALL_CHECKED_IN_ACTORS_BY_ACTOR_TYPE, filters, false).size();

            }else {

                actorList = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ALL_CHECKED_IN_ACTORS, filters, false);
                filters.clear();
                total = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ALL_CHECKED_IN_ACTORS, filters, false).size();

            }

            for (ActorCatalog actor : actorList)
                actorProfilesRegistered.add(buildActorProfileFromActorsCatalog(actor));

            LOG.info("CheckInActors.size() = " + actorProfilesRegistered.size());

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("identities", gson.toJson(actorProfilesRegistered));
            jsonObject.addProperty("total", total);

            return Response.status(200).entity(gson.toJson(jsonObject)).build();

        } catch (Exception e) {

            LOG.info(FermatException.wrapException(e).toString());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(gson.toJson(jsonObject)).build();

        }

    }

    /**
     * Get all check in actors in the  node
     * @param offSet
     * @param max
     * @return Response
     */
    @GZIP
    @GET
    @Path("/catalog")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActorsCatalog(@QueryParam("actorType") String actorType, @QueryParam("offSet") Integer offSet, @QueryParam("max") Integer max){

        LOG.info("Executing getActorsCatalog");
        LOG.info("actorType = " + actorType + " offset = " + offSet + " max = " + max);

        try {

            ActorCatalogDao actorCatalogDao = JPADaoFactory.getActorCatalogDao();

            long total;
            List<String> actorProfilesRegistered = new ArrayList<>();
            List<ActorCatalog> actorsCatalogList;
            HashMap<String,Object> filters = new HashMap<>();
            filters.put("max",max);
            filters.put("offset", offSet);
            if(actorType != null && !actorType.equals("") && !actorType.isEmpty()){
                filters.put("type",actorType);
                actorsCatalogList = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ACTOR_CATALOG_BY_ACTOR_TYPE,filters, false);
                filters.clear();
                filters.put("type",actorType);
                total = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ACTOR_CATALOG_BY_ACTOR_TYPE,filters, false).size();
            }else {
                actorsCatalogList = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ACTOR_CATALOG, filters, false);
                filters.clear();
                total = actorCatalogDao.executeNamedQuery(JPANamedQuery.GET_ACTOR_CATALOG, filters, false).size();
            }

            for (ActorCatalog actorsCatalog :actorsCatalogList) {
                actorProfilesRegistered.add(buildActorProfileFromActorsCatalog(actorsCatalog));
            }

            LOG.info("ActorsCatalog.size() = " + actorProfilesRegistered.size());

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("identities", gson.toJson(actorProfilesRegistered));
            jsonObject.addProperty("total", total);

            return Response.status(200).entity(gson.toJson(jsonObject)).build();

        } catch (Exception e) {

            LOG.info(FermatException.wrapException(e).toString());
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(gson.toJson(jsonObject)).build();

        }
    }

    private String buildActorProfileFromActorsCatalog(ActorCatalog actor){

        JsonObject jsonObjectActor = new JsonObject();
        jsonObjectActor.addProperty("ipk", actor.getId());
        jsonObjectActor.addProperty("alias", actor.getAlias());
        jsonObjectActor.addProperty("name", actor.getName());
        jsonObjectActor.addProperty("type",  actor.getActorType());
        jsonObjectActor.addProperty("photo", Base64.encodeBase64String(actor.getPhoto()));
        jsonObjectActor.addProperty("extraData", actor.getExtraData());

        if (actor.getHomeNode() != null){
            jsonObjectActor.addProperty("homeNode", gson.toJson(actor.getHomeNode()));
        }else {
            jsonObjectActor.addProperty("homeNode", "no set");
        }

        if(actor.getLocation() != null){
            jsonObjectActor.addProperty("location", gson.toJson(actor.getLocation()));
        }else {
            jsonObjectActor.addProperty("location", gson.toJson(NetworkNodeCommunicationDeviceLocation.getInstance(0d, 0d)));
        }

        return gson.toJson(jsonObjectActor);
    }

}
