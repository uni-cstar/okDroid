package unics.okdroid.kit.floatwindow;

import android.app.Application;
import android.util.Log;

import unics.okdroid.tools.app.ApplicationManager;
import unics.okdroid.tools.app.ApplicationManagerKt;


public class NoticeFloatWindowTools {

    /**
     * 应用内显示
     */
    public static final int RANGE_INSIDE = 1;

    /**
     * 系统窗口显示
     */
    public static final int RANGE_OVERLAY = 2;

    private Application mApp;
    private FloatWindowUiState mUiState;
    private FloatWindow mFloatWindow;
    private boolean mForeground = false;
    private boolean mShowing = false;

    private NoticeFloatWindowTools() {
    }

    private static class SingleTon {
        private static final NoticeFloatWindowTools instance = new NoticeFloatWindowTools();
    }

    public static NoticeFloatWindowTools getInstance() {
        return SingleTon.instance;
    }

    public void init(Application app) {
        mApp = app;
        ApplicationManagerKt.AppManager.registerAppStateChangedListener(new ApplicationManager.OnAppStateChangedListener() {
            @Override
            public void onAppBecameForeground() {
                try {
                    onForeground();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onAppBecameBackground() {
                try {
                    onBackground();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private FloatWindow createFloatWindow() {
        return new ToastFloatWindow();
    }

    public void show(FloatWindowUiState uiState) {
        if (mUiState == uiState)
            return;
        hide();
        mUiState = uiState;
        if (uiState.rangeType == RANGE_OVERLAY || mForeground) {
            showInternal(uiState);
        }
    }

    public void hide() {
        log("hide -> mUiState=" + mUiState);
        if (mFloatWindow != null) {
            mFloatWindow.hide();
        }
        mShowing = false;
    }

    public void onForeground() {
        log("onForeground -> set isForeground true and showInterval");
        mForeground = true;
        FloatWindowUiState uiState = mUiState;
        if (uiState != null && uiState.rangeType == RANGE_INSIDE) {
            if (!mShowing) {
                showInternal(uiState);
            }
        }
    }

    public void onBackground() {
        log("onBackground");
        mForeground = false;
        if (mUiState != null && mUiState.rangeType == RANGE_INSIDE) {
            log("onBackground -> hideInternal");
            hide();
        }
    }

    private void showInternal(FloatWindowUiState uiState) {
        if (mFloatWindow == null) {
            mFloatWindow = createFloatWindow();
            mFloatWindow.setup(uiState.createView(mApp), uiState.width, uiState.height, uiState.gravity, uiState.xOffset, uiState.yOffset, uiState.focusable);
        }
        mFloatWindow.show();
        mShowing = true;
    }

    private void log(String msg) {
        Log.d("NoticeFloatWindow", "log: " + msg);
    }
}
