package unics.okdroid.kit.floatwindow;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Toast实现方式：在{@link android.os.Build.VERSION_CODES#P}之前可用
 */
public class ToastFloatWindow implements FloatWindow {

    private Toast mToast;
    private Object mTN;
    private Method mShowMethod, mHideMethod;
    private Field mTNNextViewField;
    private WindowManager.LayoutParams mParams;
    private View mView;

    @Override
    public void setup(View view, int width, int height, int gravity, int xOffset, int yOffset, boolean focusable) {
        try {
            mView = view;
            mToast = new Toast(view.getContext().getApplicationContext());
            mToast.setGravity(gravity, xOffset, yOffset);
            @SuppressLint("SoonBlockedPrivateApi") Field tnField = Toast.class.getDeclaredField("mTN");
            tnField.setAccessible(true);
            mTN = tnField.get(mToast);
            assert mTN != null;
            Class<?> tnCls = mTN.getClass();
            mShowMethod = tnCls.getMethod("show");
            mHideMethod = tnCls.getMethod("hide");
            Field tnParamsField = tnCls.getDeclaredField("mParams");
            tnParamsField.setAccessible(true);
            mParams = (WindowManager.LayoutParams) tnParamsField.get(mTN);
            assert mParams != null;
            mParams.width = width;
            mParams.height = height;
            mParams.windowAnimations = 0;
            if (focusable) {
                mParams.flags =
                        mParams.flags & ~WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            } else {
                mParams.flags =
                        mParams.flags | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
            }
            mTNNextViewField = tnCls.getDeclaredField("mNextView");
            mTNNextViewField.setAccessible(true);
        } catch (NoSuchFieldException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void show() {
        try {
            mTNNextViewField.set(mTN, mView);
            mShowMethod.invoke(mTN);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void hide() {
        try {
            mHideMethod.invoke(mTN);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDestroy() {
        hide();
        mToast = null;
        mView = null;
    }


}
