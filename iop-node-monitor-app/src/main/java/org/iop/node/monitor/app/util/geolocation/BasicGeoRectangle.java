package org.iop.node.monitor.app.util.geolocation;

/**
 * Created by Manuel Perez (darkpriestrelative@gmail.com) on 25/07/16.
 */
public class BasicGeoRectangle {

    /**
     * Represents the coordinate from 225° from BasicGeoRectangle.
     */
    private final double lowerLatitude;
    private final double lowerLongitude;
    /**
     * Represents the coordinate from 45° from BasicGeoRectangle.
     */
    private final double upperLatitude;
    private final double upperLongitude;

    /**
     * This default method initializes with default values.
     */
    public BasicGeoRectangle(){
        this.lowerLatitude = -10.0;
        this.lowerLongitude = -10.0;
        this.upperLatitude = 10.0;
        this.upperLongitude = 10.0;
    }

    /**
     * Default constructor with parameters
     * @param lowerLatitude
     * @param lowerLongitude
     * @param upperLatitude
     * @param upperLongitude
     */
    public BasicGeoRectangle(
            double lowerLatitude,
            double lowerLongitude,
            double upperLatitude,
            double upperLongitude) {
        this.lowerLatitude = lowerLatitude;
        this.lowerLongitude = lowerLongitude;
        this.upperLatitude = upperLatitude;
        this.upperLongitude = upperLongitude;
    }

    /**
     * Constructor with Coordinate objects as arguments.
     * @param lowerCoordinate
     * @param upperCoordinate
     */
    public BasicGeoRectangle(
            Coordinate lowerCoordinate,
            Coordinate upperCoordinate){
        this.lowerLatitude = lowerCoordinate.getLatitude();
        this.lowerLongitude = lowerCoordinate.getLongitude();
        this.upperLatitude = upperCoordinate.getLatitude();
        this.upperLongitude = upperCoordinate.getLongitude();
    }

    /**
     * This method returns the lower latitude (225°)
     * @return
     */
    public double getLowerLatitude() {
        return lowerLatitude;
    }

    /**
     * This method returns the lower longitude (225°)
     * @return
     */
    public double getLowerLongitude() {
        return lowerLongitude;
    }

    /**
     * This method returns the upper latitude (45°)
     * @return
     */
    public double getUpperLatitude() {
        return upperLatitude;
    }

    /**
     * This method returns the upper latitude (45°)
     * @return
     */
    public double getUpperLongitude() {
        return upperLongitude;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("BasicGeoRectangle{");
        sb.append("lowerLatitude=").append(lowerLatitude);
        sb.append(", lowerLongitude=").append(lowerLongitude);
        sb.append(", upperLatitude=").append(upperLatitude);
        sb.append(", upperLongitude=").append(upperLongitude);
        sb.append('}');
        return sb.toString();
    }
}
