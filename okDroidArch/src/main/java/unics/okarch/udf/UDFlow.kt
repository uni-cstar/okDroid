/**
 * @author: unics
 * @date: 2020/7/21
 * @desc: 轻量级单向数据流实现，符合mvi原则
 */
@file:JvmName("UDFFlow")
@file:JvmMultifileClass

package unics.okarch.udf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted

/**
 * 唯一单向流
 */
fun <State : UDFData> UDFlow(
    scope: CoroutineScope,
    initialState: State,
    intentCapacity: Int = Channel.RENDEZVOUS,
    intentOnBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    intentOnUndeliveredElement: ((UDFIntent<State>) -> Unit)? = null,
    stateStarted: SharingStarted = SharingStarted.Eagerly,
    stateReplay: Int = 1
): StateUDFlow<State> {
    return StateUDFlowImpl(
        scope,
        intentCapacity,
        intentOnBufferOverflow,
        intentOnUndeliveredElement,
        stateStarted,
        stateReplay
    ) {
        initialState
    }
}

fun <State : UDFData> UDFlow(
    scope: CoroutineScope,
    intentCapacity: Int = Channel.RENDEZVOUS,
    intentOnBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    intentOnUndeliveredElement: ((UDFIntent<State>) -> Unit)? = null,
    stateStarted: SharingStarted = SharingStarted.Eagerly,
    stateReplay: Int = 1,
    stateInitializer: () -> State
): StateUDFlow<State> {
    return StateUDFlowImpl(
        scope,
        intentCapacity,
        intentOnBufferOverflow,
        intentOnUndeliveredElement,
        stateStarted,
        stateReplay,
        stateInitializer
    )
}

fun <State : UDFData> UDFlow(
    scope: CoroutineScope,
    intentCapacity: Int = Channel.RENDEZVOUS,
    intentOnBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    intentOnUndeliveredElement: ((UDFIntent<State>) -> Unit)? = null,
    stateStarted: SharingStarted = SharingStarted.Eagerly,
    stateReplay: Int = 1
): SharedUDFlow<State> {
    return SharedUDFlowImpl(
        scope,
        intentCapacity,
        intentOnBufferOverflow,
        intentOnUndeliveredElement,
        stateStarted,
        stateReplay
    )
}

interface UDFlow<Data : UDFData> : Flow<Data> {

    /**
     * 发送意图
     */
    fun sendIntent(scope: CoroutineScope, intent: UDFIntent<Data>)

    /**
     * 发送意图
     */
    suspend fun sendIntent(intent: UDFIntent<Data>)

}


var UDFLOG: Boolean = true