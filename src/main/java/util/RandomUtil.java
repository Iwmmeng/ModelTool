package util;


public class RandomUtil {
    public static Integer genRandomInt(int max, int min) {
        return  min + (int)(Math.random() * (max-min+1));
    }
}
