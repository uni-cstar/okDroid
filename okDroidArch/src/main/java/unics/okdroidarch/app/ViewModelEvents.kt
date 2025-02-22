package unics.okdroidarch.app

import android.content.Context
import android.content.Intent
import android.util.Log
import unics.okdroid.tools.ui.ProgressDialog
import unics.okdroid.tools.ui.ToastUI
import unics.okdroid.tools.ui.dialogUi
import unics.okdroid.tools.ui.droidExceptionHandler
import unics.okdroid.tools.ui.toastUi
import unics.okdroidarch.app.ViewModelEventMessage.*
import unics.okmultistate.uistate.LoaderUiState
import java.lang.ref.SoftReference

/**
 * 用于处理ViewModel中的事件
 * 参考：https://developer.android.google.cn/jetpack/guide/ui-layer/events#handle-viewmodel-events
 *
 * 即ViewModel对外发送的实现信息，一般由Activity或者Fragment监听处理
 * 目前实现的信息有：
 * @see ToastMessage toast 消息
 * @see LoadingAlertMessage loading对话框消息
 * @see HideLoadingAlertMessage loading对话框关闭消息，与[LoadingAlertMessage]对应
 * @see UiExceptionMessage ui操作异常消息，该消息会将异常抛给[droidExceptionHandler]用于异常处理器处理异常：通常是通过[dialogUi]显示一个错误提示对话框
 * @see LoaderUiMessage 多状态视图消息，通常配合droid-multistate库的[StateLayout]同步使用，效果超赞
 *
 * @param id 消息的唯一键，用于唯一标识信息
 * @param extra 用于消息附带的内容
 */
sealed class ViewModelEventMessage(val id: Long, val extra: Any?) {

    protected var target: ViewModelArch? = null

    internal fun setViewModelTargetIfNull(target: ViewModelArch?) {
        this.target = target
    }

    /**
     * 处理消息
     */
    internal fun resolve(ui: UserUi) {
        try {
            handleInternal(ui)
        } finally {
            onHandled()
        }
    }

    /**
     * 处理本条消息
     */
    protected abstract fun handleInternal(ui: UserUi)

    protected open fun onHandled() {
        this.target?.onEventMessageHandled(this)
        this.target = null
    }

    /**
     * 显示toast
     */
    class ToastMessage @JvmOverloads constructor(
        id: Long = System.nanoTime(),
        val message: String,
        val duration: Int = ToastUI.LENGTH_SHORT,
        extra: Any? = null
    ) : ViewModelEventMessage(id, extra) {

        override fun handleInternal(ui: UserUi) {
            toastUi.showToast(ui.realCtx, message, duration)
        }
    }

    /**
     * 显示loading alert
     *
     * todo: 目前一条loading 对应一个hide loading，如需使用多个，请通过不同的id实现
     */
    class LoadingAlertMessage @JvmOverloads constructor(
        val message: String,
        id: Long = System.nanoTime(),
        extra: Any? = null
    ) :
        ViewModelEventMessage(id, extra) {

        override fun handleInternal(ui: UserUi) {
            //显示当前id之前，先隐藏之前显示的相同id的对话框
            ui.hideLoadingProgressInternal(id.toString())
            val dialog = dialogUi.showLoading(ui.realCtx, message)
            ui.setTagIfAbsentBas(id.toString(), dialog)
        }
    }

    /**
     * 隐藏loading alert
     * todo: 目前一条hide loading 对应一个 loading，如需使用多个，请通过不同的id实现
     * @param id 本消息id
     * @param showMessageId 需要关闭的消息id
     */
    class HideLoadingAlertMessage @JvmOverloads constructor(
        val showMessageId: Long,
        id: Long = System.nanoTime(),
        extra: Any? = null
    ) : ViewModelEventMessage(id, extra) {

        fun dismiss() {
            target?.sendHideLoadingAlertMessage(this)
        }

        override fun handleInternal(ui: UserUi) {
            ui.hideLoadingProgressInternal(showMessageId.toString())
        }
    }

    class CmdMessage(private val cmd: Int, id: Long = System.nanoTime(), extra: Any? = null) :
        ViewModelEventMessage(id, extra) {

        @Suppress("UNCHECKED_CAST")
        override fun handleInternal(ui: UserUi) {
            when (cmd) {
                CMD_ID_FINISH -> {
                    ui.finish()
                }

                CMD_ID_CONTEXT_BLOCK -> {
                    val blockRef = extra as? SoftReference<((Context) -> Unit)?> ?: return
                    blockRef.get()?.invoke(ui.realCtx)
                }

                CMD_ID_START_ACTIVITY_FOR_RESULT -> {
                    val extra = extra as? Pair<Intent, Int> ?: return
                    ui.startActivityForResult(extra.first, extra.second)
                }
            }
        }

        companion object {

            //调用finish命令
            private const val CMD_ID_FINISH = 88881

            //访问Context执行代码块
            private const val CMD_ID_CONTEXT_BLOCK = 99991

            //访问Context执行代码块
            private const val CMD_ID_START_ACTIVITY_FOR_RESULT = 99992

            /**
             * 关闭
             */
            @JvmStatic
            fun finish(): CmdMessage {
                return CmdMessage(CMD_ID_FINISH)
            }

            /**
             * 执行context相关的代码块
             */
            @JvmStatic
            fun contextBlock(block: (Context) -> Unit): CmdMessage {
                return CmdMessage(CMD_ID_CONTEXT_BLOCK, extra = SoftReference(block))
            }

            @JvmStatic
            fun startActivityForResult(intent: Intent, requestCode: Int): CmdMessage {
                return CmdMessage(
                    CMD_ID_START_ACTIVITY_FOR_RESULT,
                    extra = Pair(intent, requestCode)
                )
            }
        }
    }

    class UiExceptionMessage @JvmOverloads constructor(
        val error: Throwable,
        id: Long = System.nanoTime(),
        extra: Any? = null
    ) :
        ViewModelEventMessage(id, extra) {

        override fun handleInternal(ui: UserUi) {
            droidExceptionHandler.handleUIException(ui.realCtx, error)
        }
    }

    class LoaderUiMessage(
        val uiState: LoaderUiState,
        id: Long = System.nanoTime(),
        extra: Any? = null
    ) : ViewModelEventMessage(id, extra) {

        override fun handleInternal(ui: UserUi) {
            if (uiState is LoaderUiState.Error) {
                droidExceptionHandler.handleCatchException(uiState.error)
            }
            if (ui is MultiStateUserUi) {
                ui.handleLoaderUiState(uiState)
            } else {
                Log.w(
                    "ViewModelArch",
                    "LoaderUiMessage (${uiState}) is ignored.because $ui is not implement ${MultiStateUserUi::class.java.name}"
                )
            }
        }
    }

    class RefreshUiMessage(
        val uiState: LoaderUiState,
        id: Long = System.nanoTime(),
        extra: Any? = null
    ) : ViewModelEventMessage(id, extra) {

        override fun handleInternal(ui: UserUi) {
            if (ui is RefresherUserUi) {
                ui.handleRefreshLoaderUiState(uiState)
            } else {
                Log.w(
                    "ViewModelArch",
                    "RefreshUiMessage (${uiState}) is ignored.because $ui is not implement ${RefresherUserUi::class.java.name}"
                )
            }
        }
    }

    class LoadMoreUiMessage(
        val uiState: LoaderUiState,
        id: Long = System.nanoTime(),
        extra: Any? = null
    ) : ViewModelEventMessage(id, extra) {

        override fun handleInternal(ui: UserUi) {
            if (ui is RefresherUserUi) {
                ui.handleLoadMoreLoaderUiState(uiState)
            } else {
                Log.w(
                    "ViewModelArch",
                    "LoadMoreUiMessage (${uiState}) is ignored.because $ui is not implement ${RefresherUserUi::class.java.name}"
                )
            }
        }
    }
}

/**
 * 隐藏loading对话框
 */
internal fun UserUi.hideLoadingProgressInternal(key: String) {
    val previous = this.getTagBasAndRemove<ProgressDialog>(key)
    previous?.dismiss()
}