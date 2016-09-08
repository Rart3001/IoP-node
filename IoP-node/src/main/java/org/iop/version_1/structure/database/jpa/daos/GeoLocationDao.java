package org.iop.version_1.structure.database.jpa.daos;

import org.apache.log4j.Logger;
import org.iop.version_1.structure.database.jpa.entities.GeoLocation;

/**
 * Created by Manuel Perez (darkpriestrelative@gmail.com) on 03/08/16.
 */
public class GeoLocationDao extends AbstractBaseDao<GeoLocation> {

    /**
     * Represent the LOG
     */
    private final Logger LOG = Logger.getLogger("debugLogger");

    /**
     * Constructor
     */
    public GeoLocationDao() {
        super(GeoLocation.class);
    }
}
