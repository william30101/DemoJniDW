package com.example.demojni;

//import java.util.ArrayList;
//import java.util.List;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {
    private static final String TAG = "william";

    List<Point> points = new ArrayList<Point>();
    Paint paint = new Paint();


	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
        //setFocusable(true);
       // setFocusableInTouchMode(true);

        //this.setOnTouchListener(this);

        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
    	//super.onDraw(canvas);
    	//if(!isInEditMode())
    	//{
    	
    	//}
    	//canvas.drawColor(Color.GRAY);
    	
    	//paint.setColor(Color.BLACK);
		//paint.setStyle(Style.STROKE);
		
		//canvas.drawRect(5, 55, 100, 100, paint);
        for (Point point : points) {
            canvas.drawCircle(point.x, point.y, 5, paint);
             Log.d(TAG, "Painting: "+point);
        }
    }
/*
    public boolean onTouch(View view, MotionEvent event) {
        // if(event.getAction() != MotionEvent.ACTION_DOWN)
        // return super.onTouchEvent(event);
        Point point = new Point();
        point.x = event.getX();
        point.y = event.getY();
        points.add(point);
        invalidate();
        Log.d(TAG, "point: " + point);
        return true;
    }
*/


    
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		//Log.i(TAG,"x = " + event.getX() + " y=" + event.getY());
		
		
		Point point = new Point();
        point.x = event.getX();
        point.y = event.getY();
        points.add(point);
        invalidate();
        Log.d(TAG, "point: " + point);
		return super.onTouchEvent(event);
	}


}


