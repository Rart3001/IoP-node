package org.iop.node.monitor.app.rest.services;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.util.GsonProvider;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.DatabaseManager;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.daos.AbstractBaseDao;
import com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.RestFulServices;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.jboss.resteasy.annotations.GZIP;

import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.rest.DataBases</code>
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 26/06/2016.
 *
 * @author lnacosta
 * @version 1.0
 * @since Java JDK 1.7
 */
@Path("/rest/api/v1/admin/databases")
public class DataBases implements RestFulServices {

    /**
     * Represent the logger instance
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(DataBases.class));

    /**
     * Represent the gson
     */
    private Gson gson;

    /**
     * Constructor
     */
    public DataBases(){
        super();
        this.gson = GsonProvider.getGson();
    }

    @GET
    public String isActive() {
        return "The DataBases WebService is running ...";
    }

    @GET
    @Path("/list")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    public Response listTables(){

        JsonObject result = new JsonObject();

        try {

            List<String> tableList = new ArrayList<>();
            for (EntityType entityType : DatabaseManager.getEntityManagerFactory().getMetamodel().getEntities()) {
                tableList.add(entityType.getName());
            }

            result.addProperty("list", gson.toJson(tableList));
            result.addProperty("success", Boolean.TRUE);

        } catch (Exception e) {

            LOG.error(e);
            result = new JsonObject();
            result.addProperty("success", Boolean.FALSE);
            result.addProperty("description", e.getMessage());
        }

        return Response.status(200).entity(gson.toJson(result)).build();
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    @GZIP
    public Response getTableData(@QueryParam("tableName") String tableName, @QueryParam("offSet") Integer offSet, @QueryParam("max") Integer max){

        LOG.info("Executing getTableData");
        LOG.info("tableName = "+tableName);
        LOG.info("offset = "+offSet);
        LOG.info("max = "+max);

        JsonObject result = new JsonObject();

        try {

            Class<?> clazz = Class.forName("com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities."+tableName);
            EntityType<?> entityType = DatabaseManager.getEntityManagerFactory().getMetamodel().entity(clazz);


            List<String> columnsName = new ArrayList<>();
            List<List<String>> rows = new ArrayList<>();

            LOG.debug("entityType = "+entityType);


            if (entityType != null) {

                for (Attribute<?,?> attribute :entityType.getAttributes()) {
                    columnsName.add(attribute.getName());
                }

                LOG.debug("columnsName = "+columnsName);

                AbstractBaseDao<?> abstractBaseDao = new AbstractBaseDao(entityType.getJavaType());
                List<Object[]> results = abstractBaseDao.list(entityType, offSet, max);

                LOG.debug("results = "+results);

                for (Object[] record: results) {

                    List<String> row = new ArrayList<>();

                    for (Object aRecord : record) {
                        row.add(String.valueOf(aRecord));
                    }

                    rows.add(row);

                }

                result.addProperty("columns", gson.toJson(columnsName));
                result.addProperty("rows",    gson.toJson(rows));
                result.addProperty("total",   abstractBaseDao.count(entityType));
                result.addProperty("success", Boolean.TRUE);

            } else {

                result = new JsonObject();
                result.addProperty("success", Boolean.FALSE);
                result.addProperty("description", "Database is empty");
            }


        } catch (Exception e) {

            LOG.error(e);
            result = new JsonObject();
            result.addProperty("success", Boolean.FALSE);
            result.addProperty("description", e.getMessage());
        }

        return Response.status(200).entity(gson.toJson(result)).build();

    }





}