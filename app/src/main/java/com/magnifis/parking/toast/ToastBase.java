package com.magnifis.parking.toast;

import com.magnifis.parking.App;
import com.magnifis.parking.Config;
import com.magnifis.parking.Log;
import com.magnifis.parking.R;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import static com.magnifis.parking.utils.Utils.*;

public abstract class ToastBase {
	
	public static class LayoutOptions {
	  public boolean dimensionsIsDP=true; // dimensions is PX 
	  public int leftMargin=0, topMargin=15;
	  public int width=ViewGroup.LayoutParams.WRAP_CONTENT, height=ViewGroup.LayoutParams.WRAP_CONTENT;
	  public int vetricalPosition=1; // 0 -- center , 1 -- top, 2 -- bottom
	  public int horizontalPosition=0; // 0 -- center , 1 -- left, 2 -- right
	  public Rect contentPadding=new Rect(17, 13, 17, 13);
	}
		
	abstract public void hide();
	
	abstract public void show();
	
	abstract public <T extends View> T getContentView();
	
}
