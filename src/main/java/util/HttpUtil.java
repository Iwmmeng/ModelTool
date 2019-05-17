package util;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

import static io.restassured.RestAssured.given;


public class HttpUtil {
    public static Response request(String url, String method) throws Exception{
        Response response;
        LoggerUtils.log(String.format("%s %s", method, url));
        switch (method) {
            case "POST":
                response = given().relaxedHTTPSValidation().post(url);
                break;
            case "GET":
                response = given().relaxedHTTPSValidation().get(url);
                break;
            default:
                throw new Exception(method + " method is not support!");
        }
        LoggerUtils.log(String.format("Response code:%d, Response body:%s",response.getStatusCode(),response.getBody().asString()));
        return response;
    }

    public static Response request(String url, String method, Map<String, Object> headers, Map<String, String> cookies, Map<String, Object> params, String body) throws Exception{
        Response response;
//        System.setProperty("http.proxyHost", "127.0.0.1");
//        System.setProperty("https.proxyHost", "127.0.0.1");
//        System.setProperty("http.proxyPort", "8888");
//        System.setProperty("https.proxyPort", "8888");
        RequestSpecification request = given().relaxedHTTPSValidation().with().redirects().follow(false);
        LoggerUtils.log(String.format("%s %s headers=%s, cookies=%s, params=%s, body=%s", method, url, headers, cookies, params, body));
        switch (method) {
            case "POST":
                if (cookies != null) {request = request.cookies(cookies);}
                if (headers != null) {request = request.headers(headers);}
                if (params != null) {request = request.formParams(params);}
                if (body != null) {request = request.body(body);}
                response = request.post(url);
                break;
            case "GET":
                if (cookies != null) {request = request.cookies(cookies);}
                if (headers != null) {request = request.headers(headers);}
                if (params != null) {request = request.params(params);}
                if (body != null) {request = request.body(body);}
                response = request.get(url);
                break;
            case "PUT":
                if (cookies != null) {request = request.cookies(cookies);}
                if (headers != null) {request = request.headers(headers);}
                if (params != null) {request = request.formParams(params);}
                if (body != null) {request = request.body(body);}
                response = request.put(url);
                break;
            case "DELETE":
                if (cookies != null) {request = request.cookies(cookies);}
                if (headers != null) {request = request.headers(headers);}
                if (params != null) {request = request.formParams(params);}
                if (body != null) {request = request.body(body);}
                response = request.delete(url);
                break;
            default:
                throw new Exception(method + " method is not support!");
        }
        LoggerUtils.log(String.format("Response code:%d, Response body:%s",response.getStatusCode(),response.getBody().asString()));
        return response;
    }
}

