package com.xioami.modeltool;

import com.xiaomi.modeltool.OpenHomeApi;
import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.TestName;
import util.LoggerUtils;
import util.StringFormat;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseTest {

    @Rule
    public TestName name = new TestName();
    static OpenHomeApi openHomeApi;
    static Response response;
    static JSONObject testResult;
    static String region = System.getProperty("region");
    static String receiverUrl = "http://10.38.164.50:8089/subscribe/" + region;
    static List<String> didList = new ArrayList<>();
//    static VirtualDevice virtualDevice = new VirtualDevice();
    static String deviceUrl = "";
    static String serviceName = "openhome";

@BeforeClass
    public static void beforeClass() throws Exception {
    if (region==null) {
        region = "DEBUG";
    }
    if(region.equals("DEBUG")) {
        receiverUrl = "http://10.38.164.50:8089/subscribe/TJ";
    }
    JSONObject beforeResult = new JSONObject();
    beforeResult.put("name", serviceName + ".beforeClass").put("result", true).put("description","ok").
            put("endpoint", InetAddress.getLocalHost().getHostName()).
            put("timestamp", System.currentTimeMillis()).put("code", 0).put("requestId", "").put("httpCode", 0);
    LoggerUtils.log(StringFormat.padding("BeforeClass Start", "=", 120));
    Map<String, Map<String, String>> deviceList;
    JSONObject virtualDeviceListStatus;
    openHomeApi = new OpenHomeApi();
    JSONObject loginResult = openHomeApi.openHomeLogin(region);
}












}
