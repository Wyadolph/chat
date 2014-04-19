package com.example.android_client;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.text.InputType;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.lang.reflect.Field;
import com.example.android_server.R;
public class MainActivity extends Activity
{
    private static BufferedReader br= null; 
    private Socket s;
    private String line;
    private PrintWriter pw;
    private EditText ipet;
    private String ip;
    private EditText nameet;
    private String screen_name;
    private Thread th;
    private final static int prefix = 12;
    private Field field;
    private Button bt;
    private boolean connected = false;
    private InputStreamReader isr;
    private TextView send;
    final Handler myhandler = new Handler()//将收到的话显示出来
    {
    	public void handleMessage(Message msg)
    	{
    		TextView show =(TextView)findViewById(R.id.display);
    		TextView friends =(TextView)findViewById(R.id.online);
    		friends.setMovementMethod(ScrollingMovementMethod.getInstance());
            show.setMovementMethod(ScrollingMovementMethod.getInstance());
    		if(msg.what == 0x123)
    		{
    			show.append(line.substring(8)+"\n");
    		}
    		if(msg.what == 0x456)
    		{
    		   Log.d("chat","get online friends");
    		   if(null != line.substring(prefix))//从NAMEACCEPTED之后开始
    		   {
    			   friends.append(line.substring(prefix));    
    		   }
    		}
    	}
    };
    protected void getField(DialogInterface dialog,boolean motivation)
    {
		try {
			field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
			if(true == motivation)
			  field.setAccessible(true);
			else
			  field.setAccessible(false);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
    }
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        ipet = new EditText(this);
        ipet.setInputType(InputType.TYPE_CLASS_PHONE);
        nameet = new EditText(this);
        new AlertDialog.Builder(this)
        .setTitle("请输入服务器IP地址")
        .setView(ipet)
        .setPositiveButton("登入", new DialogInterface.OnClickListener() 
        {  
            public void onClick(DialogInterface dialog, int whichButton) 
            {  
            	boolean flag = false; //标记对话框是否被关闭
            	ip = ipet.getText().toString();
            	setContentView(R.layout.activity_main);
            	th = new Thread(thread);
    	        th.start();
    	        try {
					Thread.sleep(2000);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
    	        if(!connected)
    	        {    
        	    	Toast.makeText(getApplicationContext(),"fail to connect",Toast.LENGTH_SHORT).show();
    	            getField(dialog,false);
				}
	        	else
	        	{
    	            flag = true;
    	            getField(dialog,true);
        	        dialog.dismiss();
        	    }
    	        if(flag)//IP输入框已经被关闭，可以打开用户名输入框
        	    {
        	    	Log.d("chat","in the name");
        	    	new AlertDialog.Builder(MainActivity.this)
                    .setTitle("请输入用户名")
                    .setView(nameet)
                    .setPositiveButton("确定", new DialogInterface.OnClickListener() 
                    {  
                        public void onClick(DialogInterface dialog, int whichButton) 
                        {  
                        	screen_name = nameet.getText().toString();
							if(screen_name.isEmpty())
							{
								getField(dialog, false);
							    dialog.dismiss();
							}
							else
							{
								getField(dialog, true);
						        pw.println(screen_name);	
							} 
						}  
                    })
                .show()
                .setCanceledOnTouchOutside(false)
                ; 
        	    } 
            }  
        })
        .show()
        .setCanceledOnTouchOutside(false)
        ;    
    }
    protected void onStart()
    {
    	super.onStart();
    	Log.d("chat","started");
    }
    protected void onPause()
    {
    	super.onPause();
    	Log.d("chat","paused");
    }
    protected void onStop()
    {
    	super.onStop();
    	Log.d("chat","stopped");
    }
    protected void onDestroy()
    {
    	try {
			s.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	super.onDestroy();
    	
    	Log.d("chat","destroyed");
    }
    protected void onResume()
    {
    	super.onResume();send = (TextView)findViewById(R.id.input);
    	Log.d("chat","resumed");
    }
    protected void onRestart()
    {
    	super.onRestart();
    	Log.d("chat","restarted");
    }
    Runnable thread = new Runnable()
    {
    	public void getMessage(int whatnum)
    	{
    		Message msg = new Message();
			msg.what = whatnum;
			msg.obj = line;
			myhandler.sendMessage(msg);
    	}
		public void run()
    	{ 
    		try
			{
    			ip = ipet.getText().toString();
    		    //ip = "192.168.1.114";
				if((s = new Socket(ip, 9999)) ==null)
				{
					Log.d("chat","null");
					return;
				}
				connected = true;
			    isr = new InputStreamReader(s.getInputStream(),"GB2312");
				br = new BufferedReader(isr);
				pw = new PrintWriter(s.getOutputStream(), true);
				bt =(Button)findViewById(R.id.button);
				send = (TextView)findViewById(R.id.input);
		   		Looper.prepare();
		   	    bt.
		        setOnClickListener(
		        	new View.OnClickListener()
				{
				    public void onClick(View v) 
				    {
					    Log.d("chat","button clicked");
						String info = send.getText().toString();
						pw.println(info);
				     }
				});
				while(true)
				{
			        line = br.readLine();
				    if(line.startsWith("NAMEACCEPTED"))
				    {
				    	Log.d("chat","get name accepted");
				    	getMessage(0x456);
	                }
				    else if (line.startsWith("MESSAGE"))
				    {
				    	Log.d("chat","get message");
				        getMessage(0x123);
				    }
				}
			} 
			catch (IOException e)
			{
				e.printStackTrace();
			}
         }
   };
   
}