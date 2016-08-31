package org.iop.version_1.structure.channels.endpoinsts.clients.helpers;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.DatabaseManager;
import org.iop.version_1.structure.database.jpa.daos.JPADaoFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.websocket.Session;

/**
 * Created by Matias Furszyfer on 30/08/16.
 */
public class DatabaseHelper {

    /**
     * Represent the LOG
     */
    private static final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(DatabaseHelper.class));

    public static void checkoutSession(Session session){
        EntityManager connection = DatabaseManager.getConnection();
        EntityTransaction transaction = connection.getTransaction();
        try {
            transaction.begin();
            /**
             * remove client from his table
             */
            JPADaoFactory.getClientDao().chaincheckOut(connection,session.getId());
            /**
             * update network service session id to null
             */
            JPADaoFactory.getNetworkServiceDao().chaincheckOut(connection,session.getId());
            /**
             * update actor catalog session id to null
             */
            JPADaoFactory.getActorCatalogDao().chaincheckOut(connection,session.getId());
            /**
             * Commit transactions
             */
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


}
