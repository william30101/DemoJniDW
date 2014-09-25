package com.example.demojni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

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
import android.widget.TextView;

public class MainActivity extends Activity {




	private static String TAG = "App";
	EditText dataText;
	TextView statusText;
	Button writeBtn,writeFileBtn,startCalBtn,uartBtn,uartReadBtn,uartWriteBtn;
	private int[] wnum;
	File sdcard,file;
	private int alllen=0;
	private String btnname;
	public int Uart_Port = -1, Baud_rate = -1;
	public static int fd;
	final Timer timer = new Timer();
	private boolean Uart_Check = false;
	String ReStr;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		
		writeBtn = (Button) findViewById(R.id.btn1);
		writeFileBtn = (Button) findViewById(R.id.btn2);
		startCalBtn = (Button) findViewById(R.id.btn3);
		uartBtn = (Button) findViewById(R.id.uartBtn);
		uartReadBtn = (Button) findViewById(R.id.uartReadBtn);
		uartWriteBtn = (Button) findViewById(R.id.uartWriteBtn);
		
		
		writeBtn.setOnClickListener(ClickListener);
		writeFileBtn.setOnClickListener(ClickListener);
		startCalBtn.setOnClickListener(ClickListener);
		uartBtn.setOnClickListener(ClickListener);
		uartReadBtn.setOnClickListener(ClickListener);
		uartWriteBtn.setOnClickListener(ClickListener);
		
		dataText = (EditText)findViewById(R.id.edText1);
		statusText = (TextView) findViewById(R.id.statustext);
		
		
		wnum = new int[500];
		
		Arrays.fill(wnum, 0);
		
		
		TimerTask task = new TimerTask(){
			public void run(){
				runOnUiThread(new Runnable(){
					@Override
					public void run(){
						if (Uart_Check) {
							ReStr = ReceiveMsgUart();
							if ( ReStr != null) {
								Log.i(TAG,"Receive message = "+ ReStr);
								//view.append(ReStr);
								//scrollView.fullScroll(ScrollView.FOCUS_DOWN);
								ReStr = null;
							}
						}
					}
					
				});
			}
		};
		
		timer.schedule(task, 1000, 100);


	}

	
	private OnClickListener ClickListener = new OnClickListener() {
	    @Override
	    public void onClick(final View v) {
	    	switch(v.getId()){
	    		case R.id.btn1 : 
	    			
                   	Runnable r = new MyThread(v);
                   	new Thread(r).start();
	    			
	    			
	    		break;
	    		case R.id.btn2 : 
	    			
	    			Runnable r2 = new MyThread(v);
                   	new Thread(r2).start();

	    			
	    			break;
	    		case R.id.btn3 : 
	    			Runnable r3 = new MyThread(v);
                   	new Thread(r3).start();
	    				
	    			break;
	    			
	    		case R.id.uartBtn : 
	    			// Open Uart here
	    			fd = MainActivity.OpenUart("ttymxc2");

	    			if (fd > 0 )
	    			{
	    				// Setting uart
	    				Uart_Check = true;
	    				statusText.setText("Connected");
	    				Baud_rate = 1; // 115200
	    				MainActivity.SetUart(Baud_rate);
	    				
	    			}
	    			break;
	    		case R.id.readbtn : 
	    			if (fd > 0 )
	    			{
	    				MainActivity.ReceiveMsgUart();
	    			}
	    			break;
	    		case R.id.uartWriteBtn : 
	    			if (fd > 0 )
	    			{
	    				MainActivity.SendMsgUart(dataText.getText().toString());
	    			}
	    			break;
	    		default:
	    			Log.i(TAG,"Invaild Button function");
	    			break;
	    	}
	    }
	};
	
	
	public class MyThread implements Runnable {

		   private View view;
		   String SendMsg;
		   
		   
			public MyThread(View v) {
			       // store parameter for later user
				   this.view = v;
			   }

			public void run() {
				
					// uiHandler.sendEmptyMessage(0);

					btnname = view.getResources().getResourceName(view.getId());
					String sub = btnname.substring(btnname.indexOf("/") + 1);
					
					if (sub.equals("btn1"))
					{
						String[] da = dataText.getText().toString().trim().split("\\s+");
						Arrays.fill(wnum, 0);
						for (int i = 0 ; i < da.length ; i++ )
						{
							wnum[i] = Integer.parseInt(da[i]);
						}
	    			
						WriteDemoData(wnum,da.length);
					}
					else if (sub.equals("btn2"))
					{
						boolean sdCardExist = Environment.getExternalStorageState()   
			                    .equals(android.os.Environment.MEDIA_MOUNTED);
				    			
						
						if (sdCardExist)
						{
				    			alllen = 0;
				    			
				    			sdcard = Environment.getExternalStorageDirectory();

				    			String dirc = sdcard.getParent();
				    			dirc = dirc + "/legacy";
				    			
				    			file = new File(dirc,"testdata.txt");
				    			Log.i(TAG," External storage path =" + dirc);

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
					    	    				wnum[i+alllen] = Integer.parseInt(daf[i]);
					    	    			}
				    			    	alllen = alllen + daf.length;
				    			    	
				    			    }
				    			    
				    			    WriteDemoData(wnum,alllen);
				    			}
				    			catch (IOException e) {
				    			    //You'll need to add proper error handling here
				    			}
						}
					}
					else if (sub.equals("btn3"))
					{
						StartCal();
					}
						
					
				
			   }
	 }
	
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
	

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		timer.cancel();
		CloseUart(fd);
		Uart_Check = false;
		super.onDestroy();
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
