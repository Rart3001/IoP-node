/*
 * @#ClientDao.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.node.monitor.app.database.jpa.daos;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantDeleteRecordDataBaseException;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantReadRecordDataBaseException;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.Client;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.List;


/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.ClientDao</code>
 * is the responsible for manage the <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.Client</code> entity
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 24/07/16
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class ClientDao extends AbstractBaseDao<Client>{

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(ClientDao.class));

    /**
     * Constructor
     */
    public ClientDao(){
        super(Client.class);
    }

    public void deleteAllClientGeolocation() throws CantDeleteRecordDataBaseException {

        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            List<Client> clientList = list();

            for (Client clientPk: clientList) {
                Query deleteQuery = connection.createQuery("DELETE FROM GeoLocation gl WHERE gl.id = :id");
                deleteQuery.setParameter("id", clientPk.getId());
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
     * Get the session id
     * @param clientId
     * @return String
     * @throws CantReadRecordDataBaseException
     */
    public String getSessionId(String clientId) throws CantReadRecordDataBaseException {

        LOG.debug("Executing getSessionId(" + clientId + ")");
        EntityManager connection = getConnection();

        try {

            TypedQuery<String> query = connection.createQuery("SELECT c.sessionId FROM Client c WHERE c.id = :id", String.class);
            query.setParameter("id", clientId);
            query.setMaxResults(1);

            List<String> ids = query.getResultList();
            return (ids != null && !ids.isEmpty() ? ids.get(0) : null);

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
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
    public void checkOut(String sessionId) throws CantDeleteRecordDataBaseException {

        LOG.debug("Executing delete("+sessionId+")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            TypedQuery<Client> query = connection.createQuery("SELECT c FROM Client c WHERE c.sessionId = :id", Client.class);
            query.setParameter("id", sessionId);
            Client client = query.getSingleResult();

            connection.remove(connection.contains(client) ? client : connection.merge(client));

            LOG.info("Deleted client = 1");

            Query deleteQuery = connection.createQuery("DELETE FROM NetworkService c WHERE c.sessionId = :id");
            deleteQuery.setParameter("id", sessionId);
            int result = deleteQuery.executeUpdate();

            LOG.info("Deleted ns = "+result);



            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            LOG.error(e);
            if (transaction.isActive()) {
                transaction.rollback();
            }
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

            TypedQuery<Client> query = connection.createQuery("SELECT c FROM Client c WHERE c.sessionId = :id", Client.class);
            query.setParameter("id", sessionId);
            Client client = query.getSingleResult();

            connection.remove(connection.contains(client) ? client : connection.merge(client));

            LOG.info("Deleted client = 1");

            Query deleteQuery = connection.createQuery("DELETE FROM NetworkService c WHERE c.sessionId = :id");
            deleteQuery.setParameter("id", sessionId);
            int result = deleteQuery.executeUpdate();

            LOG.info("Deleted ns = "+result);


        } catch (Exception e) {
            LOG.error(e);
        }

    }

}
