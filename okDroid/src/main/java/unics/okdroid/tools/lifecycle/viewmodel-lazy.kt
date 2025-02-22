/**
 * ViewModel延迟初始化相关的方法：即带参数的ViewModel对应的ViewModelFactory
 * （不需要参数的工厂和view model，压根就不需要额外实现，直接使用系统方法即可）
 * 设计思路：
 * 1、一般我们创建一个ViewModel，通常需要从Activity的Intent中获取Data或者Extra参数，或者是从Fragment的Arguments中获取参数
 * 2、一般场景一个Activity或者Fragment大多对应的是一个ViewModel为主的场景
 * @note 文件名规则：UCS+文件名第一个分类+Kt (供java使用而已)
 */
@file:JvmName("UCSViewModelKt")
@file:JvmMultifileClass

package unics.okdroid.tools.lifecycle

import androidx.activity.ComponentActivity
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.SavedStateViewModelFactoryCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * 创建推荐的ViewModel构造工厂，可以考虑作为[androidx.lifecycle.HasDefaultViewModelProviderFactory]的实现，
 * 即重写[ComponentActivity.getDefaultViewModelProviderFactory]进行自定义
 */
fun ComponentActivity.createPreferredViewModelFactory(): ViewModelProvider.Factory {
    require(application != null) {
        "Your activity is not yet attached to the Application instance. You can't request ViewModel before onCreate call."
    }
    return SavedStateViewModelFactoryCompat(
        application,
        this,
        intent?.extras,
        intent?.data
    )
}

/**
 * 合并两个工厂：即先使用自定义工厂进行创建，如果返回null，则使用默认工厂进行创建
 * @param custom 自定义工厂
 * @note 这个工厂不能作为默认工厂，否则会造成死循环：相当于该工厂作为默认工厂，如果自定义工厂返回null，又会调用默认工厂进行创建（相当于又调用了自己），这样就会造成循环调用
 */
fun HasDefaultViewModelProviderFactory.mergedViewModelFactory(custom: (Class<out ViewModel>) -> ViewModel?): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return (custom.invoke(modelClass) as? T) ?: defaultViewModelProviderFactory.create(
                modelClass
            )
        }
    }
}

/**
 * 单一ViewModel工厂：根据指定的类型创建对应的ViewModel
 * 通常用于上下文只创建一个ViewModel，并且这个ViewModel带有构造参数无法使用默认工厂构建的情况
 *
 * 如果使用了[SavedStateViewModelFactoryCompat]作为ViewModelFactory，并且构造参数符合，就没必要使用这个方法
 *
 * 使用办法：
 *  private val viewModel: LauncherViewModel by viewModels {
 *      SingleViewModelFactory {
 *          LauncherViewModel(customParams...)
 *      }
 *  }
 */
inline fun <reified V : ViewModel> SingleViewModelFactory(crossinline creator: () -> V): ViewModelProvider.Factory {
    return object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return creator.invoke() as T
        }
    }
}
