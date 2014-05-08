package com.example.myapp;


import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.uboxol.serialcomport.ReadSerialDataException;
import com.uboxol.serialcomport.SerialPortListener;
import com.uboxol.serialcomport.SerialComPortControl;
import com.uboxol.serialcomport.SerialComPort.COM_ID;
import com.uboxol.serialcomport.SerialComPort.DATA_BITS;
import com.uboxol.serialcomport.SerialComPort.STOP_BITS;
import com.uboxol.serialcomport.SerialComPort.PARITY;
import com.uboxol.serialcomport.WriteSerialDataException;

import java.io.FileOutputStream;


public class MyActivity extends Activity {


    private final static String SD_PATH = "/mnt/sdcard/";

    private String getFileNamePath(String name)
    {
        return SD_PATH + name + ".txt";
    }
    public void WriteData(String message)
    {
        try{

            FileOutputStream out = new FileOutputStream( getFileNamePath("log"),true);

            byte [] bytes = message.getBytes();

            out.write(bytes);

            out.close();

        }

        catch(Exception e){

            e.printStackTrace();

        }
    }

    public void WriteData(byte [] bytes)
    {
        try{


            if (fout != null)
            {
                fout.write(bytes);
            }
        }

        catch(Exception e){
            e.printStackTrace();
            textView.setText( textView.getText().toString() + e.toString());

        }
    }

    private FileOutputStream fout;
    private COM_ID comId = COM_ID.COM_2;
    private TextView textView = null;
    private int comStatus = -1;

    private ConnectedThread connectedThread = null;
    private SerialComPortControl serialComPortControl;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textView = (TextView)findViewById(R.id.textView);

        try {
            fout = new FileOutputStream(getFileNamePath("testAppReceiveCom" + comId),true);
        }catch (Exception e)
        {
            e.printStackTrace();
            textView.setText( textView.getText().toString() + e.toString());
        }

        findViewById(R.id.btn9600).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               serialComPortControl.open(comId, 9600, DATA_BITS.BIT_8,STOP_BITS.BIT_1,PARITY.NONE , new SerialPortListener() {
                   @Override
                   public void dispatch(int flag) {
                       if (flag == 0)
                       {
                           textView.setText(textView.getText() + "打开成功\n");
                       }
                       else
                       {
                           textView.setText(textView.getText() + "打开失败\n");
                       }

                       comStatus = flag;
                       if (connectedThread != null && connectedThread.isRunning())
                       {
                           //do nothing
                       }
                       else
                       {
                            connectedThread = new ConnectedThread();
                            connectedThread.start();
                       }
                   }
               });
            }
        });

        findViewById(R.id.btn115200).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                serialComPortControl.open(comId, 115200, DATA_BITS.BIT_8,STOP_BITS.BIT_1,PARITY.NONE , new SerialPortListener() {
                    @Override
                    public void dispatch(int flag) {
                        if (flag == 0)
                        {
                            textView.setText(textView.getText() + "打开成功\n");
                        }
                        else
                        {
                            textView.setText(textView.getText() + "打开失败\n");
                        }
                        comStatus = flag;
                        if (connectedThread != null && connectedThread.isRunning())
                        {
                            //do nothing
                        }
                        else
                        {
                            connectedThread = new ConnectedThread();
                            connectedThread.start();
                        }
                    }
                });

            }
        });

        findViewById(R.id.btnSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
            }
        });

        serialComPortControl = new SerialComPortControl("ubox.example.myApp",myHandler,this);

    }

    private void sendMessage()
    {
        if( comStatus == 0)
        {
            try {
                serialComPortControl.send("start\n");
            } catch (WriteSerialDataException e) {
                e.printStackTrace();
            }
            textView.setText(textView.getText().toString() + "发送成功\n");
//            try {
//                int max = 1000 * 1024;
//
//                String str = "abcdefghigklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ012345678910,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30\n";
//                byte[] data = str.getBytes();
//                int len = data.length;
//                int sendLen = 0;
//
//                serialComPortControl.sendMessage("start\n");
//
//                sendLen += "start\n".getBytes().length;
//                while (sendLen < max) {
//
//                    try { Thread.sleep(5); } catch (Exception e) {
//                        e.printStackTrace();
//                        textView.setText(textView.getText().toString() +e.toString() + "\n");
//                    }
//
//                    serialComPortControl.sendMessage( data, len);
//                    sendLen += len;
//                }
//                sendLen += "end\n".getBytes().length;
//                serialComPortControl.sendMessage("end\n");
//
//                textView.setText(textView.getText().toString() + "sendAll "+ sendLen  + "\n");
//            }
//            catch (Exception e)
//            {
//                e.printStackTrace();
//                textView.setText(textView.getText().toString() + e.toString() + "\n");
//            }

        }
        else
        {
            textView.setText(textView.getText().toString() + "未打开串口\n");
        }
    }

    class ConnectedThread extends Thread{
        private boolean flag;

        public ConnectedThread()
        {
            flag = true;
        }
        @Override
        public void run() {
            while (comStatus == 0 && flag)
            {
                try {
                    byte[] b = new byte[100];
                    int length = 0;

                    length = serialComPortControl.read( b,100,100);

                    if (length > 0)
                    {
                        Message msg = new Message();
                        msg.what= 0;
                        msg.arg1 = length;
                        msg.obj = b;
                        myHandler.sendMessage(msg);
                    }
                } catch (ReadSerialDataException e) {
                    e.printStackTrace();
                }
            }

            flag = false;
        }

        public void cancel()
        {
            flag = false;
        }

        public boolean isRunning()
        {
            return flag;
        }
    }
    public Handler myHandler = new Handler() {

        @Override
        public void dispatchMessage(Message msg) {

            switch (msg.what) { // 这里是可以收到值的
                //接下来就要使用到msg.obj对象，如果在发送消息时没有对它进行赋值就会抛空指针了
                case 0:
                    byte[] arr = (byte[]) msg.obj;
//                    WriteData(arr);
                    textView.setText(textView.getText().toString() + "message : " + new String(arr) + "\n");
                    break;
                default:

                    break;
            }
            if(msg.obj instanceof String){
                textView.setText(textView.getText().toString() + "debug : " + (String)msg.obj + "\n");
            }

        }
    };

    @Override
    protected void onDestroy() {
        if (connectedThread != null)
        {
            connectedThread.cancel();
        }
        try
        {
            if (fout != null)
                fout.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            textView.setText( textView.getText().toString() + e.toString());
        }
        comStatus = -1;

        serialComPortControl.close();
        super.onDestroy();
    }

    @Override
    public void onStart()
    {
        try
        {
            if (fout == null)
            {
                fout = new FileOutputStream(getFileNamePath("testAppReceiveCom" + comId),true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            textView.setText( textView.getText().toString() + e.toString());
        }

        super.onStart();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }
}
