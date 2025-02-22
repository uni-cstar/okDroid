package unics.okdroid.kit.floatwindow;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;


public class FloatWindowUiState {

    public int rangeType;

    public int width = ViewGroup.LayoutParams.MATCH_PARENT;
    public int height = ViewGroup.LayoutParams.MATCH_PARENT;
    public int gravity = Gravity.BOTTOM;

    public int xOffset = 0;
    public int yOffset = 0;
    public boolean focusable = false;

    private final FloatWindow.ViewCreator viewCreator;

    public FloatWindowUiState(FloatWindow.ViewCreator creator){
        viewCreator = creator;
    }

    public View createView(Context context){
        return viewCreator.createView(context);
    }

}
