package com.example.demojni;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class FindCornerAlgorithm {

	 List<Point> points = new ArrayList<Point>();
	 List<Point> cornerPoints = new ArrayList<Point>();
	 private String TAG = "william";
	 
	//public Point GetCorner(Point[] inAxis)
	public Point[] GetCorner(List<Point> inPoint)
	{

		//System.arraycopy(inPoint, 0, points, 0, inPoint.size());
		//retPoint = SortSquareCorner(testPointAll);
		
		Point pointArr[] = (Point[]) inPoint.toArray(new Point[0]);
		
		findCornerByCompass(pointArr);
		
		
		Point diagonal = SortSquareCorner(pointArr);
		
		
		//Point rightCorner = FindRightCorner(pointArr,diagonal.getIndex());
		//Point leftCorner = FindLeftCorner(pointArr,diagonal.getIndex() , rightCorner.getIndex());
		
		Point original = new Point();
		original.x = 0;
		original.y = 0;
		
		
		double area = CalAreaByAxis(pointArr[0],pointArr[1],pointArr[2]);
		
		Log.i(TAG,"Cal area = " + area);
		//Setting rightCorner to max (x y)
		//rightCorner.y = diagonal.y;
		
		//Point secPoint = new Point();
		//secPoint.x = rightCorner.x;
		//secPoint.y = FindSmallY(pointArr,diagonal.getIndex());
		
		//Point firPoint = new Point();
		//firPoint.x = FindSmallX(pointArr,diagonal.getIndex());
		//firPoint.y = FindSmallY(pointArr,diagonal.getIndex());
		
		//Point secPoint = new Point();
		//secPoint.x = FindBigerX(pointArr,diagonal.getIndex());
		//secPoint.y = FindSmallY(pointArr,diagonal.getIndex());
		
		
		//Point fourPoint = new Point();
		//fourPoint.x = FindSmallX(pointArr,diagonal.getIndex());
		//fourPoint.y = FindBigerY(pointArr,diagonal.getIndex());
		
		// Last original  : for draw back to original
		//Point cornerPointAll[] ={pointArr[0],rightCorner,diagonal,leftCorner,pointArr[0]};
		//Point cornerPointAll[] ={pointArr[0],rightCorner,diagonal,leftCorner,pointArr[0]};
		//Point cornerPointAll[] ={pointArr[0],secPoint,rightCorner,leftCorner,pointArr[0]};
		//Point cornerPointAll[] ={firPoint,secPoint,rightCorner,fourPoint,firPoint};
		
		// [0] = original location  
		// [cornerPoints.size() + 1] = original location 
		Point cornerPointAll[] = new Point[cornerPoints.size() + 1];
		//cornerPointAll[0] = pointArr[0];
		for (int i=0;i<cornerPoints.size();i++)
		{
			cornerPointAll[i] = cornerPoints.get(i);
		}
		
		cornerPointAll[cornerPoints.size()] = cornerPoints.get(0);
		return cornerPointAll;
	}
	
	private List<Point> findCornerByCompass(Point[] inAxis)
	{
		Point[] comSortAxis = new Point[inAxis.length];
		System.arraycopy(inAxis, 0, comSortAxis, 0, inAxis.length);
		 
		for( int i=0;  i < comSortAxis.length;  i++ )
		{
			if (comSortAxis[i].getCompass() > 45 && comSortAxis[i].isBeSelected() == false)
			{
				cornerPoints.add(comSortAxis[i]);
				//return comSortAxis[i];
			}
		}

		return cornerPoints;
	}
	
	private double CalAreaByAxis(Point Axis1,Point Axis2,Point Axis3)
	{
		double Area =1/2 * (
				(Math.abs((Axis1.x * Axis2.y ) - (Axis1.y * Axis2.x ))) + 
				(Math.abs((Axis2.x * Axis3.y ) - (Axis2.y * Axis3.x ))) + 
				(Math.abs((Axis3.x * Axis1.y ) - (Axis3.y * Axis1.x )))
				);
		
		return Area;
	}
	
	private Point SortSquareCorner(Point[] inAxis)
	{
		 Point[] xSortAxis = new Point[inAxis.length];
		 System.arraycopy(inAxis, 0, xSortAxis, 0, inAxis.length);
		 
		 int i;
	     boolean flag = true;   // set flag to true to begin first pass
	     Point tmpPoint = new Point();   //holding variable

	     while ( flag )
	     {
	            flag= false;    //set flag to false awaiting a possible swap
	            for( i=0;  i < xSortAxis.length - 1;  i++ )
	            {
	                   if ( eulaEquation(xSortAxis[i])  <  eulaEquation( xSortAxis[i+1]) )   // change to > for ascending sort
	                   {
	                	   tmpPoint = xSortAxis[ i ];                //swap elements
	                	   xSortAxis[ i ] = xSortAxis[ i+1 ];
	                	   xSortAxis[ i+1 ] = tmpPoint;
	                          flag = true;              //shows a swap occurred  
	                  } 
	            } 
	      }
	     
	     //We only compare 4 most bigger point here.
	     int loopcount = 0;
	     if (xSortAxis.length > 4)
	    	 loopcount = 4;
	     else
	    	 loopcount = xSortAxis.length;
	     
	     float max = xSortAxis[0].y;
	     int maxIndex = 0;
	     for (int j=0; j < loopcount -1 ; j++)
	     {
	    	 if (xSortAxis[j].y > max)
	    	 {
	    		 max=xSortAxis[j].y;
	    		 maxIndex = j;
	    	 }
	     }
	     
	     return xSortAxis[maxIndex];
		
	}
	
	
	private Point FindRightCorner(Point[] inAxis , int diagonalInex)
	{
		 Point[] xSortAxis = new Point[inAxis.length];
		 System.arraycopy(inAxis, 0, xSortAxis, 0, inAxis.length);
		 
		 int i;
	     boolean flag = true;   // set flag to true to begin first pass
	     Point tmpPoint = new Point();   //holding variable

	     //We only compare 4 most bigger point here.

	    // float max = xSortAxis[0].x;
	     double max = xSortAxis[0].x;
	     int maxIndex = 0;
	     for (int j=0; j < diagonalInex -1 ; j++)
	     {
	    	 /*double value = eulaEquationMinus(xSortAxis[j],xSortAxis[diagonalInex]);
	    	 if (value > max)
	    	 {
	    		 max=value;
	    		 maxIndex = j;
	    	 }*/
	    	 
	    	 
	    	 if (xSortAxis[j].x > max)
	    	 {
	    		 max=xSortAxis[j].x;
	    		 maxIndex = j;
	    	 }
	     }
	     
	     return xSortAxis[maxIndex];
		
	}
	
	private Point FindLeftCorner(Point[] inAxis, int diagonalInex , int rightIndex)
	{
		 Point[] xSortAxis = new Point[inAxis.length];
		 System.arraycopy(inAxis, 0, xSortAxis, 0, inAxis.length);
		 
		 int i;
	     boolean flag = true;   // set flag to true to begin first pass
	     Point tmpPoint = new Point();   //holding variable

	     double max = 0;
	     int maxIndex = 0;
	     for (int j=inAxis.length -1; j > diagonalInex + 1 ; j--)
	     {
	    	 double value = eulaEquationMinus(xSortAxis[j],xSortAxis[rightIndex]);
	    	 if ( value > max)
	    	 {
	    		 max=value;
	    		 maxIndex = j;
	    	 }
	     }
	     
	     return xSortAxis[maxIndex];
		
	}
	
	private float FindBigerX(Point[] inAxis,int diagonalInex)
	{
		double max = inAxis[0].x;
	     int maxIndex = 0;
	
	     //for (int j=inAxis.length - 1; j < diagonalInex + 1 ; j--)
	     for (int j=0; j < diagonalInex ; j++)
	     {
	    	 
	    	 
	    	 if (inAxis[j].x > max)
	    	 {
	    		 max = inAxis[j].x;
	    		 maxIndex = j;
	    	 }
	     }
		
		return inAxis[maxIndex].x;
	}
	
	
	private float FindBigerY(Point[] inAxis,int diagonalInex)
	{
		double max = inAxis[0].y;
	     int maxIndex = 0;
	
		for (int j=inAxis.length - 1; j > diagonalInex  ; j--)
	     {
	    	 
	    	 
	    	 if (inAxis[j].y > max)
	    	 {
	    		 max = inAxis[j].y;
	    		 maxIndex = j;
	    	 }
	     }
		return inAxis[maxIndex].y;
		
	}
	
	private float FindSmallX(Point[] inAxis,int diagonalInex)
	{
		double min = inAxis[0].x;
	     int minIndex = 0;
	
		for (int j=0; j < inAxis.length ; j++)
	     {
	    	 
	    	 
	    	 if (inAxis[j].x < min)
	    	 {
	    		 min = inAxis[j].x;
	    		 minIndex = j;
	    	 }
	     }
		
		return inAxis[minIndex].x;
	}
	
	private float FindSmallY(Point[] inAxis,int diagonalInex)
	{
		double min = inAxis[0].y;
	     int minIndex = 0;
	
		for (int j=0; j < inAxis.length ; j++)
	     {
	    	 if (inAxis[j].y < min)
	    	 {
	    		 min = inAxis[j].y;
	    		 minIndex = j;
	    	 }
	     }
		
		return inAxis[minIndex].y;
	}
	
	private double eulaEquation(Point Axis)
	{
		double distance = 0;
		distance = Math.sqrt((Axis.x * Axis.x + Axis.y * Axis.y));
		return distance;
	}
	
	
	private double eulaEquationMinus(Point Axis, Point refAxis)
	{
		double distance = 0;
		distance = Math.sqrt(( Math.abs((refAxis.x- Axis.x) * (refAxis.x- Axis.x)) + Math.abs((refAxis.y- Axis.y) * (refAxis.y- Axis.y))));
		return distance;
	}
}
