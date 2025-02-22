/**
 * Created by Lucio on 2020-11-15.
 *
 * 系统UI交互：状态栏，虚拟导航栏; 对okSysBar的包装使用，避免直接使用库代码
 */
@file:JvmName("OkSysBarKt")

package unics.okdroidarch.kit

import android.app.Activity
import androidx.annotation.ColorInt
import androidx.annotation.FloatRange
import unics.oksysbar.OkSysBar


/**
 * 设置沉浸式状态栏模式（即状态栏会覆盖布局内容），并且状态栏背景透明
 */
fun Activity.setImmersiveStatusBar() = OkSysBar.setImmersiveStatusBar(this)

/**
 * 设置沉浸式状态栏模式（即状态栏会覆盖布局内容）
 * @param color 状态栏背景色
 */
fun Activity.setImmersiveStatusBar(@ColorInt color: Int) =
    OkSysBar.setImmersiveStatusBar(this, color)

/**
 * 设置沉浸式状态栏模式（即状态栏会覆盖布局内容），并根据给定比率混合两种颜色，将得到的颜色应用到状态栏背景，如果比率为0，则使用[color1],如果比率为0.5,则均匀混合两种颜色，如果比率为1，则使用[color2]
 * @param color1 状态栏背景色1
 * @param color2 状态栏背景色2
 * @param ratio 比率
 */
fun Activity.setImmersiveStatusBar(
    @ColorInt color1: Int,
    @ColorInt color2: Int,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float
) = OkSysBar.setImmersiveStatusBar(this, color1, color2, ratio)

/**
 * 设置状态栏颜色：状态栏不会覆盖内容布局
 *
 * @param color    状态栏颜色值
 */
fun Activity.setStatusBarColor(@ColorInt color: Int) =
    OkSysBar.setStatusBarColor(this, color)

/**
 * 设置状态栏颜色：使用给定的比例在两种ARGB颜色之间进行混合；
 * 如果比率为0，则使用[color1],如果比率为0.5,则均匀混合两种颜色，如果比率为1，则使用[color2]
 * @param color1    颜色1
 * @param color2    颜色2
 * @param ratio     比率，0-1
 */
fun Activity.setStatusBarColor(
    @ColorInt color1: Int,
    @ColorInt color2: Int,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float
) = OkSysBar.setStatusBarColor(this, color1, color2, ratio)

/**
 * 设置沉浸式导航栏（即导航栏会覆盖内容布局），并且导航栏背景透明
 */
fun Activity.setImmersiveNavigationBar() = OkSysBar.setImmersiveNavigationBar(this)

/**
 * 设置沉浸式导航栏（即导航栏会覆盖内容布局）
 * @param color 导航栏背景色
 */
fun Activity.setImmersiveNavigationBar(@ColorInt color: Int) =
    OkSysBar.setImmersiveNavigationBar(this, color)

/**
 * 设置沉浸式导航栏（即导航栏会覆盖内容布局），并根据给定比率混合两种颜色，将得到的颜色应用到状态栏背景，如果比率为0，则使用[color1],如果比率为0.5,则均匀混合两种颜色，如果比率为1，则使用[color2]
 * @param color1 导航栏背景色1
 * @param color2 导航栏背景色2
 * @param ratio 比率
 */
fun Activity.setImmersiveNavigationBar(
    @ColorInt color1: Int,
    @ColorInt color2: Int,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float
) = OkSysBar.setImmersiveNavigationBar(this, color1, color2, ratio)

/**
 * 设置导航栏颜色
 * @param color    状态栏颜色值
 */
fun Activity.setNavigationBarColor(@ColorInt color: Int) =
    OkSysBar.setNavigationBarColor(this, color)

/**
 * 设置Navigation Bar颜色
 * @see setStatusBarColor
 */
fun Activity.setNavigationBarColor(
    @ColorInt color1: Int,
    @ColorInt color2: Int,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float
) = OkSysBar.setNavigationBarColor(this, color1, color2, ratio)

/**
 * 设置沉浸式系统栏（即状态栏和导航栏会覆盖布局内容），并且状态栏和导航栏背景透明
 */
fun Activity.setImmersiveSystemBar() = OkSysBar.setImmersiveSystemBar(this)

/**
 * 设置沉浸式系统栏（即状态栏和导航栏会覆盖布局内容）
 * @param color 系统栏的背景色
 */
fun Activity.setImmersiveSystemBar(@ColorInt color: Int) =
    OkSysBar.setImmersiveSystemBar(this, color)

/**
 * 设置沉浸式系统栏（即状态栏和导航栏会覆盖布局内容），并根据给定比率混合两种颜色，将得到的颜色应用到状态栏和导航栏背景，如果比率为0，则使用[color1],如果比率为0.5,则均匀混合两种颜色，如果比率为1，则使用[color2]
 * @param color1
 * @param color2
 * @param ratio 比率
 */
fun Activity.setImmersiveSystemBar(
    @ColorInt color1: Int,
    @ColorInt color2: Int,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float
) = OkSysBar.setSystemBarColor(this, color1, color2, ratio)

/*
 * 设置System Bar的颜色：即同时设置Status Bar和Navigation Bar的背景颜色
 */
fun Activity.setSystemBarColor(@ColorInt color: Int) =
    OkSysBar.setSystemBarColor(this, color)

/**
 * 设置System Bar的颜色：即同时设置Status Bar和Navigation Bar的背景颜色
 */
fun Activity.setSystemBarColor(
    @ColorInt color1: Int,
    @ColorInt color2: Int,
    @FloatRange(from = 0.0, to = 1.0) ratio: Float
) = OkSysBar.setSystemBarColor(this, color1, color2, ratio)

/**
 * 设置状态栏为浅色模式：即状态栏背景为浅色、文字为深色（比如黑色）
 */
fun Activity.setStatusBarLightMode() = OkSysBar.setStatusBarLightMode(this)

/**
 * 设置状态栏为深色模式：即状态栏背景为深色、文字为浅色（比如白色）
 */
fun Activity.setStatusBarDarkMode() = OkSysBar.setStatusBarDarkMode(this)