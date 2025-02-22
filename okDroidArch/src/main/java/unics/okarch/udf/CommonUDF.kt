/**
 * @author: chaoluo10
 * @date: 2024/6/24
 * @desc:
 */
package unics.okarch.udf

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import unics.okmultistate.uistate.LoaderUiState

data class CommonUiState<T>(
    @JvmField val data: T? = null, @JvmField val loaderUiState: LoaderUiState = LoaderUiState.LAZY
) : UDFData

/**
 * 通用UDFFlow
 */
interface CommonUDFlow<T> : SharedUDFlow<CommonUiState<T>> {

    /**
     * 加载器状态流
     */
    val loaderUiStateFlow: Flow<LoaderUiState>

    /**
     * 数据流
     */
    val dataFlow: Flow<T>

}

fun <T> CommonUDFlow(
    scope: CoroutineScope,
    intentCapacity: Int = Channel.RENDEZVOUS,
    intentOnBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    intentOnUndeliveredElement: ((UDFIntent<CommonUiState<T>>) -> Unit)? = null,
    stateStarted: SharingStarted = SharingStarted.Eagerly,
    stateReplay: Int = 1
): CommonUDFlow<T> {
    return CommonUDFlowImpl(
        scope,
        intentCapacity,
        intentOnBufferOverflow,
        intentOnUndeliveredElement,
        stateStarted,
        stateReplay
    )
}

private class CommonUDFlowImpl<T>(
    scope: CoroutineScope,
    intentCapacity: Int = Channel.RENDEZVOUS,
    intentOnBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    intentOnUndeliveredElement: ((UDFIntent<CommonUiState<T>>) -> Unit)? = null,
    stateStarted: SharingStarted = SharingStarted.Eagerly,
    stateReplay: Int = 1
) : SharedUDFlowImpl<CommonUiState<T>>(
    scope,
    intentCapacity,
    intentOnBufferOverflow,
    intentOnUndeliveredElement,
    stateStarted,
    stateReplay
), CommonUDFlow<T> {

    override val loaderUiStateFlow: Flow<LoaderUiState> = this.map {
        it.loaderUiState
    }.distinctUntilChanged()

    override val dataFlow: Flow<T> = this.filter {
        it.loaderUiState.isContentState
    }.map {
        it.data!!
    }.distinctUntilChanged()
}

object CommonUiIntent {

    /**
     * 普通工作流([T] 对应数据类型),先执行[onStart]，然后执行[onWork]，如果出现异常则执行[onCatch]
     * 在[onStart]执行时发送一个loading状态
     * 在[onCatch]执行时发送一个Error状态
     * [onWork]执行完成时发送一个Content状态
     *
     * @param catchCancellationException 是否捕获[CancellationException]
     */
    open class NormalWorkFlow<T>(
        private val onStart: suspend FlowCollector<CommonUiState<T>>.(old: CommonUiState<T>?) -> Unit = { old ->
            val noneNullOld = old ?: CommonUiState()
            emit(noneNullOld.copyByState(LoaderUiState.LOADING))
        },
        private val onCatch: suspend FlowCollector<CommonUiState<T>>.(old: CommonUiState<T>?, Throwable) -> Unit = { old, err ->
            val noneNullOld = old ?: CommonUiState()
            emit(noneNullOld.copyByState(LoaderUiState.error(err)))
        },
        private val catchCancellationException: Boolean = false,
        private val onWork: suspend (old: T?) -> T
    ) : UDFIntent<CommonUiState<T>> {

        override fun toDataFlow(old: CommonUiState<T>?): Flow<CommonUiState<T>> {
            var current = old
            return flow {
                log { "work intent onWork" }
                val data = onWork.invoke(current?.data)
                val noneNullOld = current ?: CommonUiState()
                emit(noneNullOld.copyByData(data).also {
                    log { "work result =$it" }
                })
            }.onStart {
                log { "work intent onStart" }
                onStart.invoke(this, current)
            }.catch {
                log { "work intent catch" }
                if (it !is CancellationException || catchCancellationException) {
                    log { "work intent invoke onCatch" }
                    onCatch.invoke(this, current, it)
                }
            }.onEach {
                log { "work intent onEach" }
                current = it
            }
        }
    }

    /**
     * 普通工作流(无onStart流程),执行[onWork]，如果出现异常则执行[onCatch]；用于不关心start状态的工作流
     * [onWork]执行完成时发送一个Content状态,如果发生异常则执行[onCatch]发送一个Error状态
     * @param catchCancellationException 是否捕获[CancellationException]
     * @see NormalWorkFlow
     */
    open class NoneStartWorkFlow<T>(
        private val onCatch: suspend FlowCollector<CommonUiState<T>>.(old: CommonUiState<T>?, Throwable) -> Unit = { old, err ->
            val noneNullOld = old ?: CommonUiState()
            emit(noneNullOld.copyByState(LoaderUiState.error(err)))
        },
        private val catchCancellationException: Boolean = false,
        private val onWork: suspend (old: T?) -> T
    ) : UDFIntent<CommonUiState<T>> {

        override fun toDataFlow(old: CommonUiState<T>?): Flow<CommonUiState<T>> {
            var current = old
            return flow<CommonUiState<T>> {
                val data = onWork.invoke(current?.data)
                val noneNullOld = current ?: CommonUiState()
                noneNullOld.copyByData(data)
            }.catch {
                if (catchCancellationException && it is CancellationException) {
                    onCatch.invoke(this, current, it)
                }
            }.onEach {
                current = it
            }
        }
    }


    private fun <T> CommonUiState<T>.copyByData(newData: T): CommonUiState<T> {
        return this.copy(data = newData, loaderUiState = LoaderUiState.CONTENT)
    }

    private fun <T> CommonUiState<T>.copyByState(newState: LoaderUiState): CommonUiState<T> {
        return this.copy(loaderUiState = newState)
    }

}
