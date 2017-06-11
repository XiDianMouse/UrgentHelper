package com.urgenthelper.sms;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.SmsManager;

import com.urgenthelper.broadcastreceivers.SmsReceiver;
import com.urgenthelper.ui.activity.main.MainActivity;

import java.util.ArrayList;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/11.
 */

public class PhoneControl {

    private PendingIntent mSmsSendPI;
    private PendingIntent mSmsDeliverPI;
    private BroadcastReceiver mBroadcastReceiver;

    public interface Content{
        String SEND_SMS_ACTION ="send.sms.action";
        String DELIVERED_SMS_ACTION = "delivered.sms.action";
    }

    public PhoneControl(MainActivity mainActivity){
        mSmsSendPI = PendingIntent.getBroadcast(mainActivity,0,new Intent(Content.SEND_SMS_ACTION),0);
        mSmsDeliverPI = PendingIntent.getBroadcast(mainActivity,0,new Intent(Content.DELIVERED_SMS_ACTION),0);
        mBroadcastReceiver = new SmsReceiver();
        mainActivity.registerReceiver(mBroadcastReceiver,myIntentFilter());
    }

    private IntentFilter myIntentFilter(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Content.SEND_SMS_ACTION);
        intentFilter.addAction(Content.DELIVERED_SMS_ACTION);
        return intentFilter;
    }

    public void sendMessage(String phoneNumber,String message){
        SmsManager smsManager = SmsManager.getDefault();
        ArrayList<String> msgs = smsManager.divideMessage(message);
        for(String msg:msgs){
            smsManager.sendTextMessage(phoneNumber,null,msg,mSmsSendPI,mSmsDeliverPI);
        }
    }
}
