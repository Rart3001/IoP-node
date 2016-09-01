package org.iop.node.monitor.app;

import org.apache.commons.lang3.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.node.monitor.app.server.JettyEmbeddedAppServer;


/**
 * Created by rrequena on 31/08/16.
 */
public class AppMain {

    /**
     * Represent the LOG
     */
    private static final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(AppMain.class));
    /**
     * org.iop.version_1.AppMain method
     *
     * @param args
     */
    public static void main(String[] args) {

        try {

            LOG.info("***********************************************************************");
            LOG.info("* FERMAT - Network Node Monitor Web App - Version 1.0 (2016)          *");
            LOG.info("* www.fermat.org                                                      *");
            LOG.info("***********************************************************************");
            LOG.info("");
            LOG.info("- Starting process ...");

            JettyEmbeddedAppServer jettyEmbeddedAppServer = JettyEmbeddedAppServer.getInstance();
            jettyEmbeddedAppServer.start();

            LOG.info("FERMAT - Network Node - started satisfactory...");

        } catch (Exception e) {

            LOG.error("***********************************************************************");
            LOG.error("* FERMAT - ERROR                                                      *");
            LOG.error("***********************************************************************");
            e.printStackTrace();
            System.exit(1);
        }

    }
}
