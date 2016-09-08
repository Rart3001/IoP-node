package org.iop.version_1.structure.database.jpa.daos;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.network_services.database.exceptions.CantUpdateRecordDataBaseException;
import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.AbstractBaseEntity;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Query;

/**
 * Created by mati on 12/08/16.
 */
public abstract class AbstractComponentsDao<E extends AbstractBaseEntity> extends AbstractBaseDao<E> {

    private final Logger LOG = Logger.getLogger("debugLogger");
    /**
     * Constructor
     *
     * @param entityClass
     */
    public AbstractComponentsDao(Class<E> entityClass) {
        super(entityClass);
    }

    public final void checkIn(final String publicKey,final String sessionId) throws CantUpdateRecordDataBaseException {

        LOG.debug("Executing checkIn id (" + sessionId + ")");

        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query query = connection.createQuery("UPDATE "+ClassUtils.getShortClassName(entityClass)+" a SET a.sessionId = :sessionId WHERE a.id = :id");
            query.setParameter("sessionId", sessionId);
            query.setParameter("id", publicKey);

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

    public final void checkOut(final String publicKey) throws CantUpdateRecordDataBaseException {

        LOG.debug("Executing checkOut publicKey (" + publicKey + ")");

        EntityManager connection = getConnection();
        EntityTransaction transaction = connection.getTransaction();

        try {

            transaction.begin();

            Query query = connection.createQuery("UPDATE "+ClassUtils.getShortClassName(entityClass)+" SET a.sessionId = null WHERE a.id = :id");
            query.setParameter("id", publicKey);

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

}
