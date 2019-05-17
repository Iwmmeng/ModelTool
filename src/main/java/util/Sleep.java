package util;

import java.util.concurrent.TimeUnit;

public class Sleep {
    public static void sleep(Integer timeout) throws Exception{
        LoggerUtils.log(String.format("sleep %d ms...", timeout));
        TimeUnit.MILLISECONDS.sleep(timeout);
    }
}
