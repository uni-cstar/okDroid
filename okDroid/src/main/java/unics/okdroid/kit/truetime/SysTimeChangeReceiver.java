package unics.okdroid.kit.truetime;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;

import unics.okdroid.tools.net.NetworksKt;


class SysTimeChangeReceiver extends BroadcastReceiver {

    /**
     * 动态注册
     */
    static BroadcastReceiver register(Context context) {
        SysTimeChangeReceiver receiver = new SysTimeChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_TIME_CHANGED);
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(Intent.ACTION_TIMEZONE_CHANGED);
        //网络变化，有网的时候同步一下时间
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(receiver, filter);
        return receiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String action = intent.getAction();
            if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action)
                    || Intent.ACTION_TIME_CHANGED.equals(action)
                    || Intent.ACTION_DATE_CHANGED.equals(action)
                    || Intent.ACTION_TIMEZONE_CHANGED.equals(action)) {
                TrueTime.log("onReceive:" + action);
                if (NetworksKt.isNetworkConnected(context)) {
                    TrueTime.getInstance().onSystemTimeChanged();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
