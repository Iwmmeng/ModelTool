package util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;

public class LoggerUtils {

    public static void log(String str) {
        System.out.println(dateFormat(System.currentTimeMillis()) + " " + str);
    }

    public static String dateFormat(long date) {
        SimpleDateFormat formatter = new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss SSS]");
        return formatter.format(date);
    }

    public static void logToFile(String fileName, String msg) throws IOException {
        //if file exists，append to it, if not，creat
        File file = new File(System.getProperty("user.dir") + "\\" + fileName + ".txt");
        PrintWriter pw = new PrintWriter(new FileWriter(file, true));
        String date = dateFormat(System.currentTimeMillis());
        pw.println(date + msg);
        pw.flush();
        pw.close();
    }

}
