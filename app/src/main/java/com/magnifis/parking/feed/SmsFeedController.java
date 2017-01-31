package com.magnifis.parking.feed;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.SmsMessage;
import android.text.TextUtils;

import com.magnifis.parking.App;
import com.magnifis.parking.Log;
import com.magnifis.parking.Output;
import com.magnifis.parking.Phrases;
import com.magnifis.parking.R;
import com.magnifis.parking.SmsStatusReceiver;
import com.magnifis.parking.SuccessFailure;
import com.magnifis.parking.TextMessageQue;
import com.magnifis.parking.db.RobinDB;
import com.magnifis.parking.messaging.Addressable;
import com.magnifis.parking.messaging.Message;
import com.magnifis.parking.messaging.PhoneAddressable;
import com.magnifis.parking.model.ContactRecord;
import com.magnifis.parking.model.DelegateAgentPhone;
import com.magnifis.parking.phonebook.PhoneBook;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Utils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.magnifis.parking.VoiceIO.sayAndShow;
import static com.magnifis.parking.utils.Utils.formatMessageDate;
import static com.magnifis.parking.utils.Utils.isEmpty;
import static com.magnifis.parking.utils.Utils.isEmptyOrBlank;

public class SmsFeedController extends MessageFeedController {


    private static WeakReference<SmsFeedController> selfWr = null;
    public static boolean pauseBeforeReading = false;

    public static SmsFeedController getInstance() {
        synchronized (SmsFeedController.class) {
            SmsFeedController fc = selfWr == null ? null : selfWr.get();
            if (fc == null) fc = new SmsFeedController(App.self);
            return fc;
        }
    }

    @Override
    public int getPageSize() {
        return 100;
    }

    private SmsFeedController(Context context) {
        super(context);
        selfWr = new WeakReference<SmsFeedController>(this);
    }

    final static String TAG = SmsFeedController.class.getSimpleName();

    final static String sms_cols[] = new String[]{
            "_id",
//		"thread_id",
            "person",
            "address",
//		"type",
            "date",
            "read", // 0,1
            "body"
    };

    final static int _idId = 0, personId = 1, addressId = 2, dateId = 3, readId = 4, bodyId = 5;

    public String getLastMessageSender() {

        String phone = null;
        Cursor cr = null;
        try {
            try {
                cr = App.self.getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        sms_cols,
                        null,
                        null,
                        "date desc"
                );
                if (cr.moveToFirst()) {
                    int cnt = 0;
                    List<Message> res = new ArrayList<Message>();
                    PhoneBook pb = PhoneBook.getInstance();

                    boolean firstIsNew = cr.getInt(readId) == 0;

                    do {

                        //Addressable sender=new PhoneAddressable();
                        phone = cr.getString(addressId);
                        if (!Utils.isEmpty(phone))
                            return phone;
                        /*
                        sender.setAddress(phone);
                		
                		List<ContactRecord> lst=pb.byPhone(phone, true);
                		if (!isEmpty(lst)) 
                			sender.setDisplayName(lst.get(0).getName());
                		
                		if (isEmpty(sender.getDisplayName()))
                			sender.setDisplayName(Utils.phoneNumberToSpeech(phone).toString());
                		
                		msg.setSender(sender);
                		Long d=cr.getLong(dateId);
                		if (d!=null) {
                		   msg.setReceived( new Date(d) );
                		}
                		
                		msg.setRead(fRead);
                		
                		res.add(msg);*/

                    } while (cr.moveToNext());
                }
            } catch (Throwable t) {
                t.printStackTrace(System.err);
            }
        } finally {
            if (cr != null) cr.close();
        }

        return null;
    }

    @Override
    public void getN(int N, String sinceId, boolean fNew, boolean exclId, SuccessFailure<List<Message>> handler) {
        String cond = sinceId == null
                ? null
                : ((N > 0
                ? (exclId ? "_id<" : "_id<=")
                : (exclId ? "_id>" : "_id>=")
        )
                + sinceId);

        Cursor cr = null;
        try {
            try {
                cr = App.self.getContentResolver().query(
                        Uri.parse("content://sms/inbox"),
                        sms_cols,
                        cond, //"type in (1,2)",
                        null,
                        (N < 0) ? "date" : "date desc"
                );
                if (cr.moveToFirst()) {
                    int cnt = 0;
                    List<Message> res = new ArrayList<Message>();
                    PhoneBook pb = PhoneBook.getInstance();
                    //SimpleDateFormat df=new SimpleDateFormat();

                    boolean firstIsNew = cr.getInt(readId) == 0;

                    do {

                        boolean fRead = cr.getInt(readId) == 1;

                        if (fNew && fRead) break;
                        if (++cnt > Math.abs(N)) break;

                        Message msg = new Message();
                        msg.setType(Message.TYPE_SMS);

                        try {
                            msg.setId(Long.toString(cr.getLong((_idId))));
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }

                        msg.setBody(cr.getString(bodyId));
                        Addressable sender = new PhoneAddressable();
                        String phone = cr.getString(addressId);
                        sender.setAddress(phone);

                        List<ContactRecord> lst = pb.byPhone(phone, true);
                        if (!isEmpty(lst))
                            sender.setDisplayName(lst.get(0).getName());

                        if (isEmpty(sender.getDisplayName(false)))
                            sender.setDisplayName(Utils.phoneNumberToSpeech(phone).toString());

                        msg.setSender(sender);
                        Long d = cr.getLong(dateId);
                        if (d != null) {
                            msg.setReceived(new Date(d));
                        }

                        msg.setRead(fRead);

                        res.add(msg);

                    } while (cr.moveToNext());

                    handler.onSuccess(res);
                    return;
                }
                handler.onSuccess(null);
            } catch (Throwable t) {
                t.printStackTrace(System.err);
                handler.onFailure();
            }
        } finally {
            if (cr != null) cr.close();
        }
    }

    private Message lastMessageRead = null;

    public Message getLastMessageRead() {
        return lastMessageRead;
    }

    @Override
    public void play(List<Message> ms, final boolean markAsRead, final String sayBefore) {
        play(ms, markAsRead, sayBefore, PLAY_MODE_NORMAL);
    }

    public final static int PLAY_MODE_NORMAL = 0, PLAY_MODE_HEADER_ONLY = 1, PLAY_MODE_BODY_ONLY = 2;

    public static StringBuilder getSmsPartToShow(List<Message> ms, int mode) {
        final StringBuilder sb = new StringBuilder();

        if (isEmpty(ms)) return null;
        Message m0 = ms.get(0);
         
        if (mode != PLAY_MODE_BODY_ONLY) {
            Addressable ab = m0.getSender();
            String displayName = ab.getDisplayName(false);
            if (!(isEmpty(displayName) || mode == PLAY_MODE_HEADER_ONLY)) {
                if (pauseBeforeReading) {
                    pauseBeforeReading = false;
                    sb.append(displayName);
                    sb.append(" " + Phrases.pickSmsArrivedPhrase() + " ");
                } else
                    sb.append("\n" + App.self.getString(R.string.sms_message_from) + " " + displayName + ".\n");
            } else {
                sb.append(App.self.getString(R.string.sms_message_from) + " ");
                String dn = displayName; 
                sb.append(isEmpty(dn) ? ab.getAddress() : dn);
                sb.append('\n');
                if (m0.getReceived() != null) {
                    sb.append(App.self.getString(R.string.sms_message_received) + " ");
                    sb.append(formatMessageDate(m0.getReceived()));
                    sb.append('\n');
                }
            }
        }

        if (!((mode == PLAY_MODE_HEADER_ONLY) || isEmpty(m0.getBody()))) {
            for (Message m : ms) sb.append(m.getBody());
        }

        return sb;
    }

    public static StringBuilder getSmsPartForSpeach(List<Message> ms, int mode) {
        final StringBuilder sb = new StringBuilder();

        if (isEmpty(ms)) return null;
        Message m0 = ms.get(0);

        if (mode != PLAY_MODE_BODY_ONLY) {
            Addressable ab = m0.getSender();
            String displayName = ab.getDisplayName(true);
            if (!(isEmpty(displayName) || mode == PLAY_MODE_HEADER_ONLY)) {
                if (pauseBeforeReading) {
                    pauseBeforeReading = false;
                    sb.append(displayName);
                    sb.append(" " +  Phrases.pickSmsArrivedPhrase() + " ");
                } else
                    sb.append("\n" + App.self.getString(R.string.sms_message_from) + " " + displayName + ".\n");
            } else {
                if (true) {//App.self.shouldSpellSmsSenderPhones() || ab.hasDisplayName()) {
                    sb.append("\n" + App.self.getString(R.string.sms_message_from) + " ");
                    sb.append(ab.getSynteticDisplayName(true));
                } else {
                    if (m0.getReceived() != null) {
                        sb.append('\n');
                        sb.append(App.self.getString(R.string.sms_message));
                        sb.append(' ');
                    }
                }
                sb.append('\n');
                if (m0.getReceived() != null) {
                    sb.append(App.self.getString(R.string.sms_message_received) + " ");
                    sb.append(formatMessageDate(m0.getReceived()));
                    sb.append('\n');
                }
            }
        }

        if (!((mode == PLAY_MODE_HEADER_ONLY) || isEmpty(m0.getBody()))) {
            for (Message m : ms) sb.append(m.getBody());
        }

        return sb;
    }

    public static String getSmsLanguage(Message m) {
        return MyTTS.detectLanguage(m.getBody());
    }

    class TtsSmsWrapper extends MyTTS.Wrapper {

    	Message m = null; 
    	boolean markAsRead = false; 
    	
        public TtsSmsWrapper(Object obj) {
			super(obj); 
		}
        
        public TtsSmsWrapper(StringBuilder obj, Message msg, boolean doMarkAsRead) {
			super(obj); 
			this.m = msg; 
			this.markAsRead= doMarkAsRead; 
		}

		@Override
        public void onSaid(boolean fAborted) {
            super.onSaid(fAborted);
            if (markAsRead && !m.isRead()) markAsRead(m);
            //lastReadId=m.getId();
            //lastMessageRead=m;
        }

        @Override
        public void onToSpeak() {
            super.onToSpeak();
            lastReadId = m.getId();
            lastMessageRead = m;
            App.self.setLastMessageRead(m);
        }

    }
    
    public void play(List<Message> ms, final boolean markAsRead, final String sayBefore, int mode) {
        if (!isEmpty(ms)) {
            if (!isEmpty(sayBefore)
                    && ms.size() > 1) {
                sayAndShow(sayBefore); // skip preamble if there is only one new message
            }
            for (final Message m : ms) {
            	m.getSender(); 
                final StringBuilder sbSay = new StringBuilder(), 
                					sbShow = new StringBuilder();
                if (mode != PLAY_MODE_BODY_ONLY) {
                    Addressable ab = m.getSender();
                    String displayName2say = ab.getDisplayName(true); 
                    String displayName2show = ab.getDisplayName(false); 
//                    if (!(isEmpty(displayName) || mode == PLAY_MODE_HEADER_ONLY)) {
                        if (pauseBeforeReading) {
                            pauseBeforeReading = false;
                            sbSay.append(displayName2say).append(" " +  Phrases.pickSmsArrivedPhrase() + " ");
                            sbShow.append(displayName2show).append(" " + Phrases.pickSmsArrivedPhrase() + " ");
                            
                        } else
                            sbSay.append("\n" + App.self.getString(R.string.sms_message_from) + " " + displayName2say + ".\n");
                        	sbShow.append("\n" + App.self.getString(R.string.sms_message_from) + " " + displayName2show + ".\n");
//                    }
//                    else {
//                        sb.append("\n" + App.self.getString(R.string.sms_message_from) + " ");
//                        sb.append(/*phoneNumberToSpeech(ab.getAddress())*/ab.getSynteticDisplayName(true));
//                        sb.append('\n');
//                        if (m.getReceived() != null) {
//                            sb.append(App.self.getString(R.string.sms_message_received) + " ");
//                            sb.append(formatMessageDate(m.getReceived()));
//                            sb.append('\n');
//                        }
//                    }
                }

                if (!((mode == PLAY_MODE_HEADER_ONLY) || isEmptyOrBlank(m.getBody()))) {
                    sbSay.append(m.getBody());
                    sbShow.append(m.getBody());
                }
                Output.sayAndShow(
                        context,
                        new TtsSmsWrapper(sbShow, m, markAsRead).setShowInASeparateBubble(), 
                        new TtsSmsWrapper(sbSay, m, markAsRead).setShowInASeparateBubble(), 
                        false
                );

            }
        }
    }

    @Override
    public boolean markAsRead(Message m) {
        if (m != null) {
            if (m.getId() != null) return markAsRead(m.getId());
            Object org = m.getOriginal();
            if (org != null && org instanceof SmsMessage)
                return markAsRead((SmsMessage) org);
        }
        return false;
    }

    @Override
    public boolean markAsRead(String key) {
        ContentValues cvs = new ContentValues();
        cvs.put("read", new Integer(1));

        int n = 0;

        try {
            n = App.self.getContentResolver().update(
                    Uri.parse("content://sms/inbox"), cvs, "_id=" + key, null //new String[] {key} //where, selectionArgs
            );
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return n > 1;
    }

    /**
     * Tries to mark as a seen an SMS by its timestamp and body
     *
     * @param sms
     * @return true if marked
     */
    public static boolean markAsRead(SmsMessage sms) {
        if (sms != null) {
            ContentValues cvs = new ContentValues();
            cvs.put("read", new Integer(1));

            int n = 0;

            try {
                n = App.self.getContentResolver().update(
                        Uri.parse("content://sms/inbox"), cvs,
                        " date_sent=" + sms.getTimestampMillis() + " and " +
                                " address=?" +
                                " and body=?" +
                                " and protocol=" + sms.getProtocolIdentifier(),
                        new String[]{sms.getOriginatingAddress(), sms.getMessageBody()} //where, selectionArgs
                );

                if (n > 0) Log.d(TAG, "marked as read " + sms.getTimestampMillis());
            } catch (Throwable t) {
                t.printStackTrace();
            }
            return n > 1;
        }
        return false;
    }

    public final static String INTENT_SENT_OR_FAILED = "com.magnifis.parking.SMS_SENT_OR_FAILED";
    public final static String ADDRESS = "ADDRESS";
    public final static String BODY = "BODY";
    public final static String PART = "PART";
    public final static String NPART = "NPART";
    public final static String NPARTS = "NPARTS";
    public final static String STATUS = "STATUS";
    public final static String COMPONENT = "COMPONENT";

    // !sharedPreferences.getBoolean(PfSmsExcludeSignature, false)

    public void sendSms(String phone, String body) {
        sendSms(phone, body, new Intent(INTENT_SENT_OR_FAILED));
    }

    public void sendSms(String phone, String body, Intent nfyIntent) {
        sendSms(
                phone, body,
                !App.self.getBooleanPref(R.string.PfSmsExcludeSignature),
                nfyIntent
        );
    }

    public void sendSms(String phone, String body, boolean doAdvert) {
        sendSms(phone, body, doAdvert, new Intent(INTENT_SENT_OR_FAILED));
    }

    public void sendSms(String phone, String body, boolean doAdvert, Intent nfyIntent) {
        Log.d(TAG, "sending ...");
        SmsManager sms = SmsManager.getDefault();
        
        String advert = App.self.getString(R.string.adv_for_sms);
        if (body != null && body.length() + advert.length() < 140) {
            boolean attachAd = false;
            String uid = App.self.android_id;
            if (doAdvert && !Utils.isEmpty(uid)) {
                int d = Character.getNumericValue(uid.charAt(0));
                attachAd = ((d % 5) == 1); // 20% of users
            }
            if (attachAd)
                body += "\n" + advert;
        }


        ArrayList<String> parts = sms.divideMessage(body);

        Log.d(TAG, "sending to " + phone);

        ArrayList<PendingIntent> pintents = new ArrayList<PendingIntent>();
        if (nfyIntent != null) for (int i = 0; i < parts.size(); i++) {
            nfyIntent.putExtra(ADDRESS, phone);
            if (i == (parts.size() - 1)) nfyIntent.putExtra(BODY, body);
            nfyIntent.putExtra(PART, parts.get(i));
            nfyIntent.putExtra(NPART, i);
            nfyIntent.putExtra(NPARTS, parts.size());
            nfyIntent.setData(Uri.parse("smssent://" + phone + "/?part=" + i));
            nfyIntent.setClass(App.self, SmsStatusReceiver.class);

            PendingIntent sentIntent = PendingIntent.getBroadcast(
                    App.self, 0, nfyIntent, PendingIntent.FLAG_CANCEL_CURRENT
            );

            pintents.add(sentIntent);
        }

        try {
            sms.sendMultipartTextMessage(phone, null, parts, pintents, null);
            if (!TextUtils.isEmpty(body))
                Log.d(TAG, "send:" + phone + " > " + (int) body.charAt(0));
        } catch (Throwable t) {
            t.printStackTrace();
        }

    }

    public static List<Message> from(Intent intent) {
        return from(intent.getExtras());
    }

    public static List<Message> from(Bundle b) {
        if (isEmpty(b)) {
            return null;
        }

        Utils.dump(TAG, b);
        Object[] pdus = (Object[]) b.get("pdus");

        if (isEmpty(pdus)) {
            return null;
        }

        List<Message> messageList = new ArrayList<Message>();
        for (int i = 0; i < pdus.length; i++) {
            SmsMessage sms = SmsMessage.createFromPdu((byte[]) pdus[i]);
            Log.d(TAG, "message from: " + sms.getDisplayOriginatingAddress());
            Message m = new Message();
            m.setBody(sms.getMessageBody());
            m.setType(Message.TYPE_SMS);

            Addressable sender = new PhoneAddressable();

            String phone = sms.getOriginatingAddress();

            sender.setAddress(phone);

            String senderName = PhoneBook.getBestContactName(phone);

//            if (isEmpty(senderName)) {
//                senderName = App.self.getString(R.string.someone);
//            }

            sender.setDisplayName(senderName);

            m.setSender(sender);

            m.setSent(new Date(sms.getTimestampMillis()));

            m.setOriginal(sms);

            messageList.add(m);
        }
        return messageList;
    }

    void saveSms(String body, String address, String threadId) {
        ContentValues values = new ContentValues();
        values.put("address", address);
        values.put("body", body);
        if (threadId != null) values.put("thread_id", threadId);
        App.self.getContentResolver().insert(Uri.parse("content://sms/sent"), values);
        if (!TextUtils.isEmpty(body))
            Log.d(TAG, "saveSms:" + address + " | " + (int) body.charAt(0));
    }

    public void handleSendingStatusIntent(Context ctx, TextMessageQue ma, Intent it) {
        String body = it.getStringExtra(BODY), address = it.getStringExtra(ADDRESS);
        int nparts = it.getIntExtra(NPARTS, 1), npart = it.getIntExtra(NPART, 1);

        Utils.dump(TAG, it);


        int rc = it.getIntExtra(STATUS, 89898989);

        boolean OK = rc == Activity.RESULT_OK;
        if (body != null) {
            if (OK)
                saveSms(body, address, null);
        }
        String _part = "";//(nparts>1)?" ("+(npart+1)+"/"+nparts+")":"";


        if (context != null) switch (rc) {
            case Activity.RESULT_OK:
            	
            	
            	String nm= DelegateAgentPhone.getAgentByPhone(address, context);

                if (isEmpty(nm)) nm = PhoneBook.getBestContactName(address);

                String t = App.self.getString(R.string.sms_sent_to) + " ";

                t += isEmpty(nm) ? Utils.phoneNumberToSpeech(address) : nm;

                if (npart + 1 >= nparts)
                    ma.queTextMessage(ctx, t + _part);
                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                ma.queTextMessage(ctx, App.self.getString(R.string.sms_generic_failure) + _part);
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                ma.queTextMessage(ctx, App.self.getString(R.string.sms_no_service) + _part);
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                ma.queTextMessage(ctx, App.self.getString(R.string.sms_null_pdu) + _part);
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                ma.queTextMessage(ctx, App.self.getString(R.string.sms_radion_off) + _part);
                break;
        }
    }

}
