package unics.okdroid.kit.floatwindow;

import android.content.Context;
import android.view.View;


public interface FloatWindow {

    interface ViewCreator{
        View createView(Context context);
    }
    /**
     * 初始化
     * @param view
     * @param width
     * @param height
     * @param gravity
     * @param xOffset
     * @param yOffset
     * @throws Throwable
     */
    void setup(View view,
               int width, int height, int gravity, int xOffset, int yOffset,boolean focusable);

    /**
     * 显示
     * @throws Throwable
     */
    void show();

    /**
     * 隐藏
     * @throws Throwable
     */
    void hide();

    /**
     * 销毁
     */
    void onDestroy();
}
