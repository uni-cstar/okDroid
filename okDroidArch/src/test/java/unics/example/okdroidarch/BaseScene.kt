package unics.example.okdroidarch

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import unics.okcore.coroutines.CloseableCoroutineScope
import unics.okcore.udf.UDFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.concurrent.CountDownLatch

/**
 * @author: chaoluo10
 * @date: 2024/6/21
 * @desc:
 */
abstract class BaseScene(
    protected val scope: CloseableCoroutineScope = CloseableCoroutineScope(
        SupervisorJob() + Dispatchers.Default
    )
) {

    protected abstract val uiStateFlow: UDFlow<SampleIntent, SampleUiState>

    protected fun l(msg: String) {
        println("${SimpleDateFormat("HH:mm:ss.SSS").format(Date())} ${this::class.java.simpleName} : $msg")
    }

    protected fun closeScope() {
        scope.close()
    }

    protected fun sendInitIntent(): Job {
        return scope.launch {
            uiStateFlow.sendIntent(SampleIntent.Init())
        }
    }

    protected fun sendInitIntent(countDownLatch: CountDownLatch): Job {
        return scope.launch {
            try {
                uiStateFlow.sendIntent(SampleIntent.Init())
            } catch (e: Throwable) {
                l("sendInitIntent: error $e.")
            } finally {
                countDownLatch.countDown()
                l("sendInitIntent: release lock.")
            }
        }
    }

    protected fun sendAccIntent(count: Int, countDownLatch: CountDownLatch): Job {
        return scope.launch {
            try {
                repeat(count) {
                    delay(500)
                    l("sendAccIntent: the ${it + 1} send")
                    uiStateFlow.sendIntent(SampleIntent.Acc())
                }
            } catch (e: Throwable) {
                l("sendAccIntent: error $e.")
            } finally {
                countDownLatch.countDown()
                l("sendAccIntent: release lock.")
            }

        }
    }

    protected fun delayCloseScope(delay: Long = 5000L): Job {
        return scope.launch {
            delay(delay)
            closeScope()
            l("cancel scope on delay")
        }
    }

    /**
     * 模拟整个Scope退出
     */
    protected fun delayCloseScope(countDownLatch: CountDownLatch, delay: Long = 5000L): Job {
        return scope.launch {
            delay(delay)
            closeScope()
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

}