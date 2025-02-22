package unics.droid.core.android


import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import unics.okdroid.globalContext
import unics.okdroid.tools.io.storage
import kotlinx.coroutines.runBlocking
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File

/**
 * Created by Lucio on 2020-02-28.
 */
@RunWith(AndroidJUnit4::class)
class AMStoreTest {

    @Test
    fun getPaths() = runBlocking {
        globalContext = InstrumentationRegistry.getInstrumentation().targetContext as Application
        println("isExternalStorageAvailable=${storage.isExternalStorageAvailable}")
        println("CacheDirectory=${storage.getCacheDirectory().absolutePath}")
        println("InnerCacheDirectory=${storage.getInnerCacheDirectory().absolutePath}")
        println("ImageCacheDirectory=${storage.getCacheDirectory("Image").absolutePath}")
        println("CustomCacheDirectory=${storage.getCacheDirectory("Custom")}")
        println("CustomCachePath=${File( storage.getCacheDirectory("Custom"),"cache.txt")}")

        println("FilesDirectory=${storage.getFilesDirectory().absolutePath}")
        println("InnerFilesDirectory=${storage.getInnerFilesDirectory().absolutePath}")
        println("CustomFileDirectory=${storage.getFilesDirectory("Custom")}")
        println("CustomFilePath=${File( storage.getFilesDirectory("Custom"),"child.txt")}")

        println("DownloadDirectory=${storage.getDownloadDirectory().absolutePath}")
        println("InnerDownloadDirectory=${storage.getInnerDownloadDirectory().absolutePath}")
    }


}