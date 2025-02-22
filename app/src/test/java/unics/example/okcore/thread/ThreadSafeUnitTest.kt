package unics.example.okcore.thread

/**
 * @author: chaoluo10
 * @date: 2024/4/28
 * @desc: 线程安全测试
 *
 *
 # 什么是线程安全
 线程安全指
 */
class ThreadSafeUnitTest {


}


/**
 * 单CPU多线程仍然存在线程安全问题
 *
 * 引起的原因：
 * 1、非原子性操作
 * 2、cpu缓存：cpu的缓存机制导致与主内存可能存在不一致
 * 3、cpu时间片：无法控制cpu在什么时候切换线程
 */
class SingleCPUThreadSafeUnitTest {

}