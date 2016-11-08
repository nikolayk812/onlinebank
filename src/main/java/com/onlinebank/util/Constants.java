package com.onlinebank.util;


/**
 * Application wide constants
 */
public final class Constants {
    public static final String ACCOUNT_NAME_REGEX = "^[A-Za-z0-9-]*$";

    //Spring deployment profiles
    public static final String H2 = "h2";
    public static final String POSTGRES = "postgres";
    public static final String HEROKU = "heroku";

    private Constants() {
    }
}
