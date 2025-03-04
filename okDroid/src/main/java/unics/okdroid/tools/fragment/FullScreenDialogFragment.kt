package unics.okdroid.tools.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import unics.okdroid.R

/**
 * Created by Lucio on 2021/6/4.
 */
abstract class FullScreenDialogFragment : DialogFragment() {


    final override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return createContentView(inflater, container, savedInstanceState)
    }

    override fun getTheme(): Int {
        return R.style.OkTheme_FullScreenDialog
    }

    abstract fun createContentView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onStart() {
        super.onStart()
        // 设置全屏参数
        dialog?.window?.apply {
            setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT)) // 透明背景
            setWindowAnimations(R.style.OkTheme_WindowFadeAnim)
        }
    }

}

