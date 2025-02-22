package unics.example.okdroidarch

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import unics.okcore.udf.UDFIntent

sealed class SampleIntent : UDFIntent<SampleUiState> {

    class Init : SampleIntent() {

        override fun toReduceFlow(old: SampleUiState): Flow<SampleUiState> {
            return flow {
                //每个任务模拟200ms耗时
                delay(200)
                old.copy(text = "start", count = 0, time = System.currentTimeMillis())
            }
        }

    }

    class Acc : SampleIntent() {
        override fun toReduceFlow(old: SampleUiState): Flow<SampleUiState> {
            return flow {
                //每个任务模拟200ms耗时
                delay(200)
                old.copy(text = "Acc", count = old.count + 1, time = System.currentTimeMillis())
            }
        }

    }

}