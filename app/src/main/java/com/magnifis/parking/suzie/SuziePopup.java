package com.magnifis.parking.suzie;

import static com.magnifis.parking.Launchers.startNestedActivity;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.os.PowerManager;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnHoverListener;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.magnifis.parking.Abortable;
import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.ListenAndLaunchActivity;
import com.magnifis.parking.MainActivity;
import com.magnifis.parking.PrefsActivity;
import com.magnifis.parking.Props;
import com.magnifis.parking.R;
import com.magnifis.parking.SendSmsActivity;
import com.magnifis.parking.SmsActivity;
import com.magnifis.parking.VR;
import com.magnifis.parking.cmd.SendCmdHandler;
import com.magnifis.parking.cmd.etc.CmdHandlerHolder;
import com.magnifis.parking.cmd.i.OnOrientationHandler;
import com.magnifis.parking.cmd.i.OnResumeHandler;
import com.magnifis.parking.model.GooWeather;
import com.magnifis.parking.pref.PrefConsts;
import com.magnifis.parking.tts.MyTTS;
import com.magnifis.parking.utils.Analytics;
import com.magnifis.parking.utils.Utils;
import com.magnifis.parking.views.ScalableShort;
import com.magnifis.parking.views.WeatherView;

/*

packageName describes floating button
use methods: SuzieService.showSuzie, SuzieService.hideSuzie, SuzieService.sendSuzie

*/
public class SuziePopup implements OnTouchListener {

    private VR vr = null;

    public final static String TAG="suzie";

    private Application _theApp = null;

    private static WeakReference<SuziePopup> selfWr=null;

    public static SuziePopup get() {
        return selfWr==null?null:selfWr.get();
    }

    public View getTopView() {
        return _layoutBtnBox;
    }

    public void onNetworkCommunicationError() {
        if (_layoutBtnBox != null)
            _layoutBtnBox.post(new Runnable() {

                @Override
                public void run() {
                    if (mag != null)
                        mag.showError();
                }
            });
    }

    private WindowManager _windowManager = null;
    private RelativeLayout _layoutBtnBox = null;
    private RelativeLayout _layoutOptions = null;
    private LinearLayout _layoutBubles = null;
    private WindowManager.LayoutParams _paramsBtn = null;
    private WindowManager.LayoutParams _paramsBubles = null;
    private WindowManager.LayoutParams _paramsOptions = null;
    private View _btnSuzie;
    private int buttonWidth = 0;
    private int buttonHeight = 0;
    private boolean animationWork = false;

    public boolean isOutOfScreen = false;
    public boolean isOutOfScreenInPreviousPressing = false;

    private Option[] options = null;

    // update button size
    public void checkButtonSize() {
        boolean save = isVisible();
        if (_layoutBtnBox != null) {
            _windowManager.removeView(_layoutBtnBox);
            _paramsBtn = null;
            _layoutBtnBox = null;
        }
        if (save)
            showSuzie();
    }

    // option button information (context menu)
    private class Option {
        long x;
        long y;
        long id;
    }

    // last position of button depends on screen orientation
    // if screen orientation not changed using lastx, lasty
    // else using lastleft, lastfromtop
    private int suzieLastX = 0;
    private int suzieLastY = 0;
    private boolean suzieLastOrientationLandscape = false;
    private boolean suzieLastLeft = true; // magnetic to left ?
    private float suzieLastFromTop = 0;

    private Boolean _isDragging = false;
    private boolean bubleWaitingAnswer = false;
    private SuzieHints hints = null;

    public MicAnimatorMagSuize mag = null;

    private float _draggerX, _draggerY, _draggerStartX, _draggerStartY;

    private static float _deltaX;

    private static float _deltaY;
    private int _draggerStartPointX, _draggerStartPointY;

    @SuppressWarnings("unused") private TextView _txtDebugX;
    @SuppressWarnings("unused") private TextView _txtDebugY;

    public enum Direction {
        LEFT, RIGHT, TOP, BOTTOM
    };

    private final SuzieService service;

    private int actionBarHeight = 0;
    private int buttonPadding;
    private int buttonPaddingInner;

    public SuziePopup(SuzieService service) {
        this.service=service;
        selfWr = new WeakReference<SuziePopup>(this);
        _theApp = service.getApplication();

        buttonPaddingInner=0;//App.self.scaler.scaleItShort(6.6);
        buttonPadding = buttonPaddingInner;

        actionBarHeight = /*getStatusBarHeight()*/ 0;
        buttonPadding -= getStatusBarHeight()+buttonPadding;

        //new Timer().schedule(new CheckTask(), TIMER_INTERVAL, TIMER_INTERVAL);
        hints = new SuzieHints();

        Log.d(TAG, "suzie popup created "+this);
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = _theApp.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = _theApp.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /*********************************************************************************************************/
    /************************************** PRIMARY METHODS **************************************************/
    /*********************************************************************************************************/
    static public boolean bublesEnabled = true;

    public void bubleQuery(final String _s, final ArrayList<String> matches, final ArrayList<String> old_matches) {
        if (!isVisible())
            return;

        if (!bublesEnabled)
            return;

        _layoutBubles.post(
                new Runnable() {
                    @Override
                    public void run() {

                        hideOptions();

                        // update bubles
                        LinearLayout bubleQuery = (LinearLayout) _layoutBubles.findViewById(R.id.buble_query);
                        bubleQuery.setVisibility(View.GONE);
                        bubleQuery.clearAnimation();
                        LinearLayout bubleAnswer = (LinearLayout) _layoutBubles.findViewById(R.id.buble_answer);
                        bubleAnswer.setVisibility(View.GONE);
                        bubleAnswer.clearAnimation();
                        WeatherView bubleWeather = (WeatherView) _layoutBubles.findViewById(R.id.buble_weather);
                        bubleWeather.setVisibility(View.GONE);
                        bubleWeather.clearAnimation();
                        LinearLayout bubleMessage = (LinearLayout) _layoutBubles.findViewById(R.id.buble_message);
                        bubleMessage.setVisibility(View.GONE);
                        bubleMessage.clearAnimation();

                        _layoutBubles.invalidate();

                    }
                }

        );

        // fix bug: if answer was visible, hide it !!!
	    /*
    	AlphaAnimation a;
   		a = new AlphaAnimation(0, 0);// TranslateAnimation(0, 0, -200, -200);
		a.setDuration(10);
		a.setFillAfter(true);
		bubleAnswer.startAnimation(a);
		*/
        // end fix bug

        _layoutBubles.post(
                new Runnable() {
                    @Override
                    public void run() {
                        String s=_s;

                        // add arrow
                        if (matches != null && old_matches != null && old_matches.size() == matches.size())
                            try {
                                for (int i = 0; i < matches.size(); i++) {
                                    if (s.equalsIgnoreCase(matches.get(i))) {
                                        if (!s.equalsIgnoreCase(old_matches.get(i)))
                                            s = old_matches.get(i) + " -> " + Utils.firstUpper(s, true);
                                        break;
                                    }
                                }
                            } catch (Exception e) {}

                        LinearLayout bubleQuery = (LinearLayout) _layoutBubles.findViewById(R.id.buble_query);

                        _layoutBubles.setBackgroundDrawable(null);
                        _layoutBubles.setVisibility(View.VISIBLE);
                        _paramsBubles.alpha = 1;
                        _paramsBubles.y = _paramsBtn.y;
                        _windowManager.updateViewLayout(_layoutBubles, _paramsBubles);
                        if (_paramsBtn.x > 0)
                            _layoutBubles.setPadding(_layoutBtnBox.getWidth(), 0, 0, 0);
                        else
                            _layoutBubles.setPadding(0, 0, _layoutBtnBox.getWidth(), 0);

                        // set text
                        TextView tv = (TextView) bubleQuery.findViewById(R.id.textViewBubleQuery);
                        tv.setText(Utils.firstUpper(s, false));

                        bubleQuery.setVisibility(View.VISIBLE);

                        TranslateAnimation aa;
                        if (_paramsBtn.x == 0)
                            aa = new TranslateAnimation(400, 0, 0, 0);
                        else
                            aa = new TranslateAnimation(-400, 0, 0, 0);
                        aa.setDuration(200);
                        bubleQuery.startAnimation(aa);
                    }
                }

        );

        bubleWaitingAnswer = true;
    }

    public void bubleQueryCorrect(final String s) {
        if (!isVisible())
            return;

        _layoutBubles.post(new Runnable() {

            @Override
            public void run() {
                // set text
                LinearLayout bubleQuery = (LinearLayout) _layoutBubles.findViewById(R.id.buble_query);
                TextView tv = (TextView) bubleQuery.findViewById(R.id.textViewBubleQuery);
                tv.setText(Utils.firstUpper(s, false));
            }
        });
    }

    void bubleAnswer(final CharSequence s) {

        if (isVisible())
            _layoutBtnBox.post(new Runnable() {

                @Override
                public void run() {
                    _bubleAnswer(s);
                }
            });
    }

    private void _bubleAnswer(CharSequence s) {

        if (!isRelevant())
            return;

        if (!bublesEnabled)
            return;

        hideOptions();

        WeatherView bubleWeather = (WeatherView) _layoutBubles.findViewById(R.id.buble_weather);
        if (bubleWeather.getVisibility() == View.VISIBLE)
            return;

        // update bubles
        LinearLayout bubleQuery = (LinearLayout) _layoutBubles.findViewById(R.id.buble_query);
        if (!bubleWaitingAnswer) {
            bubleQuery.setVisibility(View.GONE);
            bubleQuery.clearAnimation();
        }
        LinearLayout bubleAnswer = (LinearLayout) _layoutBubles.findViewById(R.id.buble_answer);
        _layoutBubles.setBackgroundDrawable(null);
        _layoutBubles.setVisibility(View.VISIBLE);
        _paramsBubles.alpha = 1;
        _paramsBubles.y = _paramsBtn.y;
        _windowManager.updateViewLayout(_layoutBubles, _paramsBubles);
        if (_paramsBtn.x > 0)
            _layoutBubles.setPadding(_layoutBtnBox.getWidth(), 0, 0, 0);
        else
            _layoutBubles.setPadding(0, 0, _layoutBtnBox.getWidth(), 0);

        // set text
        TextView tv = (TextView) bubleAnswer.findViewById(R.id.textViewBubleAnswer);
        if (tv != null)
            tv.setText(s);

        // set arrow
        View topArrow = bubleAnswer.findViewById(R.id.topArrow);
        if (topArrow != null)
            if (bubleWaitingAnswer) {

                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(metrics);
                screenWidth = metrics.widthPixels;
                screenHeight = metrics.heightPixels;

                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);

                layoutParams.setMargins(-Math.round((screenWidth-_layoutBtnBox.getWidth()+topArrow.getWidth())/2), 0, 0, 0);

                topArrow.setLayoutParams(layoutParams);

                topArrow.setVisibility(View.VISIBLE);
            }
            else
                topArrow.setVisibility(View.GONE);

        bubleAnswer.setVisibility(View.VISIBLE);

        TranslateAnimation aa;
        if (!bubleWaitingAnswer)
            if (_paramsBtn.x == 0)
                aa = new TranslateAnimation(400, 0, 0, 0);
            else
                aa = new TranslateAnimation(-400, 0, 0, 0);
        else
            aa = new TranslateAnimation(0, 0, -bubleQuery.getHeight(), 0);
        aa.setDuration(200);
        aa.setFillAfter(true);
        bubleAnswer.startAnimation(aa);

        _layoutBubles.invalidate();

        bubleWaitingAnswer = false;
    }

    public void showWeather(final GooWeather w) {

        if (!isVisible())
            return;

        hideOptions();

        // update bubles
        LinearLayout bubleQuery = (LinearLayout) _layoutBubles.findViewById(R.id.buble_query);
        if (!bubleWaitingAnswer)
            bubleQuery.setVisibility(View.GONE);
        LinearLayout bubleAnswer = (LinearLayout) _layoutBubles.findViewById(R.id.buble_answer);
        bubleAnswer.setVisibility(View.GONE);
        WeatherView bubleWeather = (WeatherView) _layoutBubles.findViewById(R.id.buble_weather);
        _layoutBubles.setBackgroundDrawable(null);
        _layoutBubles.setVisibility(View.VISIBLE);
        _paramsBubles.alpha = 1;
        _paramsBubles.y = _paramsBtn.y;
        _windowManager.updateViewLayout(_layoutBubles, _paramsBubles);
        if (_paramsBtn.x > 0)
            _layoutBubles.setPadding(_layoutBtnBox.getWidth(), 0, 0, 0);
        else
            _layoutBubles.setPadding(0, 0, _layoutBtnBox.getWidth(), 0);

        bubleWeather.setData(w);
        bubleWeather.setVisibility(View.VISIBLE);

		/*
		TranslateAnimation aa;
		if (!bubleWaitingAnswer)
	    	if (_paramsBtn.x == 0)
	    		aa = new TranslateAnimation(400, 0, 0, 0);
	    	else
	    		aa = new TranslateAnimation(-400, 0, 0, 0);
		else
			aa = new TranslateAnimation(0, 0, -bubleQuery.getHeight(), 0);
		aa.setDuration(200);
		aa.setFillAfter(true);
		bubleWeather.startAnimation(aa);
		*/

        bubleWaitingAnswer = false;
    }

    public static class BubleMessage implements Serializable, Cloneable {
        public Drawable icon;
        public SpannableStringBuilder name;
        public SpannableStringBuilder addr;
        public SpannableStringBuilder text;
        public SpannableStringBuilder hint;
        public SpannableStringBuilder answer;
        public OnClickListener onEditClick;
        public OnTouchListener onEditTouch;
        public OnClickListener onEditChanged;
        public OnClickListener onSendButtonClick;
        public OnClickListener onCancelButtonClick;
        public OnClickListener onKeyboardButtonClick;
        public int sel_start, sel_end;
        public boolean keyboardVisible;
        public boolean bgVisible;

        public BubleMessage() {
            icon = null;
            name = new SpannableStringBuilder();
            addr = new SpannableStringBuilder();
            text = new SpannableStringBuilder();
            hint = new SpannableStringBuilder();
            answer = new SpannableStringBuilder();
            sel_start = 0;
            sel_end = 0;
            keyboardVisible = false;
            bgVisible = false;
        }
    }

    static public void enableBubles() {
        bublesEnabled = true;
        final SuziePopup sp = SuziePopup.get();
        if (sp != null && sp._layoutBubles != null) {
            sp._layoutBubles.post(new Runnable() {

                @Override
                public void run() {
                    if (sp != null && sp._layoutBubles != null) {
                        LinearLayout bubleMessage = (LinearLayout) sp._layoutBubles.findViewById(R.id.buble_message);
                        bubleMessage.setVisibility(View.GONE);
                        sp._layoutBubles.setBackgroundDrawable(null);
                        sp._layoutBubles.invalidate();
                        if (!App.self.shouldUseSuzie())
                            sp.hideSuzie(HideAnimation.None);
                        return;
                    }
                }

            });
            sp.magneticRestore(false);
        }
        startAutoHideTimer();
    }

    static public void disableBubles() {
        hideBubles(true, false);
        bublesEnabled = false;
    }

    public void bubleMessage(final BubleMessage bm) {

        if (_layoutBubles == null || bublesEnabled)
            return;

        _layoutBubles.post(new Runnable() {

            @Override
            public void run() {
                if (bm == null) {
                    LinearLayout bubleMessage = (LinearLayout) _layoutBubles.findViewById(R.id.buble_message);
                    bubleMessage.setVisibility(View.GONE);
                    _layoutBubles.setBackgroundDrawable(null);
                    return;
                }

                hideOptions();

                stopAutoHideTimer();

                DisplayMetrics metrics = new DisplayMetrics();
                WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                wm.getDefaultDisplay().getMetrics(metrics);
                screenWidth = metrics.widthPixels;
                screenHeight = metrics.heightPixels;

                // update bubles
                LinearLayout bubleQuery = (LinearLayout) _layoutBubles.findViewById(R.id.buble_query);
                bubleQuery.setVisibility(View.GONE);
                LinearLayout bubleAnswer = (LinearLayout) _layoutBubles.findViewById(R.id.buble_answer);
                bubleAnswer.setVisibility(View.GONE);
                WeatherView bubleWeather = (WeatherView) _layoutBubles.findViewById(R.id.buble_weather);
                bubleWeather.setVisibility(View.GONE);
                LinearLayout bubleMessage = (LinearLayout) _layoutBubles.findViewById(R.id.buble_message);
                bubleMessage.setVisibility(View.VISIBLE);
                _paramsBubles.alpha = 1;
                _paramsBubles.x = 0;
                _paramsBubles.y = 0;//_paramsBtn.y;
                _windowManager.updateViewLayout(_layoutBubles, _paramsBubles);
                int padding = Math.round(Utils.convertDpToPixel(5));
                _layoutBubles.setPadding(padding, padding, padding, padding);

                LinearLayout viewCenter = (LinearLayout) bubleMessage.findViewById(R.id.layoutCenter);
                int size;
                if (service.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
                    size = Math.round(screenHeight - Utils.convertDpToPixel(124));
                else {
                    size = Math.round((screenHeight - Utils.convertDpToPixel(124))/2);
                    if (bm.answer.length() > 0) {
                        final EditText te = (EditText) bubleMessage.findViewById(R.id.textEditText);
                        size += te.getHeight();
                    }
                }
                viewCenter.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, size));

                TextView text_recipient = (TextView) bubleMessage.findViewById(R.id.textViewRecipientName);
                text_recipient.setText(bm.name);

                TextView text_addr = (TextView) bubleMessage.findViewById(R.id.textViewRecipientAddr);
                text_addr.setText(bm.addr);

                final EditText ta = (EditText) bubleMessage.findViewById(R.id.textAnswerText);
                ta.setText(bm.answer);
                final View v = bubleMessage.findViewById(R.id.acrollAnswer);
                if (bm.answer.length() > 0)
                    v.setVisibility(View.VISIBLE);
                else
                    v.setVisibility(View.GONE);

                final EditText te = (EditText) bubleMessage.findViewById(R.id.textEditText);
                te.setText(bm.text);
                te.setCursorVisible(true);
                te.setSelected(false);
                te.requestFocus();
                te.setOnClickListener(bm.onEditClick);
                te.setSelection(bm.sel_start, bm.sel_end);
                te.setOnTouchListener(bm.onEditTouch);
                te.setLongClickable(false);

                ImageView image = (ImageView) bubleMessage.findViewById(R.id.user);
                if (bm.icon == null)
                    image.setImageResource(R.drawable.com_facebook_profile_picture_blank_square);
                else
                    image.setImageDrawable(bm.icon);

                //TextView text_hint = (TextView) bubleMessage.findViewById(R.id.textViewHint);
                //text_hint.setText(bm.hint);

                bubleMessage.findViewById(R.id.buttonSend).setOnClickListener(bm.onSendButtonClick);
                bubleMessage.findViewById(R.id.buttonKeyboard).setOnClickListener(bm.onKeyboardButtonClick);
                bubleMessage.findViewById(R.id.buttonSmsCancel).setOnClickListener(bm.onCancelButtonClick);

                int shortLong[]=App.self.getShortLongDspSizes();
                if (bm.bgVisible && false)
                    if (shortLong[0]<=800)
                        _layoutBubles.setBackgroundResource(App.self.isInLanscapeMode()?R.drawable.bg_800x480:R.drawable.bg_480x800);
                    else
                        _layoutBubles.setBackgroundResource(App.self.isInLanscapeMode()?R.drawable.bg_land:R.drawable.bg);
                else
                    _layoutBubles.setBackgroundDrawable(null);

                ImageView iv = (ImageView)bubleMessage.findViewById(R.id.buttonKeyboard);
                iv.setImageResource(R.drawable.sms_keyboard);

                _layoutBubles.setVisibility(View.VISIBLE);

                // move button to fixed place below close button
                _layoutBtnBox.findViewById(R.id.btnSuzie).setBackgroundResource(R.drawable.button_free);
                _paramsBtn.x = 0;
                _paramsBtn.y = 0;
                _paramsBtn.alpha = 1;
                _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);
                _layoutBtnBox.setVisibility(View.VISIBLE);
				/*
		    	final View closeButton = bubleMessage.findViewById(R.id.placeForButton);
				_layoutBubles.postDelayed(new Runnable() {
					@Override
					public void run() {
						try {
					    	int [] location = new int[2];
					    	closeButton.getLocationInWindow(location);
					    	
							DisplayMetrics metrics = new DisplayMetrics();
							WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
							wm.getDefaultDisplay().getMetrics(metrics);
							screenWidth = metrics.widthPixels;
							screenHeight = metrics.heightPixels;
					    	
							_paramsBtn.x = screenWidth-location[0]-_layoutBtnBox.getWidth()/2-closeButton.getWidth()/2;
							_paramsBtn.y = location[1]+closeButton.getHeight();
							_windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);
					    	//animateView(false, _layoutBtnBox, _paramsBtn, x, y);
						} catch (Exception e) {}
					}
				}, 100);
				*/

                TextView tv = (TextView) bubleMessage.findViewById(R.id.hints);
                tv.setText(bm.hint);

                bubleWaitingAnswer = false;

                // hide message in activity
                SendSmsActivity s = SendSmsActivity.get();
                if (s != null)
                    s.showMessage(null);
            }
        });
    }

    private boolean prepareSuzie() {
        Log.d(TAG, "prepareSuzie "+_paramsBtn);

        if ((_paramsBtn != null)  /*&& (size==0 || size == this.size)*/) {
            return true; // already showing
        }

        if (_paramsBtn != null)
            hideSuzie(HideAnimation.ToSide);

        Log.d(TAG, "prepareSuzie start");

        Log.d(TAG, "Suzie.init"+this);

        _windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);

        try {
            if (_paramsBubles==null) createBubles();
            if (_paramsOptions==null) createOptions();
            if (_paramsBtn==null) createButton();

            Log.d(TAG, "Suzie.init: ok");

            mag = new MicAnimatorMagSuize();
            mag.micAnimationView = _layoutBtnBox.findViewById(R.id.MicAnimation);
            mag.micAnimationBgView1 = _layoutBtnBox.findViewById(R.id.MicAnimationBg);
            mag.micAnimationViewError = _layoutBtnBox.findViewById(R.id.MicAnimationError);
            mag.micAnimationViewL = null;//_layoutBtnBox.findViewById(R.id.MicAnimationL);
            mag.micAnimationBgViewL = null;//_layoutBtnBox.findViewById(R.id.MicAnimationBgL);
            mag.micBusy = _layoutBtnBox.findViewById(R.id.progressBarButton);

            return true;
        } catch (Exception e) {
            Log.d(TAG, "Suzie.init ERROR "+e.getMessage());
        }
        return false;
    }

    public void showSuzie() {
        Log.d(TAG, "ShowSuzie");
        if (prepareSuzie()) try {
            restoreSuzie();
        } catch (Exception e) {
            Log.d(TAG, "Suzie.init ERROR "+e.getMessage());
        }
    }

    public enum HideAnimation {None, ToSide, ToMainActivity};

    public void hideSuzie(HideAnimation animation) {
        if (_layoutBtnBox != null) {
            if (!isVisible())
                return;

            Log.d(TAG, "Suzie.hide");

            stopAutoHideTimer();

            // hide bubles fast!!!
            hideBubles(true, true);

            // hide options fast!!!
            hideOptions();

            WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics metrics = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(metrics);
            screenWidth = metrics.widthPixels;
            screenHeight = metrics.heightPixels;

            saveLastPos();

            // hide button fast!!!
            if (animation == HideAnimation.None || animation == HideAnimation.ToMainActivity) {
                _paramsBtn.alpha = 0;
                _layoutBtnBox.setVisibility(View.GONE);
                _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);
                return;
            }
            if (animation == HideAnimation.ToSide) {
                animateView(true, _layoutBtnBox, _paramsBtn, _paramsBtn.x, _paramsBtn.y);
                return;
            }
            /*
            if (animation == HideAnimation.ToMainActivity) {

                final int x = _paramsBtn.x;
                final int y = _paramsBtn.y;

                animationWork = true;

                final int center_x = metrics.widthPixels / 2 - _layoutBtnBox.getWidth() / 2;

                animateButton(
                        200, // duration in ms
                        null,//new DecelerateInterpolator(),
                        new Move(x, y, center_x, screenHeight - _layoutBtnBox.getWidth() * 2), // move
                        null, // rotate
                        null, // stretch
                        new Runnable() {

                            @Override
                            public void run() {
                                _paramsBtn.alpha = 0;
                                _layoutBtnBox.setVisibility(View.GONE);
                                _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);
                            }
                        });
            }
            */
        }
    }

    private void saveLastPos() {
        suzieLastX = _paramsBtn.x;
        suzieLastY = _paramsBtn.y;
        suzieLastOrientationLandscape = service.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        suzieLastLeft = _paramsBtn.x > screenWidth/2;
        suzieLastFromTop = _paramsBtn.y;
        suzieLastFromTop /= screenHeight;
    }

    public static boolean isRelevant() {
        SuziePopup sp = get();
        if (sp == null)
            return false;

        if (sp._layoutBtnBox == null)
            return false;

        return true;
    }

    public static boolean isOutOfScreen() {
        SuziePopup sp = get();
        return (sp == null)?true:sp.isOutOfScreen;
    }

    public static boolean isVisible() {
        return isVisible(false);
    }

    public static boolean isVisible(boolean checkOnScreen) {

        SuziePopup sp = get();
        if (sp == null)
            return false;

        if (sp._layoutBtnBox == null||(checkOnScreen&&sp.isOutOfScreen))
            return false;

        if (sp._paramsBtn == null)
            return false;

        if (sp._paramsBtn.alpha < 0.1)
            return false;

        return true;
    }

    public static boolean isOptionsVisible() {

        SuziePopup sp = get();
        if (sp == null)
            return false;

        if (sp._paramsOptions == null)
            return false;

        return sp._paramsOptions.alpha > 0.1;
    }

    /*********************************************************************************************************/
    /******************************************* BUSY ********************************************************/
    /*********************************************************************************************************/
    public void showBusy() {

        if (_layoutBtnBox == null)
            return;

        ProgressBar pb = (ProgressBar) _layoutBtnBox.findViewById(R.id.progressBarButton);
        if (pb == null)
            return;

        Log.d(TAG, "Suzie.show busy");
        pb.setVisibility(View.VISIBLE);
    }

    public void hideBusy() {
        if (_layoutBtnBox == null)
            return;

        _layoutBtnBox.findViewById(R.id.btnSuzie).setBackgroundResource(R.drawable.button_free);

        ProgressBar pb = (ProgressBar) _layoutBtnBox.findViewById(R.id.progressBarButton);
        if (pb == null)
            return;

        Log.d(TAG, "Suzie.show not busy");
        pb.setVisibility(View.GONE);
    }

    /*********************************************************************************************************/
    /******************************************** EVENTS *****************************************************/
    /*********************************************************************************************************/
    int screenWidth = 0, screenHeight = 0;
    boolean waitingLongClick = false;
    boolean waitingSimpleClick = false;

    @Override
    public boolean onTouch(View v, MotionEvent event) {

        Log.d(TAG, "touch "+event+" "+v);

        if (v != _btnSuzie) {
            return false;
        }
        boolean result = false;
        try {

            resetAutoHideTimer();
            if (animationWork) {
                breakAnimation();
                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_DOWN) {

                isOutOfScreenInPreviousPressing = isOutOfScreen;

                // cancel all
                //hideBubles(true, true);
                hideOptions();
                //service.abortOperation(0);

                waitingSimpleClick = true;
                waitingLongClick = true;

                _draggerX = event.getRawX();
                _draggerY = event.getRawY();
                _draggerStartX = _draggerX;
                _draggerStartY = _draggerY;
                _draggerStartPointX = _paramsBtn.x;
                _draggerStartPointY = _paramsBtn.y;

                WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics metrics = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                screenWidth = metrics.widthPixels;
                screenHeight = metrics.heightPixels;

                // long click
                new Timer().schedule(new TimerTask() {

                    @Override
                    public void run() {
                        if (_isDragging)
                            return;

                        _btnSuzie.post(new Runnable() {

                            @Override
                            public void run() {

                                waitingSimpleClick = false;

                                if (MyTTS.isSpeaking())
                                    return;

                                if (VR.isRinging())
                                    return;

                                if (!isVisible())
                                    return;

                                if (!waitingLongClick)
                                    return;

                                onButtonLongClick();
                            }
                        });
                    }
                }, 500);


                return true;
            }

            if (event.getAction() == MotionEvent.ACTION_MOVE) {

                double moveSize = Math.pow(_draggerStartX-event.getRawX(),2) + Math.pow(_draggerStartY-event.getRawY(),2);
                if (!isOutOfScreen && moveSize < 600) {
                    return false;
                }
                else {
                    waitingLongClick = false;

                    _isDragging = true;

                    _deltaX = event.getRawX() - _draggerX;
                    _deltaY = event.getRawY() - _draggerY;

                    _paramsBtn.x = _draggerStartPointX - Math.round(event.getRawX() - _draggerStartX);
                    _paramsBtn.y = _draggerStartPointY + Math.round(event.getRawY() - _draggerStartY);

                    _draggerX = event.getRawX();
                    _draggerY = event.getRawY();

                    if (isOutOfScreen) {
                        if (VR.isListening())
                            VR.get().abort();
                        if (_paramsBtn.x < screenWidth - _layoutBtnBox.getWidth() && _paramsBtn.x > 0) {
							/*
							isOutOfScreen = false;
							View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
							bbb.setBackgroundResource(R.drawable.button_free);		
							startAutoHideTimer();
							*/
                        }
                    }
                    else {
                        if (_paramsBtn.x > screenWidth - _layoutBtnBox.getWidth()) {
                            //_paramsBtn.x = screenWidth - _layoutBtnBox.getWidth()/3;
                            isOutOfScreen = true;
                            _btnSuzie.post(new Runnable() {
                                @Override
                                public void run() {
                                    View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
                                    bbb.setBackgroundResource(R.drawable.button_hidden_right);
                                }
                            });
                            stopAutoHideTimer();
                        }
                        if (_paramsBtn.x < 0) {
                            //_paramsBtn.x = -_layoutBtnBox.getWidth()*2/3;
                            isOutOfScreen = true;
                            _btnSuzie.post(new Runnable() {
                                @Override
                                public void run() {
                                    View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
                                    bbb.setBackgroundResource(R.drawable.button_hidden_left);
                                }
                            });
                            stopAutoHideTimer();
                        }
                    }
                    if (_paramsBtn.y > screenHeight - _layoutBtnBox.getHeight() + buttonPadding) {
                        _paramsBtn.y = screenHeight - _layoutBtnBox.getHeight() + buttonPadding;
                    }
                    if (_paramsBtn.y < actionBarHeight) {
                        _paramsBtn.y = actionBarHeight;
                    }

                    saveLastPos();

                    _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);

                    hideOptions();

                    result = true;
                }
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                ///////////////////
                waitingLongClick = false;

                if (_isDragging) {
                    _isDragging = false;
                    _btnSuzie.post(new Runnable() {
                        @Override
                        public void run() {
                            magneticRestore(false);
                        }
                    });

                } else {
                    if (waitingSimpleClick) {
                        waitingSimpleClick = false;
                        onButtonClick();
                    }
                }
                ///////////////////
                return true;
            }
        } catch (Exception ex) {
        }
        return result;
    }

    void onButtonClick() {
/*
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent it=new Intent(MainActivity.SHOW_TRAFFIC);
                it.setClass(App.self, MainActivity.class);
                it.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                Utils.startActivityFromNowhere(it);
            }
        }, 5000);
        */
        Log.d(TAG,"popup onButtonClick()");

        if (!VR.isRinging() && _paramsOptions.alpha < 0.1f ) {

            if (VR.isListening()) {
                VR.get().stop();
                return;
            }

            if (mag != null)
                _layoutBubles.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mag != null)
                            mag.hideError();
                    }
                });

            if (!(App.self.getActiveActivity() instanceof SmsActivity)) {
                service.onSuzieClick();
                return;
            }

            vr = VR.get();

            if (vr!=null) {

                //showBusy();

                if (vr.isStarted()) {
                    Log.d(TAG, "Suzie.click stop");

                    //vr.stop();
                    vr.stop();
                    return;
                }

                Log.d(TAG, "Suzie.click start");

                vr.start(true);
            }
        }
    }


    private void onBubleQueryClick() {
        hideBubles(false, true);
        service.abortOperation(Abortable.BY_FB_BUBBLE_CLICK);
    }

    private void onBubleAnswerClick() {
        hideBubles(false, true);
        service.abortOperation(Abortable.BY_FB_BUBBLE_CLICK);
    }

    private void onBubleWeatherClick() {
        hideBubles(true, true);
        service.abortOperation(Abortable.BY_WEATHER_CLICK);
    }

    private void onBubleCloseMessageClick() {
        service.abortOperation(0);
    }

    int suziePictureNum = 0;
    private void onOptionSettingsClick() {
		/*
		suziePictureNum++;
		View v = _layoutBtnBox.findViewById(R.id.btnSuzie);
		if (suziePictureNum == 0)
			v.setBackgroundResource(R.drawable.button_free);		
		else if (suziePictureNum == 1)
			v.setBackgroundResource(R.drawable.button_free1);
		else if (suziePictureNum == 2)
			v.setBackgroundResource(R.drawable.button_free2);
		else if (suziePictureNum == 3)
			v.setBackgroundResource(R.drawable.button_free3);
		else if (suziePictureNum == 4)
			v.setBackgroundResource(R.drawable.button_free4);
		else if (suziePictureNum == 5)
			v.setBackgroundResource(R.drawable.button_free5);
		else if (suziePictureNum == 6)
			v.setBackgroundResource(R.drawable.button_free6);
		else if (suziePictureNum == 7)
			v.setBackgroundResource(R.drawable.button_free7);
		else {
			v.setBackgroundResource(R.drawable.button_free);
			suziePictureNum = 0;
		}
		*/

        Intent it = new Intent(App.self, PrefsActivity.class);
        startNestedActivity(service,it);

        hideOptions();
        //service.doUnderstanding( Arrays.asList(new String [] {"call somebody"}));

    }

    private void simulateRecognition(String cmd) {
        VR vr = VR.get();
        if (vr == null)
            return;

        ArrayList<String> matches = new ArrayList<String>();
        matches.add(cmd);
        ArrayList<String> new_matches = vr.teachReplaceTeaching(matches);
        int res;
        if (new_matches == null)
            res = VR.RESULT_RUN_INTENT;
        else
            res = Activity.RESULT_OK;

        final Intent it = new Intent();
        it.putExtra(RecognizerIntent.EXTRA_RESULTS, new_matches);
        it.putExtra(VR.EXTRA_RESULT_NO_REPLACEMENT, matches);
        service.onActivityResult(VR.VOICE_RECOGNITION_REQUEST_CODE, res, it);
    }

    private void onOptionTeachClick() {
        App.self.hideNfyScreen();
        simulateRecognition(_theApp.getString(R.string.P_cmd_teach));
    }

    private void onOptionCloseClick() {
        App.self.hideNfyScreen();
        (new Analytics(service)).trackButtonPress("turn_off_floating_button_garbage");
        App.self.setBooleanPref(PrefConsts.PF_FL_BUTTON, false);
        hideOptions();
        hideSuzie(HideAnimation.ToSide);
    }

    private void onOptionOpenRobinClick() {
        App.self.hideNfyScreen();
        //service.doUnderstanding( Arrays.asList(new String [] {"call somebody"}));
        Intent it2 = new Intent(App.self, MainActivity.class);
        it2.setAction(MainActivity.WAKE_UP);
        Utils.startActivityFromNowhere(it2);

    }

    private void onOptionOpenMapClick() {
        App.self.hideNfyScreen();
        Intent it2 = new Intent(App.self, MainActivity.class);
        it2.setAction(MainActivity.OPEN_MAP);
        Utils.startActivityFromNowhere(it2);
    }

    /*********************************************************************************************************/
    /*********************************************************************************************************/
    /*********************************************************************************************************/
    interface LayoutChangeListener {
        public void onChange(int width, int height);
    }

    // Automatically drag button to right or left
    private void magneticRestore(boolean toOutOfScreen) {

        final int tick = 20; // ms

        final Interpolator ip_y = new OvershootInterpolator();
        //final Interpolator ip_y = new LinearInterpolator();

        final Interpolator ip_x = new OvershootInterpolator();
        //final Interpolator ip_x = new AnticipateInterpolator();
        //final Interpolator ip_x = new OvershootInterpolator();
        //final Interpolator ip_x = new AccelerateInterpolator();
        WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        int dest_x, dest_y, maxWidth = metrics.widthPixels - _layoutBtnBox.getWidth();

        if (_deltaX > -5 && _deltaX < +5)
            if (_paramsBtn.x < maxWidth/2)
                _deltaX = +5;
            else
                _deltaX = -5;

        //if (_deltaX > 0)
        if (_paramsBtn.x < maxWidth/2) {
            if (toOutOfScreen || (_paramsBtn.x < 0)) {
                dest_x = -_layoutBtnBox.getWidth()*2/3;
                //if (!toOutOfScreen)
                //_paramsBtn.x = 0;
                isOutOfScreen = true;
                View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
                bbb.setBackgroundResource(R.drawable.button_hidden_left);
                stopAutoHideTimer();
            }
            else {
                dest_x = 0;
                isOutOfScreen = false;
                View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
                bbb.setBackgroundResource(R.drawable.button_free);
                startAutoHideTimer();
            }
            dest_y = _paramsBtn.y - Math.round(_deltaY * (dest_x - _paramsBtn.x)/Math.abs(_deltaX));
        }
        else {
            if (toOutOfScreen || (_paramsBtn.x > maxWidth)) {
                dest_x = metrics.widthPixels - _layoutBtnBox.getWidth()/3;
                //if (!toOutOfScreen)
                //_paramsBtn.x = metrics.widthPixels - _layoutBtnBox.getWidth();
                isOutOfScreen = true;
                View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
                bbb.setBackgroundResource(R.drawable.button_hidden_right);
                stopAutoHideTimer();
            }
            else {
                dest_x = maxWidth;
                isOutOfScreen = false;
                View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
                bbb.setBackgroundResource(R.drawable.button_free);
                startAutoHideTimer();
            }
            dest_y = _paramsBtn.y + Math.round(_deltaY * (dest_x - _paramsBtn.x)/Math.abs(_deltaX));
        }

        if (dest_x == _paramsBtn.x && dest_y == _paramsBtn.y)
            return;

        // total length of animation in [ms]
        //final float totalLength = 700;

        // generates step size
        // when distance long (about maxWidth/2) step equals 0.1
        // when distance short step equals 1
        float step = 0.05f;//Math.abs(dest_x - _paramsBtn.x);
        //if (step < 1)
        //step = 1;
        //step = maxWidth*tick/(2*step*totalLength);
		/*
		if (dest_x > screenWidth - _layoutBtnBox.getWidth())
			dest_x = screenWidth - _layoutBtnBox.getWidth();
		if (dest_x < 0)
			dest_x = 0;
			*/
        if (dest_y > screenHeight - _layoutBtnBox.getHeight() + buttonPadding)
            dest_y = screenHeight - _layoutBtnBox.getHeight() + buttonPadding;
        if (dest_y < actionBarHeight)
            dest_y = actionBarHeight;

        final int x1 = _paramsBtn.x;
        final int y1 = _paramsBtn.y;

        if (isOutOfScreenInPreviousPressing && !isOutOfScreen)
            onButtonClick();

        class MyTimerTask extends TimerTask {

            float pos, step;
            int x2;
            int y2;
            float dy;

            public MyTimerTask(float pos, int x2, int y2, float step, float dy) {
                this.pos = pos;
                this.x2 = x2;
                this.y2 = y2;
                this.step = step;
                this.dy = dy;
            }

            @Override
            public void run() {
                _btnSuzie.post(new Runnable() {

                    @Override
                    public void run() {

                        boolean run = true;

                        pos += step;
                        if (pos > 1) {
                            pos = 1;
                            run = false;
                            _paramsBtn.x = Math.round(x2);
                            //_paramsBtn.y = Math.round(y2);
                        }
                        else {
                            _paramsBtn.x = Math.round(x1 + (x2 - x1) * ip_x.getInterpolation(pos));
                            float yyy = ip_y.getInterpolation(pos);
                            if (yyy > dy) {
                                _paramsBtn.y = Math.round(y1 + (y2 - y1) * yyy);
                                dy = yyy;
                            }
                            else
                                _paramsBtn.y = Math.round(y1 + (y2 - y1) * (dy+dy-yyy));
                        }
						
						/*
						if (_paramsBtn.x > screenWidth - _layoutBtnBox.getWidth())
							_paramsBtn.x = screenWidth - _layoutBtnBox.getWidth();
						if (_paramsBtn.x < 0)
							_paramsBtn.x = 0;
							*/

                        if (_paramsBtn.y > screenHeight - _layoutBtnBox.getHeight() + buttonPadding)
                            _paramsBtn.y = screenHeight - _layoutBtnBox.getHeight() + buttonPadding;
                        if (_paramsBtn.y < actionBarHeight)
                            _paramsBtn.y = actionBarHeight;

                        _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);

                        if (run)
                            new Timer().schedule(new MyTimerTask(pos, x2, y2, step, dy), tick);
                        else {
                            saveLastPos();
                            animationWork = false;
                        }
                    }
                });
            }

        }

        new Timer().schedule(new MyTimerTask(0, dest_x, dest_y, step, 0), tick);
    }

    private boolean animateButton(int time, final Interpolator i, final Moving f1, final Rotation f2, final Stretching f3, final Runnable afterThat) {
        Log.d(TAG,"!animateButton");
        if (_btnSuzie == null || _paramsBtn == null || (f1 == null && f2 == null && f3 == null))
            return false;

        WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        final int startX = _paramsBtn.x;
        final int startY = _paramsBtn.y;

        final int tick = 25; // ms
        float tt1 = time;
        float tt2 = tick;
        final float step = 1/(tt1/tt2); //

        Log.d(TAG,"!!!animateButton");

        final Timer timer = new Timer();
        class MyTimerTask extends TimerTask {

            float pos=0;

            public MyTimerTask(float pos) {
                this.pos = pos;
            }


            @SuppressLint("NewApi")
            @Override
            public void run() {

                _btnSuzie.post(new Runnable() {

                    @Override
                    public void run() {

                        Log.d(TAG,"v.post(new Runnable() :"+pos);

                        if (!animationWork)
                            return;

                        pos += step;
                        float t = pos;
                        if (i != null)
                            t = i.getInterpolation(t);

                        if (f1 != null) {
                            _paramsBtn.x = f1.getX(t);
                            _paramsBtn.y = f1.getY(t);
                        }
                        else {
                            _paramsBtn.x = startX;
                            _paramsBtn.y = startY;
                        }
                        if (f2 != null) {
                            if (Utils.isAndroid3orAbove)
                                _btnSuzie.setRotation(f2.getRotationAngle(t));
                        }
                        if (f3 != null) {
                            android.view.ViewGroup.LayoutParams lp = _btnSuzie.getLayoutParams();
                            lp.width = Math.round(buttonWidth*f3.getWidthKoef(t));
                            lp.height = Math.round(buttonHeight*f3.getHeightKoef(t));
                            _paramsBtn.x += (buttonWidth-lp.width)/2;
                            _paramsBtn.y += buttonHeight-lp.height;
                            _btnSuzie.setLayoutParams(lp);
                        }

                        _paramsBtn.alpha = 1;
                        _layoutBtnBox.setVisibility(View.VISIBLE);
                        _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);

                        if (pos < 1)
                            timer.schedule(new MyTimerTask(pos), tick);
                        else {
                            if (afterThat!=null)
                                _btnSuzie.post(afterThat);
                            else
                                restoreAfterAnimation();
                            return;
                        }
                    }
                });
            }
        };

        new MyTimerTask(0).run();
        //timer.schedule(new MyTimerTask(0), tick);

        return true;
    }

    // animate show/hide
    private boolean animateView(
            boolean fade,
            ViewGroup v,
            WindowManager.LayoutParams p,
            int newX, int newY
    ) {
        return  animateView(
                fade,
                v,
                p,
                newX, newY,
                null
        );
    }

    public void breakAnimation() {
        restoreAfterAnimation();
    }

    @SuppressLint("NewApi")
    private void restoreAfterAnimation() {
        animationWork = false;
        if (Utils.isAndroid3orAbove)
            _btnSuzie.setRotation(0);
        //_paramsBtn.width = buttonWidth;
        //_paramsBtn.height = buttonHeight;
        android.view.ViewGroup.LayoutParams lp = _btnSuzie.getLayoutParams();
        lp.width = buttonWidth;
        lp.height = buttonHeight;
        _btnSuzie.setLayoutParams(lp);
        _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);
    }

    private boolean animateView(
            final boolean fade,
            final ViewGroup v,
            final WindowManager.LayoutParams p,
            final int newX, final int newY,
            final Runnable afterThat
    ) {
        Log.d(TAG,"!animateView");
        if (v == null || p == null)
            return false;

        if (fade &&	p.alpha < 0.1)
            return false;

        if (!fade && p.alpha > 0.9)
            return false;

        final int oldX = p.x;
        final int oldY = p.y;

        WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        Log.d(TAG,"!!!animateView");

        Runnable r= new Runnable() {

            @Override
            public void run() {
                Log.d(TAG,"MyTimerTask.run");

                Log.d(TAG,"v.post(new Runnable() :"+p.alpha);

                boolean run = true;
                if (fade) {
                    p.alpha -= 0.1;
                    if (p.alpha < 0.1) {
                        p.alpha = 0;
								/*
								for(int i=0; i<v.getChildCount(); i++)
									v.getChildAt(i).setVisibility(View.GONE);
									*/
                        v.setVisibility(View.GONE);
                        run = false;
                    }
                    if (newX != oldX)
                        p.x = Math.round((float)newX * (1-p.alpha) + oldX * p.alpha);
                    if (newY != oldY)
                        p.y = Math.round((float)newY * (1-p.alpha) + oldY * p.alpha);
                }
                else {
                    // canceled ???
                    if (p.alpha < 0.1)
                        return;

                    p.alpha += 0.1;
                    if (p.alpha > 1) {
                        p.alpha = 1;
                        run = false;
                    }
                    if (newX != oldX)
                        p.x = Math.round((float)newX * p.alpha + oldX * (1-p.alpha));
                    if (newY != oldY)
                        p.y = Math.round((float)newY * p.alpha + oldY * (1-p.alpha));
                }

                try {
                    _windowManager.updateViewLayout(v, p);
                } catch (Exception e) {
                    e.printStackTrace();
                    if (afterThat!=null)
                        afterThat.run();
                    return;
                }

                if (run)
                    v.postDelayed(this, 30);
                else {
                    if (afterThat!=null)
                        afterThat.run();
                }

            }
        };

        if (!fade)
            v.setVisibility(View.VISIBLE);


        v.postDelayed(r, 30);

        return true;
    }

    private void createButton() {
        _paramsBtn = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT,

                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,// | WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,

                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                //| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                //| WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
                ,

                PixelFormat.TRANSLUCENT);

        _paramsBtn.width = LayoutParams.WRAP_CONTENT;
        _paramsBtn.height = LayoutParams.WRAP_CONTENT;
        _paramsBtn.format = PixelFormat.TRANSLUCENT;
        _paramsBtn.gravity = Gravity.TOP | Gravity.RIGHT;
        _paramsBtn.y = actionBarHeight;
        _paramsBtn.x = 0;
        _paramsBtn.alpha = 0;

        _layoutBtnBox = new ScalableShort(_theApp.getApplicationContext());
        _layoutBtnBox.setLayoutParams(new WindowManager.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        _layoutBtnBox.setPadding(0, buttonPaddingInner, 0, buttonPaddingInner);

        DisplayMetrics dm=App.self.getDisplayMetrics();

        LayoutInflater inflater = (LayoutInflater)service.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
		/*if (App.self.isInCarMode() && App.self.getBooleanPref("car_mode_option_big_button")) {
			inflater.inflate(R.layout.suzie_btn_layout_big, _layoutBtnBox);
		} else {*/
        inflater.inflate(R.layout.suzie_btn_layout_small, _layoutBtnBox);
        //}

        _btnSuzie = (View) _layoutBtnBox.findViewById(R.id.btnSuzie);

        _btnSuzie.setOnTouchListener(this);

        _windowManager.addView(_layoutBtnBox, _paramsBtn);
        _layoutBtnBox.setVisibility(View.GONE);
    }

    private void createBubles() {
        if (_layoutBubles!=null) return;

        _paramsBubles = new WindowManager.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT,

                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,

                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                //| WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                //| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                ,

                PixelFormat.TRANSLUCENT);

        _paramsBubles.width = LayoutParams.MATCH_PARENT;
        _paramsBubles.height = LayoutParams.WRAP_CONTENT;
        _paramsBubles.format = PixelFormat.TRANSLUCENT;
        _paramsBubles.gravity = Gravity.TOP | Gravity.RIGHT;
        _paramsBubles.y = actionBarHeight;
        _paramsBubles.x = 0;
        _paramsBubles.alpha = 0;

        LayoutInflater inflater = (LayoutInflater)service.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        _layoutBubles = (LinearLayout) inflater.inflate(R.layout.suzie_bubles, null);

        _windowManager.addView(_layoutBubles, _paramsBubles);

        _layoutBubles.findViewById(R.id.buble_query).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBubleQueryClick();
            }

        });
        _layoutBubles.findViewById(R.id.buble_answer).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBubleAnswerClick();
            }

        });
        _layoutBubles.findViewById(R.id.buble_weather).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onBubleWeatherClick();
            }

        });
    }

    private void createOptions() {
        _paramsOptions = new WindowManager.LayoutParams(
                LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT,

                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,

                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                        | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                        | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                //| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                ,

                PixelFormat.TRANSLUCENT);

        _paramsOptions.width = LayoutParams.WRAP_CONTENT;
        _paramsOptions.height = LayoutParams.MATCH_PARENT;
        _paramsOptions.format = PixelFormat.TRANSLUCENT;
        _paramsOptions.gravity = Gravity.TOP | Gravity.RIGHT;
        _paramsOptions.y = actionBarHeight;
        _paramsOptions.x = 0;
        _paramsOptions.alpha = 0;

        LayoutInflater inflater = (LayoutInflater)service.getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        _layoutOptions = (RelativeLayout) inflater.inflate(R.layout.suzie_options, null);

        _layoutOptions.setPadding(0, buttonPaddingInner, 0, buttonPaddingInner);

        _layoutOptions.setVisibility(View.GONE);
        _windowManager.addView(_layoutOptions, _paramsOptions);
        _layoutOptions.setVisibility(View.GONE);

        options = new Option[_layoutOptions.getChildCount()];
        for(int i=0; i<_layoutOptions.getChildCount(); i++)
            options[i] = new Option();

        _layoutOptions.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    float y = event.getY();
                    for(int i = 0; i < _layoutOptions.getChildCount(); i++) {
                        View button = _layoutOptions.getChildAt(i);
                        if (button.getVisibility() != View.VISIBLE)
                            continue;

                        if ((y >= options[i].y) && (y <= options[i].y + button.getHeight())) {
                            button.performClick();
                            break;
                        }
                    }

                    return true;
                }
                return false;
            }
        });

        View v;
        v = _layoutOptions.findViewById(R.id.optionCloseButton);
        v.setClickable(true);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onOptionCloseClick();
            }

        });

        v = _layoutOptions.findViewById(R.id.optionOpenRobin);
        v.setClickable(true);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onOptionOpenRobinClick();
            }

        });

        v = _layoutOptions.findViewById(R.id.optionOpenMap);
        v.setClickable(true);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onOptionOpenMapClick();
            }

        });

        v = _layoutOptions.findViewById(R.id.optionTeach);
        v.setClickable(true);
        v.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                onOptionTeachClick();
            }

        });
    }

    public static void hideBubles(final boolean fast, final boolean hideWeatherAndMessage) {
        SuziePopup sp = get();
        if (sp == null||sp._layoutBtnBox==null)
            return;

        Log.d(TAG,"hideBubles");

        if (hideWeatherAndMessage)
            Log.d(TAG,"hide weather and message");

        sp._layoutBtnBox.post(
                new Runnable() {
                    @Override
                    public void run() {
                        SuziePopup sp = get();
                        if (sp!=null)
                            _hideBubles(sp, fast, hideWeatherAndMessage);
                    }
                }
        );
    }

    private static void _hideBubles(SuziePopup sp, boolean fast, boolean hideWeather) {

        sp.bubleWaitingAnswer = false;

        if (sp._layoutBubles == null)
            return;

        if (!hideWeather) {
            WeatherView bubleWeather = (WeatherView) sp._layoutBubles.findViewById(R.id.buble_weather);
            if (bubleWeather.getVisibility() == View.VISIBLE)
                return;
            if (!sp.bublesEnabled)
                return;
        }

        if (fast) {
            LinearLayout bubleQuery = (LinearLayout) sp._layoutBubles.findViewById(R.id.buble_query);
            bubleQuery.setVisibility(View.GONE);
            LinearLayout bubleAnswer = (LinearLayout) sp._layoutBubles.findViewById(R.id.buble_answer);
            bubleAnswer.setVisibility(View.GONE);

            sp._paramsBubles.alpha = 0;
            sp._layoutBubles.setVisibility(View.GONE);
            sp._windowManager.updateViewLayout(sp._layoutBubles, sp._paramsBubles);
        }
        else {
            sp.animateView(true, sp._layoutBubles, sp._paramsBubles, sp._paramsBubles.x, sp._paramsBubles.y);
        }
    }

    void onButtonLongClick() {
        if (!(App.self.getActiveActivity() instanceof SmsActivity))
            showOptions();
    }

    public void showOptions() {
        if (!isVisible())
            return;

        hideBubles(true, true);

        int[] allowedButtons = new int[] {/*R.id.optionTeach, */R.id.optionOpenRobin, R.id.optionOpenMap, R.id.optionCloseButton};

        // calculate distance between options buttons
        WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;
        int distanceBetweenOptions, suzieHeight = _layoutBtnBox.getHeight(), maxHeight = metrics.heightPixels - suzieHeight;
        if (_paramsBtn.y < maxHeight/2)
            distanceBetweenOptions = (int) Math.round(suzieHeight*1.1);
        else
            distanceBetweenOptions = (int) -Math.round(suzieHeight*1.1);

        // hide all buttons
        for(int i = 0; i < _layoutOptions.getChildCount(); i++) {
            View v = _layoutOptions.getChildAt(i);
            v.setVisibility(View.GONE);

            options[i].id = v.getId();
        }

        // show buttons
        int optionPos = _paramsBtn.y;
        for(int i = 0; i < allowedButtons.length; i++) {

            final View btn = (View) _layoutOptions.findViewById(allowedButtons[i]);

            optionPos += distanceBetweenOptions;

            for(int k = 0; k < _layoutOptions.getChildCount(); k++)
                if (options[k].id == allowedButtons[i]) {
                    options[k].x = 0;
                    options[k].y = optionPos;
                    break;
                }

            TranslateAnimation aa = new TranslateAnimation(0, 0, _paramsBtn.y, optionPos);
            aa.setDuration(200);
            aa.setFillAfter(true);
            btn.startAnimation(aa);

            btn.setVisibility(View.VISIBLE);
        }

        _paramsOptions.alpha = 1;
        _paramsOptions.x = _paramsBtn.x;
        _paramsOptions.y = 0;
        _layoutOptions.setVisibility(View.VISIBLE);
        _windowManager.updateViewLayout(_layoutOptions, _paramsOptions);
        _layoutOptions.setVisibility(View.VISIBLE);
    }

    public void hideOptions() {

        if (_layoutOptions == null)
            return;

        _paramsOptions.alpha = 0;
        _layoutOptions.setVisibility(View.GONE);
        _windowManager.updateViewLayout(_layoutOptions, _paramsOptions);
        _layoutOptions.setVisibility(View.GONE);
    }

    public static interface Moving {
        public int getX(float t);
        public int getY(float t);
    }

    public static interface Rotation {
        public int getRotationAngle(float t);
    }

    public static interface Stretching {
        public float getWidthKoef(float t);
        public float getHeightKoef(float t);
    }

    public class Move implements Moving {

        int x1;	int y1;	int x2;	int y2;

        public Move(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        public int getX(float t) {
            if (x1 == x2)
                return x2;
            return Math.round((float)x2 * t + (float)x1 * (1-t));
        }

        @Override
        public int getY(float t) {
            if (y1 == y2)
                return y2;
            return Math.round((float)y2 * t + (float)y1 * (1-t));
        }
    }

    public class Circle implements Moving {

        float x, y, r;  double phase;

        public Circle(int x, int y, int r, double phase) {
            this.x = x;
            this.y = y;
            this.r = r;
            this.phase = phase;
        }

        @Override
        public int getX(float t) {
            return (int)Math.round(x + r * Math.cos(t*2*Math.PI+phase));
        }

        @Override
        public int getY(float t) {
            return (int)Math.round(y + r * Math.sin(t*2*Math.PI+phase));
        }
    }

    public class Rotate implements Rotation {

        float degree1, degree2;

        public Rotate(int degree1, int degree2) {
            this.degree1 = degree1;
            this.degree2 = degree2;
        }

        @Override
        public int getRotationAngle(float t) {
            return Math.round(degree2 * t + degree1 * (1-t));
        }
    }

    public class Stretch implements Stretching {

        float h1, h2, w1, w2;

        public Stretch(float h1, float h2, float w1, float w2) {
            if (h1 < 0.1f)
                h1 = 0.1f;
            if (h2 < 0.1f)
                h2 = 0.1f;
            if (w1 < 0.1f)
                w1 = 0.1f;
            if (w2 < 0.1f)
                w2 = 0.1f;
            this.h1 = h1;
            this.h2 = h2;
            this.w1 = w1;
            this.w2 = w2;
        }

        @Override
        public float getWidthKoef(float t) {
            return w2 * t + w1 * (1-t);
        }

        @Override
        public float getHeightKoef(float t) {
            return h2 * t + h1 * (1-t);
        }
    }

    public void byeAnimation() {

        if (!isVisible())
            return;

        WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        final int x = _paramsBtn.x;
        final int y = _paramsBtn.y;

        animationWork = true;

        final int center_x = metrics.widthPixels / 2 - _layoutBtnBox.getWidth() / 2;
        int dest_x, degree;
        double p=0;
        if (x < center_x) {
            dest_x = center_x - 1;//_layoutBtnBox.getWidth();
            p = Math.PI;
            degree = -360;
        }
        else {
            dest_x = center_x + 1;//_layoutBtnBox.getWidth();
            p = 0;
            degree = +360;
        }
        final double phase = p;

        animateButton(
                500, // duration in ms
                new DecelerateInterpolator(),
                new Move(x, y, dest_x, y), // move
                new Rotate(0, degree), // rotate
                null, // stretch
                new Runnable() {

                    @Override
                    public void run() {
                        _deltaX = 0;
                        _deltaY = 0;
                        magneticRestore(true);
                    }
                });
    }

    public void jokeAnimation() {

        if (!isVisible())
            return;

        animationWork = true;

        jokeAnimationRepeat(2);
    }

    private void jokeAnimationRepeat(final int repeatCount) {

        final int x = _paramsBtn.x;
        final int y = _paramsBtn.y;

        animateButton(
                200, // duration in ms
                null,
                null, // move
                null, // rotate
                new Stretch(1, 0.8f, 1, 1),  // stretch
                new Runnable() {

                    @Override
                    public void run() {
                        animateButton(
                                200, // duration in ms
                                new DecelerateInterpolator(),
                                new Move(x, y, x, y-buttonHeight/2), // move
                                null,  // rotate
                                new Stretch(0.8f, 1, 1, 1),  // stretch
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        animateButton(
                                                150, // duration in ms
                                                new LinearInterpolator(),
                                                null, // move
                                                new Rotate(0, -360), // rotate
                                                null, // stretch
                                                new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        animateButton(
                                                                150, // duration in ms
                                                                new AccelerateInterpolator(),
                                                                new Move(x, y-buttonHeight/2, x, y), // move
                                                                null, // rotate
                                                                null, // stretch
                                                                new Runnable() {

                                                                    @Override
                                                                    public void run() {
                                                                        if (isVisible() && MyTTS.isSpeaking() && (repeatCount > 1))
                                                                            jokeAnimationRepeat(repeatCount-1);
                                                                        else
                                                                            restoreAfterAnimation();
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });

    }

    public void helloAnimation() {

        if (!isVisible())
            return;

        animationWork = true;

        helloAnimationRepeat(5);
    }

    private void helloAnimationRepeat(final int repeatCount) {

        final int x = _paramsBtn.x;
        final int y = _paramsBtn.y;

        animateButton(
                100, // duration in ms
                null,
                new Move(x, y, x-buttonWidth/2, y), // move
                null, // rotate
                new Stretch(1, 1, 1, 0),  // stretch
                new Runnable() {

                    @Override
                    public void run() {
                        animateButton(
                                100, // duration in ms
                                null,
                                new Move(x-buttonWidth/2, y, x, y), // move
                                null,  // rotate
                                new Stretch(1, 1, 0, 1),  // stretch
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        if (isVisible() && MyTTS.isSpeaking() && (repeatCount > 1))
                                            helloAnimationRepeat(repeatCount-1);
                                        else
                                            restoreAfterAnimation();
                                    }
                                });
                    }
                });

    }

    public void howareyouAnimation() {

        if (!isVisible())
            return;

        animationWork = true;

        howareyouAnimationRepeat(1);
    }

    private void howareyouAnimationRepeat(final int repeatCount) {

        final int x = _paramsBtn.x;
        final int y = _paramsBtn.y;

        animateButton(
                200, // duration in ms
                new AccelerateInterpolator(),
                null, // move
                null, // rotate
                new Stretch(1, 0.8f, 1, 1),  // stretch
                new Runnable() {

                    @Override
                    public void run() {
                        animateButton(
                                200, // duration in ms
                                new DecelerateInterpolator(),
                                new Move(x, y, x, y-buttonHeight/3), // move
                                null,  // rotate
                                new Stretch(0.8f, 1, 1, 1),  // stretch
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        animateButton(
                                                200, // duration in ms
                                                new AccelerateInterpolator(),
                                                null, // move
                                                null, // rotate
                                                new Stretch(1, 0.8f, 1, 1),  // stretch
                                                new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        animateButton(
                                                                200, // duration in ms
                                                                new DecelerateInterpolator(),
                                                                new Move(x, y-buttonHeight/3, x, y-2*buttonHeight/3), // move
                                                                null,  // rotate
                                                                new Stretch(0.8f, 1, 1, 1),  // stretch
                                                                new Runnable() {

                                                                    @Override
                                                                    public void run() {
                                                                        animateButton(
                                                                                200, // duration in ms
                                                                                new AccelerateInterpolator(),
                                                                                new Move(x, y-2*buttonHeight/3, x, y), // move
                                                                                new Rotate(0, 180),  // rotate
                                                                                null,  // stretch
                                                                                new Runnable() {

                                                                                    @Override
                                                                                    public void run() {
                                                                                        animateButton(
                                                                                                200, // duration in ms
                                                                                                new DecelerateInterpolator(),
                                                                                                null, // move
                                                                                                new Rotate(180, 360),  // rotate
                                                                                                null,  // stretch
                                                                                                new Runnable() {

                                                                                                    @Override
                                                                                                    public void run() {
                                                                                                        if (isVisible() && MyTTS.isSpeaking() && (repeatCount > 1))
                                                                                                            howareyouAnimationRepeat(repeatCount-1);
                                                                                                        else
                                                                                                            restoreAfterAnimation();
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });

    }

    public void fuckAnimation() {

        if (!isVisible())
            return;

        animationWork = true;

        fuckAnimationRepeat(10);
    }

    private void fuckAnimationRepeat(final int repeatCount) {

        final int x = _paramsBtn.x;
        final int y = _paramsBtn.y;

        animateButton(
                200, // duration in ms
                null,
                new Move(x, y, x, y-buttonHeight/5), // move
                null, // rotate
                new Stretch(1, 0.9f, 1, 1),  // stretch
                new Runnable() {

                    @Override
                    public void run() {
                        animateButton(
                                200, // duration in ms
                                null,
                                null, // move
                                new Rotate(0, 20),  // rotate
                                null,  // stretch
                                new Runnable() {

                                    @Override
                                    public void run() {
                                        animateButton(
                                                400, // duration in ms
                                                null,
                                                null, // move
                                                new Rotate(20, -20),  // rotate
                                                null,  // stretch
                                                new Runnable() {

                                                    @Override
                                                    public void run() {
                                                        animateButton(
                                                                200, // duration in ms
                                                                null,
                                                                null, // move
                                                                new Rotate(-20, 0),  // rotate
                                                                null,  // stretch
                                                                new Runnable() {

                                                                    @Override
                                                                    public void run() {
                                                                        animateButton(
                                                                                200, // duration in ms
                                                                                null,
                                                                                new Move(x, y-buttonHeight/5, x, y), // move
                                                                                null, // rotate
                                                                                new Stretch(0.9f, 1, 1, 1),  // stretch
                                                                                new Runnable() {

                                                                                    @Override
                                                                                    public void run() {
                                                                                        if (isVisible() && MyTTS.isSpeaking() && (repeatCount > 1))
                                                                                            fuckAnimationRepeat(repeatCount-1);
                                                                                        else
                                                                                            restoreAfterAnimation();
                                                                                    }
                                                                                });
                                                                    }
                                                                });
                                                    }
                                                });
                                    }
                                });
                    }
                });

    }

    private void restoreSuzie() {
        Log.d(TAG, "restoreSuzie "+_layoutBtnBox+" "+isVisible()+" "+App.self.isSuzieRequied());
        if (!(_layoutBtnBox == null/*||isVisible()*/))
            if (App.self.isSuzieRequied()) {

                hideBusy();

                Log.d(TAG, "Suzie.show");

                WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
                DisplayMetrics metrics = new DisplayMetrics();
                wm.getDefaultDisplay().getMetrics(metrics);
                screenWidth = metrics.widthPixels;
                screenHeight = metrics.heightPixels;

                isOutOfScreen = false;
                _layoutBtnBox.post(new Runnable() {

                    @Override
                    public void run() {

                        View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
                        bbb.setBackgroundResource(R.drawable.button_free);
                    }
                });

                boolean orientationLandscape = false;
                try {
                    orientationLandscape = service.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
                } catch (Exception e) {}

                if (suzieLastOrientationLandscape != orientationLandscape
                        || suzieLastX < 0 || suzieLastX > metrics.widthPixels - _btnSuzie.getWidth()
                        || suzieLastY < actionBarHeight || suzieLastY > metrics.heightPixels - _btnSuzie.getHeight() + buttonPadding) {
                    if (suzieLastLeft)
                        if (isOutOfScreen)
                            suzieLastX = metrics.widthPixels - _layoutBtnBox.getWidth()/3;
                        else
                            suzieLastX = metrics.widthPixels - _btnSuzie.getWidth();
                    else
                    if (isOutOfScreen)
                        suzieLastX = -_layoutBtnBox.getWidth()*2/3;
                    else
                        suzieLastX = 0;

                    suzieLastY = Math.round(metrics.heightPixels * suzieLastFromTop);
                    if (suzieLastY < actionBarHeight)
                        suzieLastY = actionBarHeight;
                    if (suzieLastY > metrics.heightPixels - _btnSuzie.getHeight() + buttonPadding)
                        suzieLastY = metrics.heightPixels - _btnSuzie.getHeight() + buttonPadding;
                }

                _paramsBtn.x = suzieLastX;
                _paramsBtn.y = suzieLastY;
                _paramsBtn.alpha = 0.1f;
                //_layoutBtnBox.setVisibility(View.VISIBLE);
                //_windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);

                animateView(
                        false,
                        _layoutBtnBox, _paramsBtn, suzieLastX, suzieLastY,
                        new Runnable() {

                            @Override
                            public void run() {

                                if (buttonHeight == 0)
                                    buttonHeight = _btnSuzie.getHeight();

                                if (buttonWidth == 0)
                                    buttonWidth = _btnSuzie.getWidth();

                                if (!isOutOfScreen)
                                    startAutoHideTimer();

                                _layoutBtnBox.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        PowerManager pm = (PowerManager) App.self.getSystemService(Context.POWER_SERVICE);
                                        if (!SendCmdHandler.isActive() && pm.isScreenOn() && !App.self.isPhoneLocked()
                                                && !App.self.getBooleanPref("isButtonUsed") && isVisible()
                                                && !MyTTS.isSpeaking() && App.self.isSuzieRequied()) {
                                            SharedPreferences.Editor prefsEditor = App.self.getPrefs().edit();
                                            prefsEditor.putBoolean("isButtonUsed", true);
                                            prefsEditor.commit();
                                            MyTTS.speakText(new MyTTS.BubblesInButtonOnly(R.string.P_suzie_introduction));
                                        }
                                    }
                                }, 2000);

                                //animationWork = true;
                                //helloAnimationRepeat(3);
                            }

                        }
                );

                vr = VR.create(service, mag, false, false);
            }
    }

    /*********************************************************************************************************/
    /********************************* MONITORING ACTIVE TASK ************************************************/
    /*********************************************************************************************************/

    // notify on active task changed
    private void onTopActivityChanged() {
        //hideBubles(true);
        if (lastTopActivity == null)
            return;

        String packageName = lastTopActivity;
        if (Utils.isEmpty(packageName))
            return;

        String hint = hints.findHint(packageName);
        if (VR.get() != null && VR.get().isListening())
            return;

        if (MyTTS.isSpeaking())
            return;

        if (_paramsBubles == null || _paramsBubles.alpha > 0)
            return;

        if (_paramsOptions == null || _paramsOptions.alpha > 0)
            return;

        if (App.self.isPhoneLocked())
            return;

        if (!App.self.isScreenOn())
            return;

        /*******************************************************************************************************/
        // for DEBUG ONLY !
		/*
		if (hint == null) {
			MyTTS.speakText(packageName);
			return;
		}
		*/

        if (Utils.isEmpty(hint))
            return;

        // play sound
		/*
		try {
	        RingtoneManager.getRingtone(_theApp, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)).play();
	    } catch (Exception e) {}
	    */

        // show info
        bubleAnswer(App.self.getString(R.string.P_hint_suzie)+" "+hint);

        // hide info
        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                hideBubles(false, true);
            }
        }, 4000);

        //MyTTS.speakText(App.self.getString(R.string.P_hint_suzie)+" "+hint);
    }

    private final int TIMER_INTERVAL = 5000;
    private String lastTopActivity = null;

    private class CheckTask extends TimerTask {

        @Override
        public void run() {
            if (!isVisible())
                return;

            ComponentName cn = Utils.getTopActivity();
            if (cn == null)
                return;

            String old = lastTopActivity;

            lastTopActivity = cn.getPackageName();
            if (old == null || lastTopActivity.compareTo(old) != 0)
                _layoutBubles.post(new Runnable() {
                    @Override
                    public void run() {
                        onTopActivityChanged();
                    }
                });
        }
    }

    public void updateOrientation() {
        if (!isVisible())
            return;

        breakAnimation();

        hideOptions();
        //hideBubles(true, true);

        WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        boolean orientationLandscape = service.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
        if (suzieLastOrientationLandscape != orientationLandscape
                || suzieLastX < 0 || suzieLastX > metrics.widthPixels - _btnSuzie.getWidth()
                || suzieLastY < actionBarHeight || suzieLastY > metrics.heightPixels - _btnSuzie.getHeight() + buttonPadding) {
            if (suzieLastLeft)
                if (isOutOfScreen)
                    suzieLastX = metrics.widthPixels - _layoutBtnBox.getWidth()/3;
                else
                    suzieLastX = metrics.widthPixels - _btnSuzie.getWidth();
            else
            if (isOutOfScreen)
                suzieLastX = -_layoutBtnBox.getWidth()*2/3;
            else
                suzieLastX = 0;

            suzieLastY = Math.round(metrics.heightPixels * suzieLastFromTop);
            if (suzieLastY < actionBarHeight)
                suzieLastY = actionBarHeight;
            if (suzieLastY > metrics.heightPixels - _btnSuzie.getHeight() + buttonPadding)
                suzieLastY = metrics.heightPixels - _btnSuzie.getHeight() + buttonPadding;
        }

        _paramsBtn.x = suzieLastX;
        _paramsBtn.y = suzieLastY;
        _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);

        ////////////////////////////
        _paramsBubles.y = _paramsBtn.y;
        _windowManager.updateViewLayout(_layoutBubles, _paramsBubles);

        LinearLayout bubleMessage = (LinearLayout) _layoutBubles.findViewById(R.id.buble_message);
        if (bubleMessage.getVisibility() != View.VISIBLE) {
            if (_paramsBtn.x > 0)
                _layoutBubles.setPadding(_layoutBtnBox.getWidth(), 0, 0, 0);
            else
                _layoutBubles.setPadding(0, 0, _layoutBtnBox.getWidth(), 0);
        }

        OnOrientationHandler ooh = CmdHandlerHolder.getOnOrientationHandler();
        if (ooh != null)
            ooh.OnOrientationChanged();
    }

    public void returnToScreen() {
        if (!isVisible())
            return;

        if (!isOutOfScreen)
            return;

        WindowManager wm = (WindowManager) _theApp.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        screenWidth = metrics.widthPixels;
        screenHeight = metrics.heightPixels;

        isOutOfScreen = false;
        View bbb = _layoutBtnBox.findViewById(R.id.btnSuzie);
        bbb.setBackgroundResource(R.drawable.button_free);
        startAutoHideTimer();
        if (suzieLastLeft)
            suzieLastX = metrics.widthPixels - _btnSuzie.getWidth();
        else
            suzieLastX = 0;

        _paramsBtn.x = suzieLastX;
        _paramsBtn.y = suzieLastY;
        _windowManager.updateViewLayout(_layoutBtnBox, _paramsBtn);
    };

    /*********************************************************************************************************/
    Timer autoHideTimer = null;
    long lastActivityTime = 0;

    public static void startAutoHideTimer() {
        final SuziePopup sp = get();
        if (sp == null)
            return;

        if (sp.autoHideTimer != null)
            return;

        if (!isVisible(true))
            return;

        if (App.self.isInCarMode() || true/*!App.self.getBooleanPref("autoHideSuzie")*/)
            return;

        sp.autoHideTimer = new Timer();
        sp.lastActivityTime = System.currentTimeMillis();
        sp.autoHideTimer.schedule(new TimerTask() {

            @Override
            public void run() {

                Log.d("AUTOHIDE", "check");

                long now = System.currentTimeMillis();
                if (now - sp.lastActivityTime > 60000) {
                    stopAutoHideTimer();
                    if (bublesEnabled && isVisible(true)
                            && !App.self.isInCarMode()
                            && false/*App.self.getBooleanPref("autoHideSuzie")*/
                            && sp._btnSuzie != null) {
                        sp._btnSuzie.post(new Runnable() {
                            @Override
                            public void run() {
                                _deltaX = 0;
                                _deltaY = 0;
                                sp.magneticRestore(true);
                            }
                        });
                        Log.d("AUTOHIDE", "AUTO HIDE");
                    }
                }
            }
        }, 60000, 60000);

        Log.d("AUTOHIDE", "start timer");
    }

    public static void stopAutoHideTimer() {
        SuziePopup sp = get();
        if (sp == null)
            return;

        if (sp.autoHideTimer != null)
            sp.autoHideTimer.cancel();

        sp.autoHideTimer = null;

        Log.d("AUTOHIDE", "stop timer");
    }

    public static void resetAutoHideTimer() {
        SuziePopup sp = get();
        if (sp == null)
            return;

        sp.lastActivityTime = System.currentTimeMillis();

        Log.d("AUTOHIDE", "reset timer");
    }

    /*********************************************************************************************************/
}