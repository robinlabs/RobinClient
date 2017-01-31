package com.magnifis.parking.billing;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import com.android.vending.billing.IInAppBillingService;
import com.magnifis.parking.App;
import com.magnifis.parking.utils.Utils;

import android.os.RemoteException;
import android.text.TextUtils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by user on 31/12/13.
 */
public class Billing {

    public interface PurchaseListener {
        void onPurchaseFinished(Purchase purchase);
    }

    public PurchaseListener mPurchaseListener = null;

    private static final int BUY_REQUEST_CODE = 6278110;
    private boolean mStarted = false;
    private ServiceConnection mServiceConn = null;
    IInAppBillingService mService;
    boolean mSubscriptionsSupported = false;

    Map<String, Purchase> mPurchaseMap = new HashMap<String, Purchase>();
    Map<String, SkuDetails> mDetailsMap = new HashMap<String, SkuDetails>();

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isSubscriptionsSupported() {
        return mSubscriptionsSupported;
    }

    public boolean checkPayment(String skuName) {
        if (!mStarted)
            return false;

        Purchase p = mPurchaseMap.get(skuName);
        if (p == null)
            return false;

        return true;
    }

    public void start(final String mSignatureBase64) {
        // If already set up, can't do it again.
        if (mStarted || mServiceConn != null)
            return;

        // Connection to IAB service
        mServiceConn = new ServiceConnection() {
            @Override
            public void onServiceDisconnected(ComponentName name) {
                mService = null;
            }

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                mService = IInAppBillingService.Stub.asInterface(service);
                String packageName = App.self.getPackageName();
                try {
                    // check for in-app billing v3 support
                    int response = mService.isBillingSupported(3, packageName, "inapp");
                    if (response != 0)
                        return;

                    // check for v3 subscriptions support
                    response = mService.isBillingSupported(3, packageName, "subs");
                    if (response == 0)
                        mSubscriptionsSupported = true;
                }
                catch (RemoteException e) {
                    e.printStackTrace();
                    return;
                }

                // query for inventory
                (new Thread(new Runnable() {
                    public void run() {
                        if (!queryPurchases(mSignatureBase64, "inapp"))
                            return;

                        // query for subscriptions
                        if (mSubscriptionsSupported)
                            if (!queryPurchases(mSignatureBase64, "subs"))
                                return;

                        mStarted = true;
                    }
                })).start();
            }
        };

        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        if (!App.self.getPackageManager().queryIntentServices(serviceIntent, 0).isEmpty()) {
            // service available to handle that Intent
            App.self.bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        }
    }

    boolean queryPurchases(final String mSignatureBase64, final String itemType) {
        try {
            // Query purchases
            String continueToken = null;
            ArrayList<String> skuList = new ArrayList<String>();

            do {
                Bundle ownedItems = mService.getPurchases(3, App.self.getPackageName(), itemType, continueToken);

                int response = getResponseCodeFromBundle(ownedItems);
                if (response != 0)
                    return false;

                if (!ownedItems.containsKey("INAPP_PURCHASE_ITEM_LIST")
                        || !ownedItems.containsKey("INAPP_PURCHASE_DATA_LIST")
                        || !ownedItems.containsKey("INAPP_DATA_SIGNATURE_LIST")) {
                    return false;
                }

                ArrayList<String> ownedSkus = ownedItems.getStringArrayList("INAPP_PURCHASE_ITEM_LIST");
                ArrayList<String> purchaseDataList = ownedItems.getStringArrayList("INAPP_PURCHASE_DATA_LIST");
                ArrayList<String> signatureList = ownedItems.getStringArrayList("INAPP_DATA_SIGNATURE_LIST");

                for (int i = 0; i < purchaseDataList.size(); ++i) {
                    String purchaseData = purchaseDataList.get(i);
                    String signature = signatureList.get(i);
                    String sku = ownedSkus.get(i);
                    // Check Purchase signature verification
                    if (!Utils.isEmpty(mSignatureBase64))
                        if (!Security.verifyPurchase(mSignatureBase64, purchaseData, signature))
                            return false;

                    Purchase purchase = new Purchase(itemType, purchaseData, signature);

                    // Record ownership and token
                    mPurchaseMap.put(purchase.getSku(), purchase);
                    skuList.add(purchase.getSku());
                }

                continueToken = ownedItems.getString("INAPP_CONTINUATION_TOKEN");
            } while (!TextUtils.isEmpty(continueToken));

            // query sku details
            if (skuList.size() == 0)
                return true;

            Bundle querySkus = new Bundle();
            querySkus.putStringArrayList("ITEM_ID_LIST", skuList);
            Bundle skuDetails = mService.getSkuDetails(3, App.self.getPackageName(), itemType, querySkus);

            if (!skuDetails.containsKey("DETAILS_LIST"))
                return false;

            ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");

            for (String thisResponse : responseList) {
                SkuDetails d = new SkuDetails(itemType, thisResponse);
                mDetailsMap.put(d.getSku(), d);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Workaround to bug where sometimes response codes come as Long instead of Integer
    int getResponseCodeFromIntent(Intent i) {
        Object o = i.getExtras().get("RESPONSE_CODE");
        if (o == null)
            return 0;
        else if (o instanceof Integer) return ((Integer)o).intValue();
        else if (o instanceof Long) return (int)((Long)o).longValue();
        else
            return 1;
    }

    // Workaround to bug where sometimes response codes come as Long instead of Integer
    int getResponseCodeFromBundle(Bundle b) {
        Object o = b.get("RESPONSE_CODE");
        if (o == null) {
            // Bundle with null response code, assuming OK (known issue)
            return 0;
        }
        else if (o instanceof Integer) return ((Integer)o).intValue();
        else if (o instanceof Long) return (int)((Long)o).longValue();
        else {
            // Unexpected type for bundle response code
            return 1;
        }
    }

    /**
     * Initiate the UI flow for an in-app purchase. Call this method to initiate an in-app purchase,
     * which will involve bringing up the Google Play screen. The calling activity will be paused while
     * the user interacts with Google Play, and the result will be delivered via the activity's
     * {@link android.app.Activity#onActivityResult} method, at which point you must call
     * this object's {@link #handleActivityResult} method to continue the purchase flow. This method
     * MUST be called from the UI thread of the Activity.
     *
     * @param act The calling activity.
     * @param sku The sku of the item to purchase.
     * @param itemType indicates if it's a product or a subscription (ITEM_TYPE_INAPP or ITEM_TYPE_SUBS)
     * @param listener The listener to notify when the purchase process finishes
     * @param extraData Extra data (developer payload), which will be returned with the purchase data
     *     when the purchase completes. This extra data will be permanently bound to that purchase
     *     and will always be returned when the purchase is queried.
     */
    public void launchPurchaseFlow(Activity act, String sku, String itemType, String extraData) {
        try {
            Bundle buyIntentBundle = mService.getBuyIntent(3, App.self.getPackageName(), sku, itemType, itemType+"/"+extraData);
            int response = getResponseCodeFromBundle(buyIntentBundle);
            if (response != 0)
                return;

            PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
            act.startIntentSenderForResult(pendingIntent.getIntentSender(),
                    BUY_REQUEST_CODE, new Intent(),
                    Integer.valueOf(0), Integer.valueOf(0),
                    Integer.valueOf(0));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles an activity result that's part of the purchase flow in in-app billing. If you
     * are calling {@link #launchPurchaseFlow}, then you must call this method from your
     * Activity's {@link android.app.Activity@onActivityResult} method. This method
     * MUST be called from the UI thread of the Activity.
     *
     * @param requestCode The requestCode as you received it.
     * @param resultCode The resultCode as you received it.
     * @param data The data (Intent) as you received it.
     * @return Returns true if the result was related to a purchase flow and was handled;
     *     false if the result was not related to a purchase, in which case you should
     *     handle it normally.
     */
    public boolean handleActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode != BUY_REQUEST_CODE)
            return false;

        if (data == null)
            return true;

        int responseCode = getResponseCodeFromIntent(data);
        String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
        String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");
        String itemType = "inapp";
        int i = -1;
        if (dataSignature != null)
            i = dataSignature.indexOf("/");
        if (i >= 0) {
            itemType = dataSignature.substring(0, i-1);
            dataSignature = dataSignature.substring(i+1);
        }

        if (resultCode == Activity.RESULT_OK && responseCode == 0) {

            if (purchaseData == null || dataSignature == null) {
                // finished!
                return true;
            }

            Purchase purchase = null;
            try {
                purchase = new Purchase(itemType, purchaseData, dataSignature);
                String sku = purchase.getSku();

                // Verify signature
                if (!Security.verifyPurchase(itemType, purchaseData, dataSignature))
                    return true;
            }
            catch (JSONException e) {
                e.printStackTrace();
                return true;
            }

            mPurchaseMap.put(purchase.getSku(), purchase);
            if (mPurchaseListener != null)
                mPurchaseListener.onPurchaseFinished(purchase);
        }
        return true;
    }

}
