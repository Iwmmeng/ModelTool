package com.xioami.modeltool;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class util {
    JSONObject response;
/**
 *{"deviceType号": [{"propertyType号1":
                                    [{"format": "XXX", "unit": "XXX", "valueRange": [0, 100, 1]}]}]}

 *{"deviceType号": [{"propertyType号1":
 [{"format": "XXX"}, {"unit": "XXX"}, {"valueRange": [0, 100, 1]}]}]}
 *
 *
 * {"deviceType号": [{"propertyType号1":
 ["format": "XXX", "unit": "XXX", "valueRange": [0, 100, 1]]}]}
 * */
    public void getNotifyProperty() throws JSONException {

        JSONObject notifyJsonObject = new JSONObject();
        JSONObject propertyJsonObject = new JSONObject();
        JSONArray notifyJsonArray = new JSONArray();
        JSONArray propertyJsonArray = new JSONArray();
        JSONArray valueJsonArray = new JSONArray();

        if(response.getBoolean("services")){
            JSONArray servicesArray = new JSONArray(response.getString("services"));
            for(int i =0;i<servicesArray.length();i++){
                if(servicesArray.getJSONObject(i).getBoolean("properties")){
                    JSONArray propertiesArray = new JSONArray(servicesArray.getJSONObject(i));
                    for(int j=0;j<propertiesArray.length();j++){
                        if(propertiesArray.getJSONObject(j).getBoolean("access")){
                            JSONArray accessArray = propertiesArray.getJSONObject(i).getJSONArray("access");
                            for(int k=0;k<accessArray.length();k++){
                                if(accessArray.getString(i).equals("notify")){



                                }
                            }

                        }
                    }

                }

            }



        }
    }

//    if()
//    for(){
//
//    }

}
