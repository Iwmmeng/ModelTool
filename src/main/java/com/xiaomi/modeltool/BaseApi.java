package com.xiaomi.modeltool;

import org.json.JSONObject;
import util.InputStreamUtils;
import util.LoggerUtils;
import util.MiPassport;
import util.Sleep;

import java.util.HashMap;
import java.util.Map;

public class BaseApi {
    public static String region;
    public static String apiHost;
    public static Map<String, Object> headers = new HashMap<>();

    public JSONObject openHomeLogin(String region) throws Exception {
        apiHost = InputStreamUtils.getStringInfo(region, "apiHost");
        JSONObject result = new JSONObject();
        int retry = 0;
        while (retry < 3) {
            String userId = InputStreamUtils.getStringInfo(region, "userId");
            String pwd = InputStreamUtils.getStringInfo(region, "pwd");
            String redirectUri = ApiConfig.backUrl + "/cb";
            result = MiPassport.openHomeLogin(userId, pwd, ApiConfig.appId, redirectUri);
            if(result.getBoolean("result")) {
                headers.put("App-Id", ApiConfig.appId);
                headers.put("Access-Token", result.getString("accessToken"));
                headers.put("Spec-NS", ApiConfig.specNs);
                headers.put("Content-Type", "application/json");
                LoggerUtils.log("Login successful");
                return result;
            }
            retry += 1;
            Sleep.sleep(2000);
        }
        return result;
    }
}
