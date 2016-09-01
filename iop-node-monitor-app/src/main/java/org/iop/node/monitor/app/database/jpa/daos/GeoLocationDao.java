package org.iop.node.monitor.app.database.jpa.daos;

import org.apache.commons.lang.ClassUtils;
import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.GeoLocation;

/**
 * Created by Manuel Perez (darkpriestrelative@gmail.com) on 03/08/16.
 */
public class GeoLocationDao extends AbstractBaseDao<GeoLocation> {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger(ClassUtils.getShortClassName(ClientDao.class));

    /**
     * Constructor
     */
    public GeoLocationDao() {
        super(GeoLocation.class);
    }
}
