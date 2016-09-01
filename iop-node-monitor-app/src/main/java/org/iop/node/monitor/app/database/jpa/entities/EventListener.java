/*
 * @#Client.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.node.monitor.app.database.jpa.entities;


import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@Entity
@Cacheable(false)
public class EventListener extends AbstractBaseEntity<String>{

    /**
     * Represent the serialVersionUID
     */
    private static final long serialVersionUID = 1234L;


    @Id
    @NotNull
    //client package uuid
    private String id;

    /**
     * Represent the event code
     */
    private short event;

    /**
     * Condition
     */
    private String condition;

    /**
     * Represent the session
     */
    private String sessionId;

    /**
     * Constructor with parameter
     * @param sessionId
     */
    public EventListener(String id,String sessionId,short eventCode,String condition) {
        super();
        this.id = id;
        this.sessionId = sessionId;
        this.event = eventCode;
        this.condition = condition;
    }


    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public short getEvent() {
        return event;
    }

    public void setEvent(short event) {
        this.event = event;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    /**
     * Get the Session value
     *
     * @return Session
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Set the value of session
     *
     * @param sessionId
     */
    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * (non-javadoc)
     * @see AbstractBaseEntity@equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EventListener)) return false;

        EventListener that = (EventListener) o;

        return getId().equals(that.getId());

    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * (non-javadoc)
     * @see AbstractBaseEntity@hashCode()
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "EventListener{" +
                "id=" + id +
                ", event=" + event +
                ", condition=" + condition +
                ", sessionId=" + sessionId +
                '}';
    }
}
