/*
 * @#AbstractBaseDao.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.node.monitor.app.database.jpa.daos;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantDeleteRecordDataBaseException;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantInsertRecordDataBaseException;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantReadRecordDataBaseException;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantUpdateRecordDataBaseException;
import org.apache.commons.lang3.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.node.monitor.app.database.jpa.DatabaseManager;
import org.iop.node.monitor.app.database.jpa.entities.AbstractBaseEntity;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.persistence.metamodel.EntityType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.AbstractBaseDao</code> is
 * the base for all Data Access Object
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 22/07/16
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class AbstractBaseDao<E extends AbstractBaseEntity> {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(AbstractBaseDao.class));

    /**
     * Represent the entityClass
     */
    final Class<E> entityClass;

    /**
     * Constructor
     *
     * @param entityClass
     */
    public AbstractBaseDao(Class<E> entityClass) {
        this.entityClass = entityClass;
    }

    /**
     * Get a new connection
     *
     * @return EntityManager
     */
    EntityManager getConnection() {
        return DatabaseManager.getConnection();
    }

    /**
     * Find a entity by his id
     *
     * @param id
     * @return Entity
     */
    public E findById(Object id) throws CantReadRecordDataBaseException {

        LOG.debug("Executing findById(" +
                id +
                ")");

        if (id == null){
            throw new IllegalArgumentException("The id can't be null");
        }

        EntityManager connection = getConnection();
        E entity = null;

        try {
            entity = connection.find(entityClass, id);
        } catch (Exception e){
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

        return entity;

    }


    public <T> T findValueById(Object id,Class<T> tClass,String columnName) throws CantReadRecordDataBaseException {

        LOG.debug("Executing findById(" +
                id +
                ")");

        if (id == null){
            throw new IllegalArgumentException("The id can't be null");
        }
        EntityManager connection = null;
        try {
            connection = getConnection();
            TypedQuery<T> query = connection.createQuery("SELECT a." + columnName + " FROM " + ClassUtils.getShortClassName(entityClass) + " a WHERE a.id = :id", tClass);
            query.setParameter("id", id);
            query.setMaxResults(1);
            List<T> resultList = query.getResultList();

            return (!resultList.isEmpty()) ? resultList.get(0) : null;
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (connection != null) {
                connection.close();
            }
        }
        return null;

    }




    /**
     * Persist the entity into the data base
     *
     * @param entity
     * @throws CantReadRecordDataBaseException
     */
    public void persist(E entity) throws CantInsertRecordDataBaseException {

        LOG.debug("Executing persist(" + entity + ")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();
            connection.persist(entity);
            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantInsertRecordDataBaseException(CantInsertRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    /**
     * Save the entity into the data base, verify is exist; if exist make a update
     * if no make a persist
     *
     * @param entity
     * @throws CantReadRecordDataBaseException
     */
    public void save(E entity) throws CantReadRecordDataBaseException, CantUpdateRecordDataBaseException, CantInsertRecordDataBaseException {

        LOG.debug("Executing save("+entity+")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            if ((entity.getId() != null) &&
                    (exist(connection, entity.getId()))){

                try {

                    transaction.begin();
                    connection.merge(entity);
                    transaction.commit();
                    connection.flush();

                } catch (Exception e){
                    LOG.error(e);
                    if(transaction.isActive())
                        transaction.rollback();
                    throw new CantUpdateRecordDataBaseException(CantUpdateRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
                }

            }else {

                try {

                    transaction.begin();
                    connection.persist(entity);
                    connection.flush();
                    transaction.commit();

                }catch (Exception e){
                    LOG.error(e);
                    if(transaction.isActive())
                        transaction.rollback();
                    throw new CantInsertRecordDataBaseException(CantInsertRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
                }

            }

        }finally {
            connection.close();
        }

    }

    /**
     * Update a entity values in the database
     *
     * @param entity
     * @throws CantUpdateRecordDataBaseException
     */
    public void update(E entity) throws CantUpdateRecordDataBaseException {

        LOG.debug("Executing update(" +
                entityClass +
                ")");
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();
            connection.merge(entity);
            transaction.commit();
            connection.flush();

        } catch (Exception e) {
            e.printStackTrace();
            LOG.error(e);
            transaction.rollback();
            throw new CantUpdateRecordDataBaseException(CantUpdateRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    /**
     * Delete a entity from data base
     *
     * @param entity
     * @throws CantDeleteRecordDataBaseException
     */
    void delete(E entity) throws CantDeleteRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing delete(")
                .append(entityClass)
                .append(")")
                .toString());
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();
                connection.remove(connection.contains(entity) ? entity : connection.merge(entity));
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
     * Delete all entities from data base using SQL
     *
     * @throws CantDeleteRecordDataBaseException
     */
    public void delete() throws CantDeleteRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing deleteAll(").append(entityClass).append(")").toString());
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

                Query queryDelete = connection.createQuery("DELETE FROM "+ClassUtils.getShortClassName(entityClass));
                int deletedSessions = queryDelete.executeUpdate();

            transaction.commit();
            connection.flush();

            LOG.debug("deleted all " + ClassUtils.getShortClassName(entityClass) + " entities = " + deletedSessions);

        } catch (Exception e) {
            LOG.error(e);
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantDeleteRecordDataBaseException(CantDeleteRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        }finally {
            connection.close();
        }

    }

    /**
     * Delete all entities from data base using JPA context
     *
     * @throws CantDeleteRecordDataBaseException
     */
    public void deleteAll() throws CantDeleteRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing deleteAll(").append(entityClass).append(")").toString());
        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            CriteriaBuilder builder = connection.getCriteriaBuilder();
            CriteriaQuery<E> query = builder.createQuery(entityClass);
            Root<E> entities = query.from(entityClass);
            query.select(entities);
            query.orderBy(builder.asc(entities.get("id")));

            List<E> entitiesList = connection.createQuery(query).getResultList();

            for (E entity : entitiesList) {
                connection.remove(entity);
            }

            transaction.commit();
            connection.flush();

            LOG.debug("deleted all " + ClassUtils.getShortClassName(entityClass) + " entities = " + entitiesList.size());

        } catch (Exception e) {
            LOG.error(e);
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new CantDeleteRecordDataBaseException(CantDeleteRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");

        }finally {
            connection.close();
        }

    }


    /**
     * Delete all entities that match with the filters
     *
     * @param filters
     * @return int
     * @throws CantDeleteRecordDataBaseException
     */
    @Deprecated
    public int delete(Map<String, Object> filters) throws CantDeleteRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing delete(")
                .append(filters)
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaDelete<E> criteriaDelete = criteriaBuilder.createCriteriaDelete(entityClass);
            Root<E> entities = criteriaDelete.from(entityClass);

            //Verify that the filters are not empty
            if (filters != null && filters.size() > 0) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                //Walk the key map that representing the attribute names
                for (String attributeName : filters.keySet()) {

                    //Verify that the value is not empty
                    if (filters.get(attributeName) != null && filters.get(attributeName) != "") {

                        Predicate filter;

                        // If it contains the "." because it is filtered by an attribute of an attribute
                        if (attributeName.contains(".")) {

                            StringTokenizer parts = new StringTokenizer(attributeName, ".");
                            Path<Object> path = null;

                            //Walk the path for all required parts
                            while (parts.hasMoreElements()) {

                                if (path == null) {
                                    path = entities.get(parts.nextToken());
                                } else {
                                    path = path.get(parts.nextToken());
                                }
                            }

                            filter = criteriaBuilder.equal(path, filters.get(attributeName));

                        } else {
                            //Create the new condition for each attribute we get
                            filter = criteriaBuilder.equal(entities.get(attributeName), filters.get(attributeName));
                        }

                        predicates.add(filter);
                    }

                }

                // Add the conditions of the where
                criteriaDelete.where(predicates.toArray(new Predicate[predicates.size()]));

            }

            Query query = connection.createQuery(criteriaDelete);
            return query.executeUpdate();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantDeleteRecordDataBaseException(CantDeleteRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }


    /**
     * List all entities
     *
     * @return List
     */
    public List<E> list() throws CantReadRecordDataBaseException {

        LOG.debug("Executing list()");
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder builder = connection.getCriteriaBuilder();
            CriteriaQuery<E> query = builder.createQuery(entityClass);
            Root<E> entities = query.from(entityClass);
            query.select(entities);
            query.orderBy(builder.asc(entities.get("id")));

            return connection.createQuery(query).getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    /**
     * List all entities order by the attribute
     *
     * @param attributeOrder
     * @return List
     */
    public List<E> listOrderBy(String attributeOrder) throws CantReadRecordDataBaseException {

        LOG.debug("Executing listOrderBy()");
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder builder = connection.getCriteriaBuilder();
            CriteriaQuery<E> query = builder.createQuery(entityClass);
            Root<E> entities = query.from(entityClass);
            query.select(entities);
            query.orderBy(builder.asc(entities.get(attributeOrder)));
            return connection.createQuery(query).getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    /**
     * List all entities paginating
     *
     * @param offset
     * @param max
     * @return List
     * @throws CantReadRecordDataBaseException
     */
    public List<E> list(Integer offset, Integer max) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(offset)
                .append(", ")
                .append(max)
                .append(")")
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);

            Root<E> entities = criteriaQuery.from(entityClass);
            criteriaQuery.select(entities);
            criteriaQuery.orderBy(criteriaBuilder.asc(entities.get("id")));
            TypedQuery<E> query = connection.createQuery(criteriaQuery);
            query.setFirstResult(offset);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }


    /**
     * List all entities paginating
     * @param entityType
     * @param offset
     * @param max
     * @return List<Object[]>
     * @throws CantReadRecordDataBaseException
     */
    final public List<Object[]> list(EntityType<?> entityType, Integer offset, Integer max) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(offset)
                .append(", ")
                .append(max)
                .append(")")
                .toString());

        EntityManager connection = getConnection();
        connection.setProperty("javax.persistence.cache.storeMode", CacheStoreMode.BYPASS);

        try {

            CriteriaBuilder builder = connection.getCriteriaBuilder();
            CriteriaQuery <Object[]> criteriaQuery = builder.createQuery(Object[].class);
            Root<?> root = criteriaQuery.from(entityType.getJavaType());

            List<Selection> selectionList = new ArrayList<Selection>();
            for (Attribute<?,?> attribute :entityType.getAttributes()) {
                selectionList.add(root.get(attribute.getName()));
            }

            criteriaQuery.select(builder.array(selectionList.toArray(new Selection[selectionList.size()])));

            TypedQuery<Object[]> query = connection.createQuery(criteriaQuery);
            query.setFirstResult(offset);
            query.setMaxResults(max);
            return query.getResultList();

        } catch (Exception e){
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.clear();
            connection.close();
        }

    }


    /**
     * List all entities that match with the parameters
     *
     * @param attributeName
     * @param attributeValue
     * @return List
     * @throws CantReadRecordDataBaseException
     */
    public List<E> list(String attributeName, Object attributeValue) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(attributeName)
                .append(", ")
                .append(attributeValue)
                .append(")")
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder builder = connection.getCriteriaBuilder();
            CriteriaQuery<E> query = builder.createQuery(entityClass);
            Root<E> entities = query.from(entityClass);
            query.select(entities);

            Predicate attribute = null;

            if ((attributeName != null) && (attributeName != "")) {
                attribute = builder.equal(entities.get(attributeName), attributeValue);
            }

            query.where(attribute);
            query.orderBy(builder.asc(entities.get("id")));

            return connection.createQuery(query).getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    /**
     * List all entities that match with the parameters and
     * order by the attribute name
     *
     * @param attributeName
     * @param attributeValue
     * @param attributeNameOrder
     * @return List
     * @throws CantReadRecordDataBaseException
     */
    public List<E> list(String attributeName, Object attributeValue, String attributeNameOrder) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(attributeName)
                .append(", ")
                .append(attributeValue)
                .append(", ")
                .append(attributeNameOrder)
                .append(")")
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder builder = connection.getCriteriaBuilder();
            CriteriaQuery<E> query = builder.createQuery(entityClass);
            Root<E> entities = query.from(entityClass);
            query.select(entities);

            Predicate attribute = null;

            if ((attributeName != null) && (attributeName != "")) {
                attribute = builder.equal(entities.get(attributeName), attributeValue);
            }

            query.where(attribute);
            query.orderBy(builder.asc(entities.get(attributeNameOrder)));

            return connection.createQuery(query).getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }


    /**
     * List all entities that match with the filters and
     * paginating
     *
     * @param offset
     * @param max
     * @param filters
     * @return List
     * @throws CantReadRecordDataBaseException
     */
    public List<E> list(Integer offset, Integer max, Map<String, Object> filters) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(offset)
                .append(", ")
                .append(max)
                .append(", ")
                .append(filters)
                .append(")")
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<E> entities = criteriaQuery.from(entityClass);

            //Verify that the filters are not empty
            if (filters != null && filters.size() > 0) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                //Walk the key map that representing the attribute names
                for (String attributeName : filters.keySet()) {

                    //Verify that the value is not empty
                    if (filters.get(attributeName) != null && filters.get(attributeName) != "") {

                        Predicate filter;

                        // If it contains the "." because it is filtered by an attribute of an attribute
                        if (attributeName.contains(".")) {

                            StringTokenizer parts = new StringTokenizer(attributeName, ".");
                            Path<Object> path = null;

                            //Walk the path for all required parts
                            while (parts.hasMoreElements()) {

                                if (path == null) {
                                    path = entities.get(parts.nextToken());
                                } else {
                                    path = path.get(parts.nextToken());
                                }
                            }

                            filter = criteriaBuilder.equal(path, filters.get(attributeName));

                        } else {
                            //Create the new condition for each attribute we get
                            filter = criteriaBuilder.equal(entities.get(attributeName), filters.get(attributeName));
                        }

                        predicates.add(filter);
                    }

                }

                // Add the conditions of the where
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            }

            criteriaQuery.orderBy(criteriaBuilder.asc(entities.get("id")));
            TypedQuery<E> query = connection.createQuery(criteriaQuery);
            query.setFirstResult(offset);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }

    /**
     * List all entities that match with the filters
     *
     * @param filters
     * @return List
     * @throws CantReadRecordDataBaseException
     */
    public List<E> list(Map<String, Object> filters) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(filters)
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<E> entities = criteriaQuery.from(entityClass);
            criteriaQuery.select(entities);

            //Verify that the filters are not empty
            if (filters != null && filters.size() > 0) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                //Walk the key map that representing the attribute names
                for (String attributeName : filters.keySet()) {

                    //Verify that the value is not empty
                    if (filters.get(attributeName) != null && filters.get(attributeName) != "") {

                        Predicate filter;

                        // If it contains the "." because it is filtered by an attribute of an attribute
                        if (attributeName.contains(".")) {

                            StringTokenizer parts = new StringTokenizer(attributeName, ".");
                            Path<Object> path = null;

                            //Walk the path for all required parts
                            while (parts.hasMoreElements()) {

                                if (path == null) {
                                    path = entities.get(parts.nextToken());
                                } else {
                                    path = path.get(parts.nextToken());
                                }
                            }

                            filter = criteriaBuilder.equal(path, filters.get(attributeName));

                        } else {
                            //Create the new condition for each attribute we get
                            filter = criteriaBuilder.equal(entities.get(attributeName), filters.get(attributeName));
                        }

                        predicates.add(filter);
                    }

                }

                // Add the conditions of the where
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            }

            TypedQuery<E> query = connection.createQuery(criteriaQuery);
            return query.getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }


    /**
     * List all entities that match with the filters and order by the
     * attribute name.
     *
     * @param filters
     * @param attributeNameOrder
     * @return List
     * @throws CantReadRecordDataBaseException
     */
    public List<E> list(Map<String, Object> filters, String attributeNameOrder) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(filters)
                .append(", ")
                .append(attributeNameOrder)
                .append(")")
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<E> entities = criteriaQuery.from(entityClass);
            criteriaQuery.select(entities);

            //Verify that the filters are not empty
            if (filters != null && filters.size() > 0) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                //Walk the key map that representing the attribute names
                for (String attributeName : filters.keySet()) {

                    //Verify that the value is not empty
                    if (filters.get(attributeName) != null && filters.get(attributeName) != "") {

                        Predicate filter;

                        // If it contains the "." because it is filtered by an attribute of an attribute
                        if (attributeName.contains(".")) {

                            StringTokenizer parts = new StringTokenizer(attributeName, ".");
                            Path<Object> path = null;

                            //Walk the path for all required parts
                            while (parts.hasMoreElements()) {

                                if (path == null) {
                                    path = entities.get(parts.nextToken());
                                } else {
                                    path = path.get(parts.nextToken());
                                }
                            }

                            filter = criteriaBuilder.equal(path, filters.get(attributeName));

                        } else {
                            //Create the new condition for each attribute we get
                            filter = criteriaBuilder.equal(entities.get(attributeName), filters.get(attributeName));
                        }

                        predicates.add(filter);
                    }

                }

                // Add the conditions of the where
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            }

            criteriaQuery.orderBy(criteriaBuilder.asc(entities.get(attributeNameOrder)));
            TypedQuery<E> query = connection.createQuery(criteriaQuery);
            return query.getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }


    /**
     * List all entities that match with the filters, order by attribute name
     * and apply pagination
     *
     * @param offset
     * @param max
     * @param filters
     * @param attributeNameOrder
     * @return List
     * @throws CantReadRecordDataBaseException
     */
    public List<E> list(Integer offset, Integer max, Map<String, Object> filters, String attributeNameOrder) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(offset)
                .append(", ")
                .append(max)
                .append(", ")
                .append(filters)
                .append(", ")
                .append(attributeNameOrder)
                .append(")")
                .toString());
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<E> criteriaQuery = criteriaBuilder.createQuery(entityClass);
            Root<E> entities = criteriaQuery.from(entityClass);

            //Verify that the filters are not empty
            if (filters != null && filters.size() > 0) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                //Walk the key map that representing the attribute names
                for (String attributeName : filters.keySet()) {

                    //Verify that the value is not empty
                    if (filters.get(attributeName) != null && filters.get(attributeName) != "") {

                        Predicate filter;

                        // If it contains the "." because it is filtered by an attribute of an attribute
                        if (attributeName.contains(".")) {

                            StringTokenizer parts = new StringTokenizer(attributeName, ".");
                            Path<Object> path = null;

                            //Walk the path for all required parts
                            while (parts.hasMoreElements()) {

                                if (path == null) {
                                    path = entities.get(parts.nextToken());
                                } else {
                                    path = path.get(parts.nextToken());
                                }
                            }

                            filter = criteriaBuilder.equal(path, filters.get(attributeName));

                        } else {
                            //Create the new condition for each attribute we get
                            filter = criteriaBuilder.equal(entities.get(attributeName), filters.get(attributeName));
                        }

                        predicates.add(filter);
                    }

                }

                // Add the conditions of the where
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            }

            criteriaQuery.orderBy(criteriaBuilder.asc(entities.get(attributeNameOrder)));
            TypedQuery<E> query = connection.createQuery(criteriaQuery);
            query.setFirstResult(offset);
            query.setMaxResults(max);

            return query.getResultList();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    /**
     * Count all entities
     *
     * @return int
     */
    public Long count() throws CantReadRecordDataBaseException {

        LOG.debug("Executing count()");
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<E> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(connection.getCriteriaBuilder().count(root));
            TypedQuery<Long> query = connection.createQuery(criteriaQuery);
            return query.getSingleResult();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    /**
     * Count all entities
     *
     * @param entityType
     * @return
     * @throws CantReadRecordDataBaseException
     */
    public Long count(EntityType entityType) throws CantReadRecordDataBaseException {

        LOG.debug("Executing count()");
        EntityManager connection = getConnection();

        try {

            TypedQuery<Long> query = connection.createQuery("SELECT COUNT(e) FROM "+entityType.getName()+" e", Long.class);
            return  query.getSingleResult();

        } catch (Exception e){
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }

    /**
     * Count all that match whit the filters
     *
     * @param filters
     * @return int
     * @throws CantReadRecordDataBaseException
     */
    public Long count(Map<String, Object> filters) throws CantReadRecordDataBaseException {

        LOG.debug(new StringBuilder("Executing list(")
                .append(filters)
                .append(")")
                .toString());

        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<E> entities = criteriaQuery.from(entityClass);
            criteriaQuery.select(connection.getCriteriaBuilder().count(entities));

            //Verify that the filters are not empty
            if (filters != null && filters.size() > 0) {

                List<Predicate> predicates = new ArrayList<Predicate>();

                //Walk the key map that representing the attribute names
                for (String attributeName : filters.keySet()) {

                    //Verify that the value is not empty
                    if (filters.get(attributeName) != null && filters.get(attributeName) != "") {

                        Predicate filter;

                        // If it contains the "." because it is filtered by an attribute of an attribute
                        if (attributeName.contains(".")) {

                            StringTokenizer parts = new StringTokenizer(attributeName, ".");
                            Path<Object> path = null;

                            //Walk the path for all required parts
                            while (parts.hasMoreElements()) {

                                if (path == null) {
                                    path = entities.get(parts.nextToken());
                                } else {
                                    path = path.get(parts.nextToken());
                                }
                            }

                            filter = criteriaBuilder.equal(path, filters.get(attributeName));

                        } else {
                            //Create the new condition for each attribute we get
                            filter = criteriaBuilder.equal(entities.get(attributeName), filters.get(attributeName));
                        }

                        predicates.add(filter);
                    }

                }

                // Add the conditions of the where
                criteriaQuery.where(predicates.toArray(new Predicate[predicates.size()]));

            }

            TypedQuery<Long> query = connection.createQuery(criteriaQuery);
            return query.getSingleResult();

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }

    }


    /**
     * Verify is exist in the data base
     *
     * @return int
     */
    public boolean exist(Object id) throws CantReadRecordDataBaseException {

        LOG.debug("Executing exist()");
        EntityManager connection = getConnection();

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<E> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(connection.getCriteriaBuilder().count(root));

            Predicate attribute;

            if (id != null) {
                attribute = criteriaBuilder.equal(root.get("id"), id);
            } else {
                throw new IllegalArgumentException("The id can't be null.");
            }

            criteriaQuery.where(attribute);
            TypedQuery<Long> query = connection.createQuery(criteriaQuery);

            if (query.getSingleResult() > 0) {
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }

        } catch (Exception e) {
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        } finally {
            connection.close();
        }
    }


    /**
     * Verify is exist in the data base, and use a exiting
     * connection
     *
     * @param connection
     * @param id
     * @return boolean
     * @throws CantReadRecordDataBaseException
     */
    protected boolean exist(EntityManager connection, Object id) throws CantReadRecordDataBaseException {

        LOG.debug("Executing exist()");

        try {

            CriteriaBuilder criteriaBuilder = connection.getCriteriaBuilder();
            CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
            Root<E> root = criteriaQuery.from(entityClass);
            criteriaQuery.select(connection.getCriteriaBuilder().count(root));

            Predicate attribute;

            if (id != null) {
                attribute = criteriaBuilder.equal(root.get("id"), id);
            } else {
                throw new IllegalArgumentException("The id can't be null.");
            }

            criteriaQuery.where(attribute);
            TypedQuery<Long> query = connection.createQuery(criteriaQuery);

            if (query.getSingleResult() > 0){
                return Boolean.TRUE;
            } else {
                return Boolean.FALSE;
            }

        } catch (Exception e){
            LOG.error(e);
            throw new CantReadRecordDataBaseException(CantReadRecordDataBaseException.DEFAULT_MESSAGE, e, "Network Node", "");
        }
    }
}
