/**
 * 获取ViewModel
 * @see
 * @note 文件名规则：UCS+文件名第一个分类+Kt (供java使用而已)
 */
@file:JvmName("UCSViewModelKt")
@file:JvmMultifileClass

package unics.okdroid.tools.lifecycle

import androidx.activity.ComponentActivity
import androidx.annotation.MainThread
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 获取Activity的ViewModel（内联方法）
 * @see androidx.activity.viewModels
 */
@JvmOverloads
@MainThread
inline fun <reified VM : ViewModel> ComponentActivity.obtainViewModel(
    factory: ViewModelProvider.Factory? = null
): VM {
    return obtainViewModel(VM::class.java, factory)
}

/**
 * 获取Activity的ViewModel
 * @param clazz ViewModel类型
 * @param factory 自定义工厂
 * @see androidx.activity.viewModels
 */
@JvmOverloads
@MainThread
fun <VM : ViewModel> ComponentActivity.obtainViewModel(
    clazz: Class<VM>,
    factory: ViewModelProvider.Factory? = null
): VM {
    val factoryPromise = factory ?: defaultViewModelProviderFactory
    val storePromise = viewModelStore
    return ViewModelProvider(storePromise, factoryPromise)[clazz]
}

/**
 * 获取Fragment的ViewModel（内联方法）
 * @see androidx.fragment.app.viewModels
 */
@JvmOverloads
@MainThread
inline fun <reified VM : ViewModel> Fragment.obtainViewModel(
    factory: ViewModelProvider.Factory? = null
): VM {
    return obtainViewModel(VM::class.java, factory)
}

/**
 * 获取Fragment的ViewModel
 * @param clazz ViewModel类型
 * @param factory 自定义工厂
 * @see androidx.fragment.app.viewModels
 */
@JvmOverloads
@MainThread
fun <VM : ViewModel> Fragment.obtainViewModel(
    clazz: Class<VM>,
    factory: ViewModelProvider.Factory? = null
): VM {
    val factoryPromise = factory ?: defaultViewModelProviderFactory
    val storePromise = viewModelStore
    return ViewModelProvider(storePromise, factoryPromise)[clazz]
}

/**
 * 在Fragment中获取Activity的ViewModel（内联方法）
 * @see androidx.fragment.app.activityViewModels
 */
@JvmOverloads
@MainThread
inline fun <reified VM : ViewModel> Fragment.obtainActivityViewModel(
    factory: ViewModelProvider.Factory? = null
): VM {
    return obtainActivityViewModel(VM::class.java, factory)
}

/**
 * 在Fragment中获取Activity的ViewModel
 * @param clazz ViewModel类型
 * @param factory 自定义工厂
 * @see androidx.fragment.app.activityViewModels
 */
@JvmOverloads
@MainThread
fun <VM : ViewModel> Fragment.obtainActivityViewModel(
    clazz: Class<VM>,
    factory: ViewModelProvider.Factory? = null
): VM {
    val factoryPromise = factory ?: defaultViewModelProviderFactory
    val store = requireActivity().viewModelStore
    return ViewModelProvider(store, factoryPromise)[clazz]
}


