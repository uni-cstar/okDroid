package unics.okdroid.tools.device;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Point;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

/**
 * @author: chaoluo10
 * @date: 2024/4/27
 * @desc: todo 待整理方法
 */
public class ScreenUtil {
    /***
     * 获取屏幕的高度，全面屏和非全面屏
     * @param context
     * @return
     */
    public static int getFullActivityHeight(@Nullable Context context) {
        if (!isAllScreenDevice(context)) {
            return getScreenDisplayHeight(context);
        }
        return getScreenRealHeight(context);
    }

    private static final int PORTRAIT = 0;
    private static final int LANDSCAPE = 1;
    private volatile static boolean mHasCheckAllScreen;
    private volatile static boolean mIsAllScreenDevice;
    @NonNull
    private static final Point[] mRealSizes = new Point[2];


    public static Point getScreenRealSize(@NonNull Context context) {
        int orientation = context.getResources().getConfiguration().orientation;
        orientation = orientation == Configuration.ORIENTATION_PORTRAIT ? PORTRAIT : LANDSCAPE;
        if (mRealSizes[orientation] == null) {
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            if (windowManager == null) {
                return null;
            }
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            mRealSizes[orientation] = point;
        }
        return mRealSizes[orientation];
    }

    /**
     * 获取屏幕真实高度
     */
    public static int getScreenRealHeight(@NonNull Context context) {
        Point size = getScreenRealSize(context);
        if(size == null)
            return getScreenDisplayHeight(context);
        return size.y;
    }

    /**
     * 获取屏幕展示高度：全面屏可能不包含状态栏（底部特殊栏）的高度
     */
    @Px
    public static int getScreenDisplayHeight(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenDisplayWidth(@NonNull Context context){
        return context.getResources().getDisplayMetrics().widthPixels;
    }


    /***
     * 获取当前手机是否是全面屏
     * @return
     */

    public static boolean isAllScreenDevice(Context context) {
        if (mHasCheckAllScreen) {
            return mIsAllScreenDevice;
        }
        mHasCheckAllScreen = true;
        mIsAllScreenDevice = false;
        // 低于 API 21的，都不会是全面屏。。。
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return false;
        }
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            Display display = windowManager.getDefaultDisplay();
            Point point = new Point();
            display.getRealSize(point);
            float width, height;
            if (point.x < point.y) {
                width = point.x;
                height = point.y;
            } else {
                width = point.y;
                height = point.x;
            }
            if (height / width >= 1.97f) {
                mIsAllScreenDevice = true;
            }
        }
        return mIsAllScreenDevice;
    }
}
