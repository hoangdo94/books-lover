package hcmut.cse.bookslover.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Hoang Do on 5/14/2016.
 */
public class DateHelper {
    public static String getTimeAgo(String timeString) {
        TimeZone.setDefault(TimeZone.getTimeZone("GMT"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date date = sdf.parse(timeString);
            if(date == null) {
                return null;
            }
            long time = date.getTime();

            Date curDate = currentDate();
            long now = curDate.getTime();
            if (time > now || time <= 0) {
                return "vài giây trước";
            }

            int dim = getTimeDistanceInMinutes(time);

            if (dim == 0) {
                return "vài giây trước";
            } else if (dim == 1) {
                return "1 phút trước";
            } else if (dim >= 2 && dim <= 44) {
                return dim + " phút trước";
            } else if (dim >= 45 && dim <= 89) {
                return "1 tiếng trước";
            } else if (dim >= 90 && dim <= 1439) {
                return (Math.round(dim / 60)) + " tiếng trước";
            } else if (dim >= 1440 && dim <= 2519) {
                return "1 ngày trước";
            } else if (dim >= 2520 && dim <= 43199) {
                return (Math.round(dim / 1440)) + " ngày trước";
            } else if (dim >= 43200 && dim <= 86399) {
                return "1 tháng trước";
            } else if (dim >= 86400 && dim <= 525599) {
                return (Math.round(dim / 43200)) + " tháng trước";
            } else if (dim >= 525600 && dim <= 655199) {
                return "1 năm trước";
            } else if (dim >= 655200 && dim <= 914399) {
                return "Hơn 1 năm trước";
            } else if (dim >= 914400 && dim <= 1051199) {
                return "2 năm trước";
            } else if (dim > 1051200) {
                return (Math.round(dim / 525600)) + " năm trước";
            }
            return "vài giây trước";
        } catch (Exception e) {
            return "vài giây trước";
        }
    }

    static Date currentDate() {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    static int getTimeDistanceInMinutes(long time) {
        long timeDistance = currentDate().getTime() - time;
        return Math.round((Math.abs(timeDistance) / 1000) / 60);
    }

}
