/*
 * @#ClientDao.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.version_1.structure.database.jpa.daos;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantDeleteRecordDataBaseException;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantReadRecordDataBaseException;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.EventListener;

import javax.persistence.*;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;


/**

 * @version 1.0
 * @since Java JDK 1.7
 * //todo: falta hacer el checkout de todos los eventos cuando un cliente/ns/actor se desconecta
 */
public class EventListenersDao extends AbstractBaseDao<EventListener>{

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger("debugLogger");

    /**
     * Constructor
     */
    public EventListenersDao(){
        super(EventListener.class);
    }


    /**
     * Get the session id
     * @param eventId
     * @return String
     * @throws CantReadRecordDataBaseException
     */
    public String getSessionId(String eventId) throws CantReadRecordDataBaseException {

        LOG.debug("Executing getSessionId(" + eventId + ")");
        EntityManager connection = getConnection();

        try {

            TypedQuery<String> query = connection.createQuery("SELECT c.sessionId FROM EventListener c WHERE c.id = :id", String.class);
            query.setParameter("id", eventId);
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

    public List<EventListener> getEventForCodeAndCondition(short eventCode,String condition) throws CantReadRecordDataBaseException {

        LOG.debug("Executing getEventForCodeAndCondition(" + eventCode + ", condition: "+condition+")");
        EntityManager connection = getConnection();

        try {
            //todo: no hace falta cargar todo en memoria en realidad, con cargar solo el id de la session me basta pero esto es para probar si todo va bien
            TypedQuery<EventListener> query = connection.createQuery("SELECT EventListener c FROM EventListener c WHERE c.event = :eventCode AND c.condition = :condition", EventListener.class);
            query.setParameter("eventCode", eventCode);
            query.setParameter("condition", condition);
            return query.getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    public List<EventListener> getEventsForCodeAndConditions(short eventCode, List<String> conditions) throws CantReadRecordDataBaseException {
        LOG.debug("Executing getEventsForCodeAndConditions(" + eventCode + ", condition: "+conditions+")");
        EntityManager connection = getConnection();

        try {

            connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<EventListener> criteriaQuery = criteriaBuilder.createQuery(EventListener.class);
            Root<EventListener> root = criteriaQuery.from(EventListener.class);

            criteriaQuery.select(root)
                    .where(root.get("condition").in(conditions),
                            criteriaBuilder.equal(root.get("event"), eventCode
                            )
                    );

            TypedQuery<EventListener> query = connection.createQuery(criteriaQuery);
            return query.getResultList();
        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    /**
     * Delete a client from data base whit have
     * the sessionId
     *
     * @param eventPackageId
     * @throws CantDeleteRecordDataBaseException
     */
    public void removeEvent(String eventPackageId) throws CantDeleteRecordDataBaseException {

        LOG.debug("Executing delete("+eventPackageId+")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query deleteQuery = connection.createQuery("DELETE FROM EventListener c WHERE c.id = :id");
            deleteQuery.setParameter("id", eventPackageId);
            int result = deleteQuery.executeUpdate();

            LOG.info("Deleted events = "+result);

            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            LOG.error(e);
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantDeleteRecordDataBaseException(CantDeleteRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    public void removeEventListenersFromSessionId(String id) throws CantDeleteRecordDataBaseException {
        LOG.debug("Executing delete("+id+")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query deleteQuery = connection.createQuery("DELETE FROM EventListener c WHERE c.sessionId = :id");
            deleteQuery.setParameter("id", id);
            int result = deleteQuery.executeUpdate();

            LOG.info("Deleted events = "+result);

            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            LOG.error(e);
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantDeleteRecordDataBaseException(CantDeleteRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }
}
