package com.xioami.modeltool;

import com.xiaomi.modeltool.OpenHomeApi;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpUtil;
import util.RandomUtil;
import util.Sleep;

import java.util.ArrayList;
import java.util.List;

import static com.xioami.modeltool.BaseTest.receiverUrl;
import static com.xioami.modeltool.PropertyChangeNotifyTest.allNotifyPidList;


public class ResultParse {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultParse.class);
    /**
     * {"services": [{"properties":
     * [{"pid":"2.1","format": "XXX", "unit": "XXX", "valueRange": [0, 100, 1]}]}],
     * "deviceType":"xxxx",
     * "model":"yyyyy"
     * }
     */

    public static JSONObject notifyJsonObject = new JSONObject();
    public static OpenHomeApi openHomeApi = new OpenHomeApi();
    public static String notifyValue = "";

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
                            JSONArray accessArray = propertiesArray.getJSONObject(j).getJSONArray("access");
                            for (int k = 0; k < accessArray.length(); k++) {
                                //todo 需要notify&write权限 加进去，要么就通过回调来触发
                                if (accessArray.getString(k).equals("notify")) {
                                    pid = (i + 1) + "." + (j + 1);
                                    allNotifyPidList.add(pid);
                                    //todo 把pid给设置进去
                                    if (accessArray.getString(k - 1).equals("write")) {
                                        propertiesArray.getJSONObject(j).put("pid", pid);
                                        //把满足条件的propertyJsonArray摘出来放进propertyJsonObject
                                        propertyJsonArray.put(propertiesArray.getJSONObject(j));
                                        break;
                                    }
                                    break;
                                }
                            }
                        }
                    }

                }

            }
        }
        propertyJsonObject.put("properties", propertyJsonArray);
        //propertyJsonObject 放到notifyJsonArray
        notifyJsonArray.put(propertyJsonObject);
        //把notifyJsonArray放到notifyJsonObject
        notifyJsonObject.put("services", notifyJsonArray);
        notifyJsonObject.put("type", response.getString("type"));
//        notifyJsonObject.put("model", model);
        LOGGER.info("===========get the NotifyProperty JSONObject success========");
        LOGGER.info("notifyJsonObject is :{}", notifyJsonObject);
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
                            LOGGER.info("getNotifyPidJsonObject is :{}", pidJsonArray.getJSONObject(j));
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
                    LOGGER.info("notifyPidList is :{}", notifyPidList);
                    return notifyPidList;
                }
            }
        }
        return null;
    }
    public boolean changePropertyValue(JSONObject subJsonObjectStart,JSONObject subJsonObjectEnd,String pid,Object startValue,Object endValue) throws Exception {
        Boolean isSetOk= true;
        Boolean startRequest = setPropertyRequest(pid,startValue,subJsonObjectStart);
        if(!startRequest){
            isSetOk=false;
        }
        Sleep.sleep(1500);
        Boolean endRequest = setPropertyRequest(pid,endValue,subJsonObjectEnd);
        if(!endRequest){
            isSetOk=false;
        }
        notifyValue = String.valueOf(endValue);
        return isSetOk;
    }

    public Boolean setPropertyRequest(String pid,Object value,JSONObject subJsonObject) throws Exception {
        Boolean isOK= true;
        JSONArray setProperties = new JSONArray();
        setProperties.put(new JSONObject().put("pid", pid).put("value", value));
        Response response1 = openHomeApi.setProperties(setProperties);
        JSONObject responseObject1 = new JSONObject(response1.asString());
        if (response1.statusCode() != 200 || responseObject1.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
            isOK = false;
            subJsonObject.put(pid, "format is bool,setProperty fail");
            subJsonObject.put("code", response1.statusCode());
            subJsonObject.put("pid", responseObject1.getJSONArray("properties").getJSONObject(0).getString("pid"));
            subJsonObject.put("status", responseObject1.getJSONArray("properties").getJSONObject(0).getInt("status"));
            subJsonObject.put("description", responseObject1.getJSONArray("properties").getJSONObject(0).getString("description"));
            PropertyChangeNotifyTest.failTestResult.put(subJsonObject);
        } else {
            LOGGER.info(" setProperties is success  ");
        }
        return isOK;
    }







    //对最小单元的pid所在的JSONObject进行遍历后set值
    public boolean setProperties(JSONObject notifyPidJsonObject, String pid) throws Exception {
        boolean flag = true;
        //对format进行区分，方便获取取值的范围
        if (!notifyPidJsonObject.isNull("pid")) {
            JSONObject subJsonObjectStart = new JSONObject();
            JSONObject subJsonObjectEnd = new JSONObject();
            if (notifyPidJsonObject.getString("format").contains("bool")) {
                LOGGER.info(">>>>>>>>>>>>>>>>>>>>format is bool {}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                Boolean setResult = changePropertyValue(subJsonObjectStart,subJsonObjectEnd,pid,true,false);
                if(!setResult){
                    flag = false;
                }
            } else if (notifyPidJsonObject.getString("format").contains("uint") || notifyPidJsonObject.getString("format").contains("int")) {
                if (!notifyPidJsonObject.isNull("value-range")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>,format is 整型，has field value-range {}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    Integer valueIntStart = valueRange.getInt(0);
                    Integer valueIntEnd = valueRange.getInt(1);


                    Boolean setResult = changePropertyValue(subJsonObjectStart,subJsonObjectEnd,pid,valueIntStart,valueIntEnd);
                    if(!setResult){
                        flag = false;
                    }
//
//                    JSONArray setPropertiesStart = new JSONArray();
//                    JSONArray setPropertiesEnd = new JSONArray();
//
//                    notifyValue = String.valueOf(valueIntEnd);
//                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", valueIntStart));
//                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", valueIntEnd));
////                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200).body("properties[0].status",equalTo(0));
//                    Response response2 = openHomeApi.setProperties(setPropertiesStart);
//                    JSONObject responseObject2 = new JSONObject(response2.asString());
//                    if (response2.statusCode() != 200 || responseObject2.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
//                        flag = false;
//                        subJsonObjectStart.put(pid, "format is 整型，has field value-range,change the valueIntStart fail");
//                        subJsonObjectStart.put("code", response2.statusCode());
//                        subJsonObjectStart.put("pid", responseObject2.getJSONArray("properties").getJSONObject(0).getString("pid"));
//                        subJsonObjectStart.put("status", responseObject2.getJSONArray("properties").getJSONObject(0).getInt("status"));
//                        subJsonObjectStart.put("description", responseObject2.getJSONArray("properties").getJSONObject(0).getString("description"));
//                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectStart);
//                    } else {
//                        LOGGER.info("========= format is 整型,has field value-range,set valueIntStart is success  =======");
//                    }
//                    Sleep.sleep(1500);
////                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200).body("properties[0].status",equalTo(0));
//                    Response response3 = openHomeApi.setProperties(setPropertiesEnd);
//                    JSONObject responseObject3 = new JSONObject(response3.asString());
//                    if (response3.statusCode() != 200 || responseObject3.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
//                        flag = false;
//                        subJsonObjectEnd.put(pid, "format is 整型，has field value-range,change the valueIntEnd fail");
//                        subJsonObjectEnd.put("code", response3.statusCode());
//                        subJsonObjectEnd.put("pid", responseObject3.getJSONArray("properties").getJSONObject(0).getString("pid"));
//                        subJsonObjectEnd.put("status", responseObject3.getJSONArray("properties").getJSONObject(0).getInt("status"));
//                        subJsonObjectEnd.put("description", responseObject3.getJSONArray("properties").getJSONObject(0).getString("description"));
//                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectEnd);
//                    } else {
//                        LOGGER.info("========= format is 整型，has field value-range,set valueIntEnd is success  =======");
//                    }
//                    notifyValue = String.valueOf(valueIntEnd);
                }
                if (!notifyPidJsonObject.isNull("value-list")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>,format is 整型，has field value-list {}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    JSONArray valueListArray = notifyPidJsonObject.getJSONArray("value-list");
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    notifyValue = String.valueOf(valueListArray.getJSONObject(valueListArray.length() - 1).getInt("value"));
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", valueListArray.getJSONObject(0).getInt("value")));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", valueListArray.getJSONObject(valueListArray.length() - 1).getInt("value")));
//                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Response response4 = openHomeApi.setProperties(setPropertiesStart);
                    JSONObject responseObject4 = new JSONObject(response4.asString());
                    if (response4.statusCode() != 200 || responseObject4.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectStart.put(pid, "format is 整型，has field value-list,change the valueIntStart fail");
                        subJsonObjectStart.put("code", response4.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectStart);
                    } else {
                        LOGGER.info("========= format is 整型,has field value-list,set valueStart is success  =======");
                    }
                    Sleep.sleep(1500);
//                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
                    Response response5 = openHomeApi.setProperties(setPropertiesEnd);
                    JSONObject responseObject5 = new JSONObject(response5.asString());
                    if (response5.statusCode() != 200 || responseObject5.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectEnd.put(pid, "format is 整型,has field value-list,change the valueEnd fail");
                        subJsonObjectEnd.put("code", response5.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectEnd);
                    } else {
                        LOGGER.info("========= format is 整型,has field value-list,set valueIntEnd is success  =======");
                    }
                }
            } else if (notifyPidJsonObject.getString("format").contains("hex")) {
                if (!notifyPidJsonObject.isNull("value-range")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>,format is hex，has field value-range{}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    Integer min;
                    Integer max;
                    //判断十六进制是否以0x开头
                    if (valueRange.getString(0).startsWith("0x")) {
                        min = Integer.parseInt(valueRange.getString(0).substring(2), 16);
                    } else {
                        min = Integer.parseInt(valueRange.getString(0), 16);
                    }
                    if (valueRange.getString(1).startsWith("0x")) {
                        max = Integer.parseInt(valueRange.getString(1).substring(2), 16);
                    } else {
                        max = Integer.parseInt(valueRange.getString(1), 16);
                    }
                    Integer valueIntStart = RandomUtil.genRandomInt(max, (max + min) / 2 + 1);
                    Integer valueIntEnd = RandomUtil.genRandomInt((max + min) / 2, min);
                    notifyValue = String.valueOf(valueIntEnd);
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", valueIntStart));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", valueIntEnd));
//                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Response response6 = openHomeApi.setProperties(setPropertiesStart);
                    JSONObject responseObject6 = new JSONObject(response6.asString());
                    if (response6.statusCode() != 200 || responseObject6.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectStart.put(pid, "format is hex，has field value-range,change the valueIntStart fail");
                        subJsonObjectStart.put("code", response6.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectStart);
                    } else {
                        LOGGER.info("========= format is hex，has field value-range,set valueIntStart is success  =======");
                    }
                    Sleep.sleep(1500);
//                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
                    Response response7 = openHomeApi.setProperties(setPropertiesStart);
                    JSONObject responseObject7 = new JSONObject(response7.asString());
                    if (response7.statusCode() != 200 || responseObject7.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectEnd.put(pid, "format is hex，has field value-range,change the valueIntEnd fail");
                        subJsonObjectEnd.put("code", response7.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectEnd);
                    } else {
                        LOGGER.info("========= format is hex，has field value-range,set valueIntEnd is success  =======");
                    }
                }
            } else if (notifyPidJsonObject.getString("format").contains("float")) {
                if (!notifyPidJsonObject.isNull("value-range")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>,format is float，has field value-range{}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    Double min = valueRange.getDouble(0);
                    Double max = valueRange.getDouble(1);
                    notifyValue = String.valueOf(max);
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", min));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", max));
//                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Response response8 = openHomeApi.setProperties(setPropertiesStart);
                    JSONObject responseObject8 = new JSONObject(response8.asString());
                    if (response8.statusCode() != 200 || responseObject8.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectStart.put(pid, "format is float，has field value-range,change the valueIntStart fail");
                        subJsonObjectStart.put("code", response8.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectStart);
                    } else {
                        LOGGER.info("========= format is float，has field value-range,set valueIntStart is success  =======");
                    }
                    Sleep.sleep(1500);
//                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
                    Response response9 = openHomeApi.setProperties(setPropertiesStart);
                    JSONObject responseObject9 = new JSONObject(response9.asString());
                    if (response9.statusCode() != 200 || responseObject9.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectEnd.put(pid, "format is float，has field value-range,change the valueIntEnd fail");
                        subJsonObjectEnd.put("code", response9.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectEnd);
                    } else {
                        LOGGER.info("========= format is float，has field value-range,set valueIntEnd is success  =======");
                    }
                }
            } else if (notifyPidJsonObject.getString("format").contains("string")) {
                if (!notifyPidJsonObject.isNull("max-length")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>format is string，has field max-length{}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    int maxLength = notifyPidJsonObject.getInt("max-length");
                    String min = RandomStringUtils.random(maxLength - 1, "abcabc");
                    String max = RandomStringUtils.random(maxLength, "01234");
                    notifyValue = String.valueOf(max);
                    JSONArray setPropertiesStart = new JSONArray();
                    JSONArray setPropertiesEnd = new JSONArray();
                    setPropertiesStart.put(new JSONObject().put("pid", pid).put("value", min));
                    setPropertiesEnd.put(new JSONObject().put("pid", pid).put("value", max));
//                    openHomeApi.setProperties(setPropertiesStart).then().statusCode(200);
                    Response response11 = openHomeApi.setProperties(setPropertiesStart);
                    JSONObject responseObject11 = new JSONObject(response11.asString());
                    if (response11.statusCode() != 200 || responseObject11.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectStart.put(pid, "format is string，has field max-length,change the valueStringStart fail");
                        subJsonObjectStart.put("code", response11.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectStart);
                    } else {
                        LOGGER.info("========= format is string，has field max-length,set valueStringStart is success  =======");
                    }
                    Sleep.sleep(1500);
//                    openHomeApi.setProperties(setPropertiesEnd).then().statusCode(200);
                    Response response10 = openHomeApi.setProperties(setPropertiesEnd);
                    JSONObject responseObject10 = new JSONObject(response10.asString());
                    if (response10.statusCode() != 200 || responseObject10.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                        flag = false;
                        subJsonObjectEnd.put(pid, "format is string，has field max-length,change the valueStringEnd fail");
                        subJsonObjectEnd.put("code", response10.statusCode());
                        PropertyChangeNotifyTest.failTestResult.put(subJsonObjectEnd);
                    } else {
                        LOGGER.info("========= format is string，has field max-length,set valueStringEnd is success  =======");
                    }
                    //todo  放到case的断言里面去
//                    Assert.assertTrue("",isNotify(getNotify(),max));
                }
            } else {
                flag = false;
                LOGGER.error("invalid format");
            }
        } else {
            flag = false;
            LOGGER.error("invalid notifyPidJsonObject");
        }
        return flag;
    }


    public JSONObject getNotify() throws Exception {

        Response response = HttpUtil.request(receiverUrl, "GET");
        response.then().statusCode(200);
        JSONObject responseJSONObject = new JSONObject(response.getBody().asString());
        return responseJSONObject;
    }

    public Boolean isNotify(JSONObject responseJSONObject, String value) throws Exception {
        Boolean isOK = false;
        int retry = 5;
        while (retry > 0) {
            if (responseJSONObject.getString("data").equals(value)) {
                isOK = true;
                LOGGER.info("get callback success!");
                break;
            }
            Sleep.sleep(2000);
            retry -= 1;
            responseJSONObject = getNotify();
            LOGGER.info("还有{}次机会获取回调值", retry);
        }
        return isOK;
    }


    public String generateModel(String deviceType) {
        if (deviceType != null) {
            String[] strArray = deviceType.split(":");
            strArray[5] = strArray[5].replaceFirst("-", "." + strArray[3] + ".");
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







