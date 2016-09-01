package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.JPADaoFactory;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ActorCatalog;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.Client;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.GeoLocation;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.NodeCatalog;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.RestFulServices;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.DeveloperDatabaseResource</code>
 * <p/>
 * Created by Leon Acosta - (laion.cj91@gmail.com) on 29/03/2016.
 *
 * @author lnacosta
 * @version 1.0
 * @since Java JDK 1.7
 */
@Path("/rest/api/v1/developerDatabase")
public class DeveloperDatabaseResource implements RestFulServices {

    @GET
    @Path("/actorCatalog/{offset}/{max}")
    @Produces(MediaType.TEXT_PLAIN)
    public String actorCatalog(@PathParam("offset") String offset, @PathParam("max") String max){

        try {

            List<ActorCatalog> catalogList = JPADaoFactory.getActorCatalogDao().list(Integer.valueOf(offset), Integer.valueOf(max));

            if (!catalogList.isEmpty()) {

                StringBuilder stringBuilder = new StringBuilder();

                for (ActorCatalog record : catalogList) {
                    stringBuilder.append(actorCatalogToString(record));
                    stringBuilder.append("\n\n");
                }

                return stringBuilder.toString();
            } else
                return "Developer Database Restful Service says: \"Table has no content!\".";

        } catch (Exception e) {

            e.printStackTrace();
            return "Developer Database Restful Service says: \"There was an error trying to list content!\".";
        }
    }

    private String actorCatalogToString(ActorCatalog actorCatalog) {

        return "ActorCatalog{" +
                "id='" + actorCatalog.getId() + '\'' +
                ", name=" + actorCatalog.getName() +
                ", version=" + actorCatalog.getVersion() +
                ", lastUpdateType=" + actorCatalog.getLastUpdateType() +
                ", pendingPropagations=" + actorCatalog.getPendingPropagations() +
                ", triedToPropagateTimes=" + actorCatalog.getTriedToPropagateTimes() +
                ", sessionId=" + (actorCatalog.getSession() != null ? actorCatalog.getSession().getId() : "NO_SESSION") +
                ", extraData=" + (actorCatalog.getExtraData() != null ? actorCatalog.getExtraData() : "NO_EXTRA_DATA") +
                ", location=" + (actorCatalog.getLocation() != null ? actorCatalog.getLocation() : "NO_LOCATION") +
                ", homeNode=" + actorCatalog.getHomeNode().getId() +
                "} ";
    }

    @GET
    @Path("/nodeCatalog/{offset}/{max}")
    @Produces(MediaType.TEXT_PLAIN)
    public String nodeCatalog(@PathParam("offset") String offset, @PathParam("max") String max){

        try {

            List<NodeCatalog> catalogList = JPADaoFactory.getNodeCatalogDao().list(Integer.valueOf(offset), Integer.valueOf(max));

            if (!catalogList.isEmpty()) {

                StringBuilder stringBuilder = new StringBuilder();

                for (NodeCatalog record : catalogList) {
                    stringBuilder.append(nodeCatalogToString(record));
                    stringBuilder.append("\n\n");
                }

                return stringBuilder.toString();
            } else
                return "Developer Database Restful Service says: \"Table has no content!\".";

        } catch (Exception e) {

            e.printStackTrace();
            return "Developer Database Restful Service says: \"There was an error trying to list content!\".";
        }
    }

    private String nodeCatalogToString(NodeCatalog nodeCatalog) {

        return "NodeCatalog{" +
                "id='" + nodeCatalog.getId() + '\'' +
                ", url='" + nodeCatalog.getIp() + ':' +nodeCatalog.getDefaultPort() +
                ", lateNotificationsCounter=" + nodeCatalog.getLateNotificationsCounter() +
                ", offlineCounter=" + nodeCatalog.getOfflineCounter() +
                ", version=" + nodeCatalog.getVersion() +
                ", pendingPropagations=" + nodeCatalog.getPendingPropagations() +
                ", triedToPropagateTimes=" + nodeCatalog.getTriedToPropagateTimes() +
                "} ";
    }

    @GET
    @Path("/client/{offset}/{max}")
    @Produces(MediaType.TEXT_PLAIN)
    public String client(@PathParam("offset") String offset, @PathParam("max") String max){

        try {

            List<Client> clientList = JPADaoFactory.getClientDao().list(Integer.valueOf(offset), Integer.valueOf(max));

            if (!clientList.isEmpty()) {

                StringBuilder stringBuilder = new StringBuilder();

                for (Client record : clientList) {
                    stringBuilder.append(clientToString(record));
                    stringBuilder.append("\n\n");
                }

                return stringBuilder.toString();
            } else
                return "Developer Database Restful Service says: \"Table has no content!\".";

        } catch (Exception e) {

            e.printStackTrace();
            return "Developer Database Restful Service says: \"There was an error trying to list content!\".";
        }
    }

    private String clientToString(Client client){

        return "Client{" +
                "id='" + client.getId() + '\'' +
                ", location='" + client.getLocation()+
                ", session=" + (client.getSession() != null ? client.getSession().getId() : "NO_SESSION") +
                ", status=" + client.getStatus() +
                ", deviceType=" + client.getDeviceType() +
                "} ";
    }

    @GET
    @Path("/geolocation/{offset}/{max}")
    @Produces(MediaType.TEXT_PLAIN)
    public String geolocation(@PathParam("offset") String offset, @PathParam("max") String max){

        try {

            List<GeoLocation> geoLocationList = JPADaoFactory.getGeoLocationDao().list(Integer.valueOf(offset), Integer.valueOf(max));

            if (!geoLocationList.isEmpty()) {

                StringBuilder stringBuilder = new StringBuilder();

                for (GeoLocation geoLocation : geoLocationList) {
                    stringBuilder.append(geoLocationToString(geoLocation));
                    stringBuilder.append("\n\n");
                }

                return stringBuilder.toString();
            } else
                return "Developer Database Restful Service says: \"Table has no content!\".";

        } catch (Exception e) {

            e.printStackTrace();
            return "Developer Database Restful Service says: \"There was an error trying to list content!\".";
        }
    }

    private String geoLocationToString(GeoLocation geoLocation){

        return "Client{" +
                "id='" + geoLocation.getId() + '\'' +
                ", latitude='" + geoLocation.getLatitude()+
                ", longitude=" + geoLocation.getLongitude() +
                "} ";
    }

}