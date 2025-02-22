package unics.okdroidarch.data

/**
 * 还未想好如何处理：
 */
sealed interface RepositoryData<T> {

    val data: T

    /**
     * 内存数据
     */
    data class MemoryData<T>(override val data: T) : RepositoryData<T>

    /**
     * 磁盘数据
     */
    data class DiskData<T>(override val data: T) : RepositoryData<T>

    /**
     * 网络数据
     */
    data class RemoteData<T>(override val data: T) : RepositoryData<T>
}