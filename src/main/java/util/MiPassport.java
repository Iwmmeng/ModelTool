package util;

import com.xiaomi.modeltool.ApiConfig;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.json.JSONObject;

import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MiPassport {
    private static JSONObject loginResult = new JSONObject();
    private static String baseUrl = "https://account.xiaomi.com";
    private static String currentUrl = "";
    private static Map<String, String> cookies = new HashMap<>();


    /**
     * 登录小米账号
     * @param username 用户名，可以为小米账号id和手机号
     * @param password 密码
     * @param sid 服务id
     * @return
     * @throws Exception
     */
    public static JSONObject login(String username, String password, String sid) throws Exception{
        loginResult.put("result", false);
        Response response;
        Map<String, Object> params = new HashMap<>();
        String callbackUrl = baseUrl + "/sts/embed?sid=" + sid;
        params.put("callback", callbackUrl);
        params.put("_json", true);
        params.put("sid", sid);
        try {
            currentUrl = baseUrl + "/pass/serviceLogin";
            response = HttpUtil.request(currentUrl, "GET", null, null, params, null);
            String response_body = response.body().asString();
            if(response.statusCode()!=200 || !response_body.contains("&&&START&&&")) {
                setLoginResult(currentUrl, response.statusCode(), response_body);
                return loginResult;
            }
            params.clear();
            JSONObject respnose_body_json = new JSONObject(response_body.split("&&&START&&&")[1]);
            params.put("callback", respnose_body_json.getString("callback"));
            params.put("qs", respnose_body_json.getString("qs"));
            params.put("_sign", respnose_body_json.getString("_sign"));
            params.put("sid", sid);
            params.put("pwd", password);
            params.put("user", username);
            currentUrl = baseUrl + "/pass/serviceLoginAuth2";
            response = HttpUtil.request(currentUrl, "POST", null, null, params, null);
            if(response.statusCode()!=302) {
                setLoginResult(currentUrl, response.statusCode(), response_body);
                return loginResult;
            }
            String location = "";
            location = response.getHeader("location");
            List<NameValuePair> pairs = URLEncodedUtils.parse(new URI(location), "UTF-8");
            Map<String, Object> paramMap = new HashMap<>();
            Iterator i$ = pairs.iterator();
            while(i$.hasNext()) {
                NameValuePair element = (NameValuePair)i$.next();
                paramMap.put(element.getName(), element.getValue());
            }
            location = URLDecoder.decode(location, "UTF-8");
            JSONObject extension_pragma = new JSONObject(response.getHeader("extension-pragma"));
            loginResult.put("ssecurity", extension_pragma.get("ssecurity"));
            loginResult.put("nonce", extension_pragma.get("nonce"));
            loginResult.put("psecurity", extension_pragma.get("psecurity"));
            response = HttpUtil.request(location.replaceFirst("[?].*", ""), "GET", null, null, paramMap, null);
            if(response.statusCode()!=200) {
                setLoginResult(currentUrl, response.statusCode(), response_body);
                return loginResult;
            }
            loginResult.put("userId", response.getCookie("userId"));
            loginResult.put("cUserId", response.getCookie("cUserId"));
            loginResult.put("serviceToken", response.getCookie("serviceToken"));
            cookies = response.getCookies();
            loginResult.put("result", true);
            LoggerUtils.log("Login passport successful!");
        } catch (Exception e) {
            e.printStackTrace();
            loginResult.put("result", false);
            loginResult.put("httpCode", -1);
            loginResult.put("httpError", e.toString());
            loginResult.put("lastUrl", currentUrl);
            return loginResult;
        }
        return loginResult;
    }

    /**
     * openhome登录
     * @param username 小米账号id或手机号码
     * @param password 密码
     * @param appId openhome的appid
     * @param redirectUri 回调地址
     * @return
     * @throws Exception
     */
    public static JSONObject openHomeLogin(String username, String password, String appId, String redirectUri) throws Exception {
        String sid = "oauth2.0";
        login(username, password, sid);
        if(!loginResult.getBoolean("result")) {
            return loginResult;
        }
        loginResult.put("result", false);
        Response response;
        HashMap<String, Object> queryParams = new HashMap<>();
        queryParams.put("client_id", appId);
        queryParams.put("response_type", "token");
        queryParams.put("redirect_uri", redirectUri);
        queryParams.put("sid", "oauth2.0");
        queryParams.put("userId", loginResult.getString("userId"));
        response = HttpUtil.request(baseUrl + "/oauth2/authorize", "GET", null, cookies, queryParams, null);
        if(response.statusCode()!=302) {
            setLoginResult(currentUrl, response.statusCode(), response.getBody().asString());
            return loginResult;
        }
        Pattern pattern = Pattern.compile("access_token=(.+)&token_type");
        Matcher matcher = pattern.matcher(response.getBody().asString());
        String accessToken = "";
        if(matcher.find()) {
            accessToken = matcher.group(1);
            loginResult.put("accessToken", accessToken);
            loginResult.put("result", true);
            LoggerUtils.log("openhome login successful, token=" + accessToken);
        } else {
            LoggerUtils.log("openhome login failed!");
        }
        return loginResult;
    }
    private static void setLoginResult(String url, int code, String body) throws Exception{
        loginResult.put("result", false);
        loginResult.put("lastUrl", url);
        loginResult.put("httpCode", code);
        loginResult.put("httpBody", body);
        LoggerUtils.log(String.format("login failed! httpCode=%s, httpBody=%s", code, body));
    }

    public static void main(String []args) throws Exception {
        RestAssured.useRelaxedHTTPSValidation();
        JSONObject loginResult;
        loginResult = openHomeLogin("15210558931", "xiaomi@123", ApiConfig.appId, ApiConfig.backUrl+"/cb");
        System.out.println(loginResult);
        Map<String, Object> cookies = new HashMap<>();
        List<JSONObject> dataObj = new ArrayList<>();
        JSONObject device = new JSONObject();
        device.put("did", "123456");
        device.put("time", 1521000001);
        device.put("type", "event");
        device.put("key", "motion");
        device.put("value", "motion values");
        dataObj.add(device);

        cookies.put("userId", loginResult.get("userId"));
        cookies.put("yetAnotherServiceToken", loginResult.get("serviceToken"));
        cookies.put("serviceToken", loginResult.get("serviceToken"));

        JSONObject paramsList = new JSONObject();
        paramsList.put("data", dataObj);
//        SmartHomeRequest.request(cookies, loginResult.get("ssecurity").toString(), "/user/get_user_device_data", "POST", paramsList);
    }
}

