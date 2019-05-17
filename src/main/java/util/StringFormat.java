package util;

public class StringFormat {
    public static String padding(String data, String pad, Integer len) {
        Integer pad_len = (len - data.length()) / 2;
        String pad_str = "";
        for (int i=0; i<pad_len; i++) {
            pad_str = pad_str.concat(pad);
        }
        return String.format("%s %s %s", pad_str, data, pad_str);
    }
}
