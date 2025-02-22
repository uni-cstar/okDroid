package unics.okdroid.kit.imageloader

import androidx.annotation.DrawableRes
import unics.okdroid.R

/**
 * 配置信息
 */
class Configs internal constructor() {

    /**
     * 是否启用 disk cache
     */
    @JvmField
    var isDiskCacheEnabled: Boolean = true

    /**
     * 是否启用 memory ache
     */
    @JvmField
    var isMemoryCacheEnabled: Boolean = true

    /**
     * 默认图片资源
     */
    @JvmField
    var defaultImageRes: Int = R.drawable.ucs_image_placeholder

    /**
     * 默认圆角图片
     */
    @JvmField
    var defaultRoundedImageRes: Int = R.drawable.ucs_image_placeholder_corner

    /**
     * 默认圆形图片
     */
    @JvmField
    var defaultCircleImageRes: Int = R.drawable.ucs_image_placeholder_circle

    /**
     * disk 缓存目录名字,如果为空，则使用对应Engine的默认目录名字；
     * Glide默认配置的文件目录名为[com.bumptech.glide.load.engine.cache.DiskCache.Factory.DEFAULT_DISK_CACHE_DIR]
     */
    @JvmField
    var diskCacheFolderName: String? = null

    class Builder() {
        private var isDiskCacheEnabled = true
        private var isMemoryCacheEnabled: Boolean = true
        private var diskCacheFolderName: String? = null
        private var defaultImageRes: Int = R.drawable.ucs_image_placeholder
        private var defaultRoundedImageRes: Int =  R.drawable.ucs_image_placeholder_corner
        private var defaultCircleImageRes: Int = R.drawable.ucs_image_placeholder_circle

        /**
         * 是否启用Disk缓存
         */
        fun setDiskCacheEnabled(isEnable: Boolean): Builder {
            this.isDiskCacheEnabled = isEnable
            return this
        }

        /**
         * 是否启用Memory缓存
         */
        fun setMemoryCacheEnabled(isEnable: Boolean): Builder {
            this.isMemoryCacheEnabled = isEnable
            return this
        }

        /**
         * 设置Disk 缓存目录名字
         */
        fun setDiskCacheFolderName(name: String?): Builder {
            this.diskCacheFolderName = name
            return this
        }

        fun setDefaultImage(@DrawableRes resId: Int): Builder {
            this.defaultImageRes = resId
            return this
        }

        fun setDefaultRoundedImage(@DrawableRes resId: Int): Builder {
            this.defaultRoundedImageRes = resId
            return this
        }

        fun setDefaultCircleImage(@DrawableRes resId: Int): Builder {
            this.defaultCircleImageRes = resId
            return this
        }

        fun build(): Configs {
            return Configs().also {
                it.isDiskCacheEnabled = this.isDiskCacheEnabled
                it.isMemoryCacheEnabled = this.isMemoryCacheEnabled
                it.diskCacheFolderName = this.diskCacheFolderName
                it.defaultImageRes = this.defaultImageRes
                it.defaultRoundedImageRes = this.defaultRoundedImageRes
                it.defaultCircleImageRes = this.defaultCircleImageRes
            }
        }
    }
}