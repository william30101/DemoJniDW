package com.example.demojni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {


	boolean debugQueue = true;

	private static String TAG = "App";
	EditText dataText;
	TextView statusText,nanoText;
	Button writeBtn,writeFileBtn,startCalBtn,uartBtn,uartReadBtn,uartWriteBtn,thrBtn;
	private int[] wnum;
	File sdcard,file;
	private int alllen=0;
	private String btnname;
	public int Uart_Port = -1, Baud_rate = -1;
	public static int fd,nanoFd,encFd;
	final Timer timer = new Timer();
	private boolean Uart_Check = false,nanoOpend = false, encoderOpend = false;
	String ReStr,ReStrEnco,ReStrNano;

	byte[] ReByteEnco = new byte[11];
	byte[] ReByteNano = new byte[50];
	
	private int nanoCount = 0 , encoderCount = 0;  
	
	// We could modify here , to chage how many data should we get from queue.
	private int getNanoDataSize = 1 , getEncoderDataSize = 2 , beSentMessage = 13;
	private ArrayList<float[]> nanoQueue = new ArrayList<float[]>();
	private ArrayList<byte[]> encoderQueue = new ArrayList<byte[]>();
	private Handler handler = new Handler();
	private String nanoTestData[] = {"#-001.27:017:001:015","#-001.21:017:002:015",
			"#-001.10:017:003:015"};
	private String testdata2 = "12 45 89 45 36 12";
	
	private String startNanoPan[]={" INIT 3 1 2 3\r\n","MODE 0\r\n","START\r\n"};
	private int startNum = 0;
	
	Runnable rnano = new NanoThread();
	Runnable rencoder = new EncoderThread();
	Runnable rcombine = new CombineThread();
	
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
		thrBtn = (Button) findViewById(R.id.thrbtn);
		
		writeBtn.setOnClickListener(ClickListener);
		writeFileBtn.setOnClickListener(ClickListener);
		startCalBtn.setOnClickListener(ClickListener);
		uartBtn.setOnClickListener(ClickListener);
		uartReadBtn.setOnClickListener(ClickListener);
		uartWriteBtn.setOnClickListener(ClickListener);
		thrBtn.setOnClickListener(ClickListener);
		
		dataText = (EditText)findViewById(R.id.edText1);
		statusText = (TextView) findViewById(R.id.statustext);
		nanoText = (TextView) findViewById(R.id.nanoText);
		
		wnum = new int[500];
		
		Arrays.fill(wnum, 0);
		
		/*
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

*/
		
		
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
	    			fd = MainActivity.OpenUart("ttymxc4",2);

	    			if (fd > 0 )
	    			{
	    				// Setting uart
	    				Uart_Check = true;
	    				statusText.setText("Connected");
	    				Baud_rate = 1; // 115200
	    				MainActivity.SetUart(Baud_rate,2);
	    				
	    			}
	    			break;
	    		case R.id.readbtn : 
	    			if (fd > 0 )
	    			{
	    				MainActivity.ReceiveMsgUart(2);
	    			}
	    			break;
	    		case R.id.uartWriteBtn : 
	    			if (fd > 0 )
	    			{
	    				MainActivity.SendMsgUart(dataText.getText().toString(),2);
	    			}
	    			break;
	    		case R.id.thrbtn : 
	    				//Start nano thread
	    				//Runnable rnano = new NanoThread();
	    				
	    				handler.postDelayed(rnano, 100);
	    				
	                   //	new Thread(rnano).start();
	                   	//Start encoder thread
	    				//Runnable rencoder = new EncoderThread();
	                   	//new Thread(rencoder).start();
	                   	handler.postDelayed(rencoder, 50);
	                   	
	                   	//Start Combine Thread
	                   	//Runnable rcombind = new CombineThread();
	                   	//new Thread(rcombind).start();
	                   	handler.postDelayed(rcombine, 200);
	                   	
	                   	
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
						if (!dataText.getText().toString().matches(""))
						{
							String[] da = dataText.getText().toString().trim().split("\\s+");
							Arrays.fill(wnum, 0);
							for (int i = 0 ; i < da.length ; i++ )
							{
								wnum[i] = Integer.parseInt(da[i]);
							}
		    			
							WriteDemoData(wnum,da.length);
						}
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
	
	
	

	public class NanoThread implements Runnable {
		   
			public void run() {
				
				if (debugQueue)
				{
					Log.i(TAG,"NanoThread running count = " + nanoCount);
					/*ReStr = testdata1;
					//ReStr = "abcde";
					
					String[] daf = ReStr.split("\\s+");
					byte[] nanoBy = new byte[daf.length];
					for(int i=0;i<daf.length ; i++)
						nanoBy[i] = Byte.parseByte(daf[i]);
					*/
					
					//String nanoStr = ReceiveMsgUart(2);
					if (nanoCount  > 3)
						nanoCount = 0;
						
					String nanoStr = nanoTestData[nanoCount];
					String[] daf = nanoStr.split(":");
					float[] myflot = {Float.parseFloat(daf[0].substring(2, daf[0].length())),0};
					//Get data : #-001.27:017:001:015
					 
					
					nanoQueue.add(myflot);
					nanoCount++;
					
					
					handler.postDelayed(rnano,100);
				
			    }
				else
				{
					if(nanoOpend == false)
					{
						// Use UART1 for nanopan
						nanoFd = MainActivity.OpenUart("ttymxc0",2);
						if (nanoFd > 0 )
		    			{
		    				// Setting uart
		    				nanoText.setText("Connected");
		    				Baud_rate = 1; // 115200
		    				MainActivity.SetUart(Baud_rate,2);
		    				
		    				nanoOpend = true;
		    				
		    				while(startNum < 3)
		    				{
		    					MainActivity.SendMsgUart(startNanoPan[startNum], 2);
		    					startNum++;
		    				}
		    			}
					}
					
					
					if (nanoOpend) {
						ReStrNano = ReceiveMsgUart(2);
						if ( ReStrNano != null) {
							Log.i(TAG,"Receive message = "+ ReStrNano);
							String[] daf = ReStrNano.split(":");
							float[] myflot = {Float.parseFloat(daf[0].substring(2, daf[0].length())),0};
							//Add receive message from nanopan
							
							nanoQueue.add(myflot);
							//view.append(ReStr);
							//scrollView.fullScroll(ScrollView.FOCUS_DOWN);
							ReStrNano = null;
						}
					}
					
					handler.postDelayed(rnano,100);
				}
				
				
			}
	 }
	
	
	public class EncoderThread implements Runnable {

		   
			public void run() {
				
				
				if (debugQueue)
				{
					Log.i(TAG,"EncoderThread running count = " + encoderCount);
					//ReStrEnco = "12345";
					ReStrEnco = testdata2;
					//ReStr = "abcde";
					
					String[] daf = ReStrEnco.split("\\s+");
					byte[] encoBy = new byte[daf.length];
					for(int i=0;i<daf.length ; i++)
						encoBy[i] = Byte.parseByte(daf[i]);
					
					encoderQueue.add(encoBy);
					encoderCount++;
					handler.postDelayed(rencoder,50);
					
					
					
					//byte[] encoderBy = ReceiveMsgUart(1);
					
					//encoderQueue.add(encoderBy);
					//encoderCount++;
					
					
					handler.postDelayed(rencoder,50);
				
			    }
				else
				{
				
					if(encoderOpend == false)
					{
						// Use UART1 for nanopan
						encFd = MainActivity.OpenUart("ttymxc2",1);
						if (encFd > 0 )
		    			{
		    				// Setting uart
							statusText.setText("Connected");
		    				Baud_rate = 1; // 115200
		    				MainActivity.SetUart(Baud_rate,1);
		    				
		    				encoderOpend = true;
		    				
		    			}
					}
					
					
					if (encoderOpend) {
						//ReByteEnco = ReceiveMsgUart(1);
						if ( ReByteEnco != null) {
							//Log.i(TAG,"Receive message = "+ ReStrEnco);
							
							//Add receive message from nanopan
							encoderQueue.add(ReByteEnco);
							
							//view.append(ReStr);
							//scrollView.fullScroll(ScrollView.FOCUS_DOWN);
							Arrays.fill(ReByteEnco, (byte)0x00);
							//ReStrEnco = null;
						}
					}
					
					handler.postDelayed(rencoder,50);
				}
				
			   }
	 }
	
	
	
	public class CombineThread implements Runnable {

		   
		public void run() {
			
			if (debugQueue)
			{
				//byte[] beSendMsg = new byte[beSentMessage];;
				if (nanoQueue.size() >= getNanoDataSize 
						&& encoderQueue.size() >= getEncoderDataSize) 
				{
					
					//Arrays.fill(beSendMsg, (byte)0x00);
					Log.i(TAG,"nano size = " + nanoQueue.size() + " encoderQueue size = " + encoderQueue.size());
				
					// Two input here.
					ArrayList<float[]> nanoData = getNanoRange(nanoQueue,nanoQueue.size() - getNanoDataSize ,nanoQueue.size());
					ArrayList<byte[]> encoderData = getRange(encoderQueue,encoderQueue.size() - getEncoderDataSize ,encoderQueue.size());
					
					Log.i(TAG,"nanoData size = " + nanoData.size() + " encoderData size = " + encoderData.size());
					//Calculate nanopan data and encoder data here.
					//Output Data format  0x53 0x09 X4 X3 X2 X1 Y4 Y3 Y2 Y1 CRC2 CRC1 0x45
					//Save to byte array beSendMsg[13]
					//....................
					float[] nanoFloat = nanoData.get(0);
					byte[] encoByte = encoderData.get(0);

					Log.i(TAG,"nanoFloat = "  +nanoFloat[0] );
					
					
					byte[] beSendMsg = Combine(nanoData,encoderData);
					
					for (int i=0;i<beSendMsg.length;i++)
						Log.i(TAG,"test["+ i + "] = " + beSendMsg[i]);
					
					Log.i(TAG,"send string = " + beSendMsg.toString());
					
					encoderCount = 0;
					nanoCount = 0;
					
					
					nanoQueue.clear();
					encoderQueue.clear();
					
					
					handler.postDelayed(rcombine,200);

					//End
					
					// One Output Here
					//SendMsgUart(beSendMsg.toString(),2);
				}
			
		    }
			else
			{
			
			
				if(encoderOpend == true && nanoOpend == true)
				{
					//byte[] beSendMsg = new byte[beSentMessage];;
					if (nanoQueue.size() >= getNanoDataSize 
							&& encoderQueue.size() >= getEncoderDataSize) 
					{
						
						//Arrays.fill(beSendMsg, (byte)0x00);
						
					
						// Two input here.
						ArrayList<float[]> nanoData = getNanoRange(nanoQueue,nanoQueue.size() - getNanoDataSize ,nanoQueue.size());
						ArrayList<byte[]> encoderData = getRange(encoderQueue,encoderQueue.size() - getEncoderDataSize ,encoderQueue.size());
						
						
						//Calculate nanopan data and encoder data here (java layer).
						//Output Data format  0x53 0x09 X4 X3 X2 X1 Y4 Y3 Y2 Y1 CRC2 CRC1 0x45
						//Save to byte array beSendMsg[13]
						//....................
						float[] nanoFloat = nanoData.get(0);
						byte[] encoByte = encoderData.get(0);
						
						
						Log.i(TAG,"nanoFloat = "  +nanoFloat );
						//.................
						
						//End
						
						//encoderCount = 0;
						//nanoCount = 0;
						
						byte [] beSendMsg = Combine(nanoData,encoderData);
						
						for (int i=0;i<beSendMsg.length;i++)
							Log.i(TAG,"test["+ i + "] = " + beSendMsg[i]);

						nanoQueue.clear();
						encoderQueue.clear();
						// One Output Here
						SendMsgUart(beSendMsg.toString(),1);
					}
				}
			}
		}
	}
 
	
	
	
	public static ArrayList<byte[]> getRange(ArrayList<byte[]> list, int start, int last) {

		ArrayList<byte[]> temp = new ArrayList<byte[]>();

		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}

		return temp;
	}
	
	public static ArrayList<float[]> getNanoRange(ArrayList<float[]> list, int start, int last) {

		ArrayList<float[]> temp = new ArrayList<float[]>();

		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}

		return temp;
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
		MainActivity.CloseUart(encFd);
		MainActivity.CloseUart(nanoFd);
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
	public static native int OpenUart(String str, int fdNum);
	public static native int CloseUart(int fdNum);
	public static native int SetUart(int i , int fdNum);
	public static native int SendMsgUart(String msg,int fdNum);
	public static native String ReceiveMsgUart(int fdNum);
	public static native int StartCal();
	public static native byte[] Combine(ArrayList<float[]> nanoq , ArrayList<byte[]> encoq);
}
