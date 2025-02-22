package unics.example.okdroidarch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.junit.Test
import unics.okcore.coroutines.CloseableCoroutineScope
import unics.okcore.udf.UDFIntent
import unics.okcore.udf.UDFlow
import unics.okdroidarch.kit.UiIntent
import unics.okdroidarch.kit.UiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.CountDownLatch

/**
 * @author: unics
 * @date: 2020/7/21
 */
class SendThenCollectLazyScene  {

    private val scope: CloseableCoroutineScope = CloseableCoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )
    private val uiStateFlow: UDFlow<UDFIntent<UiState<Int>>, UiState<Int>> = UDFlow(scope) {
        UiState(2)
    }

    protected fun l(msg: String) {
        println("${SimpleDateFormat("HH:mm:ss.SSS").format(Date())} ${this::class.java.simpleName} : $msg")
    }

    /**
     * 模拟整个Scope退出
     */
    protected fun delayCloseScope(countDownLatch: CountDownLatch, delay: Long = 5000L): Job {
        return scope.launch {
            delay(delay)
            scope.close()
            l("cancel scope on delay")
            countDownLatch.countDown()
        }
    }


    protected fun collectUiState(countDownLatch: CountDownLatch): Job {
        return scope.launch {
            try {
                l("collectUiState start")
                collectUiState1()
                collectUiState2()
                awaitCancellation()
            } catch (e: Throwable) {
                l("collectUiState error : $e")
            } finally {
                l("collectUiState end")
                countDownLatch.countDown()
            }
        }

    }

    protected fun collectUiState1(): Job {
        return scope.launch {
            l("collectUiState1 start")
            uiStateFlow.distinctUntilChanged().collectLatest {
                //模拟：每个状态收集耗时50
                delay(50)
                l("collectUiState1 collectLatest:$it")
            }
            l("collectUiState1 end,release lock")
        }

    }

    protected fun collectUiState1(countDownLatch: CountDownLatch): Job {
        return scope.launch {
            try {
                l("collectUiState1 start")
                uiStateFlow.distinctUntilChanged().collectLatest {
                    //模拟：每个状态收集耗时50
                    delay(50)
                    l("collectUiState1 collectLatest-distinctUntilChanged:$it")
                }
            } catch (e: Throwable) {
                l("collectUiState1 error : $e")
            } finally {
                l("collectUiState1 end")
                countDownLatch.countDown()
            }
        }
    }

    protected fun collectUiState2(): Job {
        return scope.launch {
            l("collectUiState2 start")
            uiStateFlow.collectLatest {
                //模拟：每个状态收集耗时50
                delay(50)
                l("collectUiState2 collectLatest:$it")
            }
            l("collectUiState2 end,release lock")
        }
    }

    protected fun collectUiState2(countDownLatch: CountDownLatch): Job {
        return scope.launch {
            try {
                l("collectUiState2 start")
                uiStateFlow.collectLatest {
                    //模拟：每个状态收集耗时50
                    delay(50)
                    l("collectUiState2 collectLatest: $it")
                }
            } catch (e: Throwable) {
                l("collectUiState2 error : $e")
            } finally {
                l("collectUiState2 end")
                countDownLatch.countDown()
            }
        }
    }

    @Test
    fun run(): Unit = runBlocking {
        val countDownLatch = CountDownLatch(3)
        delayCloseScope(countDownLatch)
        scope.launch {
            uiStateFlow.sendIntent(UiIntent.NormalWorkFlow {
                delay(2000)
                1
            })
            countDownLatch.countDown()
        }
        delay(1000)
        collectUiState(countDownLatch)
        l("task prepared")
        countDownLatch.await()
    }


}