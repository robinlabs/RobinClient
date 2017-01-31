package com.magnifis.parking.utils;

import android.content.Intent;
import android.net.Uri;

import com.magnifis.parking.App;
import com.magnifis.parking.YoutubeActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by oded on 6/23/14.
 */
public abstract class ClientParser {

    public ClientParser(String query) {
        parse(query);
    }

    public abstract void run(Uri uri, String text);

    private void parse(String query) {

        query = query.toLowerCase();
        query = query.replace("roku", "");
        query = query.replace("rocu", "");
        query = query.replace("roka", "");
        query = query.replace("roca", "");
        query = query.replace("procol", "");

        if (query.contains("play") || query.contains("player")) {

            query = query.replace("player", "");
            query = query.replace("play", "");

            if (App.self.parserContext != null) {
                if (null == App.self.parserContext.selectionMap) return;
                if (!App.self.parserContext.isContextRelevant()) return;

                for (Map.Entry<String, String> entry : App.self.parserContext.selectionMap.entrySet()) {
                    if (entry.getKey().toLowerCase().trim().contains(query.toLowerCase().trim())) {
                        String url = entry.getValue();

                        String say = "Playing " + query;

                        if (url == null || url.length() < 4) {
                            url = fallBackUrl + entry.getKey();
                            say = "No trailer on freebase, let me try youtube";
                        }

                        callRunWithUrlOrString(url, say);
                        return;
                    }
                }

            }

            callRunWithUrlOrString(fallBackUrl + query, null);
            return;
        }


        String movieLocation = "filmed in";
        if (query.contains(movieLocation)) {
            int start = query.indexOf(movieLocation);
            query = query.substring(start, query.length());
            query = query.replace(movieLocation, "");
            callRunWithUrlOrString("http://www.imdb.com/search/title?locations=" + query, null);
            return;
        }

        if (query.contains("like") || query.contains("friends")) {

            if (query.contains("movie")) {
                callRunWithUrlOrString("https://www.facebook.com/search/me/friends/pages-liked/movie/pages/intersect", null);
                return;
            }

            if (query.contains("show")) {
                callRunWithUrlOrString("https://www.facebook.com/search/me/friends/pages-liked/tv-show/pages/intersect", null);
                return;
            }
        }

        if (tryFreeBaseQuery(query)) {
            return;
        }


        /*String genresString = "action,adventure,animation,biography,comedy,crime,documentary,drama,family,fantasy,film_noir,game_show,history,horror,music,musical,mystery,news,reality_tv,romance,sci_fi,sport,talk_show,thriller,war,western";
        String groupsString = "top_100,top_250,top_1000,now-playing-us,oscar_winners,oscar_best_picture_winners,oscar_best_director_winners,oscar_nominees,emmy_winners,emmy_nominees,golden_globe_winners,golden_globe_nominees,razzie_winners,razzie_nominees,national_film_registry,bottom_100,bottom_250,bottom_1000";
        String[] genres = genresString.split(",");
        String[] groups = groupsString.split(",");

        for (String genre : genres) {
            if(query)
        }

        for (String word : query_words) {
            for genre in genres:
            if word in genre:
            selected_genres.append(genre)
            for group in groups:
            if word in group:
            selected_groups.append(group)
        }*/


        //default
        callRunWithUrlOrString(fallBackUrl + query, null);
    }

    private boolean tryFreeBaseQuery(String query) {

        String role = null;
        String freeBaseQuery = null;

        if (query.contains("directed by")) {
            query = query.substring(query.indexOf("directed by"), query.length());
            query = query.replace("directed by", "");
            role = "directed_by";
            freeBaseQuery = "[{\"" + role + "\": \"" + query + "\",\"name\": null,\"mid\": null,\"type\": \"/film/film\",\"trailers\": [],\"initial_release_date\": null"
            		+ "}]";// + ", \"sort\": \"-initial_release_date\"}]";
        } else if (query.contains("produced by")) {
            query = query.substring(query.indexOf("produced by"), query.length());
            query = query.replace("produced by", "");
            role = "produced_by";
            freeBaseQuery = "[{\"" + role + "\": \"" + query + "\",\"name\": null,\"mid\": null,\"type\": \"/film/film\",\"trailers\": [],\"initial_release_date\": null"
                    + "}]"; // +", \"sort\": \"-initial_release_date\"}]";
        } else if (query.contains("featuring")) {
            freeBaseQuery = runFreeBaseActorSearch(query, "featuring");
        } else if (query.contains("starring")) {
            freeBaseQuery = runFreeBaseActorSearch(query, "starring");
        } else if (query.contains("with")) {
            freeBaseQuery = runFreeBaseActorSearch(query, "with");
        } else {
            return false;
        }

        String jsonString;
        try {
            String encodedQuery = URLEncoder.encode(freeBaseQuery
                    , "utf-8");
            HttpResponse response = (new DefaultHttpClient()).execute(new HttpGet(new URI("https://www.googleapis.com/freebase/v1/mqlread?query=" + encodedQuery)));
            HttpEntity entity = response.getEntity();
            jsonString = EntityUtils.toString(entity, "UTF-8");

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        }

        JSONObject json = null;
        JSONArray jsonArray = null;
        try {
            json = new JSONObject(jsonString);
            jsonArray = json.getJSONArray("result");
        } catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
        String list = "";
        String html = "";
        App.self.parserContext = new ParserContext();
        App.self.parserContext.selectionMap = new HashMap<String, String>();

        int nMax = jsonArray.length();
        if (query.contains("last") || query.contains("newest") || query.contains("most recent"))
            nMax = 1; // just a single item

        for (int i = 0; i < nMax; i++) {
            String name = jsonArray.optJSONObject(i).optString("name");
            String initial_release_date = jsonArray.optJSONObject(i).optString("initial_release_date");

            if (Utils.isEmpty(initial_release_date) || initial_release_date.equalsIgnoreCase("null"))
                continue;

            String trailer = jsonArray.optJSONObject(i).optJSONArray("trailers").optString(0);
            String mid = jsonArray.optJSONObject(i).optString("mid");
            list += name + ".\n";
            html += generateHtmlTag(name + ".\n", mid + ".\n", initial_release_date + ".\n", trailer + ".\n");

            App.self.parserContext.selectionMap.put(name, trailer);
        }

        if (list.length() < 1) {
            return false;
        }

        callRunWithUrlOrString(null, list);

        Intent it = new Intent(App.self, YoutubeActivity.class);
        String finalHtml = getHtmlHead() + html + getHtmlFooter();
        it.putExtra("HTML", finalHtml);
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        App.self.startActivity(it);

        return true;
    }

    private String runFreeBaseActorSearch(String query, String keyword) {
        query = query.substring(query.indexOf(keyword), query.length());
        query = query.replace(keyword, "");
//            freeBaseQuery = "[{\"name\": null,\"type\": \"/film/film\",\"/film/film/starring\": [{\"actor\": \"" + query + "\"}]}]";
        return "[{ \"name\": null, \"mid\": null, \"type\": \"/film/film\", \"trailers\": [], \"initial_release_date\": null, \"/film/film/starring\": [{ \"actor\": \"" + query + "\" }],"
                + "\"sort\": \"-initial_release_date\"}]";
    }

    private String getHtmlFooter() {
        return "\t\t</table>\n" +
                "\t</body>\n" +
                "</html>\n";
    }

    private String getHtmlHead() {
        return "\n" +
                "<html>\n" +
                "\t<head>\n" +
                "\t\t<meta name=\"viewport\" content=\"width=device-width, user-scalable=no\">\n" +
                "\t\t<style>\n" +
                "\t\t\tbody {\n" +
                "\t\t\t\tpadding: 0px;\n" +
                "\t\t\t\tmargin: 0px;\n" +
                "\t\t\t}\n" +
                "\t\t\timg {\n" +
                "\t\t\t\tborder: 0;\n" +
                "\t\t\t}\n" +
                "\t\t\ta {\n" +
                "\t\t\t\tborder: 0;\n" +
                "\t\t\t\tcolor: black;\n" +
                "\t\t\t\ttext-decoration: none;\n" +
                "\t\t\t}\n" +
                "\t\t\ttable.x {\n" +
                "\t\t\t\tmargin-top: 10px;\n" +
                "\t\t\t\tborder-spacing: 10px;\n" +
                "\t\t\t\tborder-collapse: collapse;\n" +
                "\t\t\t}\n" +
                "\t\t\ttable.x td img {\n" +
                "\t\t\t\twidth: 60px;\n" +
                "\t\t\t\tpadding-bottom: 10px;\n" +
                "\t\t\t}\n" +
                "\t\t\ttable.x td {\n" +
                "\t\t\t\tpadding-left: 10px;\n" +
                "\t\t\t\twidth: 99%;\n" +
                "\t\t\t\tvertical-align: top;\n" +
                "\t\t\t}\n" +
                "\t\t\ttable.x td:first-child {\n" +
                "\t\t\t\twidth: 1% !important\n" +
                "\t\t\t}\n" +
                "\t\t\ttable.x td p {\n" +
                "\t\t\t\tpadding: 0px;\n" +
                "\t\t\t\tmargin: 0px;\n" +
                "\t\t\t\tmargin-top: 10px;\n" +
                "\t\t\t\tcolor: black;\n" +
                "\t\t\t\tfont-family: ProximaNovaRgBold, Arial, Helvetica, sans-serif;\n" +
                "\t\t\t}\n" +
                "\t\t\ttable.x td p:first-child {\n" +
                "\t\t\t\tpadding: 0px !important;\n" +
                "\t\t\t\tmargin: 0px !important;\n" +
                "\t\t\t\tfont-weight: bold\n" +
                "\t\t\t}\n" +
                "\t\t\th1 {\n" +
                "\t\t\t\tcolor: #ED1848;\n" +
                "\t\t\t\tfont-family: ProximaNovaRgBold, Arial, Helvetica, sans-serif;\n" +
                "\t\t\t}\n" +
                "\t\t\timg.y {\n" +
                "\t\t\t\tpadding-top: 6px;\n" +
                "\t\t\t\twidth: 60px !important;\n" +
                "\t\t\t}\t\t\n" +
                "\t\t</style>\n" +
                "\t</head>\n" +
                "\t<body>\n" +
                "\t\t<table class=x>\n"
                +
                "\t\t\t<tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<a href='http://roku.com'><img class=y src=\"http://www.roku.com/sites/all/themes/roku/images/logo.png\"></a>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<h1>Now this is TV.</h1>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n"
                ;
    }

    private String generateHtmlTag(String name, String mid, String initial_release_date, String trailer) {
//        return name + mid + initial_release_date + trailer;

        name = cleanUp(name);
        mid = cleanUp(mid);
        initial_release_date = cleanUp(initial_release_date);
        trailer = cleanUp(trailer);
        if (trailer.length() < 4) {
            trailer = fallBackUrl + name + " trailer";
        }

        return "\t\t\t<tr>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<a href=\"" + trailer + "\"><img src=\"https://www.googleapis.com/freebase/v1/image" + mid + "\"></a>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t\t<td>\n" +
                "\t\t\t\t\t<p><a href=\"" + trailer + "\">" + name + "</a></p>\n" +
                "\t\t\t\t\t<p>" + initial_release_date + "</p>\n" +
                "\t\t\t\t</td>\n" +
                "\t\t\t</tr>\n";
    }

    private String cleanUp(String tag) {
        return tag.replace(".\n", "");
    }


    private void callRunWithUrlOrString(String uriString, String text) {
        Uri uri = null;
        if (uriString != null) {
            uri = Uri.parse(uriString);
        }
        run(uri, text);
    }


    String fallBackUrl = "http://www.google.com/search?btnI&q=youtube+";
//    String fallBackUrl = "https://www.youtube.com/results?search_query=";

}
