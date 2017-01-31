package com.magnifis.parking.views;

import com.magnifis.parking.R;

import android.content.Context;

import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.TextView;



// from http://stackoverflow.com/questions/2617266/how-to-adjust-text-font-size-to-fit-textview
public class FontFitTextView extends TextView {

	public FontFitTextView(Context context) {
		this(context, null);
	}

	
	public FontFitTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		initialise();
	}


    public FontFitTextView(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }
    
    Float maxSize=null;

    private void initialise() {
        mTestPaint = new Paint();
        mTestPaint.set(this.getPaint());
        //max size defaults to the initially specified text size unless it is too small
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText(String text, int textWidth) 
    { 
        if (textWidth <= 0)
            return;
        
        if (maxSize==null) {
           if (getTag(R.string.scaled)!=null) 
        	  maxSize=getTextSize();
           else
        	  return;
        }
        
        int targetWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
        float hi = maxSize;//100;
        float lo = 2;
        final float threshold = 0.5f; // How close we have to be

        mTestPaint.set(this.getPaint());

        while((hi - lo) > threshold) {
            float size = (hi+lo)/2;
            mTestPaint.setTextSize(size);
            if(mTestPaint.measureText(text) >= targetWidth) 
                hi = size; // too big
            else
                lo = size; // too small
        }
        // Use lo so that we undershoot rather than overshoot
        this.setTextSize(TypedValue.COMPLEX_UNIT_PX, lo);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
        int height = getMeasuredHeight();
        refitText(this.getText().toString(), parentWidth);
        this.setMeasuredDimension(parentWidth, height);
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    //Attributes
    private Paint mTestPaint;
	
}
