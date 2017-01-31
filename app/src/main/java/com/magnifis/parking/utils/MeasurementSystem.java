package com.magnifis.parking.utils;

import static com.robinlabs.utils.BaseUtils.isEmpty;

import java.util.TimeZone;

import com.magnifis.parking.App;

public class MeasurementSystem {

	/***
	 * 
	 *  Fahrenheit is used in the United States, Belize, Palau and
         the United States territories of Puerto Rico,
          Guam and the U.S. Virgin Islands 
	 * 
	 *  tzid=America/Anchorage    // Alaska
	 *  tzid=America/Los_Angeles  // Pacific
	 *  tzid=America/Phoenix      // Mountain, Arizona
	 *  tzid=America/Chicago      // Central
     *  tzid=America/New_York     // Eastern, Pelau
     *  tzid=Atlantic/South_Georgia // Mid-Atlantic
     *  tzid=Pacific/Guam
     *  tzid=America/Puerto_Rico,  SystemV/AST4
     *  tzid=America/Belize
     *   tzid=America/St_Thomas, America/Virgin
     *  
     United States (US)
    tzid: America/Adak
        alias: America/Atka
        alias: US/Aleutian
    tzid: America/Anchorage
        alias: SystemV/YST9YDT
        alias: US/Alaska
    tzid: America/Boise
    tzid: America/Chicago
        alias: CST6CDT
        alias: SystemV/CST6CDT
        alias: US/Central
    tzid: America/Denver
        alias: MST7MDT
        alias: Navajo
        alias: SystemV/MST7MDT
        alias: US/Mountain
    tzid: America/Detroit
        alias: US/Michigan
    tzid: America/Indiana/Knox
        alias: America/Knox_IN
        alias: US/Indiana-Starke
    tzid: America/Indiana/Marengo
    tzid: America/Indiana/Vevay
    tzid: America/Indianapolis
        alias: America/Fort_Wayne
        alias: America/Indiana/Indianapolis
        alias: EST
        alias: SystemV/EST5
        alias: US/East-Indiana
    tzid: America/Juneau
    tzid: America/Kentucky/Monticello
    tzid: America/Los_Angeles
        alias: PST8PDT
        alias: SystemV/PST8PDT
        alias: US/Pacific
        alias: US/Pacific-New
    tzid: America/Louisville
        alias: America/Kentucky/Louisville
    tzid: America/Menominee
    tzid: America/New_York
        alias: EST5EDT
        alias: SystemV/EST5EDT
        alias: US/Eastern
    tzid: America/Nome
    tzid: America/North_Dakota/Center
    tzid: America/Phoenix
        alias: MST
        alias: SystemV/MST7
        alias: US/Arizona
    tzid: America/Shiprock
    tzid: America/Yakutat
    tzid: Pacific/Honolulu
        alias: HST
        alias: SystemV/HST10
        alias: US/Hawaii
United States Minor Outlying Islands (UM)
    tzid: Pacific/Johnston
    tzid: Pacific/Midway
    tzid: Pacific/Wake
     *  
     *  see also @link http://mm.icann.org/pipermail/tz/2005-February/012842.html
     *  see @link http://userpage.chemie.fu-berlin.de/diverse/doc/ISO_3166.html
	 */
	private static String fhCountryIsoCodes[]={
	 "us", "usa",
	 "um", "umi", /*UNITED STATES MINOR OUTLYING ISLANDS*/
	 "vi", "vir", // VIRGIN ISLANDS (U.S.) 
	 "pw", "plw",  // palau,
	 "bz", "blz",
	 "pr", "pri"
	};
	
	private static String imperialCountryIsoCodes[]={
		 "us", "usa",
		 "um", "umi", /*UNITED STATES MINOR OUTLYING ISLANDS*/
		 "vi", "vir", // VIRGIN ISLANDS (U.S.) 
		 "lr", "lbr", // Liberia
		 "uk", "gb", "gbr",
		 "mm", "bu", "mmr"  // Burma, Myanmar
	};
	
	private static String fhTimeZone[]={
	    "America/Adak",
        "America/Atka",
        "US/Aleutian",
        "America/Anchorage",
        "SystemV/YST9YDT",
        "US/Alaska",
        "America/Boise",
        "America/Chicago",
        "CST6CDT",
        "SystemV/CST6CDT",
        "US/Central",
        "America/Denver",
        "MST7MDT",
        "Navajo",
        "SystemV/MST7MDT",
        "US/Mountain",
        "America/Detroit",
        "US/Michigan",
        "America/Indiana/Knox",
        "America/Knox_IN",
        "US/Indiana-Starke",
        "America/Indiana/Marengo",
        "America/Indiana/Vevay",
        "America/Indianapolis",
        "America/Fort_Wayne",
        "America/Indiana/Indianapolis",
        "EST",
        "SystemV/EST5",
        "US/East-Indiana",
        "America/Juneau",
    "America/Kentucky/Monticello",
    "America/Los_Angeles",
        "PST8PDT",
        "SystemV/PST8PDT",
        "US/Pacific",
        "US/Pacific-New",
    "America/Louisville",
    "America/Kentucky/Louisville",
    "America/Menominee",
    "America/New_York",
    "EST5EDT",
    "SystemV/EST5EDT",
    "US/Eastern",
    "America/Nome",
    "America/North_Dakota/Center",
    "America/Phoenix",
    "MST",
    "SystemV/MST7",
    "US/Arizona",
    "America/Shiprock",
    "America/Yakutat",
    "Pacific/Honolulu",
    "HST",
    "SystemV/HST10",
    "US/Hawaii",
    "Pacific/Johnston",
    "Pacific/Midway",
    "Pacific/Guam",
    "America/Puerto_Rico",
    "SystemV/AST4",
    "America/Belize",
    "America/St_Thomas",
    "America/Virgin"
	};
	
	private static String imperialTimeZone[]={
	    "America/Adak",
        "America/Atka",
        "US/Aleutian",
        "America/Anchorage",
        "SystemV/YST9YDT",
        "US/Alaska",
        "America/Boise",
        "America/Chicago",
        "CST6CDT",
        "SystemV/CST6CDT",
        "US/Central",
        "America/Denver",
        "MST7MDT",
        "Navajo",
        "SystemV/MST7MDT",
        "US/Mountain",
        "America/Detroit",
        "US/Michigan",
        "America/Indiana/Knox",
        "America/Knox_IN",
        "US/Indiana-Starke",
        "America/Indiana/Marengo",
        "America/Indiana/Vevay",
        "America/Indianapolis",
        "America/Fort_Wayne",
        "America/Indiana/Indianapolis",
        "EST",
        "SystemV/EST5",
        "US/East-Indiana",
        "America/Juneau",
    "America/Kentucky/Monticello",
    "America/Los_Angeles",
        "PST8PDT",
        "SystemV/PST8PDT",
        "US/Pacific",
        "US/Pacific-New",
    "America/Louisville",
    "America/Kentucky/Louisville",
    "America/Menominee",
    "America/New_York",
    "EST5EDT",
    "SystemV/EST5EDT",
    "US/Eastern",
    "America/Nome",
    "America/North_Dakota/Center",
    "America/Phoenix",
    "MST",
    "SystemV/MST7",
    "US/Arizona",
    "America/Shiprock",
    "America/Yakutat",
    "Pacific/Honolulu",
    "HST",
    "SystemV/HST10",
    "US/Hawaii",
    "Pacific/Johnston",
    "Pacific/Midway",
    "Pacific/Guam",
    "America/Virgin",
    ///////
    "Africa/Monrovia",
    "Asia/Rangoon",
    
    "Europe/Belfast",
    "Europe/London",
    "GB",
    "GB-Eire"
	};
	
	public static boolean detectIfTempInCelsius() {
		String c=App.self.getSimCountryIso();
		if (!isEmpty(c)) {
		  if (Utils.indexOf(fhCountryIsoCodes, c.toLowerCase())>=0) return false;
		} else {
		  TimeZone tz=TimeZone.getDefault();
		  String tzid=tz.getID();
		  if (!isEmpty(tzid))
			  if (Utils.indexOf(fhTimeZone, tzid)>=0) return false; 
		}
		return true;
	}
	
	public static boolean detectIfMetricSystem() {
		String c=App.self.getSimCountryIso();
		if (!isEmpty(c)) {
		  if (Utils.indexOf(imperialCountryIsoCodes, c.toLowerCase())>=0) return false;
		} else {
		  TimeZone tz=TimeZone.getDefault();
		  String tzid=tz.getID();
		  if (!isEmpty(tzid))
			  if (Utils.indexOf(imperialTimeZone, tzid)>=0) return false; 
		}
		return true;
	}

}
