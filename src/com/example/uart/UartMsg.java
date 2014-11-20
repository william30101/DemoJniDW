package com.example.uart;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.example.demojni.MainActivity;

import android.util.Log;

public class UartMsg extends BaseCmd{

	private String[] cmdStr= {"direction","stop","pitchAngle","stretch","stopBySensor","ask",
			"destination","health","axis","ret","startBySensor","mapFromPIC32","encoder","mapControl","mode"};
	private byte[] cmdByte = {0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09,0x0a,0x0b,0x0c,0x0d,0x0e,0x0f};
	
	private static String TAG = "uart";
	public static int fd = 0,nanoFd = 0,driFd = 0;
	private boolean Uart_Check = false,nanoOpend = false;
	private static boolean encoderOpend = false;
	public int Uart_Port = -1;
	public static int Baud_rate = -1;
	ByteArrayOutputStream retStreamDatas;
	
	DirectionCmd direc = new DirectionCmd();
	StopCmd scmd = new StopCmd();
	
	static MainActivity main = new MainActivity();
	
	public UartMsg()
	{
		super.SetByte(cmdStr,cmdByte,2);
	}
	
	public byte[] GetAllByte(String[] inStr) throws IOException {

		retStreamDatas = new ByteArrayOutputStream();

		switch (super.GetByteNum(inStr[0], 2)) {
		case 0x01:

			direc.SetByte(inStr);
			retStreamDatas = direc.GetAllByte();
			break;

		case 0x02:

			scmd.SetByte(inStr);
			retStreamDatas = scmd.GetAllByte();
			break;

		case 0x03:
			//angleCmd.SetByte(inStr);
			//retStreamDatas = angleCmd.GetAllByte();
			break;

		case 0x04:
			//stretCmd.SetByte(inStr);
			//retStreamDatas = stretCmd.GetAllByte();
			break;

		case 0x05:
			break;

		case 0x06:
			//askCmd.SetByte(inStr);
			//retStreamDatas = askCmd.GetAllByte();
			break;

		case 0x07:
			break;

		// Need to modify it.
		case 0x08:
			//healCmd.GetByte(inStr);
			break;

		case 0x09:
			/*
			Integer R = new Integer(255);
			byte r = R.byteValue();
			Integer R2 = new Integer(200);
			byte r2 = R2.byteValue();
			byte[] test = { 0x01, 0x01, r, 0x01, 0x02, r2, 0x00, 0x00 };
			inStr[1] = new String(test, "ISO-8859-1");
			axisCmd.SetByte(inStr);
			retStreamDatas = axisCmd.GetAllByte();*/
			break;

		case 0x0a:
			break;

		default:
			break;
		}

		byte[] retBytes = retStreamDatas.toByteArray();
		retStreamDatas.reset();
		
		for (int i =0;i<retBytes.length;i++)
		{
			retBytes[i] = (byte) (retBytes[i] & 0xFF);
		}

		return retBytes;
	}
	
	//Baudrate [19200 , 115200 , 9600]
	
	public static int OpenSetUartPort(String portName)
	{
		
		// mxc0 for driving board , 19200
		// mxc2 for nanoPan , Baudrate 115200
		if (portName.equals("ttymxc4")) {
			Log.i(TAG,"ttymxc4 opend");
			//portName = "ttymxc4";
			//nanoFd = OpenUart(portName, 1 );
			driFd = main.OpenUart(portName, 1 );
			
			//if (nanoFd > 0) {
			if (driFd > 0) {
				encoderOpend = true;
				Baud_rate = 0; // 19200
				main.SetUart(Baud_rate, 1);
				fd = driFd;
			}

		} 
		else if (portName.equals("ttymxc2"))
		{
			Log.i(TAG,"ttymxc2 opend");
			//portName = "ttymxc4";
			//nanoFd = OpenUart(portName, 1 );
			nanoFd = main.OpenUart(portName, 2 );
			
			//if (nanoFd > 0) {
			if (nanoFd > 0) {

				Baud_rate = 2; // 9600
				main.SetUart(Baud_rate, 2);
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
	
	/*static
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
	public static native int WeightSet(float dwWeight , float encoderWeight);
	public static native float[] EKF(float a,float b,float c,int left,int right,int degree);
	*/
}
