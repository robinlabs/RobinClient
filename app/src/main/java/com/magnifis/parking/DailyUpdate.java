package com.magnifis.parking;

import android.accounts.Account;
import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.Browser;
import android.provider.CalendarContract;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.text.format.DateFormat;
import android.text.format.DateUtils;

import com.magnifis.parking.pref.PasswordPreference;
import com.magnifis.parking.utils.ValueSortedMap;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.mail.Address;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.InternetAddress;

import static com.magnifis.parking.VoiceIO.sayAndShow;

/**
 * Created by oded on 1/14/14.
 */
public class DailyUpdate {

    private final Robin robin;
    Context context;
    private String userName;


    DailyUpdate(Context context) {
        this.context = context;
        this.robin = new Robin();
        this.userName = robin.getNameOfUser();
    }

    public String playAgenda() {

        String wait = ". . \n\n\n";

        List<String> groups = new ArrayList<String>();

        groups.add(getMissedCallsYouDidNotReply());
        groups.add(getCalendarEvents());
        groups.add(getSmsString());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            groups.add(DailyNotificationListener.getAll(context));
        }

//        groups.add(getRecentCalls(true));//missed only
//        groups.add(getRecentCalls(false));

        String textToSpeak = App.self.getString(R.string.ok_confirm) + " " + userName + App.self.getString(R.string.scan_start);
        for (String group : groups) {
            if (group == null) continue;

            sayAndShow(group);

            textToSpeak += group + wait;
        }
        textToSpeak += App.self.getString(R.string.recap_done);

        textToSpeak = cleanStringsThatAreNotReadable(textToSpeak);

//        sayAndShow(textToSpeak);

        getGmail();
        getNews();

        return textToSpeak;
    }

    public void getNews() {
        (new NewsReader()).multiExecute();
    }

    public void getGmail() {
        (new GmailReader()).multiExecute();
    }

    private String cleanStringsThatAreNotReadable(String textToSpeak) {
//        textToSpeak = textToSpeak.replace();

        return textToSpeak;
    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    String getCalendarEvents() {
        //return if sdk < 14
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            return null;

        ValueSortedMap<String, Long> valueSortedMap = new ValueSortedMap<String, Long>();

        String resultString = null;

        long startDate = System.currentTimeMillis() - 3600000;
        long endDate = System.currentTimeMillis() + 3600000 * 24 * 7;
        String[] columns = new String[]{CalendarContract.Instances.TITLE,
                CalendarContract.Instances.EVENT_LOCATION,
                CalendarContract.Instances.BEGIN,
                CalendarContract.Instances.END};

        Uri.Builder eventsUriBuilder = CalendarContract.Instances.CONTENT_URI
                .buildUpon();
        ContentUris.appendId(eventsUriBuilder, startDate);
        ContentUris.appendId(eventsUriBuilder, endDate);
        Uri eventsUri = eventsUriBuilder.build();
        Cursor cur = null;
        cur = context.getContentResolver().query(eventsUri, columns, null, null, CalendarContract.Instances.BEGIN + " ASC");

        while (cur.moveToNext()) {
            String location = cur.getString(cur.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION));
            String name = cur.getString(cur.getColumnIndex(CalendarContract.Instances.TITLE));
            Long date = cur.getLong(cur.getColumnIndex(CalendarContract.Instances.BEGIN));

            if (location == null || name.contains(location)) {
                location = "";
            } else {
                location = String.format(App.self.getString(R.string.meeting_entry), location);
            }

            valueSortedMap.put(name + " " + location, date);
        }

        cur.close();

        for (String event : valueSortedMap.keySetAscending()) {

            long date = valueSortedMap.get(event);
            String stringDate;

            if (date < System.currentTimeMillis() + 1000 * 3600) {
                stringDate = DateUtils.getRelativeTimeSpanString(date, System.currentTimeMillis(), 0L).toString();
            } else if (isToday(date)) {
                stringDate = App.self.getString(R.string.today_on) + DateFormat.format("HH", new Date(date)).toString();
            } else if (isTomorrow(date)) {
                stringDate = App.self.getString(R.string.tomorrow_on) + DateFormat.format("HH", new Date(date)).toString();
            } else if (isNextWeek(date)) {
                stringDate = App.self.getString(R.string.on_) + " " + DateFormat.format("EEEE", new Date(date)).toString() + " next week";
            } else {
                stringDate = App.self.getString(R.string.on_) + " " + DateFormat.format("EEEE", new Date(date)).toString();
            }

            if (resultString == null) {

                if (valueSortedMap.size() > 7) {
                    resultString = App.self.getString(R.string.busy_week);
                } else {
                    resultString = App.self.getString(R.string.light_schedule);
                }
            }

            resultString += event + " " + stringDate + ".. \n\n";
        }

        return resultString;
    }


    String getMissedCallsYouDidNotReply() {

        String resultString = null;

        ValueSortedMap<String, Long> callMap = new ValueSortedMap<String, Long>();
        Set<String> notMissed = new HashSet<String>();

        PackageManager pm = context.getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) return null;

        long now = System.currentTimeMillis();
        long weekAgo = now - 1000 * 3600 * 24 * 7;

        Cursor cursor = getPhonesCursor(weekAgo, now);

        while (cursor.moveToNext()) {
            String name = (cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
            if (name == null) continue;

            int callDirection = (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)));
            int duration = (cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)));

            if (callDirection != CallLog.Calls.MISSED_TYPE
                    && duration > 5) {
                notMissed.add(name);
            } else {
                if (!notMissed.contains(name)) {
                    Long date = (cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)));
                    if (date == null) continue;
                    callMap.put(name, date);
                }
            }


        }

        cursor.close();

        if (callMap.size() < 1) {
            return null;
        }

        if (callMap.size() < 2) {
            String name = (String) callMap.keySetAscending().toArray()[0];
            return name + App.self.getString(R.string.call_back) +
                    DateUtils.getRelativeTimeSpanString(callMap.get(name), System.currentTimeMillis(), 0L).toString();
        }

        for (String name : callMap.keySetDescending()) {

            long date = callMap.get(name);

            String stringDate = DateUtils.getRelativeTimeSpanString(date, System.currentTimeMillis(), 0L).toString();

            if (resultString == null) {
                String callCount = App.self.getString(R.string.call_count) + " " + callMap.size();
                if (callMap.size() > 5) {
                    callCount = App.self.getString(R.string.call_popular) + callCount;
                }

                resultString = App.self.getString(R.string.people_to_call) + callCount + " " + App.self.getString(R.string.call_didnt_return);
            }

            resultString += name + " " + App.self.getString(R.string._from_) + " " + stringDate + ".. \n\n";
        }

        return resultString;
    }

    private Cursor getPhonesCursor(long startDate, long endDate) {

        Uri queryUri = android.provider.CallLog.Calls.CONTENT_URI;
        String selection = CallLog.Calls.DATE + ">? and " + CallLog.Calls.DATE + "<?";
        String[] selectionArgs = new String[]{String.valueOf(startDate), String.valueOf(endDate)};
        String sortOrder = CallLog.Calls.DATE + " DESC";

        Cursor c = context.getContentResolver().query(
                queryUri,
                getPhoneProjection(),
                selection,
                selectionArgs,
                sortOrder);
        return c;
    }

    String[] getPhoneProjection() {
        String[] projection = new String[]{
                ContactsContract.Contacts._ID,
                CallLog.Calls._ID,
                CallLog.Calls.NUMBER,
                CallLog.Calls.CACHED_NAME,
                CallLog.Calls.CACHED_NUMBER_TYPE,
                CallLog.Calls.DATE,
                CallLog.Calls.DURATION,
                CallLog.Calls.TYPE
        };

        return projection;
    }

    public List<String> getAllSms() {
        List<String> msgs = new ArrayList<String>();

        List<UpdateItem> msgList = new ArrayList<UpdateItem>();

        Cursor cursor = context.getContentResolver().query(Uri.parse("content://sms/inbox"), null, null, null, null);
        if (cursor.moveToFirst())
            do {
                UpdateItem updateItem = new UpdateItem();

                for (int idx = 0; idx < cursor.getColumnCount(); idx++) {

                    String columnName = cursor.getColumnName(idx);
                    if (columnName.equals(Telephony.Sms.Inbox.DATE)) {
                        updateItem.sentDate = cursor.getLong(idx);
                    }

                    if (columnName.equals(Telephony.Sms.Inbox.BODY)) {
                        updateItem.body = cursor.getString(idx);
                    }

                    if (columnName.equals(Telephony.Sms.Inbox.ADDRESS)) {
                        updateItem.senderAddress = cursor.getString(idx);
                    }

//                msgData += " " + cursor.getColumnName(idx) + ":" + cursor.getString(idx);
                }

                msgList.add(updateItem);
            } while (cursor.moveToNext()
//                && date > 0
//                && date > (System.currentTimeMillis() - 86400000)
                    );

        for (UpdateItem t : msgList) {

            if (t == null) continue;
            if (t.senderAddress == null) continue;

            if (t.senderAddress.length() < 10) continue;
            if (t.sentDate < (System.currentTimeMillis() - 1 * 86400000)) continue;

            msgs.add(t.body);
        }

        return msgs;
    }

    String getSmsString() {
        //return if sdk < 19
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return null;

        //TODO:don't read sms from short numbers

        String resultString = null;

        List<String> list = getAllSms();
        for (String sms : list) {
            if (resultString == null) {
                resultString = App.self.getString(R.string.recent_texts);
            }
            resultString += sms + ".. ";
        }

        return resultString;
    }


    private Message[] getEmails() throws MessagingException {

        Account ac = App.self.getGmailAccount();
        String accountName = ac.name;
//        String passKey = App.self.getStringPref("gmailPassword");
//        String password= PasswordPreference.getDecoded(ms.getPasswordPrefKey());
        String pass = PasswordPreference.getDecoded("gmailPassword");

        Properties props = new Properties();
        //IMAPS protocol
        props.setProperty("mail.store.protocol", "imaps");
        //Set host address
        props.setProperty("mail.imaps.host", "imaps.gmail.com");
        //Set specified port
        props.setProperty("mail.imaps.port", "993");
        //Using SSL
        props.setProperty("mail.imaps.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.setProperty("mail.imaps.socketFactory.fallback", "false");
        //Setting IMAP session
        Session imapSession = Session.getInstance(props);

        Store store = imapSession.getStore("imaps");
        //Connect to server by sending username and password.
        //Example mailServer = imap.gmail.com, username = abc, password = abc
        String mailServer = "imap.gmail.com";
        store.connect(mailServer, accountName, pass);
        //Get all mails in Inbox Forlder
        Folder inbox = store.getFolder("Inbox");
        inbox.open(Folder.READ_ONLY);
        //Return result to array of message
        Message[] result = inbox.getMessages();
        return result;
    }

    private String formatEmailListToReadableString(Message[] emails) throws MessagingException, IOException {

        ValueSortedMap<String, Long> map = new ValueSortedMap<String, Long>();

        String result = null;

        for (Message m : emails) {

            if (result == null) {
                result = App.self.getString(R.string.read_your_email);
            }

            Address[] from = m.getFrom();
            if (from == null) continue;
            if (from[0] == null) continue;
            InternetAddress ia = (InternetAddress) from[0];

            String senderName = ia.getPersonal();
            String subject = m.getSubject();
            String conjunction = App.self.getString(R.string._wrote_);

            if (senderName == null && subject == null) continue;

            if (senderName == null) senderName = App.self.getString(R.string.someone);

            if (subject == null) subject = App.self.getString(R.string.no_subject_mail);

            subject = subject.replace("Re:", "").replace("Fw:", "").replace("re:", "").replace("fw:", "").replace("'", "");

            /*
            result += senderName;
            result += conjunction;
            result += subject
            result += ".. \n\n";
            */

            String entry = (senderName + " " + conjunction + " " + subject).replace("  ", " ");

            map.put(entry, m.getReceivedDate().getTime());

            /*
            String fullBody = MailFeedController.extractBodyText(m.getContent(), m.getContentType());
            String body = mailFeedController.filterMsgHistory(fullBody);

            if (body != null) {
                result += " : ";
                result += stripHtml(body);
            }
            */

        }

        for (String m : map.keySetDescending()) {
            result += m;
            result += ".. \n\n";
        }

        return result;
    }

    boolean isToday(long date) {
        Calendar c1 = Calendar.getInstance(); // today

        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(date); // your date

        if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
            return true;
        }
        return false;
    }

    boolean isTomorrow(long date) {
        Calendar c1 = Calendar.getInstance(); // today
        c1.add(Calendar.DAY_OF_YEAR, 1);

        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(date); // your date

        if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR)) {
            return true;
        }
        return false;
    }

    boolean isNextWeek(long date) {
        Calendar c1 = Calendar.getInstance(); // today
        c1.add(Calendar.WEEK_OF_YEAR, 1);

        Calendar c2 = Calendar.getInstance();
        c2.setTimeInMillis(date); // your date

        if (c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR)) {
            return true;
        }
        return false;
    }

    String getBrowserHistory() {

        String result = null;
        List<String> resultsList = new ArrayList<String>();
        String[] proj = new String[]{Browser.BookmarkColumns.TITLE, Browser.BookmarkColumns.URL, Browser.BookmarkColumns.DATE};
        String sel = Browser.BookmarkColumns.BOOKMARK + " = 0"; // 0 = history, 1 = bookmark
        String limit = "1000";
        String sortOrder = String.format("%s limit " + limit + " ", Browser.BookmarkColumns.DATE + " DESC");
        Cursor mCur = context.getContentResolver().query(Browser.BOOKMARKS_URI, proj, sel, null, sortOrder);
        if (mCur == null) return null;

        mCur.moveToFirst();
        String title = "";
        String url = "";
        if (mCur.moveToFirst() && mCur.getCount() > 0) {
            boolean cont = true;
            while (mCur.isAfterLast() == false && cont) {
                title = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.TITLE));
                url = mCur.getString(mCur.getColumnIndex(Browser.BookmarkColumns.URL));
                // Do something with title and url

                if (title.contains("news")
                        || url.contains("news")
                        || title.contains("article")
                        || url.contains("article")) {
                    resultsList.add(title);
                }

                mCur.moveToNext();
            }
        }


        /*int min = 0;
        int max = resultsList.size()- 1;
        int rand = min + (int) (Math.random() * ((max - min) + 1));*/
        Set<String> content = getFreshContent(resultsList);

        for (String newsItem : content) {

            if (result == null) {
                result = App.self.getString(R.string.interesting_news);
            }

            result += newsItem + ".. \n\n";
        }

        return result;
    }

    public Set<String> getFreshContent(List<String> queryStrings) {

        Set<String> titles = new HashSet<String>();

        Set<String> urlStrings = new HashSet<String>();

        for (String queryString : queryStrings) {

            String query = null;
            try {
                query = URLEncoder.encode(queryString, "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            urlStrings.add("https://news.google.com/news/feeds?output=rss&q=" + query);
        }

        for (String s : urlStrings) {

            Document doc = null; // connect to url and parse its content into a document
            try {
                doc = Jsoup.connect(s).get();
            } catch (IOException e) {
                e.printStackTrace();
                return titles;
            }

            Elements listItems = doc.select("item");
            /*for (Element item : listItems) {
                Element title = item.select("title").first();
                titles.add(title.text());
            }*/
            for (Element item : listItems) {
                int count = 0;

                if (count > 1) break;

                Element title = item.select("title").first();
                if (title != null) {
                    titles.add(title.text());
                    count++;
                }
            }

            if (titles.size() > 9) return titles;
        }
        return titles;
    }

    class UpdateItem {
        String senderAddress;
        String body;
        long sentDate;
    }

    private class GmailReader extends MultiAsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String read = null;
            try {
                Message[] emails = getEmails();

                read = formatEmailListToReadableString(emails);
            } catch (MessagingException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return read;
        }

        @Override
        protected void onPostExecute(String result) {
            Output.sayAndShow(context, result);
        }
    }

    private class NewsReader extends MultiAsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            Output.sayAndShow(context, App.self.getString(R.string.please_wait));
        }

        @Override
        protected String doInBackground(String... params) {
            String read = getBrowserHistory();
            return read;
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                Output.sayAndShow(context, result);
            }
        }
    }


}
