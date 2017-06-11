package com.urgenthelper.broadcastreceivers;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.blankj.utilcode.utils.ToastUtils;
import com.urgenthelper.sms.PhoneControl;

/**
 * @auther gbh
 * Email:xidian_guobenhao@163.com
 * Created on 2017/6/11.
 */

public class SmsReceiver extends BroadcastReceiver{

    public SmsReceiver(){

    }

    @Override
    public void onReceive(Context context, Intent intent){
        if(PhoneControl.Content.SEND_SMS_ACTION.equals(intent.getAction())){
            if(getResultCode()== Activity.RESULT_OK){
                ToastUtils.showShortToast("短信发送成功");
            }else{
                ToastUtils.showShortToast("短信发送失败");
            }
        }else if(PhoneControl.Content.DELIVERED_SMS_ACTION.equals(intent.getAction())){
            ToastUtils.showShortToast("收件人已成功接收");
        }
    }
}
