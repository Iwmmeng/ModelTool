package com.xioami.modeltool;

import com.xiaomi.modeltool.ApiConfig;
import io.restassured.response.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.InputStreamUtils;
import util.ResultParse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static io.restassured.RestAssured.given;
import static util.ResultParse.allNotifyPidList;
import static util.ResultParse.failTestResult;

public class PropertyChangeNotifyTest extends BaseTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyChangeNotifyTest.class);
    public static List<String> supportNotifyList = new ArrayList();
@Before
public void before(){


}

    @Test
    public void test01() throws Exception {
        Map<String, Map<String, String>> deviceList;
        deviceList = InputStreamUtils.getDeviceList(region);
        Map<String, String> device = null;
        String type = null;
        String did = null;
        for (String key : deviceList.keySet()) {
            did = key;
            device = deviceList.get(did);
            if (device.get("type") != null) {
                type = device.get("type");
                break;
            }
        }
        //todo 拿到type后获取该type的所有属性
        String url = "http://miot-spec.org/miot-spec-v2/instance";
//        String type = "urn:miot-spec-v2:device:camera:0000A01C:chuangmi-ipc009:1";
//        String type = "urn:miot-spec-v2:device:air-purifier:0000A007:zhimi-m2:1";
        Map map = new HashMap();
        map.put("type", type);
        Response response1 = given().params(map).get(url);
        response1.then().statusCode(200);
        JSONObject jsonObject = new JSONObject(response1.asString());
        Assert.assertNotNull(jsonObject);
        ResultParse parse = new ResultParse();
        Assert.assertNotNull(parse.getNotifyProperty(jsonObject));
        List<String> pidNotifyList = parse.getNotifyPidList();
        Assert.assertNotNull(pidNotifyList);
        for (int k = 0; k < pidNotifyList.size(); k++) {
            //todo 更改第3位
//            k =3;
            JSONObject subJsonObject = new JSONObject();
            String subPid = did + "." + pidNotifyList.get(k);
            LOGGER.info("===== 开始了开始了开始了,第 {} 次循环，total： {}次", k + 1, pidNotifyList.size());
            JSONObject pidJSONObject = parse.getNotifyPidJsonObject(pidNotifyList.get(k));
            List<String> properties = new ArrayList<>();
            properties.add(subPid);
            LOGGER.info("subscribe begin!");
            response = openHomeApi.subscribe("properties-changed", properties, null, receiverUrl);
//            response.then().statusCode(200).body("properties[0].status",equalTo(0));
            JSONObject responseObject = new JSONObject(response.asString());
            LOGGER.info("responseObject is {}", responseObject);
            if (response.statusCode() != 200 || responseObject.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                subJsonObject.put(subPid, "subscribe fail");
                subJsonObject.put("code", response.statusCode());
                subJsonObject.put("pid", responseObject.getJSONArray("properties").getJSONObject(0).getString("pid"));
                subJsonObject.put("status", responseObject.getJSONArray("properties").getJSONObject(0).getInt("status"));
                subJsonObject.put("description", responseObject.getJSONArray("properties").getJSONObject(0).getString("description"));
                failTestResult.put(subJsonObject);
                continue;
            } else {
                LOGGER.info("======== subscribe success! ========");
            }
            //setProperties的值
            LOGGER.info("change properties begin");
            Boolean setResult = parse.setProperties(pidJSONObject, subPid);
            //todo  需要考虑判断setResult是否为空吗？？？
            if (!setResult) {
                continue;
            }
            //获取回调
            Boolean result = parse.isNotify(parse.getNotify(), ResultParse.notifyValue);
            if (!result) {
                subJsonObject.put(subPid, "获取回调失败");
                failTestResult.put(subJsonObject);
                continue;
            } else {
                LOGGER.info("======== get callback  success! ========");
            }

            //取消订阅
            LOGGER.info("unSubscribe begin!");
            response = openHomeApi.unSubscribe("properties-changed", properties);
//            response.then().statusCode(200).body("properties[0].status",equalTo(0));
            if (response.statusCode() != 200 || responseObject.getJSONArray("properties").getJSONObject(0).getInt("status") != 0) {
                subJsonObject.put(subPid, "unSubscribe fail");
                subJsonObject.put("code", response.statusCode());
                subJsonObject.put("pid", subPid);
                subJsonObject.put("status", responseObject.getJSONArray("properties").getJSONObject(0).getInt("status"));
                failTestResult.put(subJsonObject);
                continue;
            } else {
                LOGGER.info("======== unSubscribe success! ========");
            }
            supportNotifyList.add(subPid);
        }
//todo 增加一个所有包含notify的列表
        LOGGER.info("all notify pid size:{},list:{}",allNotifyPidList.size(),allNotifyPidList);
        LOGGER.info("write权限notify的pid size:{},list:{}",pidNotifyList.size(),pidNotifyList);
        LOGGER.info("write权限notify的failTestResult size:{},list:{}",failTestResult.length(),failTestResult);
        LOGGER.info("write权限notify的passNotifyList size:{},list:{}",supportNotifyList.size(),supportNotifyList);
    }

}
