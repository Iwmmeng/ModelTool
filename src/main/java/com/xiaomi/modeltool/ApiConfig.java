package com.xiaomi.modeltool;

public class ApiConfig { public static String oauthTokenUrl = "https://account.xiaomi.com";
    public static String devicesUrl = "/api/v1/devices";
    public static String servicesUrl = "/api/v1/services";
    public static String propertiesUrl = "/api/v1/properties";
    public static String actionUrl = "/api/v1/action";
    public static String homeUrl = "/api/v1/homes";
    public static String informationUrl = "/api/v1/device-information";
    public static String scenesUrl = "/api/v1/scenes";
    public static String triggerSceneUrl = "/api/v1/scene";
    public static String subscribeUrl = "/api/v1/subscriptions";
    public static String timerUrl = "/api/v1/timer";
    public static String orderPropertiesUrl = "/api/v1/order-properties";
    public static String backUrl = "http://topentest.iot.mi.com:28085";
    public static String sercet = "7l2eCVZhedrAds/RWhEPmQ==";
    public static String appId = "2882303761517904749";
    public static String specNs = "miot-spec-v2";
    public static String smsUrl = "http://10.108.163.63/sender/sms";

    //todo 在BaseTest里面会被重新改写给与赋值
    public static String receiverUrl = "http://10.38.164.50:8089/subscribe/TJ";

    public static int VIRTUAL_DEVICE_ERROR = -901; //虚拟设备异常
    public static int NOTIFY_SERVICE_ERROR = -902; //回调地址异常

}
