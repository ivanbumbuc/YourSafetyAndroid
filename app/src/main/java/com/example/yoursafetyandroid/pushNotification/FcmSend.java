package com.example.yoursafetyandroid.pushNotification;

import android.content.Context;
import android.os.StrictMode;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class FcmSend {
    public static  String BASE_URL = "https://fcm.googleapis.com/fcm/send";
    public static String SERVER_KEY = "AAAA9TjraEk:APA91bHwOsPURSMwFE-iIiPqKaTUrgbOJgfwF2tLyPsyJBjJ7LVYvfyIpsrMYgz0yKOYAj1rPDtW9lPxZ3gJZ6YI36dgtCbxOg2WS_FhnEUDhSC9CslzOl051_rBGlLmsiB4NmozldcD";

    public static void pushNotification(Context context, String token, String title, String message)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        RequestQueue queue = Volley.newRequestQueue(context);

        try{
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("to", token);

            JSONObject jsonObjectNotification = new JSONObject();
            jsonObjectNotification.put("title",title);
            jsonObjectNotification.put("body",message);
            jsonObject.put("notification", jsonObjectNotification);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, BASE_URL, jsonObject, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    System.out.println("FCM" + response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {

                }
            })
            {
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String, String> p = new HashMap<>();
                    p.put("Content-Type","application/json");
                    p.put("Authorization","key="+SERVER_KEY);
                    return p;
                }
            };
            queue.add(jsonObjectRequest);
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }
    }
}
