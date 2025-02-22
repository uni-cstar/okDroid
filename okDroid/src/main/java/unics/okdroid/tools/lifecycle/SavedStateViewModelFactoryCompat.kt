/**
 * 不能修改包名，否则无法访问lifecycle内部的方法
 */
package androidx.lifecycle

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import android.os.Bundle
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryOwner
import unics.okdroid.deprecated.log.ilogi
import java.lang.reflect.Constructor
import java.lang.reflect.InvocationTargetException
import java.util.*

/**
 * 兼容[SavedStateViewModelFactory],在创建ViewModel的时候，支持将[android.app.Activity.getIntent]中的[android.content.Intent.getData]作为参数传递
 * 因此使用该工厂，支持如下类型的构造函数
 *
 * 设计思路：
 * 1、一般我们创建一个ViewModel，通常需要从Activity的Intent中获取Data或者Extra参数，或者是从Fragment的Arguments中获取参数
 * 2、一般场景一个Activity或者Fragment大多对应的是一个ViewModel为主的场景
 *
 * 说明：
 * SavedStateHandle参数可以做变量保存和恢复，并且其包含了Intent.getExtras()中的参数
 * Uri类型的参数来源于Intent.getData()
 *
 * constructor(application:Application):AndroidViewModel(application) [mFactory]支持
 * constructor(application:Application, savedStateHandler:SavedStateHandle):AndroidViewModel(application)  [mFactory]支持
 * constructor(application:Application, data:Uri?):AndroidViewModel(application) 本类支持
 * constructor(application:Application, savedStateHandler:SavedStateHandle, data:Uri?):AndroidViewModel(application) 本类支持
 *
 * constructor():ViewModel()  [mFactory]支持
 * constructor(savedStateHandler:SavedStateHandle):ViewModel() [mFactory]支持
 * constructor(data:Uri?):ViewModel() 本类支持
 * constructor(savedStateHandler:SavedStateHandle, data:Uri?):ViewModel() 本类支持
 * constructor(application:Application):ViewModel() 本类兼容支持:Application的目的只是为了兼容droid-arch提供的ViewModel的子类
 * constructor(application:Application,data:Uri?):ViewModel() 本类兼容支持:Application的目的只是为了兼容droid-arch提供的ViewModel的子类
 * constructor(application:Application,savedStateHandler:SavedStateHandle):ViewModel() 本类兼容支持:Application的目的只是为了兼容droid-arch提供的ViewModel的子类
 * constructor(application:Application,savedStateHandler:SavedStateHandle, data:Uri?):ViewModel() 本类兼容支持:Application的目的只是为了兼容droid-arch提供的ViewModel的子类
 *
 */
@SuppressLint("RestrictedApi")
class SavedStateViewModelFactoryCompat(
    private val mApplication: Application?,
    private val mFactory: SavedStateViewModelFactory,
    private val mDefaultArgs: Bundle?,
    private val mDataUri: Uri?,
    private val mLifecycle: Lifecycle,
    private val mSavedStateRegistry: SavedStateRegistry
) : ViewModelProvider.KeyedFactory() {

    companion object {

        @JvmStatic
        private val URI_ANDROID_VIEWMODEL_SIGNATURE = arrayOf(
            Application::class.java,
            Uri::class.java
        )

        @JvmStatic
        private val SAVED_STATED_URI_ANDROID_VIEWMODEL_SIGNATURE = arrayOf(
            Application::class.java,
            SavedStateHandle::class.java,
            Uri::class.java
        )

        @JvmStatic
        private val APPLICATION_VIEWMODEL_SIGNATURE = arrayOf<Class<*>>(
            Application::class.java
        )

        @JvmStatic
        private val APPLICATION_URI_VIEWMODEL_SIGNATURE = arrayOf<Class<*>>(
            Application::class.java,
            Uri::class.java
        )

        @JvmStatic
        private val APPLICATION_SAVED_STATED_VIEWMODEL_SIGNATURE = arrayOf<Class<*>>(
            Application::class.java,
            SavedStateHandle::class.java,
        )

        @JvmStatic
        private val APPLICATION_SAVED_STATED_URI_VIEWMODEL_SIGNATURE = arrayOf<Class<*>>(
            Application::class.java,
            SavedStateHandle::class.java,
            Uri::class.java
        )

        @JvmStatic
        private val URI_VIEWMODEL_SIGNATURE = arrayOf<Class<*>>(
            Uri::class.java
        )

        @JvmStatic
        private val SAVED_STATED_URI_VIEWMODEL_SIGNATURE = arrayOf<Class<*>>(
            SavedStateHandle::class.java,
            Uri::class.java
        )

        @JvmStatic
        private fun <T> findMatchingConstructor(
            modelClass: Class<T>,
            signature: Array<Class<*>>
        ): Constructor<T>? {
            for (constructor in modelClass.constructors) {
                val parameterTypes = constructor.parameterTypes
                if (Arrays.equals(signature, parameterTypes)) {
                    return constructor as Constructor<T>
                }
            }
            return null
        }
    }

    @JvmOverloads
    constructor(
        application: Application?,
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle? = null,
        dataUri: Uri? = null
    ) : this(
        mApplication = application,
        mLifecycle = owner.lifecycle,
        mDefaultArgs = defaultArgs,
        mDataUri = dataUri,
        mSavedStateRegistry = owner.savedStateRegistry,
        mFactory = SavedStateViewModelFactory(application, owner, defaultArgs)
    )

    private fun createController(key: String): SavedStateHandleController {
        return SavedStateHandleController.create(
            mSavedStateRegistry,
            mLifecycle,
            key,
            mDefaultArgs
        )
    }

    override fun <T : ViewModel> create(key: String, modelClass: Class<T>): T {
        val isAndroidViewModel = AndroidViewModel::class.java.isAssignableFrom(modelClass)
        //构造函数参数中是否包含SavedStateHandle参数类型
        var constructor: Constructor<T>? = null
        var constructorArgs: Array<Any?>? = null
        var controller: SavedStateHandleController? = null

        if (isAndroidViewModel && mApplication != null) {//AndroidViewModel类型
            //AndroidViewModel只需要查找带有Uri参数的构造函数，其他的默认实现即可支持
            constructor = findMatchingConstructor(
                modelClass,
                SAVED_STATED_URI_ANDROID_VIEWMODEL_SIGNATURE
            )?.also {
                controller = createController(key)
                constructorArgs = arrayOf(mApplication, controller?.handle, mDataUri)
                ilogi {
                    "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> AndroidViewModel(Application, SavedStateHandle, Uri)"
                }
            } ?: findMatchingConstructor(
                modelClass,
                URI_ANDROID_VIEWMODEL_SIGNATURE
            )?.also {
                constructorArgs = arrayOf(mApplication, mDataUri)
                ilogi {
                    "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> AndroidViewModel(Application, Uri)"
                }
            }
        } else {
            constructor = findMatchingConstructor(
                modelClass,
                SAVED_STATED_URI_VIEWMODEL_SIGNATURE
            )?.also {
                controller = createController(key)
                constructorArgs = arrayOf(controller?.handle, mDataUri)
                ilogi {
                    "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> ViewModel(SavedStateHandle, Uri)"
                }
            } ?: findMatchingConstructor(
                modelClass,
                URI_VIEWMODEL_SIGNATURE
            )?.also {
                constructorArgs = arrayOf(mDataUri)
                ilogi {
                    "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> ViewModel(Uri)"
                }
            }

            //兼容[ViewModel]构造函数带有Application类型的参数
            if (constructor == null && mApplication != null) {
                constructor = findMatchingConstructor(
                    modelClass,
                    APPLICATION_SAVED_STATED_URI_VIEWMODEL_SIGNATURE
                )?.also {
                    controller = createController(key)
                    constructorArgs = arrayOf(mApplication, controller?.handle, mDataUri)
                    ilogi {
                        "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> ViewModel(Application, SavedStateHandle, Uri)"
                    }
                } ?: findMatchingConstructor(
                    modelClass,
                    APPLICATION_SAVED_STATED_VIEWMODEL_SIGNATURE
                )?.also {
                    controller = createController(key)
                    constructorArgs = arrayOf(mApplication, controller?.handle)
                    ilogi {
                        "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> ViewModel(Application, SavedStateHandle)"
                    }
                } ?: findMatchingConstructor(
                    modelClass,
                    APPLICATION_URI_VIEWMODEL_SIGNATURE
                )?.also {
                    constructorArgs = arrayOf(mApplication, mDataUri)
                    ilogi {
                        "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> ViewModel(Application, Uri)"
                    }
                } ?: findMatchingConstructor(modelClass, APPLICATION_VIEWMODEL_SIGNATURE)?.also {
                    constructorArgs = arrayOf(mApplication)
                    ilogi {
                        "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> ViewModel(Application)"
                    }
                }
            }
        }

        if (constructor == null || constructorArgs == null) {
            ilogi {
                "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> not found constructor,use default factory."
            }
            return mFactory.create<T>(modelClass)
        }

        try {
            val viewmodel: T = constructor.newInstance(*constructorArgs!!)
            if (controller != null) {
                ilogi {
                    "SavedStateViewModelFactoryCompat#create(key=$key,modelClass=${modelClass}) -> view model bind controller by setTagIfAbsent method"
                }
                viewmodel.setTagIfAbsent(
                    AbstractSavedStateViewModelFactory.TAG_SAVED_STATE_HANDLE_CONTROLLER,
                    controller
                )
            }
            return viewmodel
        } catch (e: IllegalAccessException) {
            throw RuntimeException("Failed to access $modelClass", e)
        } catch (e: InstantiationException) {
            throw RuntimeException("A $modelClass cannot be instantiated.", e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(
                "An exception happened in constructor of "
                        + modelClass, e.cause
            )
        }

//        if (hasSaveStateArg) {
//            val controller =
//                SavedStateHandleController.create(
//                    mSavedStateRegistry,
//                    mLifecycle,
//                    key,
//                    mDefaultArgs
//                )
//            try {
//                val viewmodel: T
//                if (isAndroidViewModel && mApplication != null) {
//                    //add uri param
//                    viewmodel =
//                        constructor.newInstance(mApplication, controller.handle, mDataUri)
//                } else {
//                    //add uri param
//                    viewmodel = constructor.newInstance(controller.handle, mDataUri)
//                }
//                viewmodel.setTagIfAbsent(
//                    AbstractSavedStateViewModelFactory.TAG_SAVED_STATE_HANDLE_CONTROLLER,
//                    controller
//                )
//                return viewmodel
//            } catch (e: IllegalAccessException) {
//                throw RuntimeException("Failed to access $modelClass", e)
//            } catch (e: InstantiationException) {
//                throw RuntimeException("A $modelClass cannot be instantiated.", e)
//            } catch (e: InvocationTargetException) {
//                throw RuntimeException(
//                    "An exception happened in constructor of "
//                            + modelClass, e.cause
//                )
//            }
//        } else {
//            try {
//                val viewmodel: T
//                if (isAndroidViewModel && mApplication != null) {
//                    //add uri param only
//                    viewmodel = constructor.newInstance(mApplication, mDataUri)
//                } else {
//                    //add uri param only
//                    viewmodel = constructor.newInstance(mDataUri)
//                }
//                return viewmodel
//            } catch (e: IllegalAccessException) {
//                throw RuntimeException("Failed to access $modelClass", e)
//            } catch (e: InstantiationException) {
//                throw RuntimeException("A $modelClass cannot be instantiated.", e)
//            } catch (e: InvocationTargetException) {
//                throw RuntimeException(
//                    "An exception happened in constructor of "
//                            + modelClass, e.cause
//                )
//            }
//        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // ViewModelProvider calls correct create that support same modelClass with different keys
        // If a developer manually calls this method, there is no "key" in picture, so factory
        // simply uses classname internally as as key.
        val canonicalName = modelClass.canonicalName
            ?: throw IllegalArgumentException("Local and anonymous classes can not be ViewModels")
        return create(canonicalName, modelClass)
    }

}