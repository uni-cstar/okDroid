package unics.okdroid.kit.truetime;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * 处理设备时间不准的问题
 *
 * @see #sync()
 * @see #async()
 * @see #currentTimeMillis()
 * @see #currentTimeMillisOrDefault()
 */
public class TrueTime {

    private static class SingleToneHolder {
        private static final TrueTime INSTANCE = new TrueTime();
    }

    public static TrueTime getInstance() {
        return SingleToneHolder.INSTANCE;
    }

    /**
     * 注册监听系统时间发生变化的广播
     * @param context
     * @return
     */
    public static BroadcastReceiver registerTimeChangedReceiver(Context context){
        return SysTimeChangeReceiver.register(context);
    }

    /**
     * 时间同步器
     */
    public interface Synchronizer {
        long sync();
    }

    /**
     * 同步回调
     */
    public interface Callback {
        void onTimeSynced(long mills);
    }

    private static final int WHAT_NOTIFY_TIME_SYNCED = 1;

    private long mSyncedTimeMillis = 0;

    private long mElapsedWhenSynced = 0;

    //待同步的请求数量
    private int mSyncingPendingCount = 0;

    private Synchronizer mSyncher = new NtpTimeSynchronizer();

    private ArrayList<Callback> mCallbacks;

    private final Handler.Callback mHandlerCallback = new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message msg) {
            if (msg.what == WHAT_NOTIFY_TIME_SYNCED) {
                if (mCallbacks != null) {
                    long time = currentTimeMillis();
                    for (Callback item : mCallbacks) {
                        item.onTimeSynced(time);
                    }
                }
                return true;
            }
            return false;
        }
    };
    private Handler mHandler;

    private TrueTime() {
    }

    /**
     * 时间是否进行了同步
     */
    public boolean isSynced() {
        return mSyncedTimeMillis > 0;
    }

    public boolean isSyncing() {
        return mSyncingPendingCount > 0;
    }

    /**
     * 系统时间发生了变化
     */
    void onSystemTimeChanged() {
        log("onSystemTimeChanged --");
        if (isSynced()) {
            setSyncedTimeMillis(syncedCurrentTimeMillis());
        } else {
            if (isSyncing()) {
                log("onSystemTimeChanged -> is syncing,waiting.");
                mSyncingPendingCount += 1;
                return;
            }
            async();
            log("onSystemTimeChanged -> async.");
        }
    }

    /**
     * 同步时间
     */
    public synchronized long sync() {
        long mills = syncImpl();
        mSyncingPendingCount = 0;
        return mills;
    }

    public synchronized void async() {
        if (isSyncing()) {
            mSyncingPendingCount += 1;
            return;
        }
        mSyncingPendingCount += 1;
        new Thread(new Runnable() {
            @Override
            public void run() {
                syncImpl();
                mSyncingPendingCount = 0;
                if (mCallbacks != null) {
                    if (mHandler == null) {
                        mHandler = new Handler(Looper.getMainLooper(), mHandlerCallback);
                    }
                    mHandler.sendEmptyMessage(WHAT_NOTIFY_TIME_SYNCED);
                }
            }
        }).start();
    }

    public long currentTimeMillis() {
        if (!isSynced()) {
            throw new IllegalStateException("请先调用sync/async同步时间");
        }
        return syncedCurrentTimeMillis();
    }

    public long currentTimeMillisOrDefault() {
        if (!isSynced()) {
            return System.currentTimeMillis();
        }
        return syncedCurrentTimeMillis();
    }

    public void setSynchronizer(@NonNull Synchronizer synchronizer) {
        this.mSyncher = synchronizer;
    }

    public void addCallback(@NonNull Callback callback) {
        if (mCallbacks == null) {
            mCallbacks = new ArrayList<>();
        }
        mCallbacks.add(callback);
    }

    public void removeCallback(@NonNull Callback callback) {
        if (mCallbacks == null)
            return;
        mCallbacks.remove(callback);
    }

    @RequiresPermission(Manifest.permission.SET_TIME)
    public void setDeviceTime(Context context) {
        if (!isSynced()) {
            throw new IllegalStateException("请先调用sync/async同步时间");
        }
        long time = syncedCurrentTimeMillis();
        SystemClock.setCurrentTimeMillis(time);
        // 更新系统时钟设置,0是不自动更新，1自动更新
//        Settings.Global.putInt(context.getContentResolver(), Settings.Global.AUTO_TIME, 1);
    }

    private long syncedCurrentTimeMillis() {
        long elapsed = SystemClock.elapsedRealtime();
        //时间差
        long duration = elapsed - mElapsedWhenSynced;
        return mSyncedTimeMillis + duration;
    }

    private long syncImpl() {
        long value = mSyncher.sync();
        setSyncedTimeMillis(value);
        return value;
    }

    private void setSyncedTimeMillis(long millis) {
        setSyncedTimeMillis(millis, SystemClock.elapsedRealtime());
    }

    private void setSyncedTimeMillis(long millis, long elapsed) {
        mSyncedTimeMillis = millis;
        mElapsedWhenSynced = elapsed;
//        log(String.format("setSyncedTimeMillis -> currentTimeMillis=%d syncedTimeMillis=%d systemTimeUncalibrated=%b",
//                System.currentTimeMillis(), mSyncedTimeMillis, mSystemTimeUncalibrated));
    }

    static void log(String message) {
        Log.d("TrueTime", message);
    }

    /**
     * 基于NTP的时间同步器
     */
    public static class NtpTimeSynchronizer implements Synchronizer {

        public static String[] NTP_SERVERS = new String[]{
                "ntp1.aliyun.com",
//                "ntp2.aliyun.com",
//                "ntp3.aliyun.com",
//                "ntp4.aliyun.com",
//                "ntp5.aliyun.com",
//                "ntp6.aliyun.com",
//                "ntp7.aliyun.com",
                "cn.pool.ntp.org",
                "cn.ntp.org.cn",
                "sg.pool.ntp.org",
                "tw.pool.ntp.org",
                "jp.pool.ntp.org",
                "hk.pool.ntp.org",
                "th.pool.ntp.org",
                "time.windows.com",
                "time.nist.gov",
                "time.apple.com",
                "time.asia.apple.com",
                "dns1.synet.edu.cn",
                "news.neu.edu.cn",
                "dns.sjtu.edu.cn",
                "dns2.synet.edu.cn",
                "ntp.glnet.edu.cn",
                "s2g.time.edu.cn",
                "ntp-sz.chl.la",
                "ntp.gwadar.cn",
                "3.asia.pool.ntp.org"
        };

        private final SntpClient mClient;
        private final String[] mServers;
        private int mTimeout = 1000;
        private int mMaxRequestCount = 0;

        public NtpTimeSynchronizer() {
            this(NTP_SERVERS, 1000);
            mMaxRequestCount = 5;
        }

        /**
         * @param ntpServers ntp服务器站点集合
         * @param timeout    每个请求消耗的时间
         */
        public NtpTimeSynchronizer(String[] ntpServers, int timeout) {
            if (ntpServers == null || ntpServers.length == 0) {
                throw new IllegalArgumentException("the ntp servers is not allowed to be empty.");
            }
            mServers = ntpServers;
            mClient = new SntpClient();
            mTimeout = timeout;
            mMaxRequestCount = ntpServers.length;
        }

        @Override
        public long sync() {
            for (int i = 0; i < mMaxRequestCount; i++) {
                try {
                    if (mClient.requestTime(mServers[i], mTimeout)) {
                        return mClient.getNtpTime() + SystemClock.elapsedRealtime() - mClient.getNtpTimeReference();
                    }
                } catch (Throwable e) {
                    log("Ntp error " + e);
                    e.printStackTrace();
                }
            }
            throw new RuntimeException("ntp sync time fail,the result is 0.");
        }
    }

    /**
     * 基于Http请求的时间同步器
     * 这方法要注意，因为如果时间本身不准的情况下，很多站点都不能正常访问
     */
    @Deprecated
    public static class NetworkTimeSynchronizer implements Synchronizer {

        private final String mUrl;
        private static final String BACKUP_URL = "https://www.baidu.com/";

        public NetworkTimeSynchronizer() {
            this("https://www.tencent.com/");
        }

        public NetworkTimeSynchronizer(@NonNull String url) {
            if (TextUtils.isEmpty(url)) {
                throw new IllegalArgumentException("The url parameter is not allowed to be empty.");
            }
            this.mUrl = url;
        }

        @Override
        public long sync() {
            try {
                long time = requestTime(mUrl);
                if (time <= 0) {
                    time = requestTime(BACKUP_URL);
                }
                if (time <= 0)
                    throw new RuntimeException("sync time fail,the result is 0.");
                return time;
            } catch (Throwable e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

        private long requestTime(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("HEAD");
            long serverTime = connection.getDate();
            log(String.format("requestTime -> the request date result of %s is %s", url, serverTime));
            try {
                connection.disconnect();
            } catch (Throwable e) {
                e.printStackTrace();
            }

            if (serverTime > 0) {
                return serverTime;
            }
            return 0;
        }
    }

}
