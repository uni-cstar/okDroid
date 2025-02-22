@file:JvmName("UDFFlow")
@file:JvmMultifileClass

package unics.okarch.udf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch

/**
 * @author: chaoluo10
 * @date: 2024/7/4
 * @desc:
 */

/**
 *
 * SSOTUDF: single source of truth ans unidirectional data flow
 * SSOT:single source of truth 单一数据源
 * UDF:unidirectional data flow 单向数据流
 * 一个UDF 对应的MVI架构所需要的定义
 * 参考链接：https://developer.android.com/topic/architecture?hl=zh-cn#single-source-of-truth
 */
interface SharedUDFlow<Data : UDFData> : UDFlow<Data> {
    /**
     * 当前状态值
     */
    val value: Data?
}


/**
 * 轻量级单向数据流实现；Intent的发送基于[Channel]实现，State的提供基于[shareIn]创造的ShareFlow实现
 * [SharedUDFIntent]输入的意图类型
 * [State]输出的状态类型
 */
internal open class SharedUDFlowImpl<State : UDFData>(
    scope: CoroutineScope,
    intentCapacity: Int = Channel.RENDEZVOUS,
    intentOnBufferOverflow: BufferOverflow = BufferOverflow.SUSPEND,
    intentOnUndeliveredElement: ((UDFIntent<State>) -> Unit)? = null,
    stateStarted: SharingStarted = SharingStarted.Eagerly,
    stateReplay: Int = 1
) : SharedUDFlow<State> {

    //intent通道
    private val _intentChannel: Channel<UDFIntent<State>> =
        Channel(intentCapacity, intentOnBufferOverflow, intentOnUndeliveredElement)

    //state流
    @OptIn(FlowPreview::class)
    private val _uiStateFlow: SharedFlow<State> = _intentChannel.consumeAsFlow().flatMapConcat {
        log { "UDFlowOfPartImpl: channel#cosumeFlow#flatMapConcat $it" }
        it.toDataFlow(_currentState)
    }.onEach {
        //缓存当前状态
        log { "channel#cosumeFlow#onEach cacheResult:$it" }
        _currentState = it
    }.shareIn(scope, stateStarted, stateReplay)

    //当前状态，支持延迟初始化
    private var _currentState: State? = null

    override val value: State? get() = _currentState

    @InternalCoroutinesApi
    override suspend fun collect(collector: FlowCollector<State>) {
        log { "collect $collector" }
        collector.emitAll(_uiStateFlow)
    }

    override fun sendIntent(scope: CoroutineScope, intent: UDFIntent<State>) {
        log { "sendIntent($scope $intent)" }
        scope.launch {
            _intentChannel.send(intent)
        }
    }

    /**
     * 如果没有接收者，此处可能会被挂起（受channel的配置影响）
     */
    override suspend fun sendIntent(intent: UDFIntent<State>) {
        log { "sendIntent($intent)" }
        _intentChannel.send(intent)
    }

}