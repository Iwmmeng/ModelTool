package com.xioami.modeltool;

import com.xiaomi.modeltool.OpenHomeApi;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpUtil;
import util.RandomUtil;
import util.Sleep;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.xioami.modeltool.BaseTest.receiverUrl;
import static org.hamcrest.Matchers.*;
import io.restassured.matcher.RestAssuredMatchers.*;
import        org.hamcrest.Matchers.*;
import static io.restassured.RestAssured.given;
import io.restassured.module.jsv.JsonSchemaValidator.*;

public class ResultParse {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultParse.class);
    /**
     * {"services": [{"properties":
     *                             [{"pid":"2.1","format": "XXX", "unit": "XXX", "valueRange": [0, 100, 1]}]}],
     * "deviceType":"xxxx",
     * "model":"yyyyy"
     * }
     *
     */

    public static JSONObject notifyJsonObject= new JSONObject();
    public static OpenHomeApi openHomeApi = new OpenHomeApi();
    public static String notifyValue="";
    public JSONObject getNotifyProperty(JSONObject response) throws JSONException {
        JSONObject propertyJsonObject = new JSONObject();
        JSONArray notifyJsonArray = new JSONArray();
        JSONArray propertyJsonArray = new JSONArray();
        List notifyPidList = new ArrayList();
        String pid = null;
        String model = null;
        if (!response.isNull("services")) {
            JSONArray servicesArray = response.getJSONArray("services");
            for (int i = 0; i < servicesArray.length(); i++) {
                if (!servicesArray.getJSONObject(i).isNull("properties")) {
                    JSONArray propertiesArray = servicesArray.getJSONObject(i).getJSONArray("properties");
                    for (int j = 0; j < propertiesArray.length(); j++) {
                        if (!propertiesArray.getJSONObject(j).isNull("access")) {
                            JSONArray accessArray = propertiesArray.getJSONObject(i).getJSONArray("access");
                            for (int k = 0; k < accessArray.length(); k++) {
                                if (accessArray.getString(k).equals("notify")) {
                                    model = generateModel(response.getString("type"));
                                    //todo 把pid给设置进去
                                    pid = (i+1) + "." + (j+1);
                                    propertiesArray.getJSONObject(j).put("pid", pid);
                                    //把满足条件的propertyJsonArray摘出来放进propertyJsonObject
                                    propertyJsonArray.put(propertiesArray.getJSONObject(j));
                                    break;
                                }
                            }
                        }
                    }

                }

            }
        }
        propertyJsonObject.put("properties",propertyJsonArray);
        //propertyJsonObject 放到notifyJsonArray
        notifyJsonArray.put(propertyJsonObject);
        //把notifyJsonArray放到notifyJsonObject
        notifyJsonObject.put("services",notifyJsonArray);
        notifyJsonObject.put("type",response.getString("type"));
        notifyJsonObject.put("model",model);
        LOGGER.info("===========get the NotifyProperty JSONObject success========");
        LOGGER.info("notifyJsonObject is :{}",notifyJsonObject);
        return notifyJsonObject;
    }
    public JSONObject getNotifyPidJsonObject(String pid) throws JSONException {
        if ((notifyJsonObject.length() != 0)) {
            JSONArray sArray = notifyJsonObject.getJSONArray("services");
            for (int i = 0; i < sArray.length(); i++) {
                if (!sArray.getJSONObject(i).isNull("properties")) {
                    JSONArray pidJsonArray = sArray.getJSONObject(i).getJSONArray("properties");
                    for (int j = 0; j < pidJsonArray.length(); j++) {
                        if (pidJsonArray.getJSONObject(j).getString("pid").equals(pid)) {
                            LOGGER.info("===========get the NotifyPidJsonObject  success========");
                            LOGGER.info("getNotifyPidJsonObject is :{}",pidJsonArray.getJSONObject(j));
                            return pidJsonArray.getJSONObject(j);
                        }
                    }
                }
            }
        }
        return null;
    }
    public List getNotifyPidList() throws JSONException {
        List notifyPidList = new ArrayList();
        if ((notifyJsonObject.length() != 0)) {
            JSONArray sArray = notifyJsonObject.getJSONArray("services");
            for (int i = 0; i < sArray.length(); i++) {
                if (!sArray.getJSONObject(i).isNull("properties")) {
                    JSONArray pidJsonArray = sArray.getJSONObject(i).getJSONArray("properties");
                    for (int j = 0; j < pidJsonArray.length(); j++) {
                       String pid = pidJsonArray.getJSONObject(j).getString("pid");
                        notifyPidList.add(pid);
                    }
                    LOGGER.info("===========get the getNotifyPidList list success========");
                    LOGGER.info("notifyPidList is :{}",notifyPidList);
                    return notifyPidList;
                }
            }
        }
        return null;
    }

    public void setProperties(JSONObject notifyPidJsonObject,String pid) throws Exception {
//        JSONArray setProperties = new JSONArray();
//       Response  response = openHomeApi.setProperties(setProperties);
        //对format进行区分，方便获取取值的范围
        if(!notifyPidJsonObject.isNull("pid")) {
            if (notifyPidJsonObject.getString("format").contains("bool") ) {
                LOGGER.info("========= format is bool =======");
                JSONArray setPropertiesStart = new JSONArray();
                JSONArray setPropertiesEnd = new JSONArray();
                setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", true));
                openHomeApi.setProperties(setPropertiesStart).then().statusCode(200).body("properties[0].status",equalTo(0));
                LOGGER.info("========= format is bool,setPropertiesStart is success  =======");
                Sleep.sleep(1500);
                setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", false));
                openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200).body("properties[0].status",equalTo(0));
                LOGGER.info("========= format is bool,setPropertiesEnd is success  =======");
                notifyValue=String.valueOf(false);
            } else if (notifyPidJsonObject.getString("format").contains("uint")||notifyPidJsonObject.getString("format").contains("int")) {
                if(!notifyPidJsonObject.isNull("value-range")){
                    LOGGER.info("========= format is 整型，has field value-range =======");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
//                    Integer min = valueRange.getInt(0);
//                    Integer max = valueRange.getInt(1);
//                    Integer valueIntStart = RandomUtil.genRandomInt(max, (max + min)/2 + 1);
//                    Integer valueIntEnd = RandomUtil.genRandomInt((max + min )/2, min);

                    Integer valueIntStart = valueRange.getInt(0);
                    Integer valueIntEnd = valueRange.getInt(1);

                    notifyValue=String.valueOf(valueIntEnd);
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", valueIntStart));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", valueIntEnd));
                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200).body("properties[0].status",equalTo(0));
                    LOGGER.info("========= format is 整型,set valueIntStart is success  =======");
                    Sleep.sleep(1500);
                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200).body("properties[0].status",equalTo(0));
                    LOGGER.info("========= format is 整型,set valueIntEnd is success  =======");
                    notifyValue=String.valueOf(valueIntEnd);
                }if(!notifyPidJsonObject.isNull("value-list")){
                    LOGGER.info("========= format is 整型，has field value-list =======");
                    JSONArray valueListArray = notifyPidJsonObject.getJSONArray("value-list");
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    notifyValue=String.valueOf(valueListArray.getJSONObject(valueListArray.length()-1).getInt("value"));
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", valueListArray.getJSONObject(0).getInt("value")));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", valueListArray.getJSONObject(valueListArray.length()-1).getInt("value")));
                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Sleep.sleep(1500);
                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
                }
            }else if(notifyPidJsonObject.getString("format").contains("hex") ){
                if(!notifyPidJsonObject.isNull("value-range")){
                    LOGGER.info("========= format is hex，has field value-range =======");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    Integer min;
                    Integer max;
                    //判断十六进制是否以0x开头
                    if(valueRange.getString(0).startsWith("0x")){
                         min = Integer.parseInt(valueRange.getString(0).substring(2),16);
                    }else{
                        min = Integer.parseInt(valueRange.getString(0),16);
                    }
                    if(valueRange.getString(1).startsWith("0x")){
                        max = Integer.parseInt(valueRange.getString(1).substring(2),16);
                    }else{
                        max = Integer.parseInt(valueRange.getString(1),16);
                    }
                    Integer valueIntStart = RandomUtil.genRandomInt(max, (max + min)/2 + 1);
                    Integer valueIntEnd = RandomUtil.genRandomInt((max + min )/2, min);
                    notifyValue=String.valueOf(valueIntEnd);
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", valueIntStart));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", valueIntEnd));
                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Sleep.sleep(1500);
                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
            }
        }else if(notifyPidJsonObject.getString("format").contains("float")){
                if(!notifyPidJsonObject.isNull("value-range")){
                    LOGGER.info("========= format is float，has field value-range =======");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    Double min=  valueRange.getDouble(0);
                    Double max=  valueRange.getDouble(1);
                    notifyValue=String.valueOf(max);
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", min));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", max));
                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Sleep.sleep(1500);
                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
                }
            }else if(notifyPidJsonObject.getString("format").contains("string")){
                if(!notifyPidJsonObject.isNull("max-length")){
                    LOGGER.info("========= format is string，has field max-length =======");
                    int maxLength = notifyPidJsonObject.getInt("max-length");
                    String min =  RandomStringUtils.random(maxLength-1,"abcabc");
                    String max = RandomStringUtils.random(maxLength,"01234");
                    notifyValue=String.valueOf(max);
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", min));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", max));
                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Sleep.sleep(1500);
                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
           //todo  放到case的断言里面去
//                    Assert.assertTrue("",isNotify(getNotify(),max));
                }
            }else {
                LOGGER.error("invalid format");
            }
        }else{
            LOGGER.error("invalid notifyPidJsonObject");
        }
    }
    public JSONObject getNotify() throws Exception {

        Response response = HttpUtil.request(receiverUrl, "GET");
        response.then().statusCode(200);
        JSONObject responseJSONObject = new JSONObject(response.getBody().asString());
        return responseJSONObject;
    }
     public Boolean isNotify(JSONObject responseJSONObject,String value) throws Exception {
        Boolean isOK = false;
         int retry = 5;
        while (retry > 0){
            if(responseJSONObject.getString("data").equals(value)) {
                isOK =true;
                LOGGER.info("get callback success!");
                break;
            }
            Sleep.sleep(2000);
            retry -= 1;
            responseJSONObject = getNotify();
            LOGGER.info("第{}次获取回调值",retry);
        }
        return isOK;
    }
    public void subscribe(){

    }









    public String generateModel(String deviceType) {
        if (deviceType != null) {
            String[] strArray = deviceType.split(":");
            strArray[5] = strArray[5].replaceFirst("-", "."+strArray[3] + ".");
            return strArray[5];
        } else return null;
    }


    @Test
    public void test01() throws Exception {
//        String response = given().
//                get("http://miot-spec.org/miot-spec-v2/instance?type=urn:miot-spec-v2:device:camera:0000A01C:chuangmi-ipc009:1")
//                .peek().asString();
//
//        JSONObject jsonObject = new JSONObject(response);
//        System.out.println(jsonObject.isNull("description"));
//        System.out.println(jsonObject.isNull("description22"));
//        System.out.println(getNotifyProperty(jsonObject));
//        System.out.println(generateModel("urn:miot-spec-v2:device:camera:0000A01C:chuangmi-ipc009-2-3:1"));
//        LOGGER.info("=====");
//        String min =  RandomStringUtils.random(7,"abc");
//        System.out.println(min);
        Response response = HttpUtil.request(receiverUrl, "GET");
        response.prettyPeek();


    }
    @Test
    public void test3() throws JSONException {
        String str = "{\"unit\":\"arcdegrees\",\"access\":[\"read\",\"write\",\"notify\"],\"iid\":2,\"format\":\"uint16\",\"description\":\"Image Rollover\",\"pid\":\"2.2\",\"value-range\":[0,180,180],\"type\":\"urn:miot-spec-v2:property:image-rollover:00000058:chuangmi-ipc009:1\"}";
        JSONObject jsonObject = new JSONObject(str);
        JSONArray jsonArray = jsonObject.getJSONArray("value-range");
        System.out.println(jsonArray);

    }


}







