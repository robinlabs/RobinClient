package com.robinlabs.persona;

import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.magnifis.parking.App;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by oded on 5/7/14.
 */
public class Sync {

    final static URI SERVER_URL = URI.create("http://173.255.249.124:8080/persona/v0/");
    static String user_object = "user_object";
    static String user_id = "user_id";

    static void saveToServer(User user) {
        (new PostToServer()).execute();
    }

    static void getFromServer() {
        (new GetFromServer()).execute();
    }


    static class PostToServer extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            try {
                doPost();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }


            return null;
        }


    }


    static void doPost() throws IOException, JSONException {
        HttpClient httpclient = new DefaultHttpClient();

        HttpPost httppost = new HttpPost(SERVER_URL);

        JSONObject wrappedUser = new JSONObject();
//        wrappedUser.put("user", App.self.user()); TODO:bring back, when persona comes back


        String jsonObjectString = wrappedUser.toString();

        // Add your data
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
        nameValuePairs.add(new BasicNameValuePair(user_id, User.getServerUserId()));
        nameValuePairs.add(new BasicNameValuePair(user_object, jsonObjectString));
        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

        // Execute HTTP Post Request
        HttpResponse response = httpclient.execute(httppost);

        //persist user id if this is an unrecognized user
        if (User.getServerUserId() == null) {
            String json_string = EntityUtils.toString(response.getEntity());
//            String json_string = URLDecoder.decode(json_string_encoded, "UTF-8");

            JSONObject wrapperObject = new JSONObject(json_string);
//            App.self.user = (User) wrapperObject.get("user");

            String userId = wrapperObject.getJSONObject("user").getString(user_id);
            Log.d("test", userId);
            User.localUserData.edit().putString(User.USER_ID_SHARED_PREF, userId).commit();
        }
    }

    static class GetFromServer extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] objects) {

            String id = User.getServerUserId();
            if (id == null) {
                (new PostToServer()).execute();
                return null;
            }

            HttpClient httpClient = new DefaultHttpClient();

            Uri.Builder b = Uri.parse(SERVER_URL.toString()).buildUpon();
            b.appendQueryParameter("user_id", id);

            HttpGet getRequest = new HttpGet(b.build().toString());

            String json;
            try {
                HttpResponse response = httpClient.execute(getRequest);
                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                json = reader.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }


            try {
                JSONObject wrapper = new JSONObject(json);
                User u = new User(wrapper.get(User.USER_OBJECT).toString());
//                App.self.user = u;TODO:bring back, when persona comes back
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }
    }

}
