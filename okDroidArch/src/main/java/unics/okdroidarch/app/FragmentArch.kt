package unics.okdroidarch.app

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import unics.okcore.exception.tryIgnore
import unics.okdroid.tools.ui.Dialog
import unics.okdroid.widget.removeFromParent
import unics.okmultistate.bindStateLayout
import unics.okmultistate.uistate.LoaderUiState

abstract class FragmentArch : Fragment, MultiStateUserUi, RefresherUserUi {

//    companion object {
//        private const val EXTRA_VIEW_FLAG = "_view_cacheable_"
//        private const val EXTRA_LAYOUT_ID_FLAG = "_layout_id_"
//    }

    // Can't use ConcurrentHashMap, because it can lose values on old apis (see b/37042460)
    private val bagOfTags: MutableMap<String, Any> = HashMap()

    override val realCtx: Context
        get() = requireContext()

    /**
     * 是否启用View缓存：默认开启,可以设置为false避免view 缓存
     */
    protected open val viewCacheable: Boolean = true

    protected var contentView: View? = null
        private set

    private var hasCallInitView: Boolean = false

    constructor() : super()
    constructor(contentLayoutId: Int) : super(contentLayoutId)

    protected open fun createContentView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = null

    final override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return if (viewCacheable) {
            contentView?.also {
                it.removeFromParent()
            } ?: createContentViewInternal(inflater, container, savedInstanceState).also {
                contentView = it
            }
        } else {
            createContentViewInternal(inflater, container, savedInstanceState)
        }
    }

    private fun createContentViewInternal(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //先用super去加载layoutid的布局，如果为空，再加载对应的内容布局
        return super.onCreateView(inflater, container, savedInstanceState) ?: createContentView(
            inflater,
            container,
            savedInstanceState
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!hasCallInitView) {
            hasCallInitView = true
            initView(view, savedInstanceState)
        }
    }

    /**
     * 初始化view：只会在第一次view创建的时候调用
     */
    protected open fun initView(view: View, savedInstanceState: Bundle?) {

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (viewCacheable) {
            contentView?.removeFromParent()
        } else {
            contentView = null
        }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> setTagIfAbsentBas(key: String, newValue: T): T {
        var previous: T?
        synchronized(bagOfTags) {
            previous = bagOfTags[key] as T?
            if (previous == null) {
                bagOfTags[key] = newValue
            }
        }
        val result = if (previous == null) newValue else previous!!
        return result
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getTagBas(key: String): T? {
        synchronized(bagOfTags) { return bagOfTags[key] as T? }
    }

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> getTagBasAndRemove(key: String): T? {
        synchronized(bagOfTags) {
            val value = bagOfTags[key] as T?
            bagOfTags.remove(key)
            return value
        }
    }

    override fun removeTagBas(key: String) {
        synchronized(bagOfTags) {
            bagOfTags.remove(key)
        }
    }

    override fun finish() {
        activity?.finish()
    }

    override fun handleLoaderUiState(uiState: LoaderUiState) {
        view?.bindStateLayout()?.setLoaderUiState(uiState)
    }

    override fun handleRefreshLoaderUiState(uiState: LoaderUiState) {
        view?.bindStateLayout()?.setLoaderUiState(uiState)
    }

    override fun handleLoadMoreLoaderUiState(uiState: LoaderUiState) {
        view?.bindStateLayout()?.setLoaderUiState(uiState)
    }

    override fun onDestroy() {
        tryIgnore {
            //在销毁之前先关闭可能存在的未关闭的对话框
            bagOfTags.values.forEach {
                if (it is Dialog && it.isShowing) {
                    it.dismiss()
                }
            }
        }
        super.onDestroy()
    }

//    @Deprecated("不推荐使用")
//    override fun requestPermission(permissions: Array<String>, requestCode: Int) {
//        requestPermissions(permissions, requestCode)
//    }
}