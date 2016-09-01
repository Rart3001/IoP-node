package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.exceptions.CantReadRecordDataBaseException;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.RestFulServices;
import com.google.gson.JsonObject;
import org.apache.commons.lang.ClassUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.GZIP;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.Nodes</code>
 * <p/>
 *
 * Created by Roberto Requena - (rart3001@gmail.com) on 25/05/2016.
 *
 * @author  rrequena
 * @version 1.0
 * @since   Java JDK 1.7
 */
@Path("/rest/api/v1/nodes")
public class Nodes implements RestFulServices {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(Nodes.class));

    /**
     * Constructor
     */
    public Nodes(){

    }

    @GZIP
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response isOnline(){

        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("online", Boolean.TRUE);
        return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();
    }

    @GZIP
    @GET
    @Path("/registered/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isNodeRegistered(@PathParam("id") String identityPublicKey){

        LOG.info("Executing getNodeProfile");
        LOG.info("identityPublicKey = "+identityPublicKey);

        try {

            Boolean online = JPADaoFactory.getNodeCatalogDao().exist(identityPublicKey);

            LOG.info("Is registered = " + online);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("isRegistered",online);

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        } catch (CantReadRecordDataBaseException e) {

            e.printStackTrace();
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("isRegistered", Boolean.FALSE);
            jsonObject.addProperty("details", e.getMessage());

            return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();

        }

    }

    @GZIP
    @GET
    @Path("/get/my/public/ip")
    @Produces(MediaType.APPLICATION_JSON)
    public Response whatIsMyPublicIp(@Context HttpServletRequest requestContext){

        LOG.info("Executing whatIsMyPublicIp");

        try {

            String yourPublicIP = requestContext.getRemoteAddr();

            LOG.info("yourPublicIP = " + yourPublicIP);

            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("success", Boolean.TRUE);
            jsonObject.addProperty("ip", yourPublicIP);

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
