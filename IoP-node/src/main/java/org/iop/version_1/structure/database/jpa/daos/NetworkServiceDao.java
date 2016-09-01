/*
 * @#NetworkServiceDao.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.version_1.structure.database.jpa.daos;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantDeleteRecordDataBaseException;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.NetworkService;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import java.util.List;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.NetworkServiceDao</code>
 * is the responsible for manage the <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.NetworkService</code> entity
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 24/07/16
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class NetworkServiceDao extends AbstractBaseDao<NetworkService> {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(NetworkServiceDao.class));

    /**
     * Constructor
     */
    public NetworkServiceDao(){
        super(NetworkService.class);
    }

    public void deleteAllNetworkServiceGeolocation() throws CantDeleteRecordDataBaseException {

        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            List<NetworkService> networkServiceList = list();

            for (NetworkService networkServicePk: networkServiceList) {
                Query deleteQuery = connection.createQuery("DELETE FROM GeoLocation gl WHERE gl.id = :id");
                deleteQuery.setParameter("id", networkServicePk.getId());
                deleteQuery.executeUpdate();
            }

            transaction.commit();
            connection.flush();

        }catch (Exception e){
            LOG.error(e);
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantDeleteRecordDataBaseException(e, "Network Node", "");
        }finally {
            connection.close();
        }
    }

    /**
     * Delete a client from data base whit have
     * the sessionId
     *
     * @param sessionId
     * @throws CantDeleteRecordDataBaseException
     */
    void checkOut(String sessionId) throws CantDeleteRecordDataBaseException {

        LOG.debug("Executing delete("+sessionId+")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query deleteQuery = connection.createQuery("DELETE FROM NetworkService c WHERE c.sessionId = :id");
            deleteQuery.setParameter("id", sessionId);
            int result = deleteQuery.executeUpdate();

            LOG.debug("Delete row = "+result+"");

            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            LOG.error(e);
            transaction.rollback();
            throw new CantDeleteRecordDataBaseException(CantDeleteRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    /**
     * This method only append transaction to an open connection
     *
     * @param connection
     * @param sessionId
     * @throws CantDeleteRecordDataBaseException
     */
    public void chaincheckOut(EntityManager connection,String sessionId) throws CantDeleteRecordDataBaseException {
        try {
            Query deleteQuery = connection.createQuery("DELETE FROM NetworkService c WHERE c.sessionId = :id");
            deleteQuery.setParameter("id", sessionId);
            int result = deleteQuery.executeUpdate();
            LOG.info("Deleted ns = "+result);
        } catch (Exception e) {
            LOG.error(e);
        }

    }
}
