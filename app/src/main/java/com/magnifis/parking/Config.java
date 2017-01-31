package com.magnifis.parking;

public class Config {
    final public
    static boolean
            vwversion=false,
            oldmic=false,
            bt=true,
            gmail_only=false,
            samsungversion=false,
            floatingButton=true,
            neo_phonebook_matcher=true,
            double_toast=false,
            debug=false,
            new_style_vip_advancing=true,
            vip_contact_red=debug,
            inject_concatenation_in_matcher=false,
            allow_sms_dialogs_while_phone_is_locked=true,
            allow_to_reply_sms_multiple_times=true,
            dont_notify_on_calls_in_silent_or_vibro_mode=true,
            use_sms_notification_service=false,
            use_sms_notification_app_suzie=true,
            use_mag_toasts=false,
            use_mag_sms_toasts=true,
            use_mag_call_toasts=true,
            bubles=true,
            use_itro=true,
            split_search_result_by_semicolon=true,
            use_google_online_voice_in_russian=true,
            car_mode_system=false,
            billing=false,   // last version
            good_phonetic_search_result_has_more_priority_than_associations=true,
            rotate_map = false, // to enable map rotation change in the manifest MainActivity.screenOrientation="portrait"
            new_teaching = false, // oded;
            is_persona_on = false,
            roku_version = false,
            
            isRobinApiV2 = true, 
            
            capture_audio_data_if_possible = true,
            show_nce_place=false
            
            ;
    ;

    final public static String locale=null;//"ru";
    final public static String hiddenPrefs[] = {
            "smsExcludeSignature",
            "keepScreenAwake"
    };

}
