package com.magnifis.parking;

import com.magnifis.parking.utils.Utils;

import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.TextView;

class ScalingParams {
	protected ViewGroup.LayoutParams layoutParams=null;
	protected double xScale=1, yScale=1;
	protected Float textSize=null;
	
	public ViewGroup.LayoutParams getLayoutParams() {
		return layoutParams;
	}
	public void setLayoutParams(ViewGroup.LayoutParams layoutParams) {
		this.layoutParams = layoutParams;
	}
	public double getxScale() {
		return xScale;
	}
	public void setxScale(double xScale) {
		this.xScale = xScale;
	}
	public double getyScale() {
		return yScale;
	}
	public void setyScale(double yScale) {
		this.yScale = yScale;
	}
	public Float getTextSize() {
		return textSize;
	}
	public void setTextSize(Float textSize) {
		this.textSize = textSize;
	}
	
	public ScalingParams() {}

	public ScalingParams(View v, LayoutParams lp) {
	    if (lp!=null) setLayoutParams(Utils.cloneLayoutParams(lp));
	    if (v instanceof TextView) {
	      TextView tv=(TextView)v;
	      setTextSize(tv.getTextSize());
	    }
	    leftPadding=v.getPaddingLeft();
	    topPadding=v.getPaddingTop();
	    bottomPading=v.getPaddingBottom();
	    rightPadding=v.getPaddingRight();
	 }

	protected int leftPadding=0, topPadding=0, bottomPading=0, rightPadding=0;

	public int getLeftPadding() {
		return leftPadding;
	}
	public void setLeftPadding(int leftPadding) {
		this.leftPadding = leftPadding;
	}
	public int getTopPadding() {
		return topPadding;
	}
	public void setTopPadding(int topPadding) {
		this.topPadding = topPadding;
	}
	public int getBottomPading() {
		return bottomPading;
	}
	public void setBottomPading(int bottomPading) {
		this.bottomPading = bottomPading;
	}
	public int getRightPadding() {
		return rightPadding;
	}
	public void setRightPadding(int rightPadding) {
		this.rightPadding = rightPadding;
	} 
	
	
}