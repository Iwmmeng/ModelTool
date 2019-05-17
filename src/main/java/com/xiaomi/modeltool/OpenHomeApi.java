package com.xiaomi.modeltool;

import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;

import static com.xiaomi.modeltool.BaseApi.apiHost;
import static com.xiaomi.modeltool.BaseApi.headers;


public class OpenHomeApi extends BaseApi {


    /**
     * 设置多个设备的多个属性
     * @param properties [
    {"pid": "AAAD.1.1",
    "value": true,},
    {"pid": "AAAC.1.1",
    "value": true,}]
     * @return
     * @throws Exception
     */
    public Response setProperties(JSONArray properties) throws Exception {
        Response response;
        JSONObject body = new JSONObject();
        body.put("properties", properties);
        String url = "https://"+ apiHost + ApiConfig.propertiesUrl;
        response = OpenHomeRequest.request(url, "PUT", headers, null, body.toString());
        return response;
    }
    /**
     * 订阅事件
     * @param topic 事件主题
     * @param valueList 事件内容，详情见API描述
     * @param customData 自定义数据，它将附加到将来的事件通知上。调用方可以使用该字段存储有关本次订阅的相关信息
     * @param receiverUrl 事件通知地址，需要调用方提供，用于接收将来的事件通知
     * @return
     * @throws Exception
     */
    public <T> Response subscribe(String topic, T valueList, JSONObject customData, String receiverUrl) throws Exception {
        Response response;
        String url = "https://"+ apiHost + ApiConfig.subscribeUrl;
        JSONObject body = new JSONObject();
        body.put("topic", topic).put(getTopicValue(topic), valueList).put("custom-data", customData).put("receiver-url", receiverUrl);
        response = OpenHomeRequest.request(url, "POST", headers, null, body.toString());
        return response;
    }

    /**
     * 取消订阅事件
     * @param topic
     * @param valueList
     * @return
     * @throws Exception
     */
    public <T> Response unSubscribe(String topic, T valueList) throws Exception {
        Response response;
        String url = "https://"+ apiHost + ApiConfig.subscribeUrl;
        JSONObject body = new JSONObject();
        body.put("topic", topic).put(getTopicValue(topic), valueList);
        response = OpenHomeRequest.request(url, "DELETE", headers, null, body.toString());
        return response;
    }


    private String getTopicValue(String topic) throws Exception{
        switch (topic) {
            case "properties-changed":
                return "properties";
            case "event-occured":
                return "events";
            case "devices-status-changed":
                return "devices";
            case "homes-changed":
                throw new Exception("subscribe event not implemented!");
            default:
                throw new Exception("subscribe event not support!");
        }
    }




}
