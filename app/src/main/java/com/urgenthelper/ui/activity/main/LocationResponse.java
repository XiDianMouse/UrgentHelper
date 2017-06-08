package com.urgenthelper.ui.activity.main;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.Poi;
import com.blankj.utilcode.utils.ToastUtils;
import com.urgenthelper.app.MyApp;
import com.urgenthelper.listeners.OnReceiverListener;
import com.urgenthelper.tcp.SendTask;
import com.urgenthelper.tcp.TcpController;

import java.lang.ref.WeakReference;
import java.util.List;
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
public class LocationResponse implements BDLocationListener,OnReceiverListener<String>{
    public static final int MSG_UNRESPONSE = 0x01;
    public static final int MSG_NETWORKRESPONSE = 0x02;
    public static MsgHandler mMsgHandler;

    private MyApp mMyApp;
    private TcpController mTcpController;
    public static ConcurrentHashMap<String,FutureTask<Integer>> mNetWorkMap;
    private ThreadPoolExecutor mThreadPoolExector;

    public LocationResponse(MyApp myApp){
        mMyApp = myApp;
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
        //Receive Location
        StringBuffer sb = new StringBuffer(256);
        sb.append("time : ");
        sb.append(location.getTime());//获取定位时间
        sb.append("\nerror code : ");
        sb.append(location.getLocType());
        sb.append("\nlatitude : ");
        sb.append(location.getLatitude());
        sb.append("\nlontitude : ");
        sb.append(location.getLongitude());
        sb.append("\nradius : ");
        sb.append(location.getRadius());//获取定位精准度
        if (location.getLocType() == BDLocation.TypeGpsLocation) {
            //GPS定位结果
            sb.append("\nspeed : ");
            sb.append(location.getSpeed());// 单位：公里每小时
            sb.append("\nsatellite : ");
            sb.append(location.getSatelliteNumber());
            sb.append("\nheight : ");
            sb.append(location.getAltitude());// 单位：米
            sb.append("\ndirection : ");
            sb.append(location.getDirection());// 单位度
            sb.append("\naddr : ");
            sb.append(location.getAddrStr());
            sb.append("\ndescribe : ");
            sb.append("gps定位成功");

        } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {
            sb.append("\naddr : ");//网络定位结果
            sb.append(location.getAddrStr());
            sb.append("\noperationers : ");//运营商信息
            sb.append(location.getOperators());
            sb.append("\ndescribe : ");
            sb.append("网络定位成功");
        } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {
            //离线定位结果
            sb.append("\ndescribe : ");
            sb.append("离线定位成功，离线定位结果也是有效的");
        } else if (location.getLocType() == BDLocation.TypeServerError) {
            sb.append("\ndescribe : ");
            sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
        } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
            sb.append("\ndescribe : ");
            sb.append("网络不同导致定位失败，请检查网络是否通畅");
        } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
            sb.append("\ndescribe : ");
            sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
        }
        sb.append("\nlocationdescribe : ");
        sb.append(location.getLocationDescribe());// 位置语义化信息
        List<Poi> list = location.getPoiList();// POI数据
        if (list != null) {
            sb.append("\npoilist size = : ");
            sb.append(list.size());
            for (Poi p : list) {
                sb.append("\npoi= : ");
                sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
            }
        }
        //输出信息:sb.toString
        sendLocData(location);
    }

    //将定位结果发送出去
    private void sendLocData(BDLocation location){
        mMyApp.mLocationClient.stop();//停止定位
        String sendData = "15702923681+"+location.getLongitude()+","+location.getLatitude();
        submitTask(sendData);
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
        private WeakReference<LocationResponse> mRef;

        public MsgHandler(LocationResponse locationResponse) {
            this.mRef = new WeakReference<>(locationResponse);
        }

        public void handleMessage(Message msg) {//此方法在ui线程运行
            LocationResponse locationResponse = this.mRef.get();
            if (locationResponse ==null) {
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
