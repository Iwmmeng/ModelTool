package util;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InputStreamUtils {
    public static String getStringInfo(String region, String key) throws IOException {
        Yaml yaml = new Yaml();
        java.io.InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("devicesInfo.yml");
        Map<String, Map<Integer, String>> ret = yaml.load(inputStream);
        inputStream.close();
        return ret.get(region).get(key);
    }

    public static Map<String,String> getListInfo(String region, String key) throws IOException {
        Yaml yaml = new Yaml();
        java.io.InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("devicesInfo.yml");
        Map<String, Map<Integer, Map<String,String>>> ret = yaml.load(inputStream);
        inputStream.close();
        return ret.get(region).get(key);
    }
    public static Map<String, Map<String, String>> getDeviceList(String region) throws IOException {
        Yaml yaml = new Yaml();
        java.io.InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("devicesInfo.yml");
        Map<String, Map<String, Map<String,String>>> ret = yaml.load(inputStream);
        inputStream.close();
        Map<String, Map<String, String>> regionInfo = ret.get(region);
        Map<String, Map<String, String>> deviceList = new HashMap<String, Map<String, String>>();
        for (String key:regionInfo.keySet()) {
            try {
                if (regionInfo.get(key).size() >= 0) {
                    deviceList.put(key, regionInfo.get(key));
                }
            } catch (Exception e) {
            }
        }
        return deviceList;
    }

    public static String getMobilePhone(String group) {
        Yaml yaml = new Yaml();
        java.io.InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("mobilePhone.yml");
        Map<String, Map<Integer, String>> ret = yaml.load(inputStream);
        try {
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        String phoneList = String.join(",",ret.get(group).values());
        return phoneList;
    }
    public static void main(String []args) throws Exception {
        Map<String, Map<String, String>> result;
        Map<String, Map<String, String>> deviceList = new HashMap <String, Map<String, String>>();
        result = InputStreamUtils.getDeviceList("DEBUG");
        System.out.println(result);
    }
}
