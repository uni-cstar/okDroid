package unics.okdroidarch.app

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import unics.okcore.coroutines.ControlledRunner
import unics.okmultistate.uistate.LoaderUiState
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Lucio on 2022/3/23.
 * @note 为确保viewmodel的事件可用，需要先在UI（[ActivityArch]、[FragmentArch]是对应子类]）
 * 中进调用[UserUi.registerViewModelEventsRepeatOnLifecycle]方法进行事件绑定
 */
open class ViewModelArch : ViewModel() {

    private val joinPreviousRunners: ConcurrentHashMap<String, ControlledRunner<Any?>> =
        ConcurrentHashMap()

    private val cancelPreviousRunners: ConcurrentHashMap<String, ControlledRunner<Any?>> =
        ConcurrentHashMap()

    private val _eventUiState = MutableStateFlow<List<ViewModelEventMessage>>(emptyList())

    /**
     * 事件流：Toast、Alert、Loading、Error等事件，传递到Activity去执行
     */
    val eventUiState: StateFlow<List<ViewModelEventMessage>> = _eventUiState

    /**
     * 执行或加入之前启动的[key]相同的任务：即如果之前已经启动了同名Key的任务，并且未执行完成，则当前认为会忽略，并
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> launchOrJoinPrevious(key: String, func: suspend () -> T): T {
        val runner = synchronized(joinPreviousRunners) {
            joinPreviousRunners.getOrPut(key) {
                ControlledRunner()
            }
        }
        return runner.joinPreviousOrRun(func) as T
    }


    /**
     * 执行并取消之前启动的[key]相同的任务
     */
    @Suppress("UNCHECKED_CAST")
    suspend fun <T> launchAndCancelPrevious(key: String, func: suspend () -> T): T {
        val runner = synchronized(cancelPreviousRunners) {
            cancelPreviousRunners.getOrPut(key) {
                ControlledRunner()
            }
        }
        return runner.cancelPreviousThenRun(func) as T
    }

    /**
     * 该方法在捕获异常的时候发送了一条UI异常消息
     */
    inline fun <R : Any?> tryUiCatching(block: () -> R): Result<R> {
        return runCatching(block).onFailure {
            sendUiExceptionMessage(it)
        }
    }

    /**
     * 该方法在捕获异常的时候发送了一条[LoaderUiState]异常消息
     */
    inline fun <R : Any> tryMultiStateUiCatching(block: () -> R): Result<R> {
        return runCatching(block).onFailure {
            sendMultiStateMessage(LoaderUiState.error(it))
        }
    }

    /**
     * 发送toast消息
     * @return 返回值应该没有用
     */
    fun sendToastMessage(
        message: String,
        duration: Int = Toast.LENGTH_SHORT
    ): ViewModelEventMessage.ToastMessage {
        val eventMessage =
            ViewModelEventMessage.ToastMessage(message = message, duration = duration)
        sendToastMessage(eventMessage)
        return eventMessage
    }

    /**
     * 发送toast消息
     */
    fun sendToastMessage(message: ViewModelEventMessage.ToastMessage) {
        return sendEventMessage(message)
    }

    /**
     * 发送loading对话框消息
     * @see sendLoadingAlertMessage(@param ViewModelEventMessage.LoadingAlertMessage)
     */
    fun sendLoadingAlertMessage(
        message: String,
        extra: Any? = null
    ): ViewModelEventMessage.HideLoadingAlertMessage {
        val eventMessage =
            ViewModelEventMessage.LoadingAlertMessage(message = message, extra = extra)
        return sendLoadingAlertMessage(eventMessage)
    }

    /**
     * 发送loading对话框消息
     * @return 返回[ViewModelEventMessage.HideLoadingAlertMessage]对象，借助[ViewModelEventMessage.HideLoadingAlertMessage.dismiss]方法即可发送关闭对应loading对话框消息
     */
    fun sendLoadingAlertMessage(message: ViewModelEventMessage.LoadingAlertMessage): ViewModelEventMessage.HideLoadingAlertMessage {
        val hideMessage = ViewModelEventMessage.HideLoadingAlertMessage(
            showMessageId = message.id,
            extra = message
        ).also {
            it.setViewModelTargetIfNull(this)
        }
        sendEventMessage(message)
        return hideMessage
    }

    fun sendFinishMessage() {
        sendCmdMessage(ViewModelEventMessage.CmdMessage.finish())
    }

    fun sendContextBlockMessage(block: (Context) -> Unit) {
        sendCmdMessage(ViewModelEventMessage.CmdMessage.contextBlock(block))
    }

    fun sendStartActivityForResultMessage(intent: Intent, requestCode: Int) {
        sendCmdMessage(ViewModelEventMessage.CmdMessage.startActivityForResult(intent, requestCode))
    }

    fun sendCmdMessage(message: ViewModelEventMessage.CmdMessage) {
        sendEventMessage(message)
    }

    fun sendUiExceptionMessage(throwable: Throwable) {
        sendUiExceptionMessage(ViewModelEventMessage.UiExceptionMessage(throwable))
    }

    fun sendUiExceptionMessage(message: ViewModelEventMessage.UiExceptionMessage) {
        sendEventMessage(message)
    }

    /**
     * 注意：消息接受者，必须实现[MultiStateUserUi]接口，否则消息无法响应实现
     */
    fun sendMultiStateMessage(uiState: LoaderUiState) {
        sendMultiStateMessage(ViewModelEventMessage.LoaderUiMessage(uiState))
    }

    /**
     * 注意：消息接受者，必须实现[MultiStateUserUi]接口，否则消息无法响应实现
     */
    fun sendMultiStateMessage(message: ViewModelEventMessage.LoaderUiMessage) {
        sendEventMessage(message)
    }

    /**
     * 注意：消息接受者，必须实现[RefresherUserUi]接口，否则消息无法响应实现
     */
    fun sendRefreshMessage(uiState: LoaderUiState) {
        sendRefreshMessage(ViewModelEventMessage.RefreshUiMessage(uiState))
    }

    /**
     * 注意：消息接受者，必须实现[RefresherUserUi]接口，否则消息无法响应实现
     */
    fun sendRefreshMessage(message: ViewModelEventMessage.RefreshUiMessage) {
        sendEventMessage(message)
    }

    /**
     * 注意：消息接受者，必须实现[RefresherUserUi]接口，否则消息无法响应实现
     */
    fun sendLoadMoreMessage(uiState: LoaderUiState) {
        sendLoadMoreMessage(ViewModelEventMessage.LoadMoreUiMessage(uiState))
    }

    /**
     * 注意：消息接受者，必须实现[RefresherUserUi]接口，否则消息无法响应实现
     */
    fun sendLoadMoreMessage(message: ViewModelEventMessage.LoadMoreUiMessage) {
        sendEventMessage(message)
    }

    /**
     * 处理了对应消息
     */
    fun onEventMessageHandled(message: ViewModelEventMessage) {
        onEventMessageHandled(message.id)
    }

    /**
     * 处理了某条消息：该方法会移除所有指定id的消息
     * 通常用于消息处理之后调用该方法
     */
    fun onEventMessageHandled(id: Long) {
        _eventUiState.update { currentUiState ->
            currentUiState.filterNot {
                it.id == id
            }
        }
    }

    internal fun sendHideLoadingAlertMessage(message: ViewModelEventMessage.HideLoadingAlertMessage) {
        sendEventMessage(message)
    }

    private fun sendEventMessage(message: ViewModelEventMessage) {
        message.setViewModelTargetIfNull(this)
        _eventUiState.update {
            it + message
        }
    }

}
