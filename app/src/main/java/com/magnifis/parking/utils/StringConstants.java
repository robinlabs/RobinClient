package com.magnifis.parking.utils;

import com.magnifis.parking.App;
import com.magnifis.parking.Config;

/**
 * Created by oded on 2/12/14.
 */
public class StringConstants {

    private static boolean isDevMode() {
        return 
        		(App.self.robin().isDebugMode() || Config.isRobinApiV2); 
    }
    
    private static boolean isTestingMode() {
        return (App.self.robin().isTestingMode()); 
    }

    private static boolean russianProductionMode() {
        if (App.self.isInRussianMode() && !isDevMode()) {
            return true;
        }
        return false;
    }

    private static boolean russianDevMode() {
        if (App.self.isInRussianMode() && isDevMode()) {
            return true;
        }
        return false;
    }

    static boolean englishProductionMode() {
        if (!App.self.isInRussianMode() && !isDevMode()) {
            return true;
        }
        return false;
    }

    static boolean englishDevMode() {
        if (!App.self.isInRussianMode() && isDevMode()) {
            return true;
        }
        return false;
    }
    
    static boolean englishTestingMode() {
        if (!App.self.isInRussianMode() && isTestingMode()) {
            return true;
        }
        return false;
    }



    public static String understanding_url()  {

    	 if (englishDevMode()) { 
//         	return "http://dev-pi.magnifis.com:8080/robinai/api/v0"; 
         	if (Config.isRobinApiV2)
         		return "http://173.255.249.124:8080/api/v2/nlu";
         				//"http://stage-pi.magnifis.com:8080/api/v2/nlu"; 
         	return "http://dev-pi.magnifis.com:8080/magpark/mageo";
         }
         
         if (englishTestingMode()) {
         	return "http://stage-pi.magnifis.com:8080/api/v2/nlu"; 
         	//return "http://stage-pi.magnifis.com:8080/magpark/mageo";
         }

        if (russianProductionMode())
            return "http://mageo-ru.magnifis.com:81/magpark_ru/mageo";

        if (russianDevMode())
            return "http://dev-pi.magnifis.com:8080/magpark_ru/mageo";

        //if(englishProductionMode())
        return "http://mageo.magnifis.com/magpark/mageo";
    }

    public static String traffic_url() {

        if (isDevMode())
            return "http://173.255.249.124:8080/magtraffic/traffic_service";

        if (russianProductionMode())
            return "http://traffic-ru.magnifis.com:81/magtraffic/traffic_service";

        //if(englishProductionMode())
        return "http://traffic.magnifis.com/magtraffic/traffic_service";
    }

    public static String poi_url() {

        if (isDevMode())
            return "http://173.255.249.124:8080/magpoi/poi_service";

        if (russianProductionMode())
            return "http://magpoi-ru.magnifis.com:81/magpoi/poi_service";

        //if(englishProductionMode())
        return "http://magpoi.magnifis.com/magpoi/poi_service";
    }

    public static String parking_url() {

        if (isDevMode()) {
          return "http://173.255.249.124:8080/magdelegate/parking_service";
        }

        if (russianProductionMode())
            return "http://parking-ru.magnifis.com:81/magdelegate/parking_service";

        //if(englishProductionMode())
        return "http://parking.magnifis.com/magdelegate/parking_service";
    }

	public static String speech_audio_logger_url() {
		//return "http://54.187.243.38:7010/api/speech-upload";
		return "http://54.186.62.99:7010/api/speech-upload"; 
	}

}

//todo: back up
/*

english

<string name="understanding_url">http://173.255.249.124:8080/magpark/mageo</string>
<string name="traffic_url">http://173.255.249.124:8080/magtraffic/traffic_service</string>
<string name="poi_url">http://173.255.249.124:8080/magpoi/poi_service</string>
<string name="parking_url">http://173.255.249.124:8080/magdelegate/parking_service</string>

    <!-- <string name="understanding_url">http://173.255.249.124:8080/maglab/mageo</string> -->

<string name="understanding_url">http://mageo.magnifis.com/magpark/mageo</string>
<string name="parking_url">http://parking.magnifis.com/magdelegate/parking_service</string>
<string name="poi_url">http://magpoi.magnifis.com/magpoi/poi_service</string>
<string name="traffic_url">http://traffic.magnifis.com/magtraffic/traffic_service</string>


russian

<string name="understanding_url">http://173.255.249.124:8080/magpark_ru/mageo</string>
<string name="traffic_url">http://173.255.249.124:8080/magtraffic/traffic_service</string>
<string name="poi_url">http://173.255.249.124:8080/magpoi/poi_service</string>
<string name="parking_url">http://173.255.249.124:8080/magdelegate/parking_service</string>

<string name="understanding_url">http://mageo-ru.magnifis.com:81/magpark_ru/mageo</string>
<string name="parking_url">http://parking-ru.magnifis.com:81/magdelegate/parking_service</string>
<string name="poi_url">http://magpoi-ru.magnifis.com:81/magpoi/poi_service</string>
<string name="traffic_url">http://traffic-ru.magnifis.com:81/magtraffic/traffic_service</string>


*/