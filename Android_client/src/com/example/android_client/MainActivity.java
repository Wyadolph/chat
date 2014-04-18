package com.example.android_client;

import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
    private static ServerSocket serverSocket = null;
    private static Socket client = null;
    private final static int port = 9999;
    private static BufferedReader br= null; 
    private static BufferedWriter bw = null;
    private Socket s;
    private String line;
    private EditText edittext;
    private PrintWriter pw;
    private EditText ipet;
    private String ip;
    private EditText nameet;
    private String screen_name;
    private Thread th;
    public static final int prefix = 12;
    final Handler myhandler = new Handler()
    {
    	public void handleMessage(Message msg)
    	{
    		TextView show =(TextView)findViewById(R.id.display);
    		TextView friends =(TextView)findViewById(R.id.online);
    		show.setMovementMethod(ScrollingMovementMethod.getInstance());
    		if(msg.what == 0x123)
    		{
    			show.append(line.substring(8)+"\n");
    		}
    		if(msg.what == 0x456)
    		{
    		   if(null != line.substring(prefix))//从NAMEACCEPTED之后开始
    			  friends.append(line.substring(prefix));
    			    ;
    		}
    	}
    };
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
            	try 
            	{
            	    Field field = dialog.getClass()
            	            .getSuperclass().getDeclaredField(
            	                     "mShowing" );
            	    field.setAccessible( true );
            	     //   将mShowing变量设为false，表示对话框已关闭 
            	    if(!ip.equals("192.168.1.115"))
            	        field.set(dialog, false );
            	    else
            	    {
            	    	field.set(dialog, true);
            	        th = new Thread(thread);
            	        th.start();
            	    	flag = true;
            	    }
            	    dialog.dismiss();
            	    if(flag)//IP输入框已经被关闭，可以打开用户名输入框
            	    {
            	    	new AlertDialog.Builder(MainActivity.this)
	                    .setTitle("请输入用户名")
	                    .setView(nameet)
	                    .setPositiveButton("确定", new DialogInterface.OnClickListener() 
	                    {  
	                        public void onClick(DialogInterface dialog, int whichButton) 
	                        {  
	                        	screen_name = nameet.getText().toString();
								try {
									 Field field = dialog.getClass()
									        .getSuperclass().getDeclaredField(
									                 "mShowing" );
									 field.setAccessible( true );
									 if(screen_name.isEmpty())
			                    	     field.set(dialog, false);
									 else
										 field.set(dialog, true);
									 dialog.dismiss();
									 Log.d("chat",screen_name);
									 pw.println(screen_name);
								} catch (NoSuchFieldException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalAccessException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (IllegalArgumentException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}  
								setContentView(R.layout.activity_main);
	                        }  
	                    })
                    .show()
                    .setCanceledOnTouchOutside(false)
                    ; 
            	    } 
            	}
            	catch (Exception e)
            	{

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
    	super.onDestroy();
    	Log.d("chat","destroyed");
    }
    protected void onResume()
    {
    	super.onResume();
    	Log.d("chat","resumed");
    }
    protected void onRestart()
    {
    	super.onRestart();
    //	th.start();
    	Log.d("chat","restarted");
    }
    Runnable thread = new Runnable()
    {
		public void run()
    	{ 
    		try
			{
    			ip = ipet.getText().toString();
    			Log.d("chat",ip);
				s = new Socket(ip, 9999);
				InputStreamReader isr = new InputStreamReader(s.getInputStream(),"GB2312");
				br = new BufferedReader(isr);
				pw= new PrintWriter(s.getOutputStream(), true);
		   		Looper.prepare();
				while(true)
				{
			        line = br.readLine();
				    //Log.d("chat",line);
				    if(line.startsWith("SUBMITNAME"))
				    {
				    //	pw.println(screen_name);
				    }
				    else if(line.startsWith("NAMEACCEPTED"))
				    {
				    	Message msg = new Message();
						msg.what = 0x456;
						msg.obj = line;
						myhandler.sendMessage(msg);
				    	
						Button bt=(Button)findViewById(R.id.button);
						bt.setOnClickListener(new View.OnClickListener() {
						private TextView send = (TextView)findViewById(R.id.input);
						@Override 
						public void onClick(View v) {
							// TODO Auto-generated method stub
							Log.d("chat","button clicked");
							String info = send.getText().toString();
							pw.println(info);
							/*Message msg = new Message();
							msg.what = 0x456;
							msg.obj = info;
							myhandler.sendMessage(msg);
							send.setText("");*/
						}
						});
				    }
				    //else if(line.startsWith("MESSAGE"))
				    else if (line.startsWith("MESSAGE"))
				    {
				    	Message msg = new Message();
						msg.what = 0x123;
						msg.obj = line;
						myhandler.sendMessage(msg);
				    }
				}
			} 
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
         }
   };
   
}
