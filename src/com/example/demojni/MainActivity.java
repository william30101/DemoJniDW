package com.example.demojni;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;

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


	boolean debugNanoQueue = false;
	boolean debugEncoderQueue = false;

	private static String TAG = "App";
	EditText dataText;
	TextView statusText,nanoText;
	Button writeBtn,writeFileBtn,startCalBtn,uartBtn,uartReadBtn,uartWriteBtn,thrBtn;
	private int[] wnum;
	File sdcard,file;
	private int alllen=0;
	private String btnname;
	public int Uart_Port = -1, Baud_rate = -1;
	final Timer timer = new Timer();
	private boolean Uart_Check = false,nanoOpend = false, encoderOpend = false;
	String ReStr,ReStrEnco,ReStrNano;

	//byte[] ReByteEnco = new byte[11];
	byte[] ReByteNano = new byte[50];
	byte [] ReByteEnco = new byte[11];
	private int nanoInterval = 100 , encoderWriteInterval = 80 , encoderReadInterval = 80 , combineInterval = 200;
	
	
	
	private static EncoderCmd encoderCmd = new EncoderCmd();
	private int nanoCount = 0 , encoderCount = 0;  
	
	// We could modify here , to chage how many data should we get from queue.
	private int getNanoDataSize = 3 , getEncoderDataSize = 2 , beSentMessage = 13;
	
	private static ArrayList<float[]> nanoQueue = new ArrayList<float[]>();
	private static ArrayList<byte[]> encoderQueue = new ArrayList<byte[]>();
	
	private Handler handler = new Handler();
	
	private String nanoTestData[] = {"#-001.27:017:001:015","#-001.21:017:002:015",
			"#-001.10:017:003:015"};
	private byte[] encoderTestData = {0x53,0x0d,(byte)0x02,0x30,0x03,0x15,0x01,(byte)0xff,0x00,0x00,0x45};
	
	private byte[] askEncoderData = {0x53,0x06,0x0d,0x00,0x00,0x45};
	
	private String NanoPanCmd[]={"INIT 3 1 2 3\r\n","MODE 0\r\n"};
	private String NanoStartCmd = "START\r\n";
	//private String startNanoPan="START\r\n";
	private int startNum = 0 , minusNumber = 0 ;
	
	Runnable rNano = new NanoThread();
	Runnable rWEncoder = new EncoderWriteThread();
	Runnable rREncoder = new EncoderReadThread();
	Runnable rCombine = new CombineThread();
	
	
	float[] nanoFloat = new float[getNanoDataSize];
	
	public static int fd = 0,nanoFd = 0,driFd = 0;
	
	private boolean writerFirst = true; // Write First
	private static int writingWriters = 0;
	private static int waitingWriters = 0;
	private static int readingReaders = 0;
	
	
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
		//statusText = (TextView) findViewById(R.id.statustext);
		nanoText = (TextView) findViewById(R.id.nanoText);
		
		wnum = new int[500];
		
		Arrays.fill(wnum, 0);
		
		Arrays.fill(ReByteEnco, (byte)0x01);
		
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
	    			
                   //Runnable r = new MyThread(v);
                  // 	new Thread(r).start();
	    			
	    			Log.i(TAG,"send "+NanoStartCmd);
	    			MainActivity.SendMsgUartNano(NanoStartCmd);
                   	
	    			
	    		break;
	    		case R.id.btn2 : 
	    			
	    			Runnable r2 = new MyThread(v);
                   	new Thread(r2).start();

	    			
	    			break;
	    		case R.id.btn3 : 
	    			//Runnable r3 = new MyThread(v);
                   //	new Thread(r3).start();
	    			
	    			String ReStrEnco = null;
					try {
						ReStrEnco = new String(askEncoderData, "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MainActivity.SendMsgUart(ReStrEnco,1,askEncoderData);
	    			
	    			//MainActivity.SendMsgUartNano(startNanoPan);
	    			break;
	    			
	    		case R.id.uartBtn : 
	    			// Open Uart here
	    			
	    			OpenSetUartPort("ttymxc4");
	    			OpenSetUartPort("ttymxc2");
	    			
	    			/*
	    			fd = MainActivity.OpenUart("ttymxc0",2);

	    			if (fd > 0 )
	    			{
	    				// Setting uart
	    				Uart_Check = true;
	    				//statusText.setText("Connected");
	    				Baud_rate = 1; // 115200
	    				MainActivity.SetUart(Baud_rate,2);
	    				
	    			}*/
	    			break;
	    		case R.id.uartReadBtn : 
	    			
	    			Log.i(TAG,"send "+NanoPanCmd[startNum]);
	    			MainActivity.SendMsgUartNano(NanoPanCmd[startNum]);
	    			if (startNum < 1)
	    				startNum++;
	    			else
	    				startNum = 0;
	    			/*if (fd > 0 )
	    			{
	    				MainActivity.ReceiveMsgUart(2);
	    			}
	    			*/
	    			break;
	    		case R.id.uartWriteBtn : 
	    			//if (fd > 0 )
	    			//{
	    			//	MainActivity.SendMsgUartNano(dataText.getText().toString());
	    			//}
	    			//Log.i(TAG,"send "+NanoStartCmd);
	    			//MainActivity.SendMsgUartNano(NanoStartCmd);
	    			
	    			break;
	    		case R.id.thrbtn : 
	    				//Start nano thread
	    				//Runnable rnano = new NanoThread();
	    				
	    				handler.postDelayed(rNano, 100);
	    				
	                   //	new Thread(rnano).start();
	                   	//Start encoder thread
	    				//Runnable rencoder = new EncoderThread();
	                   	//new Thread(rencoder).start();
	                    handler.postDelayed(rWEncoder, encoderWriteInterval);
	                    handler.postDelayed(rREncoder, encoderReadInterval);
	                   	
	                   	//Start Combine Thread
	                   	//Runnable rcombind = new CombineThread();
	                   	//new Thread(rcombind).start();

	                   	handler.postDelayed(rCombine, 200);
	                   	
	                   	
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
						//StartCal();
						//MainActivity.SendMsgUart(startNanoPan,2);
					}			
				
			   }
	 }
	
	
	

	public class NanoThread implements Runnable {
		   
		public void run() {
			
			if (debugNanoQueue)
			{
				//Log.i(TAG,"NanoThread running count = " + nanoCount);
				/*ReStr = testdata1;
				//ReStr = "abcde";
				
				String[] daf = ReStr.split("\\s+");
				byte[] nanoBy = new byte[daf.length];
				for(int i=0;i<daf.length ; i++)
					nanoBy[i] = Byte.parseByte(daf[i]);
				*/
				
				//String nanoStr = ReceiveMsgUart(2);
				if (nanoCount  > 2)
					nanoCount = 0;
					
				String nanoStr = nanoTestData[nanoCount];
				String[] daf = nanoStr.split(":");
				float[] myflot = {Float.parseFloat(daf[0].substring(2, daf[0].length())),0};
				//Get data : #-001.27:017:001:015
				 
				
				nanoQueue.add(myflot);
				nanoCount++;
				
				
				handler.postDelayed(rNano,nanoInterval);
			
		    }
			else
			{
				
				if (nanoOpend) {
					
					ReStrNano = ReceiveMsgUart(2);
					//Log.i(TAG,"rec msg = " + ReStrNano);
					if ( ReStrNano != null) {
						//Log.i(TAG,"Nano Receive message = "+ ReStrNano + " leng= " + ReStrNano.length());
						String[] line20 =  ReStrNano.split("\r\n");
						//Log.i(TAG,"Nano line20 = " + line20[0]);
						for(int i=0 ;i< line20.length;i++)
						{
							//Log.i(TAG,"Nano line20[" + i + " ] = " + line20[i]);
							if (line20[i].contains("#") && line20[i].length() >= 5 && line20[i].contains(":"))
							{
								String[] daf = line20[i].split(":");

							
								float[] myflot = {Float.parseFloat(daf[0].substring(2, daf[0].length())),0};
								//Add receive message from nanopan
								Log.i(TAG,"Nano my float distance = " + myflot[0]);
								nanoQueue.add(myflot);
								//view.append(ReStr);
								//scrollView.fullScroll(ScrollView.FOCUS_DOWN);
								ReStrNano = null;
							}
						}
					}
				}
				
				handler.postDelayed(rNano,nanoInterval);
			}
			
			
		}
	}
	
	
	public class EncoderWriteThread implements Runnable {

		public void run() {

			if (debugEncoderQueue) {
				writeLock();
				//MainActivity.SendMsgUart("test",2,askEncoderData);
				Log.i(TAG,"Write ask data");
				writeUnLock();
				handler.postDelayed(rWEncoder, encoderWriteInterval);
			} else {
				
				//Log.i(TAG,"opend fd = " + uartCmd.GetDrivingOpend());
				if (encoderOpend == true) {
					writeLock();
					Log.i(TAG,"Send Ask to Driving board");
					String ReStrEnco = null;
					try {
						ReStrEnco = new String(askEncoderData, "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MainActivity.SendMsgUart(ReStrEnco,1,askEncoderData);
					writeUnLock();
				}

				handler.postDelayed(rWEncoder, encoderWriteInterval);
			}

		}
	}
	
	
	public class EncoderReadThread implements Runnable {

		public void run() {

			if (debugEncoderQueue) {
				
				readLock();
				int dataSize = 8;
				byte[] dataByte = new byte[dataSize];
				
				Log.i(TAG, "EncoderThread running count = " + encoderCount);
				// ReStrEnco = "12345";
				//ReStrEnco = new String(endoerTestData, "ISO-8859-1");;
				// ReStr = "abcde";
				Arrays.fill(dataByte, (byte)0x00);
				// dataByte[0]  is xPolarity
				// dataByte[1] -> [2] is X axis
				// dataByte[3]  is yPolarity
				// dataByte[4] -> [5] is Y axis
				// dataByte[6] -> [7] CRC 16 , 0x00 = not used

				dataByte = Arrays.copyOfRange(encoderTestData, 2, 10);
				
				/*
				try {
					encoderDataByteArr = ReStrEnco.getBytes("ISO-8859-1");
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				
				//encoderCmd.SetByte(ReStrEnco);
				//Log.i(TAG,"add encoder queue data = " + encoderCmd.GetDataByte());
				encoderQueue.add(dataByte);
				encoderCount++;
				
				readUnLock();
				
				handler.postDelayed(rREncoder, encoderReadInterval);

				// byte[] encoderBy = ReceiveMsgUart(1);

				// encoderQueue.add(encoderBy);
				// encoderCount++;


			} else {

				/*if (uartCmd.GetDrivingOpend() == false) {
					// Use UART1 for nanopan
					encFd = uartCmd.OpenSetUartPort("ttymxc0");
				}*/
				
				
				//Log.i(TAG,"encoder fd = " + encoderOpend);
				if (encoderOpend == true) {
					
					readLock();
					//ReStrEnco = ReceiveMsgUart(1);

					ReByteEnco = ReceiveByteMsgUart(1);

					//Log.i(TAG,"receive msg = " + ReByteEnco);
					

					if (ReByteEnco[0] != 0x01)
					{

						//Log.i(TAG,"Receive message = "+ ReStrEnco);
						//encoderCmd.SetByte(ReStrEnco);
						//byte [] test = encoderCmd.GetDataByte();
						Log.i(TAG,"Encoder Receive test[0] = "+ReByteEnco[0] + "test1 = "+ ReByteEnco[1] + "test2 = "+ ReByteEnco[2]+ "test3 = "+ ReByteEnco[3]+ 
								"test4 = "+ ReByteEnco[4] + "test5 = "+ ReByteEnco[5]);
						// Add receive message from Driving Board
						
						encoderCmd.SetDataByte(ReByteEnco);
						byte [] test = encoderCmd.GetDataByte();
						
						encoderQueue.add(encoderCmd.GetDataByte());
						
						 
						 //Log.i(TAG,"receive Data byte = " + Arrays.copyOfRange(ReByteEnco, 2, 10));
						 //encoderQueue.add(Arrays.copyOfRange(ReByteEnco, 2, 10));
						
						//Log.i(TAG,"receive msg = " + ReStrEnco);

						// view.append(ReStr);
						// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
						// Arrays.fill(ReByteEnco, (byte)0x00);
						// ReStrEnco = null;
					}
					
					readUnLock();
				}

				handler.postDelayed(rREncoder, encoderReadInterval);
			}

		}
	}
	
	
	
	public class CombineThread implements Runnable {

		public void run() {

			// Log.i(TAG,"encoderOpend = " + encoderOpend + "  nanoOpend = "
			// + nanoOpend );
			if ( ( nanoOpend== true || debugNanoQueue == true) 
					&& ( encoderOpend == true || debugEncoderQueue == true ) ) {
				// byte[] beSendMsg = new byte[beSentMessage];;

				Log.i(TAG, "nanoQueue.size() = " + nanoQueue.size()
						+ " encoderQueue.size() = " + encoderQueue.size());

				if (nanoQueue.size() >= getNanoDataSize
						&& encoderQueue.size() >= getEncoderDataSize) {

					// Arrays.fill(beSendMsg, (byte)0x00);

					minusNumber = nanoQueue.size() - getNanoDataSize;
					// Two input here.

					if (nanoQueue.size() % 3 != 0) {

						minusNumber = nanoQueue.size() - getNanoDataSize
								- (nanoQueue.size() % 3);
					}

					// Two input here.
					ArrayList<float[]> nanoData = getNanoRange(nanoQueue,
							minusNumber, nanoQueue.size()
									- (nanoQueue.size() % 3));
					ArrayList<byte[]> encoderData = getEncoderRange(encoderQueue,
							encoderQueue.size() - getEncoderDataSize,
							encoderQueue.size());

					// Calculate nanopan data and encoder data here (java
					// layer).
					// Output Data format 11 bytes 
					// [0x53] [0x09] [X Polarity] [X2] [X1] [Y polarity] [Y2] [Y1] [CRC2] [CRC1] [0x45]
					// Save to byte array beSendMsg[11]
					// ....................

					for (int i = 0; i < nanoData.size(); i++) {
						nanoFloat = nanoData.get(i);
						Log.i(TAG, "combine nanoFloat [" + i + " ] = "
								+ nanoFloat[0]);
					}

					ArrayList<int[]> encoderDataQueue = new ArrayList<int[]>();
					byte[] encoByte = encoderData.get(0);
					int[] tempInt = new int[3]; // L Wheel , R Wheel , Compass
					for (int i=0;i<encoderData.size();i++)
					{
						tempInt[0]  = ( (encoByte[0] << 8) & 0xff00 | (encoByte[1] & 0xff));
						tempInt[1]  = ( (encoByte[2] << 8) & 0xff00 | (encoByte[3] & 0xff));
						tempInt[2]  = ( (encoByte[4] << 8) & 0xff00 | (encoByte[5] & 0xff));
						
						encoderDataQueue.add(tempInt);
					}
					
					
					
					
					Combine(nanoData, encoderDataQueue);
					// .................

					// End
					encoderCount = 0;
					nanoCount = 0;
					nanoQueue.clear();
					encoderQueue.clear();
					// One Output Here
					// SendMsgUart(beSendMsg.toString(),1);
				}

				handler.postDelayed(rCombine, combineInterval);
			}
		}
	}
 
	
	
	
	public static ArrayList<byte[]> getEncoderRange(ArrayList<byte[]> list, int start, int last) {

		ArrayList<byte[]> temp = new ArrayList<byte[]>();
		Log.i(TAG,"encoder start = " + start + " last = " + last);
		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}

		return temp;
	}
	
	public static ArrayList<float[]> getNanoRange(ArrayList<float[]> list, int start, int last) {

		ArrayList<float[]> temp = new ArrayList<float[]>();
		Log.i(TAG,"start = " + start + " last = " + last);
		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}

		return temp;
	}

	
	public int OpenSetUartPort(String portName)
	{
		
		// mxc0 for driving board , 19200
		// mxc2 for nanoPan , Baudrate 115200
		if (portName.equals("ttymxc4")) {
			Log.i(TAG,"ttymxc4 opend");
			//portName = "ttymxc4";
			//nanoFd = OpenUart(portName, 1 );
			driFd = OpenUart(portName, 1 );
			
			//if (nanoFd > 0) {
			if (driFd > 0) {
				encoderOpend = true;
				Baud_rate = 0; // 19200
				SetUart(Baud_rate, 1);
				fd = driFd;
			}

		} else if (portName.equals("ttymxc2")) {

			nanoFd = OpenUart(portName, 2 );
			//driFd = OpenUart(portName, 2 );
			if (nanoFd > 0) {
				nanoOpend = true;
				Baud_rate = 1; // 115200
				SetUart(Baud_rate, 2);
				fd = nanoFd;
			}
		}
		else
		{
			fd = 0;
		}
		
		
		Log.i(TAG, " portname = "  + portName +" fd = " + fd);
		

		return fd;
		
	}
	
	

	 
	 public synchronized void readLock() {
	    try {
	        while(writingWriters > 0 || (writerFirst && waitingWriters > 0)) {
	            wait();
	        }
	    }
	    catch(InterruptedException e) {
	        e.printStackTrace();
	    }

	    readingReaders++;
	 }
	 
	 public synchronized void readUnLock() {
	    readingReaders--;
	    writerFirst = true;
	    notifyAll();
	 }
	 
	 public synchronized void writeLock() {
	    waitingWriters++;
	    try {
	        while(readingReaders > 0 || writingWriters > 0) {
	            wait();
	        }
	    }
	    catch(InterruptedException e) {
	        e.printStackTrace();
	    }
	    finally {
	        waitingWriters--;
	    }

	    writingWriters++;
	 }
	 
	 public synchronized void writeUnLock() {
	    writingWriters--;
	    writerFirst = false;
	    notifyAll();
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
		MainActivity.CloseUart(driFd);
		MainActivity.CloseUart(nanoFd);
		nanoQueue.clear();
		encoderQueue.clear();
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
	public static native int SendMsgUart(String msg,int fdNum,byte[] inByte);
	public static native int SendMsgUartNano(String msg);
	public static native String ReceiveMsgUart(int fdNum);
	public static native byte[] ReceiveByteMsgUart(int fdNum);
	public static native int StartCal();
	public static native byte[] Combine(ArrayList<float[]> nanoq , ArrayList<int[]> encoq);
}