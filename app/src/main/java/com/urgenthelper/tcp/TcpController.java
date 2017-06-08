package com.urgenthelper.tcp;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.urgenthelper.listeners.OnReceiverListener;
import com.urgenthelper.ui.activity.main.LocationResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/7.
 */

public class TcpController implements Runnable{
    private OnReceiverListener<String> mOnReceiverListener;
    public Handler mThreadHandler;
    public Socket mSocket;
    private BufferedReader mBufferedReader;
    private OutputStream mOutputStream;
    private static TcpController instance;

    private TcpController(LocationResponse locationResponse){
        mOnReceiverListener = locationResponse;
    }

    public static TcpController getInstance(LocationResponse locationResponse){
        if(instance==null){
            instance = new TcpController(locationResponse);
        }
        return instance;
    }

    public static TcpController getInstance(){
        return instance;
    }

    @Override
    public void run(){
        try {
            mSocket = new Socket("117.34.105.157", 19527);
            mSocket.setKeepAlive(true);
            // 如果输入流等待设定毫秒还未获得服务端发送数据(即read方法的超时时间)，则提示超时引发SocketTimeoutException，
            // 此时Socket仍然有效。若设置为0为永不超时。
            mSocket.setSoTimeout(0);
            mBufferedReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
            mOutputStream = mSocket.getOutputStream();
            startReadThread();
            writeDataToServer();
        }
        catch (SocketTimeoutException e){
        }
        catch (IOException e) {
        }
    }

    private void startReadThread(){
        //启动一条子线程来读取服务器响应的数据
        new Thread(){
            @Override
            public void run(){
                String content = null;
                //不断读取Socket输入流中的内容
                try{
                    while((content=mBufferedReader.readLine())!=null){
                        //界面显示数据
                        if(mOnReceiverListener!=null){
                            mOnReceiverListener.onReceive(content,null);
                        }
                    }
                }catch(IOException e){

                }
            }
        }.start();
    }

    private void writeDataToServer(){
        Looper.prepare();
        mThreadHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                try{
                    //向服务器写入数据
                    Bundle bundle = msg.getData();
                    String cmd = bundle.getString("cmd")+"\r\n";
                    mOutputStream.write(cmd.getBytes("utf-8"));
                }catch (IOException e){

                }
            }
        };
        Looper.loop();
    }
}

