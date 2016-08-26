/*
 * @#ActorCatalog.java - 2016
 * Copyright Fermat.org, All rights reserved.
 */
package org.iop.version_1.structure.database.jpa.entities;

import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.enums.ProfileStatus;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.profiles.ActorProfile;
import com.google.gson.annotations.Expose;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 * The class <code>com.bitdubai.fermat_p2p_plugin.layer.communications.network.node.developer.bitdubai.version_1.structure.database.jpa.entities.ActorCatalog</code>
 * represent the data of the actor into the catalog
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 23/07/16
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
@Entity
@NamedQueries({
    @NamedQuery(name="ActorCatalog.getActorCatalogById",        query = "SELECT a FROM ActorCatalog a WHERE a.id = :id"),
    @NamedQuery(name="ActorCatalog.getActorCatalogByActorType", query = "SELECT a FROM ActorCatalog a WHERE a.actorType = :type"),
    @NamedQuery(name="ActorCatalog.getActorCatalog",            query = "SELECT a FROM ActorCatalog a")
})
public class ActorCatalog extends AbstractBaseEntity<String>{

    /**
     * Represent the serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Represent the Identity public key
     */
    @Id
    @NotNull
    @Expose
    private String id;

    /**
     * Represent the location
     */
    @MapsId
    @OneToOne(cascade = {CascadeType.ALL}, targetEntity = GeoLocation.class, orphanRemoval = true)
    @Expose
    private GeoLocation location;

    /**
     * Represent the actorType
     */
    @NotNull
    @Expose
    private String actorType;

    /**
     * Represent the alias
     */
    @Expose
    private String alias;

    /**
     * Represent the extraData
     */
    @Expose
    private String extraData;

    /**
     * Represent the name
     */
    @NotNull
    @Expose
    private String name;

    /**
     * Represent the photo
     */
    @Lob
    @Basic(fetch= FetchType.LAZY)
    @Expose
    private byte[] photo;

    /**
     * Represent the hostedTimestamp
     */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Expose
    private Timestamp hostedTimestamp;

    /**
     * Represent the lastUpdateTime
     */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Expose
    private Timestamp lastUpdateTime;

    /**
     * Represent the lastConnection
     */
    @NotNull
    @Temporal(TemporalType.TIMESTAMP)
    @Expose
    private Timestamp lastConnection;

    /**
     * Represent the thumbnail
     */
    @Lob
    @Basic(fetch= FetchType.EAGER)
    @Expose
    private byte[] thumbnail;

    /**
     * Represent the homeNode
     */
    @ManyToOne @MapsId
    @Expose
    private NodeCatalog homeNode;

    /**
     * Represent the session
     */
//    @OneToOne (targetEntity = ActorSession.class, mappedBy="actor")
//    @Expose(serialize = false, deserialize = false)
//    private ActorSession session;
    @Expose(serialize = false, deserialize = false)
    private String sessionId;
    /**
     * Represent the signature
     */
    @Expose
    private String signature;

    /**
     * Represents the version
     */
    @Expose
    private Integer version;

    /**
     * Represents the version
     */
//    @Expose
//    private ActorCatalogUpdateTypes lastUpdateType;

    /**
     * Represents the pendingPropagations
     */
    @Expose(serialize = false, deserialize = false)
    private Integer pendingPropagations;

    /**
     * Represents the triedToPropagateTimes
     */
    @Expose(serialize = false, deserialize = false)
    private Integer triedToPropagateTimes;


    /**
     * Constructor with parameters
     * @param actorProfile
     * @param thumbnail
     * @param homeNodePublicKey
     * @param signature
     */
    public ActorCatalog(final ActorProfile actorProfile, final byte[] thumbnail, final String homeNodePublicKey, final String signature) {
        super();
        this.id = actorProfile.getIdentityPublicKey();
        this.name = actorProfile.getName();
        this.alias = actorProfile.getAlias();
        this.extraData = actorProfile.getExtraData();
        this.photo = actorProfile.getPhoto();
        this.actorType = actorProfile.getActorType();
        this.hostedTimestamp = new Timestamp(System.currentTimeMillis());
        this.lastUpdateTime = new Timestamp(System.currentTimeMillis());
        this.lastConnection = new Timestamp(System.currentTimeMillis());
        this.thumbnail = thumbnail;
        this.homeNode = new NodeCatalog(homeNodePublicKey);
        this.sessionId = null;
        this.signature = signature;

        if (actorProfile.getLocation() != null){
            this.location = new GeoLocation(this.id, actorProfile.getLocation().getLatitude(), actorProfile.getLocation().getLongitude());
        }else {
            this.location = null;
        }
    }

    /**
     * Constructor with parameters, this constructor
     * feel the attribute used for complete a ActorProfile
     * only no extra data load
     *
     * @param id
     * @param location
     * @param actorType
     * @param alias
     * @param extraData
     * @param name
     * @param thumbnail
     * @param sessionId
     * @param homeNodeId
     */
    public ActorCatalog(String id, GeoLocation location, String actorType, String alias, String extraData, String name, byte[] thumbnail, String sessionId, String homeNodeId) {
        this.id = id;
        this.location = location;
        this.actorType = actorType;
        this.alias = alias;
        this.extraData = extraData;
        this.name = name;
        this.thumbnail = thumbnail;
        this.sessionId = sessionId;
        this.homeNode = new NodeCatalog(homeNodeId);
    }

    /**
     * Get the value of id
     *
     * @return id
     */
    @Override
    public String getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Get the value of location
     *
     * @return location
     */
    public GeoLocation getLocation() {
        return location;
    }

    /**
     * Set the Location
     * @param latitude
     * @param longitude
     */
    public void setLocation(Double latitude, Double longitude) {
        this.location = new GeoLocation(this.id, latitude, longitude);
    }

    /**
     * Get the value of actorType
     *
     * @return actorType
     */
    public String getActorType() {
        return actorType;
    }

    /**
     * Set the value of actorType
     *
     * @param actorType
     */
    public void setActorType(String actorType) {
        this.actorType = actorType;
    }

    /**
     * Get the value of alias
     *
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Set the value of alias
     *
     * @param alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Get the value of extraData
     *
     * @return extraData
     */
    public String getExtraData() {
        return extraData;
    }

    /**
     * Set the value of extraData
     *
     * @param extraData
     */
    public void setExtraData(String extraData) {
        this.extraData = extraData;
    }

    /**
     * Get the value of name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the value of name
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the value of photo
     *
     * @return photo
     */
    public byte[] getPhoto() {
        return photo;
    }

    /**
     * Set the value of photo
     *
     * @param photo
     */
    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    /**
     * Get the value of hostedTimestamp
     *
     * @return hostedTimestamp
     */
    public Timestamp getHostedTimestamp() {
        return hostedTimestamp;
    }

    /**
     * Set the value of hostedTimestamp
     *
     * @param hostedTimestamp
     */
    public void setHostedTimestamp(Timestamp hostedTimestamp) {
        this.hostedTimestamp = hostedTimestamp;
    }

    /**
     * Get the value of lastUpdateTime
     *
     * @return lastUpdateTime
     */
    public Timestamp getLastUpdateTime() {
        return lastUpdateTime;
    }

    /**
     * Set the value of lastUpdateTime
     *
     * @param lastUpdateTime
     */
    public void setLastUpdateTime(Timestamp lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    /**
     * Get the value of lastConnection
     *
     * @return lastConnection
     */
    public Timestamp getLastConnection() {
        return lastConnection;
    }

    /**
     * Set the value of lastConnection
     *
     * @param lastConnection
     */
    public void setLastConnection(Timestamp lastConnection) {
        this.lastConnection = lastConnection;
    }

    /**
     * Get the value of thumbnail
     *
     * @return thumbnail
     */
    public byte[] getThumbnail() {
        return thumbnail;
    }

    /**
     * Set the value of thumbnail
     *
     * @param thumbnail
     */
    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    /**
     * Get the value of homeNode
     *
     * @return homeNode
     */
    public NodeCatalog getHomeNode() {
        return homeNode;
    }

    /**
     * Set the value of homeNode
     *
     * @param homeNode
     */
    public void setHomeNode(NodeCatalog homeNode) {
        this.homeNode = homeNode;
    }

    /**
     * Get the value of session
     *
     * @return session
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Set the value of session
     *
     * @param sessionId
     */
    public void setSession(String sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * Get the value of signature
     *
     * @return signature
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Set the value of signature
     *
     * @param signature
     */
    public void setSignature(String signature) {
        this.signature = signature;
    }

    /**
     * Get the Version value
     *
     * @return Version
     */
    public Integer getVersion() {
        return version;
    }

    /**
     * Set the value of version
     *
     * @param version
     */
    public void setVersion(Integer version) {
        this.version = version;
    }

//    /**
//     * Get the LastUpdateType value
//     *
//     * @return LastUpdateType
//     */
//    public ActorCatalogUpdateTypes getLastUpdateType() {
//        return lastUpdateType;
//    }

//    /**
//     * Set the value of lastUpdateType
//     *
//     * @param lastUpdateType
//     */
//    public void setLastUpdateType(ActorCatalogUpdateTypes lastUpdateType) {
//        this.lastUpdateType = lastUpdateType;
//    }

    /**
     * Get the PendingPropagations value
     *
     * @return PendingPropagations
     */
    public Integer getPendingPropagations() {
        return pendingPropagations;
    }

    /**
     * Set the value of pendingPropagations
     *
     * @param pendingPropagations
     */
    public void setPendingPropagations(Integer pendingPropagations) {
        this.pendingPropagations = pendingPropagations;
    }

    /**
     * Get the TriedToPropagateTimes value
     *
     * @return TriedToPropagateTimes
     */
    public Integer getTriedToPropagateTimes() {
        return triedToPropagateTimes;
    }

    /**
     * Set the value of triedToPropagateTimes
     *
     * @param triedToPropagateTimes
     */
    public void setTriedToPropagateTimes(Integer triedToPropagateTimes) {
        this.triedToPropagateTimes = triedToPropagateTimes;
    }

    /**
     * (non-javadoc)
     * @see AbstractBaseEntity@equals(Object)
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActorCatalog)) return false;

        ActorCatalog that = (ActorCatalog) o;

        return getId().equals(that.getId());

    }

    /**
     * (non-javadoc)
     * @see AbstractBaseEntity@hashCode()
     */
    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    /**
     * (non-javadoc)
     *
     * @see AbstractBaseEntity@toString()
     */
    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ActorCatalog{");
        sb.append("id='").append(id).append('\'');
        sb.append(", location=").append((location != null ? location.getId() : null));
        sb.append(", actorType='").append(actorType).append('\'');
        sb.append(", alias='").append(alias).append('\'');
        sb.append(", extraData='").append(extraData).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", photo=").append(Arrays.toString(photo));
        sb.append(", hostedTimestamp=").append(hostedTimestamp);
        sb.append(", lastUpdateTime=").append(lastUpdateTime);
        sb.append(", lastConnection=").append(lastConnection);
        sb.append(", thumbnail=").append(Arrays.toString(thumbnail));
        sb.append(", homeNode=").append((homeNode != null ? homeNode.getId() : null));
        sb.append(", session=").append((sessionId != null ? sessionId : null));
        sb.append(", signature='").append(signature).append('\'');
        sb.append('}');
        return sb.toString();
    }

    /**
     * Get the ActorProfile representation
     * @return actorProfile
     */
    public ActorProfile getActorProfile(){

        ActorProfile actorProfile = new ActorProfile();
        actorProfile.setIdentityPublicKey(this.getId());
        actorProfile.setAlias(this.getAlias());
        actorProfile.setName(this.getName());
        actorProfile.setActorType(this.getActorType());
        actorProfile.setPhoto(this.getPhoto());
        actorProfile.setPhoto(this.getThumbnail());
        actorProfile.setExtraData(this.getExtraData());
        actorProfile.setLocation(this.getLocation());
        actorProfile.setHomeNodeIdentifier(this.getHomeNode().getId());

        if (sessionId != null){
            actorProfile.setStatus(ProfileStatus.ONLINE);
        }else {
            actorProfile.setStatus(ProfileStatus.UNKNOWN);
        }

        return actorProfile;
    }


}