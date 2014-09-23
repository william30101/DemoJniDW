package com.example.demojni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

	private static String TAG = "App";
	EditText DataText;
	Button WriteBtn,WriteFileBtn,StartCalBtn;
	private int[] wnum;
	File sdcard,file;
	private int flen = 0;
	private int oflen = 0;
	private static boolean first = true;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		WriteBtn = (Button) findViewById(R.id.btn1);
		WriteFileBtn = (Button) findViewById(R.id.btn2);
		StartCalBtn = (Button) findViewById(R.id.btn3);
		
		WriteBtn.setOnClickListener(ClickListener);
		WriteFileBtn.setOnClickListener(ClickListener);
		StartCalBtn.setOnClickListener(ClickListener);
		
		DataText = (EditText)findViewById(R.id.edText1);
		
		
		
		wnum = new int[500];
		
		Arrays.fill(wnum, 0);
		


	}

	
	private OnClickListener ClickListener = new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
	    	switch(v.getId()){
	    		case R.id.btn1 : 
	    			String[] da = DataText.getText().toString().trim().split("\\s+");
	    			Arrays.fill(wnum, 0);
	    			for (int i = 0 ; i < da.length ; i++ )
	    			{
	    				wnum[i] = Integer.parseInt(da[i]);
	    			}
	    			
	    			WriteDemoData(wnum,da.length);
	    		break;
	    		case R.id.btn2 : 
	    			
	    			boolean sdCardExist = Environment.getExternalStorageState()   
                    .equals(android.os.Environment.MEDIA_MOUNTED);
	    			
	    			flen = 0;
	    			
	    			sdcard = Environment.getExternalStorageDirectory();

	    			String dirc = sdcard.getParent();
	    			dirc = dirc + "/legacy";
	    			
	    			file = new File(dirc,"testdata.txt");
	    			Log.i(TAG," External storage path =" + sdcard);

	    			try {
	    			    BufferedReader br = new BufferedReader(new FileReader(file));
	    			    String line;
	    			    int i=0;
	    			    Arrays.fill(wnum, 0);
	    			    while ((line = br.readLine()) != null) {
	    			    	
	    			    	Log.i(TAG,"line = "+line);
	    			    	String[] daf = line.split("\\s+");

	    			    		for ( i = 0  ; i < daf.length ; i++ )
		    	    			{
		    	    				wnum[i+oflen] = Integer.parseInt(daf[i]);
		    	    			}
	    			    		
	    			    	
	    			    	oflen = daf.length;
	    			    	
	    			    }
	    			    
	    			    WriteDemoData(wnum,flen);
	    			}
	    			catch (IOException e) {
	    			    //You'll need to add proper error handling here
	    			}

	    			
	    			break;
	    		case R.id.btn3 : 
	    				StartCal();
	    			break;
	    		default:
	    			Log.i(TAG,"Invaild Button function");
	    			break;
	    	}
	    }
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		
		return super.onOptionsItemSelected(item);
	}
	
	
	static
	{
		try
		{
			System.loadLibrary("hello");
			Log.i(TAG, "Trying to load libhello.so");
		}
		catch(UnsatisfiedLinkError ule)
		{
			Log.i(TAG, "WARNING: could not to load libhello.so");
		}
	}
	
	public static native int WriteDemoData(int[] data, int size);
	public static native int OpenUart(String str);
	public static native void CloseUart(int i);
	public static native int SetUart(int i);
	public static native int SendMsgUart(String msg);
	public static native String ReceiveMsgUart();
	public static native int StartCal();
}
