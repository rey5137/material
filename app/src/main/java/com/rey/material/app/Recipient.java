package com.rey.material.app;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Rey on 3/2/2015.
 */
public class Recipient {
    long contactId;
    String name;
    String number;
    String lookupKey;

    @Override
    public String toString(){
        JSONObject obj = new JSONObject();
        try {
            obj.put("name", name);
            obj.put("number", number);
        } catch (JSONException e) {}
        return obj.toString();
    }
}
