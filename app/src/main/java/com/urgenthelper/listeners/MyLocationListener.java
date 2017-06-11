package com.urgenthelper.listeners;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.mapapi.map.MyLocationData;
import com.blankj.utilcode.utils.ToastUtils;
import com.urgenthelper.tcp.SendTask;
import com.urgenthelper.tcp.TcpController;
import com.urgenthelper.ui.activity.main.MainActivity;

import java.lang.ref.WeakReference;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/7.
 */

//BDLocationListener为结果监听接口，异步获取定位结果
public class MyLocationListener implements BDLocationListener,OnReceiverListener<String>{
    public static final int MSG_UNRESPONSE = 0x01;
    public static final int MSG_NETWORKRESPONSE = 0x02;
    public static MsgHandler mMsgHandler;

    private MainActivity mMainActivity;
    private TcpController mTcpController;
    public static ConcurrentHashMap<String,FutureTask<Integer>> mNetWorkMap;
    private ThreadPoolExecutor mThreadPoolExector;
    public int cmdStyle;//0:正常定位 1:紧急报警

    public MyLocationListener(MainActivity mainActivity){
        mMainActivity = mainActivity;
        mMsgHandler = new MsgHandler(this);
        mTcpController = TcpController.getInstance(this);
        mNetWorkMap = new ConcurrentHashMap<>();
        mThreadPoolExector = new ThreadPoolExecutor(1,
                1,
                1,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<Runnable>(5),
                new CustomThreadFactory(),
                new CustomRejectedExecutionHandler());
        cmdStyle = 0;
    }

    private class CustomThreadFactory implements ThreadFactory {
        private AtomicInteger count = new AtomicInteger(0);
        @Override
        public Thread newThread(Runnable r){
            Thread t = new Thread(r);
            t.setName("Thread-"+count.getAndIncrement());
            return t;
        }
    }

    private class CustomRejectedExecutionHandler implements RejectedExecutionHandler {
        @Override
        public void rejectedExecution(Runnable r,ThreadPoolExecutor executor){
            ToastUtils.showShortToast("任务添加过于频繁");
        }
    }

    @Override
    public void onReceiveLocation(BDLocation location) {
        MyLocationData data = new MyLocationData.Builder()
                .accuracy(location.getRadius())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .build();
        mMainActivity.mBaiduMap.setMyLocationData(data);
        switch(cmdStyle){
            case 1:
                sendLocData(location);
                break;
        }
    }

    //将定位结果发送出去
    private void sendLocData(BDLocation location){
        cmdStyle = 0;
        //sendLocDataByNetwork(location);
        sendLocDataBySms(location);
    }

    private void sendLocDataByNetwork(BDLocation location){
        String sendData = "15702923681+"+location.getLongitude()+","+location.getLatitude();
        submitTask(sendData);
    }

    private void sendLocDataBySms(BDLocation location){
        mMainActivity.mPhoneControl.sendMessage("15229307318",location.getLongitude()+","+location.getLatitude());
    }

    private void submitTask(String cmd){//使用线程池提交任务
        FutureTask<Integer> preTask = null;
        preTask = mNetWorkMap.get(cmd);
        if(preTask!=null && !preTask.isDone()){//之前的任务尚未完成,直接取消,不添加任务,先将前面未执行的相同任务完成
            return;
        }
        SendTask task = new SendTask(this,cmd);
        FutureTask<Integer> futureTask = new FutureTask<>(task);
        mNetWorkMap.put(cmd,futureTask);
        mThreadPoolExector.submit(futureTask);
    }

    public void sendData(String cmd){
        Handler handler = mTcpController.mThreadHandler;
        Message message = new Message();
        Bundle bundle = new Bundle();
        bundle.putString("cmd",cmd);
        message.setData(bundle);
        handler.sendMessage(message);
    }


    @Override
    public void onConnectHotSpotMessage(String s, int i) {

    }

    @Override
    public void onReceive(String data,String description){
        Message message = mMsgHandler.obtainMessage(MSG_NETWORKRESPONSE);
        Bundle bundle = new Bundle();
        bundle.putString("receive",data);
        message.setData(bundle);
        message.sendToTarget();
    }

    //创建静态内部类，防止内存泄漏
    public static class MsgHandler extends Handler {
        private WeakReference<MyLocationListener> mRef;

        public MsgHandler(MyLocationListener mMyLocationListener) {
            this.mRef = new WeakReference<>(mMyLocationListener);
        }

        public void handleMessage(Message msg) {//此方法在ui线程运行
            MyLocationListener mMyLocationListener = this.mRef.get();
            if (mMyLocationListener ==null) {
                return;
            }
            switch (msg.what) {
                case MSG_UNRESPONSE:
                    ToastUtils.showShortToast("发送失败,请重试");
                    break;
                case MSG_NETWORKRESPONSE:
                    Bundle bundle = msg.getData();
                    String data = bundle.getString("receive");
                    String key = data.substring(2);
                    //已接收成功,将任务取消
                    FutureTask<Integer> futureTask = mNetWorkMap.get(key);
                    if(futureTask!=null && !futureTask.isDone()){
                        futureTask.cancel(true);
                    }
                    ToastUtils.showShortToast("发送成功:"+key);
                    break;
            }
        }
    }
}
