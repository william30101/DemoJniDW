package com.example.demojni;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.xmlpull.v1.XmlPullParser;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Xml;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.graphics.Color;

public class MainActivity extends Activity {

	boolean debugNanoQueue = false;
	boolean debugEncoderQueue = true;

	private static String TAG = "App";
	EditText dataText;
	TextView drivingStatus,nanoStatus;
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
	private int nanoInterval = 100 , encoderWriteWiatInterval = 20 , encoderReadWaitInterval = 80 , combineInterval = 300;
	
	
	
	private static EncoderCmd encoderCmd = new EncoderCmd();
	private int nanoCount = 0 , encoderCount = 0;  
	
	// We could modify here , to chage how many data should we get from queue.
	private int getNanoDataSize = 3 , getEncoderDataSize = 2 , beSentMessage = 13;
	
	private static ArrayList<float[]> nanoQueue = new ArrayList<float[]>();
	private static ArrayList<byte[]> encoderQueue = new ArrayList<byte[]>();
	
	private Handler handler = new Handler();
	
	private String nanoTestData[] = {
			"#-001.27:017:001:015","#-001.21:017:002:015","#-001.10:017:003:015",
			"#-002.27:017:001:015","#-003.21:017:002:015","#-004.10:017:003:015",
			"#-006.27:017:001:015","#-007.21:017:002:015"
										};
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
	Runnable rEncoder = new EncoderThreadPool();
	
	private ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
	
	float[] nanoFloat = new float[getNanoDataSize];
	float[] nanoFloat_1 = new float[getNanoDataSize];
	
	public static int fd = 0,nanoFd = 0,driFd = 0;
	
	private boolean writerFirst = true; // Write First
	private static int writingWriters = 0;
	private static int waitingWriters = 0;
	private static int readingReaders = 0;
	
	
	private boolean isNeedAdd = false;
	DirectionCmd direc = new DirectionCmd();
	StopCmd scmd = new StopCmd();
	AngleCmd angleCmd = new AngleCmd();
	StretchCmd stretCmd = new StretchCmd();
	
	ByteArrayOutputStream retStreamDatas;
	
	// For Draw point
	DrawView drawView;

	// End for Draw Point
	
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
		
		
		drivingStatus = (TextView) findViewById(R.id.drivingStatusText);
		nanoStatus = (TextView) findViewById(R.id.nanoStatusText);
		
		drawView = (DrawView) findViewById(R.id.drawView1);
		
		//Open uart when App lunch
		
		if (OpenSetUartPort("ttymxc4") > 0)
			drivingStatus.setText("Driving mxc4 connected");
		
		if (OpenSetUartPort("ttymxc2") > 0)
			nanoStatus.setText("Nano mxc2 connected");
		
		
		ImageButton backward = (ImageButton) findViewById(R.id.backward);
		ImageButton forward = (ImageButton) findViewById(R.id.forward);
		ImageButton left = (ImageButton) findViewById(R.id.left);
		ImageButton right = (ImageButton) findViewById(R.id.right);
		ImageButton stop = (ImageButton) findViewById(R.id.stop);
		ImageButton forRig = (ImageButton) findViewById(R.id.forRig);
		ImageButton forLeft = (ImageButton) findViewById(R.id.forLeft);
		ImageButton bacRig = (ImageButton) findViewById(R.id.bacRig);
		ImageButton bacLeft = (ImageButton) findViewById(R.id.bacLeft);

		Button angleBottom = (Button) findViewById(R.id.angleBottom);
		Button angleMiddle = (Button) findViewById(R.id.angleMiddle);
		Button angleTop = (Button) findViewById(R.id.angleTop);
		Button stretchBottom = (Button) findViewById(R.id.stretchBottom);
		Button stretchTop = (Button) findViewById(R.id.stretchTop);

		Button axisBtn = (Button) findViewById(R.id.axisBtn);

		Button askBtn = (Button) findViewById(R.id.askBtn);

		backward.setOnClickListener(ClickListener);
		forward.setOnClickListener(ClickListener);
		left.setOnClickListener(ClickListener);
		right.setOnClickListener(ClickListener);
		stop.setOnClickListener(ClickListener);
		forRig.setOnClickListener(ClickListener);
		forLeft.setOnClickListener(ClickListener);
		bacRig.setOnClickListener(ClickListener);
		bacLeft.setOnClickListener(ClickListener);

		angleBottom.setOnClickListener(ClickListener);
		angleMiddle.setOnClickListener(ClickListener);
		angleTop.setOnClickListener(ClickListener);
		stretchBottom.setOnClickListener(ClickListener);
		stretchTop.setOnClickListener(ClickListener);

		
		nanoStatus = (TextView) findViewById(R.id.nanoStatusText);
		drivingStatus = (TextView) findViewById(R.id.drivingStatusText);
		
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
					MainActivity.SendMsgUart(1,askEncoderData);
	    			
	    			//MainActivity.SendMsgUartNano(startNanoPan);
	    			break;
	    			
	    		case R.id.uartBtn : 
	    			// Open Uart here
	    			
	    			if (OpenSetUartPort("ttymxc4") > 0)
	    				drivingStatus.setText("Driving mxc4 connected");
	    			
	    			if (OpenSetUartPort("ttymxc2") > 0)
	    				
	    				nanoStatus.setText("Nano mxc2 connected");
	    			
	    			//float test[] = floatTest();
	    			
	    			//Log.i(TAG,"find float test[0] = " + test[0] + " test[1] = " + test[1]);
	    			
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
	    				handler.postDelayed(rNano, 100);
    				
//	                   	new Thread(rNano).start();
	                   	//Start encoder thread
//	    				Runnable rencoder = new EncoderThread();
//	                   	new Thread(rencoder).start();
//	                    handler.postDelayed(rWEncoder, encoderWriteInterval);
//	                    handler.postDelayed(rREncoder, encoderReadInterval);
	                   	
	                   	//Start Combine Thread
//	                   	Runnable rcombind = new CombineThread();
//	                   	new Thread(rcombind).start();


	    				
	    				handler.postDelayed(rEncoder, encoderWriteWiatInterval + encoderReadWaitInterval);
	    				

	                    
	                   	handler.postDelayed(rCombine, 200);
	                   	
	                   	
	    			break;
	    		case R.id.angleBottom:
					Log.i(TAG,"angleBottom");
					//XMPPSet.XMPPSendText("james1", "pitchAngle bottom");
				try {
					SendUartByte("pitchAngle bottom");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					break;
				case R.id.angleMiddle:
					Log.i(TAG,"angleMiddle");
					//XMPPSet.XMPPSendText("james1", "pitchAngle middle");
				try {
					SendUartByte("pitchAngle middle");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					break;
				case R.id.angleTop:
					Log.i(TAG,"angleTop");
					//XMPPSet.XMPPSendText("james1", "pitchAngle top");
				try {
					SendUartByte("pitchAngle top");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					break;
				case R.id.stretchBottom:
					Log.i(TAG,"stretchBottom");
					//XMPPSet.XMPPSendText("james1", "stretch bottom");
				try {
					SendUartByte("stretch bottom");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					break;
				case R.id.stretchTop:
					Log.i(TAG,"stretchTop");
					//XMPPSet.XMPPSendText("james1", "stretch top");
				try {
					SendUartByte("stretch top");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
					break;

	    		case R.id.right :	
	    		case R.id.left :	
	    		case R.id.backward :	
	    		case R.id.forward :	
	    		case R.id.forLeft :	
	    		case R.id.forRig :	
	    		case R.id.bacLeft :	
	    		case R.id.bacRig :	
	    			Runnable r3 = new MyThread(v);
                   	new Thread(r3).start();

	    			
	    			break;
	    		default:
	    			Log.i(TAG,"Invaild Button function");
	    			break;
	    	}
	    }
	};
	
	
	private void SendUartByte(String inStr) throws IOException
	{
		
		String[] inM = inStr.split("\\s+");
		retStreamDatas = new ByteArrayOutputStream();
		
		if (inM[0].equals("pitchAngle") )
		{
			angleCmd.SetByte(inM);
			retStreamDatas = angleCmd.GetAllByte();
		}
		else if (inM[0].equals("stretch"))
		{
			stretCmd.SetByte(inM);
			retStreamDatas = stretCmd.GetAllByte();
		}
		
		byte[] retBytes = retStreamDatas.toByteArray();
		retStreamDatas.reset();
		
		for (int i =0;i<retBytes.length;i++)
		{
			retBytes[i] = (byte) (retBytes[i] & 0xFF);
		}
		
		
		
		//Log.i(TAG,"retBytes = " + retBytes);
		if (encoderOpend == true) {
			SendMsgUart(1,retBytes);
		}
	}
	
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
					else
					{
						if (encoderOpend == true) {
							retStreamDatas = new ByteArrayOutputStream();
							String[] sendStr = {"direction",sub};
							direc.SetByte(sendStr);
							try {
								retStreamDatas = direc.GetAllByte();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							
							byte[] retBytes = retStreamDatas.toByteArray();
							retStreamDatas.reset();
							
							for (int i =0;i<retBytes.length;i++)
							{
								retBytes[i] = (byte) (retBytes[i] & 0xFF);
							}
							
							SendMsgUart(1,retBytes);
						}
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
				if (nanoCount  > (nanoTestData.length - 1))
					nanoCount = 0;
					
				String nanoStr = nanoTestData[nanoCount];
				String[] daf = nanoStr.split(":");
				float[] myflot = {Float.parseFloat(daf[2]),Float.parseFloat(daf[0].substring(2, daf[0].length()))};
				
				//Log.i(TAG,"anchor number = " + myflot[0] + "data = " + myflot[1]);
				
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

								if (daf.length == 4)
								{
									float[] myflot = {
											Float.parseFloat(daf[2]),
											Float.parseFloat(daf[0].substring(
													2, daf[0].length())) };

									
									
									// If data > 0 , we use it , else ignore it.
									if (myflot[1] > 0) 				
									{
										Log.i(TAG, "Nano my float distance = "
												+ myflot[1]);
										nanoQueue.add(myflot);
									}
									// view.append(ReStr);
									// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
									ReStrNano = null;
								}
							}
						}
					}
				}
				
				handler.postDelayed(rNano,nanoInterval);
			}
			
			
		}
	}
	
	
	public class EncoderThreadPool implements Runnable {

		public void run() {

			//Log.i(TAG, "EncoderThreadPool");
			singleThreadExecutor.execute(rWEncoder);
			singleThreadExecutor.execute(rREncoder);

			handler.postDelayed(rEncoder, encoderWriteWiatInterval
					+ encoderReadWaitInterval);
		}

	}
	
	public class EncoderWriteThread implements Runnable {

		public void run() {

			if (debugEncoderQueue) {
				//writeLock();
				//MainActivity.SendMsgUart("test",2,askEncoderData);
				Log.i(TAG,"Write ask data");
				//writeUnLock();
				//handler.postDelayed(rWEncoder, encoderWriteWiatInterval);
			} else {
				
				//Log.i(TAG,"opend fd = " + uartCmd.GetDrivingOpend());

				if (encoderOpend == true) {
					//writeLock();
					Log.i(TAG,"Send Ask to Driving board");
					String ReStrEnco = null;
					try {
						ReStrEnco = new String(askEncoderData, "ISO-8859-1");
					} catch (UnsupportedEncodingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					MainActivity.SendMsgUart(1,askEncoderData);

				}
				try {
					Thread.sleep(encoderWriteWiatInterval);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//handler.postDelayed(rWEncoder, encoderWriteInterval);
			}

		}
	}
	
	
	public class EncoderReadThread implements Runnable {

		@TargetApi(Build.VERSION_CODES.GINGERBREAD) public void run() {

			if (debugEncoderQueue) {
				
				//readLock();
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
				
				//readUnLock();
				
				//handler.postDelayed(rREncoder, encoderReadWaitInterval);

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
					
					//readLock();
					//ReStrEnco = ReceiveMsgUart(1);
					//Log.i(TAG,"encoder read running");
					//while(true);
					//handler.postDelayed(rREncoder, encoderReadInterval);

					ReByteEnco = ReceiveByteMsgUart(1);

					
				
					//Log.i(TAG,"Length="+ReByteEnco.length+",0="+ ReByteEnco[0]+",1="+ReByteEnco[1]);
						//Log.i(TAG,"encoder rec msg = " + ReByteEnco + " leng = " + ReByteEnco.length);
						//for(int i=0;i<ReByteEnco.length;i++)
						//	Log.i("wr","encoder data[ " + i + "] = " + ReByteEnco[i]);
						if (  ReByteEnco.length  ==  11 && ReByteEnco[0] == 0x53 &&  ReByteEnco[1] == 0x0d)
						{
							
							//Log.i(TAG,"Receive message = "+ ReStrEnco);
							//encoderCmd.SetByte(ReStrEnco);
							//byte [] test = encoderCmd.GetDataByte();
							Log.i(TAG,"Encoder Receive test[0] = "+ReByteEnco[0] + "test1 = "+ ReByteEnco[1] + "test2 = "+ ReByteEnco[2]+ "test3 = "+ ReByteEnco[3]+ 
									"test4 = "+ ReByteEnco[4] + "test5 = "+ ReByteEnco[5]);
							//Log.i("123", "test6 = "+ ReByteEnco[6]);
							// Add receive message from Driving Board
							
							encoderCmd.SetDataByte(ReByteEnco);
							//byte [] test = encoderCmd.GetDataByte();
							
							encoderQueue.add(encoderCmd.GetDataByte());
							
							 
							 //Log.i(TAG,"receive Data byte = " + Arrays.copyOfRange(ReByteEnco, 2, 10));
							 //encoderQueue.add(Arrays.copyOfRange(ReByteEnco, 2, 10));
							
							//Log.i(TAG,"receive msg = " + ReStrEnco);
	
							// view.append(ReStr);
							// scrollView.fullScroll(ScrollView.FOCUS_DOWN);
							// Arrays.fill(ReByteEnco, (byte)0x00);
							// ReStrEnco = null;
							
							try {
								Thread.sleep(encoderReadWaitInterval);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						
						//readUnLock();
					
				}

				//handler.postDelayed(rREncoder, encoderReadWaitInterval);
			}

		}
	}
	
	
	
	public class CombineThread implements Runnable {

		public void run() {

			// Log.i(TAG,"encoderOpend = " + encoderOpend + "  nanoOpend = "
			// + nanoOpend );
			//Log.i(TAG, "nanoQueue.size() = " + nanoQueue.size()
			//		+ " encoderQueue.size() = " + encoderQueue.size());
			
			if ( ( nanoOpend== true || debugNanoQueue == true) 
					&& ( encoderOpend == true || debugEncoderQueue == true ) ) {
				// byte[] beSendMsg = new byte[beSentMessage];;

				Log.i(TAG, "nanoQueue.size() = " + nanoQueue.size()
						+ " encoderQueue.size() = " + encoderQueue.size());

				if (nanoQueue.size() >= getNanoDataSize + 4
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
					// Encoder data format
					// [L Polarity] [L2] [L1] [R polarity] [R2] [R1] [COM2] [COM1] [0x45]
					// Save to byte array beSendMsg[11]
					// ....................
					for (int i = 0; i < nanoData.size(); i++) {
						nanoFloat = nanoData.get(i);
						nanoFloat_1[i]=nanoFloat[1];
						Log.i(TAG, "combine nanoFloat [" + i + " ] = "
								+ nanoFloat_1[i]);
						
					}

					ArrayList<int[]> encoderDataQueue = new ArrayList<int[]>();
					byte[] encoByte = encoderData.get(0);
					int[] tempInt = new int[3]; // L Wheel , R Wheel , Compass
					for (int i=0;i<encoderData.size();i++)
					{
						tempInt[0]  = ( (encoByte[1] << 8) & 0xff00 | (encoByte[2] & 0xff));
						if (encoByte[0] == 2)
							tempInt[0] = -tempInt[0];
						
						tempInt[1]  = ( (encoByte[4] << 8) & 0xff00 | (encoByte[5] & 0xff));
						if (encoByte[3] == 2)
							tempInt[1] = -tempInt[1];
						
						tempInt[2]  = ( (encoByte[6] << 8) & 0xff00 | (encoByte[7] & 0xff));
						
						
						Log.i(TAG,"encoder data L=" + tempInt[0] + " R=" + tempInt[1] + " com = " + tempInt[2]);
						
						encoderDataQueue.add(tempInt);
					}
					
		///監看nanopan輸入值
					Log.i(TAG,"Nano1=" + nanoFloat_1[0] + " Nano2=" + nanoFloat_1[1] + " Nano3= " + nanoFloat_1[2]);
		///------EKF-----------------------------------------------------------------------------------------
					float robotLocation[] = EKF((float)nanoFloat_1[0],(float)nanoFloat_1[1],(float)nanoFloat_1[2],(int) tempInt[0],(int) tempInt[1],(int) tempInt[2]);
		///--------------------------------------------------------------------------------------------------
					Point point = new Point();
					point.x = robotLocation[0];
					point.y = robotLocation[1];
					
					//for test
					//point.x = 200;
					//point.y = 300;
					drawView.points.add(point);
					drawView.postInvalidate();
					
					try {
						Thread.sleep(50);
					} catch (Exception e) {
						e.printStackTrace();
					}
					// End
					encoderCount = 0;
					nanoCount = 0;
					nanoQueue.clear();
					encoderQueue.clear();
					// One Output Here
					// SendMsgUart(beSendMsg.toString(),1);
				}

				
			}
			handler.postDelayed(rCombine, combineInterval);
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
		
		/*****************************************/
		// example : arr{1,2,3,1,2,3,1,2}        //
		// We need 2nd {1,2,3} , so we clear arraylist , and add newest
		/*****************************************/
		
		for (int i=0;i< (list.size() / 3 );i++)
		{
			if (list.get(i*3)[0] == 1 && list.get(i*3 + 1)[0] == 2 && list.get(i*3 + 2)[0] == 3)
			{
				temp.clear();	// If we got newest data , clear old data .
				temp.add(list.get(i*3));
				temp.add(list.get(i*3 + 1));
				temp.add(list.get(i*3 + 2));
			}
		}
		
		/*Log.i(TAG,"start = " + start + " last = " + last);
		for (int x = start; x < last; x++) {
			temp.add(list.get(x));
			}
		 */
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
	public static native int SendMsgUartNano(String msg);
	public static native int SendMsgUart(int fdNum,byte[] inByte);
	public static native String ReceiveMsgUart(int fdNum);
	public static native byte[] ReceiveByteMsgUart(int fdNum);
	public static native int StartCal();
	public static native byte[] Combine(ArrayList<float[]> nanoq , ArrayList<int[]> encoq);
	public static native float[] floatTest();
	public static native float[] EKF(float a,float b,float c,int left,int right,int degree);
}