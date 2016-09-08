/*
 * @#DatabaseManager.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.version_1.structure.database.jpa;

import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.*;
import org.iop.version_1.structure.util.ProviderResourcesFilesPath;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.IOException;
import java.security.ProtectionDomain;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.DatabaseManager</code> are the
 * database manager class
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 22/07/16
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class DatabaseManager {

    /**
     * Represent the LOG
     */
    private static final Logger LOG = Logger.getLogger("debugLogger");

    /**
     * Represent the value of DIR_NAME
     */
    public static final String DIR_NAME = "database";

    /**
     * Represent the value of DIR_NAME
     */
    public static final String DATA_BASE_NAME = "network_node.odb";

    /**
     * Represent the value of CONNECTION_URL
     */
    public static final String CONNECTION_URL = "objectdb://localhost/"+DATA_BASE_NAME+";user=admin;password=admin";

    /**
     * Represent the entityManagerFactory instance
     */
    private static EntityManagerFactory entityManagerFactory;

    /**
     * Represent the executorService instance
     */
    private static ExecutorService executorService = Executors.newSingleThreadExecutor();

    /**
     * Get a new instance to a Entity manager that
     * represent a connection with the data base.
     *
     * @return EntityManager
     */
    public static EntityManager getConnection() {

        if (entityManagerFactory != null){
            return entityManagerFactory.createEntityManager();
        }else {
            throw new RuntimeException("Cant get Connection, entityManagerFactory = "+ null);
        }

    }

    /**
     * Close de data base
     */
    public static void closeDataBase(){

        if (entityManagerFactory != null && entityManagerFactory.isOpen()){
            entityManagerFactory.close();
        }
    }

    /**
     * Get the EntityManagerFactory value
     *
     * @return EntityManagerFactory
     */
    public static EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    /**
     * Get the path of the objectdb.jar
     * @return path
     */
    public static String getObjectDbJarPath(){
        ProtectionDomain domain = com.objectdb.Utilities.class.getProtectionDomain();
        return domain.getCodeSource().getLocation().getFile();
    }

    /**
     * Get the path of the objectdb configuration
     * @return path
     */
    public static String getObjectDbConfigurationFilePath(){
        return DatabaseManager.class.getClassLoader().getResource("META-INF/object-db.conf").toString();
    }

    public static void start(){

        /*
         * Configure environment
         */
        String path = ProviderResourcesFilesPath.createNewFilesPath(DIR_NAME);
        //String pathDbConfFile = getObjectDbConfigurationFilePath();

        executorService.execute(() -> {


            LOG.info("Database path: "+path);
            //LOG.info("Database Configuration File: "+pathDbConfFile);

            try {

                String command = "java -Dobjectdb.temp.avoid-page-recycle=true -Dobjectdb.home="+path+" -cp "+ getObjectDbJarPath() +" com.objectdb.Server start";
                LOG.info("Initializing objectdb database in server mode, whit command:");
                LOG.info(command);

                Runtime.getRuntime().exec(command);

            } catch (IOException e) {
                e.printStackTrace();
            }

        });

        try {
            LOG.info("Waiting 10 seconds to the database server start");
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            LOG.warn(e);
        }

        LOG.info("Open a database connection (create a new database if it doesn't exist yet)");
        entityManagerFactory = Persistence.createEntityManagerFactory("node-pu");


        /*
         * Create tables at start up
         */
        EntityManager connection = entityManagerFactory.createEntityManager();
        connection.getMetamodel().entity(ActorCatalog.class);
        connection.getMetamodel().entity(Client.class);
        connection.getMetamodel().entity(GeoLocation.class);
        connection.getMetamodel().entity(NetworkService.class);
        connection.getMetamodel().entity(NodeCatalog.class);
        connection.getMetamodel().entity(EventListener.class);
        connection.close();

    }
}
