/**
 * 
 */
package com.magnifis.parking;

import android.content.SearchRecentSuggestionsProvider;

/**
 * @author zeev
 *
 */
public class TheRecentSuggestionsProvider extends
		SearchRecentSuggestionsProvider {
	
	final public static String AUTHORITY="com.magnifis.parking.search";
	final public static int MODE=DATABASE_MODE_QUERIES;
	
	public TheRecentSuggestionsProvider() {
		 setupSuggestions(AUTHORITY, MODE);
	}

}
