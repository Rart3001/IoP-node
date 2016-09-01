/*
* @#AvailableNodes.java - 2016
* Copyright bitDubai.com., All rights reserved.
 * You may not modify, use, reproduce or distribute this software.
* BITDUBAI/CONFIDENTIAL
*/
package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_api.layer.osa_android.location_system.Location;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.profiles.NodeProfile;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.GeoLocation;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.NodeCatalog;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.RestFulServices;
import com.google.gson.JsonObject;
import org.apache.commons.lang.ClassUtils;
import org.jboss.logging.Logger;
import org.jboss.resteasy.annotations.GZIP;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.AvailableNodes</code>
 * <p/>
 * Created by Hendry Rodriguez - (elnegroevaristo@gmail.com) on 26/04/16.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@Path("/rest/api/v1/available/nodes")
public class AvailableNodes implements RestFulServices {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(AvailableNodes.class));

    /**
     * Constructor
     */
    public AvailableNodes(){

    }

    @POST
    @GZIP
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAvailableNodesProfile(@FormParam("latitude") String latitudeString, @FormParam("longitude") String longitudeString){

        JsonObject jsonObject = new JsonObject();

        LOG.info("Executing listAvailableNodesProfile");
        LOG.info("latitude = " + latitudeString + " longitude = " + longitudeString);

        try {

            /*
             * Cast to Double the String Receive
             */
            Double latitudeSource = Double.parseDouble(latitudeString);
            Double longitudeSource = Double.parseDouble(longitudeString);

            /*
             * Get the locationSource to do the filter of Geolocation
             */
            Location locationSource = new GeoLocation("", latitudeSource, longitudeSource);

            /*
             * Get the node catalog list
             */
            List<NodeCatalog> nodesCatalogs = JPADaoFactory.getNodeCatalogDao().findAllNearTo(locationSource, 0, 5);

            if(!nodesCatalogs.isEmpty()) {

                List<NodeProfile> listNodeProfile = new ArrayList<>(nodesCatalogs.size());

                for (NodeCatalog nodesCatalog : nodesCatalogs)
                    listNodeProfile.add(nodesCatalog.getNodeProfile());

                jsonObject.addProperty("success", Boolean.TRUE);
                jsonObject.addProperty("data", GsonProvider.getGson().toJson(listNodeProfile));

            }else{

                jsonObject.addProperty("success", Boolean.FALSE);
                jsonObject.addProperty("message", "There isn't content in the Table.");

            }

        } catch (Exception e) {
            jsonObject.addProperty("success", Boolean.FALSE);
            jsonObject.addProperty("message", GsonProvider.getGson().toJson(e));
        }

       return Response.status(200).entity(GsonProvider.getGson().toJson(jsonObject)).build();
    }
}
