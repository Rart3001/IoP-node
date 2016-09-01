package org.iop.node.monitor.app.util.geolocation;

/**
 * Created by Manuel Perez (darkpriestrelative@gmail.com) on 25/07/16.
 */
public class Coordinate {

    /**
     * Represents the coordinates
     */
    private final double latitude;
    private final double longitude;

    /**
     * Default constructor with parameters
     * @param latitude
     * @param longitude
     */
    public Coordinate(
            double latitude,
            double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * This method returns the latitude
     * @return
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * This method returns the longitude
     * @return
     */
    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Coordinate{");
        sb.append("latitude=").append(latitude);
        sb.append(", longitude=").append(longitude);
        sb.append('}');
        return sb.toString();
    }

}
