/*
 * @#AbstractBaseEntity.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.node.monitor.app.database.jpa.entities;

import java.io.Serializable;

/**
 * The class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.AbstractBaseEntity</code> is
 * the base for all entities
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 22/07/16
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public abstract class AbstractBaseEntity<TYPE> implements Serializable{

    /**
     * Return the id of the entity
     * @return id
     */
    public abstract TYPE getId();

    /**
     * (non-javadoc)
     * @see Object#hashCode()
     */
    public abstract int hashCode();

    /**
     * (non-javadoc)
     * @see Object#toString()
     */
    public abstract String toString();

    /**
     * (non-javadoc)
     * @see Object#equals(Object)
     */
    public abstract boolean equals(Object o);
}
