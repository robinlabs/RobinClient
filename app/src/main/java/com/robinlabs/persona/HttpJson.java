package com.robinlabs.persona;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by oded on 5/15/15.
 */
public abstract class HttpJson {
    abstract public void onResponse(HttpResponse httpResponse, JSONObject jsonResponse);


    class HttpAsyncTask extends AsyncTask {
        private HttpRequestBase httpRequest;
        private HttpResponse response;
        private JSONObject jsonResponse;

        HttpAsyncTask(HttpRequestBase httpRequest) {
            this.httpRequest = httpRequest;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            StringBuilder builder = null;
            try {
                response = new DefaultHttpClient().execute(httpRequest);

                Log.d("Post JSON response", response.getStatusLine().getReasonPhrase());

                BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
                builder = new StringBuilder();
                for (String line = null; (line = reader.readLine()) != null; ) {
                    builder.append(line).append("\n");
                }
                JSONTokener tokener = new JSONTokener(builder.toString());
                jsonResponse = new JSONObject(tokener);

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Object o) {
            super.onPostExecute(o);
            onResponse(response, jsonResponse);
        }
    }

}
