package util;

import com.xiaomi.modeltool.ApiConfig;
import com.xiaomi.modeltool.OpenHomeApi;
import io.restassured.response.Response;
import org.apache.commons.lang3.RandomStringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;



public class ResultParse {
    private static final Logger LOGGER = LoggerFactory.getLogger(ResultParse.class);
    public static JSONArray failTestResult = new JSONArray();
    public static List allNotifyPidList = new ArrayList();
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
            LOGGER.info("start value set fail");
        }else {
            LOGGER.info("start value set success");
        }
        Sleep.sleep(1500);
        Boolean endRequest = setPropertyRequest(pid,endValue,subJsonObjectEnd);
        if(!endRequest){
            isSetOk=false;
            LOGGER.info("end value set fail");
        }else {
            LOGGER.info("end value set success");
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
            failTestResult.put(subJsonObject);
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
                }else {
//                    LOGGER.info("bool format not have value-range or value-list");
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
                } else if (!notifyPidJsonObject.isNull("value-list")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>,format is 整型，has field value-list {}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    JSONArray valueListArray = notifyPidJsonObject.getJSONArray("value-list");
                    int valueIntStart = valueListArray.getJSONObject(0).getInt("value");
                    int valueIntEnd = valueListArray.getJSONObject(valueListArray.length() - 1).getInt("value");
                    Boolean setResult = changePropertyValue(subJsonObjectStart,subJsonObjectEnd,pid,valueIntStart,valueIntEnd);
                    if(!setResult){
                        flag = false;
                    }
                }else {
                    LOGGER.info("uint,int format not have value-range or value-list");
                }
            } else if (notifyPidJsonObject.getString("format").contains("hex")) {
                if (!notifyPidJsonObject.isNull("value-range")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>,format is hex，has field value-range{}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    Integer min =null;
                    Integer max=null;
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
                    Boolean setResult = changePropertyValue(subJsonObjectStart,subJsonObjectEnd,pid,min,max);
                    if(!setResult){
                        flag = false;
                    }
                }else {
                    LOGGER.info("hex format not have value-range ");
                }
            } else if (notifyPidJsonObject.getString("format").contains("float")) {
                if (!notifyPidJsonObject.isNull("value-range")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>,format is float，has field value-range{}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    JSONArray valueRange = notifyPidJsonObject.getJSONArray("value-range");
                    Double min = valueRange.getDouble(0);
                    Double max = valueRange.getDouble(1);
                    Boolean setResult = changePropertyValue(subJsonObjectStart,subJsonObjectEnd,pid,min,max);
                    if(!setResult){
                        flag = false;
                    }
                }else {
                    LOGGER.info("float format not have value-range ");
                }
            } else if (notifyPidJsonObject.getString("format").contains("string")) {
                if (!notifyPidJsonObject.isNull("max-length")) {
                    LOGGER.info(">>>>>>>>>>>>>>>>>>>>format is string，has field max-length{}" + notifyPidJsonObject.getString("format") + "<<<<<<<<<<<<<<<<<<<");
                    int maxLength = notifyPidJsonObject.getInt("max-length");
                    String min = RandomStringUtils.random(maxLength - 1, "abcabc");
                    String max = RandomStringUtils.random(maxLength, "01234");
                    Boolean setResult = changePropertyValue(subJsonObjectStart,subJsonObjectEnd,pid,min,max);
                    if(!setResult){
                        flag = false;
                    }else {
                        LOGGER.info("string format not have max-length");
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

        Response response = HttpUtil.request(ApiConfig.receiverUrl, "GET");
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
        Response response = HttpUtil.request(ApiConfig.receiverUrl, "GET");
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







