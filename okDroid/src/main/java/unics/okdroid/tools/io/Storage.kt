@file:JvmName("StoragesKt")
@file:JvmMultifileClass

package unics.okdroid.tools.io

import java.io.File

/**
 * 文件存储统一管理
 */
var storage: Storage = DefaultStorage

interface Storage {

    /**
     * 外置存储是否可用
     */
    val isExternalStorageAvailable: Boolean

    /**
     * 获取内部缓存目录
     * app内部缓存目录路径[android.content.Context.getCacheDir]，eg./data/user/0/{packagename}/cache 用户无法查看目录（root可看）
     */
    fun getInnerCacheDirectory(): File

    /**
     * 获取缓存目录
     * 如果外部存储可用，则为[android.content.Context.getExternalCacheDir]，eg：/storage/emulated/0/Android/data/{packagename}/cache
     * 否则为app内部缓存目录路径[getInnerCacheDirectory]，用户无法查看
     */
    fun getCacheDirectory(): File

    /**
     * 获取缓存目录，路径值为[getCacheDirectory]/[child]
     * @param child 子目录路径
     */
    fun getCacheDirectory(child: String): File

    /**
     * 获取内部文件目录
     * app内部文件目录路径[android.content.Context.getFilesDir]，用户无法查看 ,eg. /data/user/0/{packagename}/files
     */
    fun getInnerFilesDirectory(): File

    /**
     * 获取文件目录
     * 如果外部存储可用，则为[android.content.Context.getExternalFilesDir]，eg. /storage/emulated/0/Android/data/{packagename}/files
     * 否则为app内部缓存目录路径[getInnerFilesDirectory]
     */
    fun getFilesDirectory(): File

    /**
     * 获取文件目录，路径值为[getFilesDirectory]/[child]
     * @param child 子目录路径
     */
    fun getFilesDirectory(child: String): File

    /**
     * 获取内部Download目录
     * app内部文件目录路径[android.content.Context.getFilesDir].parent，用户无法查看 ,eg. /data/user/0/{packagename}/files/Download
     */
    fun getInnerDownloadDirectory(): File

    /**
     * 获取Download目录
     * 如果外部存储可用，则为[android.content.Context.getExternalFilesDir].parent，eg.  /storage/emulated/0/Android/data/{packagename}/files/Download
     * 否则为app内部缓存目录路径[getInnerDownloadDirectory]
     */
    fun getDownloadDirectory(): File

    /**
     * 确保目录可用
     */
    fun ensureDirectoryAvailable(file: File) {
        require(!file.exists() || file.isDirectory) { "$file is not a directory." }
        if (!file.exists() && !file.mkdirs()) {
            throw FileSystemException(file, null, "create directory failed.")
        }
    }

    /**
     * 确保文件可用，如果文件不存在则创建
     */
    fun ensureFileAvailable(file: File) {
        ensureFileAvailable(file, false)
    }

    /**
     * 确保文件可用，如果文件不存在则创建
     * @param recreateIfExists 文件如果已经存在，是否删除重建
     */
    fun ensureFileAvailable(file: File, recreateIfExists: Boolean) {
        require(!file.exists() || file.isFile) { "$file is not a file." }

        if (file.exists()) {
            if (recreateIfExists) {
                //删除重建
                if (!file.delete()) {
                    throw FileSystemException(
                        file,
                        null,
                        "can not recreate file,fail delete exists file."
                    )
                }
                if (!file.createNewFile()) {
                    throw FileSystemException(file, null, "create file failed.")
                }
            }
        } else {
            val parent = file.parentFile
            if (parent != null && !parent.exists() && !parent.mkdirs()) {
                throw FileSystemException(parent, null, "create parent directory failed.")
            }

            if (!file.createNewFile()) {
                throw FileSystemException(file, null, "create file failed.")
            }
        }
    }
}

