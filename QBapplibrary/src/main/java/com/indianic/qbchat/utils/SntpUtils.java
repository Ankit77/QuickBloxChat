package com.indianic.qbchat.utils;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by igorkhomenko on 2/11/16.
 */
public class SntpUtils {
    public static Date getUTCDate(){
        long nowAsPerDeviceTimeZone = getUTCTimestamp();
        return new Date(nowAsPerDeviceTimeZone);
    }

    public static long getUTCTimestamp(){
        long nowAsPerDeviceTimeZone = 0;
        SntpClient sntpClient = new SntpClient();

        boolean success = sntpClient.requestTime("time.apple.com", 30000);

        if (success) {
            nowAsPerDeviceTimeZone = sntpClient.getNtpTime();
            Calendar cal = Calendar.getInstance();
            TimeZone timeZoneInDevice = cal.getTimeZone();
            int differentialOfTimeZones = timeZoneInDevice.getOffset(System.currentTimeMillis());
            nowAsPerDeviceTimeZone -= differentialOfTimeZones;
        }

        return nowAsPerDeviceTimeZone;
    }
}
