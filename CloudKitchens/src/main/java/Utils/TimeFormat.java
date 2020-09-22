package Utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeFormat {
    public static String systemToSimpleDateFormat(long timeInMillis) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyy-MM-dd 'at' HH:mm:ss z");
        Date date = new Date(timeInMillis);
        return formatter.format(date);
    }
}
