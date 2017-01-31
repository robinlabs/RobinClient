package com.robinlabs.persona;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by oded on 11/9/14.
 */
abstract public class PostJSON extends HttpJson {

    private String postUrl;
    private JSONObject jsonObject;

    PostJSON(String postUrl, JSONObject jsonObject) throws UnsupportedEncodingException {
        this.postUrl = postUrl;
        this.jsonObject = jsonObject;

        run();
    }

    private void run() throws UnsupportedEncodingException {
        HttpPost httpPost = new HttpPost(postUrl);

        String stringJson = jsonObject.toString();
        StringEntity se = new StringEntity(stringJson);
        se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
        httpPost.setEntity(se);

        new HttpAsyncTask(httpPost).execute();
    }
}
