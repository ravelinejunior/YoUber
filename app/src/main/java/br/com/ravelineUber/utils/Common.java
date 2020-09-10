package br.com.ravelineUber.utils;

import br.com.ravelineUber.model.Driver;

public class Common {

    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static final String DRIVERS_LOCATION_REFERENCES = "DriversLocation";
    public static Driver currentUser;

    public static String buildWelcomeMessage() {

        if(currentUser != null )
            return new StringBuilder("Welcome ")
                    .append(Common.currentUser.getFirstName())
                    .append(" ")
                    .append(Common.currentUser.getLastName()).toString();
        else return  "";
    }
}
