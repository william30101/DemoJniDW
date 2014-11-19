package com.example.demojni;

import java.util.ArrayList;
import java.util.List;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class DrawView extends View {
    private static final String TAG = "draw";
    
    List<Point> points = new ArrayList<Point>();
    static List<Point> cornerPoints = new ArrayList<Point>();
    static List<Point> outterPoints = new ArrayList<Point>();
    
    private boolean cornerCompass = false;
    
    private boolean drawOutter = false;
    
   
	public boolean isDrawOutter() {
		return drawOutter;
	}

	public void setDrawOutter(boolean drawOutter) {
		this.drawOutter = drawOutter;
	}

	public boolean isCornerCompass() {
		return cornerCompass;
	}

	public void setCornerCompass(boolean cornerCompass) {
		this.cornerCompass = cornerCompass;
	}

	public List<Point> getPoints() {
		return points;
	}

	public void setPoints(List<Point> points) {
		this.points = points;
	}

	Paint paint = new Paint();
    
	Path path=new Path();
	Path pathOutter=new Path();
	Canvas canvas;
	
	Point cornerP = new Point();
	
	float firsttouchX;
	float firsttouchY;
	float current_TouchX;
	float current_TouchY;
	int count = 0;
	static int pointIndex = 0;
	
	public boolean findCorner = false;
	
	public DrawView(Context context, AttributeSet attrs) {
		super(context, attrs);
		
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);
    }

    @SuppressLint("DrawAllocation") @Override
    public void onDraw(Canvas canvas) {
    	super.onDraw(canvas);
    	
    	//Point point = new Point();
    	
    	
    	
    	
        for (Point point : points) {
        	if (point.getCompass() < 45)
        		paint.setColor(Color.RED);
        	else if (point.getCompass() >= 45)
        		paint.setColor(Color.BLUE);
        	
        	paint.setStyle(Paint.Style.STROKE);
        	canvas.drawCircle(point.x, point.y, 5, paint);
            Log.d(TAG, "index : " + point.getIndex()+"Painting: "+point );
        }
        
        // Draw the Corner that we finded. 
        if (findCorner)
        {
        	paint.setColor(Color.GREEN);
        	 for (Point corPoint : cornerPoints) {
        		 //canvas.drawCircle(corPoint.x, corPoint.y, 5, paint);
             	
             	path.lineTo(corPoint.x, corPoint.y);
             	path.moveTo(corPoint.x, corPoint.y);
             	canvas.drawPath(path, paint);
             	
             	Log.d(TAG, "index : " + corPoint.getIndex()+" corner path Painting: "+corPoint );
        	 }
        	
        }
        
        if (drawOutter)
        {
        	paint.setColor(Color.YELLOW);
        	 for (Point outPoint : outterPoints) {
        		 canvas.drawCircle(outPoint.x, outPoint.y, 5, paint);
             	
        		 pathOutter.lineTo(outPoint.x, outPoint.y);
        		 pathOutter.moveTo(outPoint.x, outPoint.y);
             	canvas.drawPath(pathOutter, paint);
             	
             	Log.d(TAG, "index : " + outPoint.getIndex()+" outter path Painting: "+outPoint );
        	 }
        	
        }
        
        
        /*

		int row = 10;
		int col = 10;
    	
		if (drawMap)
		{
			for(int i=0; i<row; i++){
				for(int j=0; j<col; j++){
					if(map[i][j] == 0){							
						paint.setColor(Color.WHITE);			
						paint.setStyle(Style.FILL);				
						canvas.drawRect(fixMapData+j*(span+1), fixMapData+i*(span+1), 
								fixMapData+j*(span+1)+span,fixMapData+i*(span+1)+span, paint);
					}
					else if(map[i][j] == 1){//�¦�
						paint.setColor(Color.BLACK);
						paint.setStyle(Style.FILL);
						canvas.drawRect(fixMapData+j*(span+1), fixMapData+i*(span+1),
								fixMapData+j*(span+1)+span, fixMapData+i*(span+1)+span, paint);					
					}
				}
			}
		}*/
        
    	/*
        if (count == 0){
			path.moveTo(firsttouchX, firsttouchY);
			count++;
        }
//        */
//        current_TouchX = point.x;
//        current_TouchY = point.y;
//        
//        points.add(point);
        //invalidate();
//        
    	//paint.setStyle(Paint.Style.STROKE);
    	//paint.setStrokeWidth(1);
       // path.lineTo(current_TouchX, current_TouchY);
		//path.moveTo(current_TouchX, current_TouchY);
        //canvas.drawPath(path, paint);
    }

	@SuppressLint("ClickableViewAccessibility") @Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub
		Point point = new Point();
		
        point.x = event.getX();
        point.y = event.getY();
        point.setIndex(pointIndex);
        if (cornerCompass)
        	point.setCompass(90);
        else
        	point.setCompass(0);
        point.setBeSelected(false);
		pointIndex++;
		
		points.add(point);
		/*synchronized (pointIndex) {

			try {
				point.setIndex(pointIndex);
				pointIndex++;
			} catch (Exception e) {
				e.printStackTrace();
			}

		}*/
        
        //if (count == 0){
	    //   firsttouchX  = point.x;
	    //    firsttouchY  = point.y;
		//	path.moveTo(firsttouchX, firsttouchY);
		//	count++;
       // }
     
        //current_TouchX = point.x;
        //current_TouchY = point.y;
        
        //points.add(point);
        invalidate();

        Log.d(TAG, "point: " + point);
		return super.onTouchEvent(event);
	}
	
	public void drawCorner(Point[] cornerCordinate)
	{
		
		for (int i=0;i<cornerCordinate.length;i++)
			cornerPoints.add(cornerCordinate[i]);
	}
	
	public void drawOutterLine(Point[] cornerCordinate , float outterSize)
	{
		
		float outterXSize = 0, outterYSize=0;
		Point outPoint[] = new Point[cornerCordinate.length ];
		
		//outPoint[0].x = 0;
		//outPoint[0].y = 0;
		//outPoint[0].setBeSelected(false);
		//outPoint[0].setCompass(0);
		//outPoint[0].setIndex(0);
		
		for (int i=0;i<cornerCordinate.length;i++)
		{	
			outPoint[i] = new Point();
			if (i == 0)
			{
				outterXSize = -outterSize;
				outterYSize = -outterSize;
			}
			else if (i == 1)
			{
				outterXSize = outterSize;
				outterYSize = -outterSize;
			}
			else if (i == 2)
			{
				outterXSize = outterSize;
				outterYSize = outterSize;
			}
			else if (i == 3)
			{
				outterXSize = -outterSize;
				outterYSize = outterSize;
			}
			
			//outPoint.x = cornerCordinate[i].x + outterXSize;
			//outPoint.y = cornerCordinate[i].y + outterYSize;

			if (i == (cornerCordinate.length -1))
			{
				outPoint[i].x = outPoint[0].x;
				outPoint[i].y = outPoint[0].y;
				outPoint[i].setIndex(outPoint[0].getIndex()) ;
				outPoint[i].setCompass(outPoint[0].getCompass()) ;
				outPoint[i].setBeSelected(outPoint[0].isBeSelected()) ;
				outterPoints.add(outPoint[i]);
			}
			else if (i == (cornerCordinate.length -2))
			{
				double d0 = eulaEquationMinus(outPoint[0],outPoint[2]);
				
				
				
				outPoint[i].x = outPoint[0].x;
				outPoint[i].y = outPoint[cornerCordinate.length -3].y;
				
				
				outPoint[i].setIndex(cornerCordinate[i].getIndex()) ;
				outPoint[i].setCompass(cornerCordinate[i].getCompass()) ;
				outPoint[i].setBeSelected(cornerCordinate[i].isBeSelected()) ;
				outterPoints.add(outPoint[i]);
			}
			else
			{
				outPoint[i].x = cornerCordinate[i].x + outterXSize;
				outPoint[i].y = cornerCordinate[i].y + outterYSize;
				outPoint[i].setIndex(cornerCordinate[i].getIndex()) ;
				outPoint[i].setCompass(cornerCordinate[i].getCompass()) ;
				outPoint[i].setBeSelected(cornerCordinate[i].isBeSelected()) ;
				outterPoints.add(outPoint[i]);
			}
		}
		
		
		
	}
	
	private double eulaEquationMinus(Point Axis, Point refAxis)
	{
		double distance = 0;
		distance = Math.sqrt(( Math.abs((refAxis.x- Axis.x) * (refAxis.x- Axis.x)) + Math.abs((refAxis.y- Axis.y) * (refAxis.y- Axis.y))));
		return distance;
	}
	
}


