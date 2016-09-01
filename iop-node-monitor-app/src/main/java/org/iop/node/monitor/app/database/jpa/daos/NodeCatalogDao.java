/*
 * @#NodeCatalogDao.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.node.monitor.app.database.jpa.daos;

import com.bitdubai.fermat_api.layer.osa_android.location_system.Location;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.data.DiscoveryQueryParameters;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantReadRecordDataBaseException;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantUpdateRecordDataBaseException;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.GeoLocation;
import org.iop.version_1.structure.database.jpa.entities.NodeCatalog;
import org.iop.version_1.structure.util.geolocation.BasicGeoRectangle;
import org.iop.version_1.structure.util.geolocation.CoordinateCalculator;

import java.util.*;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.NodeCatalogDao</code>
 * is the responsible for manage the <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.NodeCatalog</code> entity
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 22/07/16
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class NodeCatalogDao extends AbstractBaseDao<NodeCatalog> {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(NodeCatalogDao.class));

    /**
     * Constructor
     */
    public NodeCatalogDao() {
        super(NodeCatalog.class);
    }

    public final void increaseLateNotificationCounter(final String id,
                                                      final Integer quantity) throws CantUpdateRecordDataBaseException {

        LOG.debug("Executing increaseLateNotificationCounter quantity (" + quantity + ")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query query = connection.createQuery("UPDATE NodeCatalog a SET a.lateNotificationsCounter = :lateNotificationsCounter WHERE a.id = :id");
            query.setParameter("id", id);
            query.setParameter("lateNotificationsCounter", quantity);
            query.executeUpdate();

            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantUpdateRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    public final void changeOfflineCounter(final String  id      ,
                                           final Integer quantity) throws CantUpdateRecordDataBaseException {

        LOG.debug("Executing increaseLateNotificationCounter quantity (" + quantity + ")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query query = connection.createQuery("UPDATE NodeCatalog a SET a.offlineCounter = :offlineCounter WHERE a.id = :id");
            query.setParameter("id", id);
            query.setParameter("offlineCounter", quantity);
            query.executeUpdate();

            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantUpdateRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    public final void increaseTriedToPropagateTimes(final String id) throws CantUpdateRecordDataBaseException {

        LOG.debug("Executing increaseTriedToPropagateTimes id (" + id + ")");

        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query query = connection.createQuery("UPDATE NodeCatalog a SET a.triedToPropagateTimes = a.triedToPropagateTimes+1 WHERE a.id = :id");
            query.setParameter("id", id);
            query.executeUpdate();

            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantUpdateRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    public final Long getCountOfNodesToPropagateWith(final String identityPublicKey) throws CantReadRecordDataBaseException {

        LOG.debug("Executing getCountOfNodesToPropagateWith identityPublicKey (" + identityPublicKey + ")");

        EntityManager connection = getConnection();

        try {

            String sqlQuery ="SELECT COUNT(a.id) " +
                    "FROM NodeCatalog a " +
                    "WHERE a.id <> :id";

            TypedQuery<Long> q = connection.createQuery(sqlQuery, Long.class);

            q.setParameter("id", identityPublicKey);

            return (long) q.getSingleResult().intValue();

        } catch (Exception e){
            throw new CantReadRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    public final Long getCountOfItemsToShare(final Long currentNodesInCatalog) throws CantReadRecordDataBaseException {

        LOG.debug("Executing getCountOfItemsToShare currentNodesInCatalog (" + currentNodesInCatalog + ")");

        EntityManager connection = getConnection();

        try {

            String sqlQuery ="SELECT COUNT(a.id) " +
                    "FROM NodeCatalog a " +
                    "WHERE a.pendingPropagations > 0 AND a.triedToPropagateTimes > :triedToPropagateTimes";

            TypedQuery<Long> q = connection.createQuery(sqlQuery, Long.class);

            q.setParameter("triedToPropagateTimes", currentNodesInCatalog);

            return (long) q.getSingleResult().intValue();

        } catch (Exception e){
            throw new CantReadRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

//    public final List<NodePropagationInformation> listItemsToShare(final Long currentNodesInCatalog) throws CantReadRecordDataBaseException {
//
//        LOG.debug("Executing NodeCatalogDao.listItemsToShare currentNodesInCatalog (" + currentNodesInCatalog + ")");
//
//        EntityManager connection = getConnection();
//        connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
//
//        try {
//
//            String sqlQuery ="SELECT NEW NodePropagationInformation(a.id, a.version) " +
//                    "FROM NodeCatalog a " +
//                    "WHERE a.triedToPropagateTimes < :currentNodesInCatalog AND a.pendingPropagations > 0";
//
//            TypedQuery<NodePropagationInformation> q = connection.createQuery(sqlQuery, NodePropagationInformation.class);
//
//            q.setParameter("currentNodesInCatalog", currentNodesInCatalog);
//
//            return q.getResultList();
//
//        } catch (Exception e){
//            throw new CantReadRecordDataBaseException(e, "Network Node", "");
//        } finally {
//            connection.close();
//        }
//    }
//
//    public final List<NodePropagationInformation> listInformationRequested(final Integer max   ,
//                                                                           final Integer offset) throws CantReadRecordDataBaseException {
//
//        LOG.debug("Executing NodeCatalogDao.listInformationRequested max (" + max + ") offset (" + offset + ")");
//
//        EntityManager connection = getConnection();
//        connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
//
//        try {
//
//            String sqlQuery ="SELECT NEW NodePropagationInformation(a.id, a.version) FROM NodeCatalog a";
//
//            TypedQuery<NodePropagationInformation> q = connection.createQuery(sqlQuery, NodePropagationInformation.class);
//
//            q.setFirstResult(offset);
//            q.setMaxResults(max);
//
//            return q.getResultList();
//
//        } catch (Exception e){
//            throw new CantReadRecordDataBaseException(e, "Network Node", "");
//        } finally {
//            connection.close();
//        }
//    }
//
//    public final NodePropagationInformation getNodePropagationInformation(final String publicKey) throws CantReadRecordDataBaseException {
//
//        LOG.debug("Executing NodeCatalogDao.getNodePropagationInformation publicKey (" + publicKey + ")");
//
//        EntityManager connection = getConnection();
//        connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);
//
//        try {
//
//            String sqlQuery ="SELECT NEW NodePropagationInformation(n.id, n.version) " +
//                    "FROM NodeCatalog n " +
//                    "WHERE a.id < :publicKey";
//
//            TypedQuery<NodePropagationInformation> q = connection.createQuery(sqlQuery, NodePropagationInformation.class);
//
//            q.setParameter("publicKey", publicKey);
//
//            return q.getSingleResult();
//
//        } catch (Exception e){
//            throw new CantReadRecordDataBaseException(e, "Network Node", "");
//        } finally {
//            connection.close();
//        }
//    }

    public final List<NodeCatalog> listNodesToPropagateWith(final String  identityPublicKey,
                                                            final Integer max              ,
                                                            final Integer offset           ) throws CantReadRecordDataBaseException {

        LOG.debug("Executing listNodesToPropagateWith identityPublicKey (" + identityPublicKey + "), max ("+ max + "), offset ("+offset+")");

        EntityManager connection = getConnection();
        connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<NodeCatalog> criteriaQuery = criteriaBuilder.createQuery(NodeCatalog.class);
            Root<NodeCatalog> entities = criteriaQuery.from(NodeCatalog.class);

            criteriaQuery.select(entities);

            Predicate filter = criteriaBuilder.notEqual(entities.get("id"), identityPublicKey);

            criteriaQuery.where(filter);

            List<Order> orderList = new ArrayList<>();
            orderList.add(criteriaBuilder.asc(entities.get("lateNotificationsCounter")));
            orderList.add(criteriaBuilder.asc(entities.get("offlineCounter")));

            criteriaQuery.orderBy(orderList);

            TypedQuery<NodeCatalog> query = connection.createQuery(criteriaQuery);

            query.setFirstResult(offset);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception e){
            throw new CantReadRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }
    }


    public final void decreasePendingPropagationsCounter(final String id) throws CantUpdateRecordDataBaseException {

        LOG.debug("Executing decreasePendingPropagationsCounter");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query query = connection.createQuery("UPDATE NodeCatalog a SET a.pendingPropagations = a.pendingPropagations-1 WHERE a.id = :id");
            query.setParameter("id", id);
            query.executeUpdate();

            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantUpdateRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    public List<NodeCatalog> findAllNearTo(final Location nearTo,
                                           final int      max   ,
                                           final int      offset) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(nearTo)
                .append(", ")
                .append(max)
                .append(", ")
                .append(offset)
                .append(")")
                .toString());

        EntityManager connection = getConnection();
        connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);


        try {
            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<NodeCatalog> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<NodeCatalog> entities = criteriaQuery.from(entityClass);
            BasicGeoRectangle basicGeoRectangle = CoordinateCalculator.calculateCoordinate(nearTo, 10);
            List<Predicate> predicates = new ArrayList<>();
            Predicate filter;

            //create criteria builder with location
            //Lower corner queries
            Path<Double> path = entities.get("location").get("latitude");
            //lower latitude
            filter = criteriaBuilder.greaterThan(path, basicGeoRectangle.getLowerLatitude());
            predicates.add(filter);
            //lower longitude
            path = entities.get("location").get("longitude");
            filter = criteriaBuilder.greaterThan(path, basicGeoRectangle.getLowerLongitude());
            predicates.add(filter);
            //upper latitude
            path = entities.get("location").get("latitude");
            filter = criteriaBuilder.lessThan(path, basicGeoRectangle.getUpperLatitude());
            predicates.add(filter);
            //upper longitude
            path = entities.get("location").get("longitude");
            filter = criteriaBuilder.lessThan(path, basicGeoRectangle.getUpperLongitude());
            predicates.add(filter);


            // Add the conditions of the where
            criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            //TODO: determinate the distance of every actor and order it
            //criteriaQuery.orderBy(criteriaBuilder.asc(entities.get(attributeNameOrder)));
            Root<NodeCatalog> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(root);

            TypedQuery<NodeCatalog> query = connection.createQuery(criteriaQuery);

            query.setFirstResult(offset);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception e){
            throw new CantReadRecordDataBaseException(
                    CantReadRecordDataBaseException.DEFAULT_MESSAGE,
                    e,
                    "Network Node",
                    "Cannot load records from database");
        } finally {
            connection.close();
        }

    }

    /**
     * This method returns a list of nodes filtered by discoveryQueryParameters
     * @param discoveryQueryParameters
     * @param nodePublicKey
     * @param max
     * @param offset
     * @return
     * @throws CantReadRecordDataBaseException
     */
    public List<NodeCatalog> findAll(
            final DiscoveryQueryParameters discoveryQueryParameters,
            final String nodePublicKey,
            int max,
            int offset) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(discoveryQueryParameters)
                .append(", ")
                .append(nodePublicKey)
                .append(", ")
                .append(max)
                .append(", ")
                .append(offset)
                .append(")")
                .toString());
        EntityManager connection = getConnection();
        connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);

        try {
            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<NodeCatalog> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<NodeCatalog> entities = criteriaQuery.from(entityClass);
            BasicGeoRectangle basicGeoRectangle = new BasicGeoRectangle();
            Map<String, Object> filters = buildFilterGroupFromDiscoveryQueryParameters(
                    discoveryQueryParameters);
            //Verify that the filters are not empty
            if (filters != null && filters.size() > 0) {

                List<Predicate> predicates = new ArrayList<>();

                //We are gonna calculate the geoRectangle only in case this filter exists
                if(filters.containsKey("location")){
                    //Getting GeoLocation from client
                    GeoLocation clientGeoLocation = getClientGeoLocation(
                            nodePublicKey);
                    //Calculate the BasicGeoRectangle
                    double distance;
                    try{
                        distance = (double) filters.get("distance");
                    } catch (ClassCastException cce){
                        //In this case, we assume a minimum distance as 1Km
                        distance = 1;
                    }
                    basicGeoRectangle = CoordinateCalculator
                            .calculateCoordinate(
                                    clientGeoLocation,
                                    distance);
                }

                //Walk the key map that representing the attribute names
                for (String attributeName : filters.keySet()) {

                    //Verify that the value is not empty
                    if (filters.get(attributeName) != null && filters.get(attributeName) != "") {

                        Predicate filter;

                        // If it contains the "." because it is filtered by an attribute of an attribute
                        if (attributeName.contains(".")) {

                            StringTokenizer parts = new StringTokenizer(attributeName,".");
                            Path<Object> path = null;

                            //Walk the path for all required parts
                            while (parts.hasMoreElements()) {

                                if (path == null) {
                                    path = entities.get(parts.nextToken());
                                }else {
                                    path = path.get(parts.nextToken());
                                }
                            }

                            filter = criteriaBuilder.equal(path, filters.get(attributeName));

                        }else{

                            if(attributeName.contains("location")){
                                //create criteria builder with location
                                //Lower corner queries
                                Path<Double> path = entities.get(attributeName);
                                path = path.get("latitude");
                                //lower latitude
                                filter = criteriaBuilder.greaterThan(
                                        path, basicGeoRectangle.getLowerLatitude());
                                predicates.add(filter);
                                //lower longitude
                                path = entities.get(attributeName);
                                path = path.get("longitude");
                                filter = criteriaBuilder.greaterThan(
                                        path, basicGeoRectangle.getLowerLongitude());
                                predicates.add(filter);
                                //upper latitude
                                path = entities.get(attributeName);
                                path = path.get("latitude");
                                filter = criteriaBuilder.lessThan(
                                        path, basicGeoRectangle.getUpperLatitude());
                                predicates.add(filter);
                                //upper longitude
                                path = entities.get(attributeName);
                                path = path.get("longitude");
                                filter = criteriaBuilder.lessThan(
                                        path, basicGeoRectangle.getUpperLongitude());
                                predicates.add(filter);
                                //The location filters are set, we will continue;
                                continue;

                            } else {

                                //Create the new condition for each attribute we get
                                filter = criteriaBuilder.equal(entities.get(attributeName), filters.get(attributeName));

                            }

                        }

                        predicates.add(filter);
                    }

                }

                // Add the conditions of the where
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            }

            //TODO: determinate the distance of every actor and order it
            //criteriaQuery.orderBy(criteriaBuilder.asc(entities.get(attributeNameOrder)));
            Root<NodeCatalog> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(root);
            TypedQuery<NodeCatalog> query = connection.createQuery(criteriaQuery);
            return query.getResultList();

        } catch (Exception e){
            throw new CantReadRecordDataBaseException(
                    CantReadRecordDataBaseException.DEFAULT_MESSAGE,
                    e,
                    "Network Node",
                    "Cannot load records from database");
        } finally {
            connection.close();
        }

    }

    /**
     * Construct database filter from discovery query parameters.
     *
     * @param params parameters to filter the actors catalog table.
     *
     * @return a list with the database table filters.
     */
    private Map<String, Object> buildFilterGroupFromDiscoveryQueryParameters(final DiscoveryQueryParameters params){

        final Map<String, Object> filters = new HashMap<>();

        if (params.getIdentityPublicKey() != null)
            filters.put("id", params.getIdentityPublicKey());

        if (params.getLastConnectionTime() != null)
            filters.put("location", params.getLocation());

        if (params.getLastConnectionTime() != null)
            filters.put("distance", params.getDistance());

        return filters;
    }

    /**
     * This method returns the nodeCatalog location by nodePublicKey
     * @param nodePublicKey
     * @return
     * @throws CantReadRecordDataBaseException
     */
    private GeoLocation getClientGeoLocation(String nodePublicKey)
            throws CantReadRecordDataBaseException {
        NodeCatalog nodeCatalog = findById(nodePublicKey);
        return nodeCatalog.getLocation();
    }

    /**
     * This method returns the node version by Id
     * @param nodeId
     * @return
     * @throws CantReadRecordDataBaseException
     */
    public int getNodeVersionById(String nodeId) throws CantReadRecordDataBaseException {
        NodeCatalog nodeCatalog = findById(nodeId);
        if(nodeCatalog == null){
            //Default version
            return 0;
        }
        return nodeCatalog.getVersion();
    }

    /**
     * This method returns the ip to propagate with
     * @param identityPublicKey
     * @param seedNodeIp
     * @return
     * @throws CantReadRecordDataBaseException
     */
    public final String getNodeIpToPropagateWith(
            final String identityPublicKey,
            final String seedNodeIp) throws CantReadRecordDataBaseException {

        LOG.debug("Executing getNodeIpToPropagateWith identityPublicKey (" + identityPublicKey + "), seedNodeIp ("+ seedNodeIp + ")");

        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<NodeCatalog> criteriaQuery = criteriaBuilder.createQuery(NodeCatalog.class);
            Root<NodeCatalog> entities = criteriaQuery.from(NodeCatalog.class);

            criteriaQuery.select(entities);

            Predicate filter = criteriaBuilder.notEqual(entities.get("id"), identityPublicKey);

            criteriaQuery.where(filter);

            filter = criteriaBuilder.notEqual(entities.get("ip"), seedNodeIp);

            criteriaQuery.where(filter);

            List<Order> orderList = new ArrayList<>();
            orderList.add(criteriaBuilder.asc(entities.get("lateNotificationsCounter")));
            orderList.add(criteriaBuilder.asc(entities.get("offlineCounter")));

            criteriaQuery.orderBy(orderList);

            TypedQuery<NodeCatalog> query = connection.createQuery(criteriaQuery);

            List<NodeCatalog> nodeList = query.getResultList();
            if(nodeList.isEmpty()){
                return null;
            } else{
                String nodeIp = nodeList.get(0).getIp();
                //I'll set null this list trying to call the Garbage Collector
                return nodeIp;
            }

        } catch (Exception e){
            throw new CantReadRecordDataBaseException(e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

}
