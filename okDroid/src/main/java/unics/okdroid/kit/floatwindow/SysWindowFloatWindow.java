package unics.okdroid.kit.floatwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.View;
import android.view.WindowManager;

/**
 * 基于悬浮窗模式的实现
 */
public class SysWindowFloatWindow implements FloatWindow {

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mLayoutParams;
    private View mView;

    @Override
    public void setup(View view, int width, int height, int gravity, int xOffset, int yOffset, boolean focusable) {
        mWindowManager = (WindowManager) view.getContext().getSystemService(Context.WINDOW_SERVICE);
        mView = view;
        mLayoutParams = new WindowManager.LayoutParams();
        mLayoutParams.flags =
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;
        mLayoutParams.format = PixelFormat.TRANSLUCENT;
        mLayoutParams.windowAnimations = 0;
        mLayoutParams.type = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? WindowManager.LayoutParams.TYPE_SYSTEM_ALERT : WindowManager.LayoutParams.TYPE_TOAST;
        mLayoutParams.width = width;
        mLayoutParams.height = height;
        mLayoutParams.gravity = gravity;
        mLayoutParams.x = xOffset;
        mLayoutParams.y = yOffset;
        if (focusable) {
            mLayoutParams.flags =
                    mLayoutParams.flags & ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        } else {
            mLayoutParams.flags =
                    mLayoutParams.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        }
    }

    @Override
    public void show() {
        if (mView == null) {
            throw new RuntimeException("请先调用setup方法");
        }
        mWindowManager.addView(mView, mLayoutParams);
    }

    @Override
    public void hide() {
        if (mView != null) {
            mWindowManager.removeView(mView);
        }
    }

    @Override
    public void onDestroy() {
        hide();
        mView = null;
    }

}
