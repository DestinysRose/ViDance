package com.sample.vidance.app;

/**
 * Created by Danil on 27.03.2017.
 * Collaboration by Michelle on 09.03-2017
 */

public class AppConfig {
    // URL to verify login
    public static String URL_LOGIN = "http://thevidance.com/test/login.php";

    // URL to register user
    public static String URL_REGUSER = "http://thevidance.com/test/reg_user.php";

    // URL to register child
    public static String URL_REGCHILD = "http://thevidance.com/test/reg_child.php";

    // URL to get list of children registered under user
    public static String URL_GETCHILD = "http://thevidance.com/test/get_child.php";

    // URL to get list of children registered under user
    public static String URL_GETSTORY = "http://thevidance.com/test/get_stories.php";

    // URL to get list of children registered under user
    public static String URL_TEST = "http://thevidance.com/test/updateBehaviours.php";

    // URL to get list of recorded updates by child to show current records and target
    public static String TARGET_WEEK = "http://thevidance.com/targetBehaviour.php";

    // URL to get list of recorded updates dates
    public static String RECORD_DATES = "http://thevidance.com/recordDates.php";

    // URL to post values to PHP
    public static String KEY_CID = "childId";
    public static String KEY_DATE = "date1";
    public static String KEY_END_DATE = "date2";
    public static String KEY_AMOUNT = "amount";

    //JSON array name
    public static String JSON_ARRAY = "result";

}
