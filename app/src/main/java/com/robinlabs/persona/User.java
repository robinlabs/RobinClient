package com.robinlabs.persona;

import android.content.Context;
import android.content.SharedPreferences;

import com.magnifis.parking.App;
import com.magnifis.parking.Config;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by oded on 5/7/14.
 */
public class User extends JSONObject {

    public final static String USER_OBJECT = "user";
    public final static String USER_ID = "user_id";

    public final static String FULL_NAME = "FULL_NAME";
    public final static String NICK_NAME = "NICK_NAME";

    public final static String COMMUTE = "COMMUTE";
    public final static String LOCATION = "LOCATION";
    public final static String TIME = "TIME";
    public final static String NAME = "NAME";

    public static SharedPreferences localUserData;
    public static String USER_ID_SHARED_PREF = "USER_ID_SHARED_PREF";

    public User() {
        super();
    }

    public final static boolean isPersonaOn = Config.is_persona_on;

    public User(String jsonString) throws JSONException {
        super(jsonString);
    }

    static {
        localUserData = App.self.getSharedPreferences("local_user_data", Context.MODE_PRIVATE);
    }


    public void save() {
        Sync.saveToServer(this);
    }

    public void getFromServer() {
        Sync.getFromServer();
    }


    static String getServerUserId() {
        return App.self.android_id;
    }
}
