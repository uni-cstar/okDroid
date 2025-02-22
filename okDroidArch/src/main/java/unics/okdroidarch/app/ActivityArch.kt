package unics.okdroidarch.app

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import unics.okcore.exception.tryIgnore
import unics.okdroid.tools.lifecycle.createPreferredViewModelFactory
import unics.okdroid.tools.ui.Dialog
import unics.okmultistate.bindStateLayout
import unics.okmultistate.uistate.LoaderUiState

/**
 * Created by Lucio on 2022/3/23.
 * @see MultiStateUserUi.handleLoaderUiState 本类默认采用[bindStateLayout]方式提供多状态视图的缺省实现：该方式会通过获取contentview的第一个view来判断，
 * 如果该view本身支持多状态视图，则直接返回该view，否则会创建一个多状态视图组件并附加到contentview结点下。
 */
abstract class ActivityArch : AppCompatActivity, MultiStateUserUi, RefresherUserUi {

    constructor() : super()

    constructor(contentLayoutId: Int) : super(contentLayoutId)

    override val realCtx: Context
        get() = this

    // Can't use ConcurrentHashMap, because it can lose values on old apis (see b/37042460)
    private val bagOfTags: MutableMap<String, Any> = HashMap()

    private var mFactory: ViewModelProvider.Factory? = null

    @PublishedApi
    internal val sTag: String = this.javaClass.simpleName

    /**
     * 是否使用推荐的构造工厂:建议使用，推荐的工厂在支持系统工厂的基础上，还扩展支持了Uri参数的构造函数，并且会自动注册[ViewModelArch]的事件绑定，避免使用者忘记注册的情况
     */
    protected var mUsePreferredFactory: Boolean = true

    @Suppress("UNCHECKED_CAST")
    override fun <T : Any> setTagIfAbsentBas(key: String, newValue: T): T {

        var previous: T?
        synchronized(bagOfTags) {
            previous = bagOfTags[key] as T?
            if (previous == null) {
                bagOfTags[key] = newValue
            }
        }
        return if (previous == null) newValue else previous!!
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

    override fun handleLoaderUiState(uiState: LoaderUiState) {
        bindStateLayout().setLoaderUiState(uiState)
    }

    /**
     * 处理下拉刷新状态
     */
    override fun handleRefreshLoaderUiState(uiState: LoaderUiState) {
        bindStateLayout().setLoaderUiState(uiState)
    }

    /**
     * 处理上拉加载更多状态
     */
    override fun handleLoadMoreLoaderUiState(uiState: LoaderUiState) {
        bindStateLayout().setLoaderUiState(uiState)
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

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        if (!mUsePreferredFactory) {
            return super.getDefaultViewModelProviderFactory()
        }
        require(application != null) {
            "Your activity is not yet attached to the Application instance. You can't request ViewModel before onCreate call."
        }
        if (mFactory == null) {
            mFactory = createPreferredViewModelFactory()
        }
        return mFactory!!
    }


    /**
     * 内联 -> 消息懒加载
     */
    inline fun logv(creator: () -> String) {
        unics.okdroid.deprecated.log.logv(sTag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun logd(creator: () -> String) {
        unics.okdroid.deprecated.log.logd(sTag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun logi(creator: () -> String) {
        unics.okdroid.deprecated.log.logi(sTag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun loge(creator: () -> String) {
        unics.okdroid.deprecated.log.loge(sTag, creator)
    }

    /**
     * 内联 -> 消息懒加载
     */
    inline fun loge(e: Throwable, creator: () -> String) {
        unics.okdroid.deprecated.log.loge(sTag, e, creator)
    }

//    override fun requestPermission(permissions: Array<String>, requestCode: Int) {
//        ActivityCompat.requestPermissions(this,permissions,requestCode)
//    }
}