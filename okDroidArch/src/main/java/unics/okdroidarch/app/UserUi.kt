package unics.okdroidarch.app

import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import unics.okdroid.tools.app.AnonyContext
import unics.okmultistate.uistate.LoaderUiState

/**
 * 用户UI：用于对Activity和Fragment之间无差别处理逻辑
 */
interface UserUi : LifecycleOwner, AnonyContext {

    /*目前没有更好的办法：本意是想在UserUi中提供一个容器，用于方便向内存放一些对象，类似ViewModel内部的实现方式*/
    fun <T : Any> setTagIfAbsentBas(key: String, newValue: T): T

    fun <T : Any> getTagBas(key: String): T?

    fun <T : Any> getTagBasAndRemove(key: String): T?

    fun removeTagBas(key: String)
    /*目前没有更好的办法：本意是想在UserUi中提供一个容器，用于方便向内存放一些对象*/

}

/**
 * 多状态视图UI，与[UserUi]不同的是多了处理加载器状态的能力
 */
interface MultiStateUserUi : UserUi {

    /**
     * 处理多状态视图消息
     */
    fun handleLoaderUiState(uiState: LoaderUiState)
}

/**
 * 处理包含有上下拉刷新的场景
 */
interface RefresherUserUi : UserUi {

    fun handleRefreshLoaderUiState(uiState: LoaderUiState)

    fun handleLoadMoreLoaderUiState(uiState: LoaderUiState)

}

/**
 * 注册ViewModel事件的处理逻辑
 */
fun UserUi.registerViewModelEventsRepeatOnLifecycle(
    viewModel: ViewModelArch,
    state: Lifecycle.State
) {
    val ui = this
    lifecycleScope.launch {
        ui.preferredLifeCycleOwner.repeatOnLifecycle(state) {
            viewModel.eventUiState.collectLatest {
                it.firstOrNull()?.resolve(ui)
            }
        }
    }
}

fun UserUi.registerViewModelEvents(viewModel: ViewModelArch) {
    val ui = this
    preferredLifeCycleOwner.lifecycleScope.launch {
        viewModel.eventUiState.collectLatest {
            it.firstOrNull()?.resolve(ui)
        }
    }
}

fun UserUi.registerViewModelEventsWhenCreated(viewModel: ViewModelArch) {
    val ui = this
    preferredLifeCycleOwner.lifecycleScope.launchWhenCreated {
        viewModel.eventUiState.collectLatest {
            it.firstOrNull()?.resolve(ui)
        }
    }
}

fun UserUi.registerViewModelEventsWhenStarted(viewModel: ViewModelArch) {
    val ui = this
    preferredLifeCycleOwner.lifecycleScope.launchWhenStarted {
        viewModel.eventUiState.collectLatest {
            it.firstOrNull()?.resolve(ui)
        }
    }
}

fun UserUi.registerViewModelEventsWhenResumed(viewModel: ViewModelArch) {
    val ui = this

    preferredLifeCycleOwner.lifecycleScope.launchWhenResumed {
        viewModel.eventUiState.collectLatest {
            it.firstOrNull()?.resolve(ui)
        }
    }
}

/**
 * 如果UserUi是Fragment，则优先使用[Fragment.getViewLifecycleOwner],否则才使用自身的[LifecycleOwner]
 */
inline val UserUi.preferredLifeCycleOwner: LifecycleOwner
    get() = (this as? Fragment)?.viewLifecycleOwner ?: this