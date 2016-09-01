package org.iop.node.monitor.app.util.geolocation;

import com.bitdubai.fermat_api.layer.osa_android.location_system.Location;

/**
 * Created by Manuel Perez (darkpriestrelative@gmail.com) on 25/07/16.
 */
public class CoordinateCalculator {

    /**
     * Represents Earth Radius in kilometers
     * Note: this value is provided by NASA on 25/07/2016, mor information here:
     * http://nssdc.gsfc.nasa.gov/planetary/factsheet/earthfact.html
     */
    private static final double R = 6378.137;

    /**
     * This method returns a BasicGeoRectangle calculated by the values given by the arguments.
     * The lower coordinate represents the lower left from the rectangle and the upper coordinate
     * represents the upper right from the rectangle.
     * @param geoLocation
     * @param distance distance in kilometers
     * @return
     */
    public static BasicGeoRectangle calculateCoordinate(
            Location geoLocation,
            double distance){
        if(geoLocation==null){
            //If geolocation is null we assume coordinate [0.0,0.0]
            return calculateCoordinate(
                    0.0,
                    0.0,
                    distance);
        }
        return calculateCoordinate(
                geoLocation.getLatitude(),
                geoLocation.getLongitude(),
                distance);
    }

    /**
     * This method returns a BasicGeoRectangle calculated by the values given by the arguments.
     * The lower coordinate represents the lower left from the rectangle and the upper coordinate
     * represents the upper right from the rectangle.
     * @param latitude in degrees
     * @param longitude in degrees
     * @param distance in kilometers
     * @return
     */
    public static BasicGeoRectangle calculateCoordinate(
            double latitude,
            double longitude,
            double distance){
        //Calculate the real radius
        distance = Math.sqrt(2)*distance;
        //Calculate lower coordinate at 225°
        Coordinate lowerCoordinate = calculatePoint(latitude, longitude, distance, 225);
        //Calculate upper coordinate at 45°
        Coordinate upperCoordinate = calculatePoint(latitude, longitude, distance, 45);
        return new BasicGeoRectangle(lowerCoordinate, upperCoordinate);
    }

    /**
     * This method returns the coordinate destination from the coordinate given as argument to the
     * distance in the selected angle (bearing).
     * @param latitude in degrees
     * @param longitude in degrees
     * @param distance in kilometers
     * @param bearing in degrees
     * @return
     */
    private static Coordinate calculatePoint(
            double latitude,
            double longitude,
            double distance,
            double bearing){
        double rad = Math.toRadians(bearing);
        double lat1 = Math.toRadians(latitude);
        double lon1 = Math.toRadians(longitude);
        //Calculate destination points
        double lat2 = Math.asin( Math.sin(lat1)*Math.cos(distance/R) +
                Math.cos(lat1)*Math.sin(distance/R)*Math.cos(rad) );
        double lon2 = lon1 + Math.atan2(Math.sin(rad)*Math.sin(distance/R)*Math.cos(lat1),
                Math.cos(distance/R)-Math.sin(lat1)*Math.sin(lat2));
        //Create coordinate
        return new Coordinate(
                Math.toDegrees(lat2),
                Math.toDegrees(lon2));
    }

}
