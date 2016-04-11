package br.com.fiap.fiapplayer.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateHelper {

    public static String getTime(long timestamp){

        Date date = new java.util.Date(timestamp);
        return new SimpleDateFormat("mm:ss").format(date);

    }

}
