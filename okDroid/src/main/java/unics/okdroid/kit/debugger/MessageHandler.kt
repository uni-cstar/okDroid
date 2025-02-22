package unics.okdroid.kit.debugger

import android.util.Log

fun interface MessageHandler {

    fun handle(tag: String, message: String)

    /**
     * 打印，消息收到的时候立即打印
     */
    object Printer : MessageHandler {
        override fun handle(tag: String, message: String) {
            Log.i(tag, message)
        }
    }

    /**
     * 懒处理：消息先保存在列表中
     * @param maxCount 最大缓存消息数
     */
    class Lazy(private val maxCount: Int = 500) : MessageHandler {

        //        private val caches = LinkedList<String>()
        private val caches = mutableListOf<String>()

        @Synchronized
        override fun handle(tag: String, message: String) {
            val count = caches.size
            if (count >= maxCount) {
                caches.removeFirst()
            }
            caches.add("$tag: $message")
        }

        fun list(): List<String> {
            return caches.toList()
        }

        fun clear() {
            caches.clear()
        }
    }

    /**
     * 包装多个处理器
     */
    class Wrapper(private vararg val handlers: MessageHandler) : MessageHandler {
        override fun handle(tag: String, message: String) {
            handlers.forEach {
                it.handle(tag, message)
            }
        }
    }

}