package com.xioami.modeltool;

import io.restassured.response.Response;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.InputStreamUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.hamcrest.Matchers.*;

import static io.restassured.RestAssured.given;

public class PropertyChangeNotifyTest extends BaseTest{
    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyChangeNotifyTest.class);
    public static List<String> notSupportNotifyList = new ArrayList();

    @Test
    public void test01() throws Exception {
        Map<String, Map<String, String>> deviceList;
        deviceList = InputStreamUtils.getDeviceList(region);
        Map<String, String> device = null;
        String did = null;
        for (String key:deviceList.keySet()) {
            did = key;
            device = deviceList.get(did);
            if (device.get("type") != null) {
                break;
            }
        }
        //todo 拿到type后获取该type的所有属性
        String url = "http://miot-spec.org/miot-spec-v2/instance";
        String type = "urn:miot-spec-v2:device:camera:0000A01C:chuangmi-ipc009:1";
        Map map = new HashMap();
        map.put("type",type);
        Response response1 = given().params(map).get(url).peek();
        response1.then().statusCode(200);
        JSONObject jsonObject = new JSONObject(response1.asString());
        Assert.assertNotNull(jsonObject);
        ResultParse parse =new ResultParse();
        Assert.assertNotNull(parse.getNotifyProperty(jsonObject));
        List<String> pidNotifyList = parse.getNotifyPidList();
        Assert.assertNotNull(pidNotifyList);
        for(int k=0;k<pidNotifyList.size();k++){
//        for(String pid:pidNotifyList){
            //todo 更改第3位
            k =3;
            String subPid = did+"."+pidNotifyList.get(k);
            LOGGER.info("===== 开始了开始了开始了,第 {} 次循环，total： {}次",k+1,pidNotifyList.size());
           JSONObject pidJSONObject= parse.getNotifyPidJsonObject(pidNotifyList.get(k));
            List<String> properties = new ArrayList<>();
            properties.add(subPid);
            LOGGER.info("subscribe begin!");
            response = openHomeApi.subscribe("properties-changed", properties, null, receiverUrl);
            response.then().statusCode(200).body("properties[0].status",equalTo(0));
            LOGGER.info("======== subscribe success! ========");
            //修改properties的值，触发notify
            LOGGER.info("change properties begin");
            parse.setProperties(pidJSONObject,subPid);
            Boolean result = parse.isNotify(parse.getNotify(),ResultParse.notifyValue);
//            Assert.assertTrue("获取回调失败",result);
            if(!result){
                notSupportNotifyList.add(subPid);
            }


            //取消订阅
            LOGGER.info("unSubscribe begin!");
            response = openHomeApi.unSubscribe("properties-changed", properties);
            response.then().statusCode(200).body("properties[0].status",equalTo(0));
            LOGGER.info("======== unSubscribe success! ========");


        }



    }


@Test
    public void test(){
        Boolean a = true;
    System.out.println(String.valueOf(a));
}





}
