package com.xiaomi.modeltool;

import io.restassured.response.Response;
import org.json.JSONObject;
import util.HttpUtil;

import java.util.Map;
import java.util.UUID;

public class OpenHomeRequest {
    public static Response request(String url, String method, Map<String, Object> headers, Map<String, Object> params, String body) throws Exception {
        String requestId = getUUID32();
        headers.put("requestId", requestId);
        Response response = HttpUtil.request(url, method, headers, null, params, body);
         return response;
    }
    private static String getUUID32() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}
