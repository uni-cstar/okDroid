/**
 * @author: unics
 * @date: 2020/7/21
 * @desc: 轻量级单向数据流实现，符合mvi原则
 */
package unics.okarch.udf

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 用户意图：可理解未发起的操作行为，比如下拉刷新是一个意图，点击是一个意图；
 * 一个意图的结果通常是产生[UDFData]的部分，再借助原来的UiState产生新的UiState，因此意图提供转换成[]
 */
interface UDFIntent<Data : UDFData> {

    fun toDataFlow(old: Data?): Flow<Data>

    companion object {

        /**
         * 创建意图
         * @param dataFlowFactory 数据流工厂，
         */
        fun <Data : UDFData> UDFIntent(
            dataFlowFactory: (Data?) -> Flow<Data>
        ): UDFIntent<Data> {
            return object : UDFIntent<Data> {
                override fun toDataFlow(old: Data?): Flow<Data> {
                    return dataFlowFactory(old)
                }
            }
        }

        /**
         * 创建意图
         * @param dataFlowFactory 部分数据流工厂，返回的数据只需要关注部分即可
         */
        fun <Data : UDFData, Part : Any> UDFIntent(
            dataFlowFactory: () -> Flow<Part>, reducer: (Data?, Part) -> Data
        ): UDFIntent<Data> {
            return object : UDFIntent<Data> {
                override fun toDataFlow(old: Data?): Flow<Data> {
                    return dataFlowFactory()
                        .map {
                            reducer(old, it)
                        }
                }
            }
        }

    }


}

