/**
 * Created by Lucio on 2020-03-10.
 */
@file:JvmName("NetworksKt")
@file:JvmMultifileClass

package unics.okdroid.tools.net

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.DhcpInfo
import android.net.wifi.WifiManager
import android.os.Build
import android.text.format.Formatter
import android.util.Log
import androidx.annotation.IntDef
import androidx.annotation.RequiresPermission
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.InetAddress
import java.net.NetworkInterface
import java.net.SocketException
import java.util.*

/**
 * 无网络
 */
const val NETWORK_TYPE_NONE = -1

/**
 * 手机网络
 */
const val NETWORK_TYPE_MOBILE = 1

/**
 * WIFI
 */
const val NETWORK_TYPE_WIFI = 2

/**
 * 未知网络
 */
const val NETWORK_TYPE_UNKNOWN = 3

/**
 * 有线
 */
const val NETWORK_TYPE_ETHERNET = 9

@Retention(AnnotationRetention.SOURCE)
@IntDef(
    flag = true,
    value = [NETWORK_TYPE_NONE, NETWORK_TYPE_WIFI, NETWORK_TYPE_MOBILE, NETWORK_TYPE_ETHERNET, NETWORK_TYPE_UNKNOWN]
)
annotation class NetworkType


/**
 * 是否是IPV4地址
 */
inline val String?.isIPV4: Boolean
    get() {
        if (this.isNullOrEmpty())
            return false
        return this.matches(Regex("^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$"))
    }

/**
 * 是否是有效的IPV4地址
 */
inline val String?.isValidIPV4: Boolean get() = this.isIPV4 && this != "0.0.0.0"

/**
 * 是否是Mac地址
 */
inline val String?.isMacAddress: Boolean
    get() {
        if (this.isNullOrEmpty())
            return false
        return this.matches(Regex("([A-Fa-f0-9]{2}[-,:]){5}[A-Fa-f0-9]{2}"))
    }

/**
 * 获取网络类型
 */
@NetworkType
@RequiresPermission(value = Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.getNetworkType(): Int {
    return if (Build.VERSION.SDK_INT >= 23) {
        getNetworkType23()
    } else {
        getNetworkTypeDefault()
    }
}

/**
 * 网络是否连接
 */
@RequiresPermission(value = Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.isNetworkConnected(): Boolean {
    val cm =
        applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
    if (Build.VERSION.SDK_INT < 23) {
        return cm.activeNetworkInfo?.isConnected ?: false
    } else {
        val an = cm.activeNetwork ?: return false
        return cm.getNetworkCapabilities(an) != null
    }
}

/**
 * wifi 是否连接
 */
@RequiresPermission(value = Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.isWifiConnected(): Boolean {
    return isNetworkConnected(this, NETWORK_TYPE_WIFI)
}

/**
 * 手机信号是否打开
 */
@RequiresPermission(value = Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.isGPRSConnected(): Boolean {
    return isNetworkConnected(this, NETWORK_TYPE_MOBILE)
}

/**
 * 获取Wifi SSID(wifi名字)
 */
@RequiresPermission(allOf = [Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.ACCESS_NETWORK_STATE])
fun Context.getCurrentSsid(): String? {
    if (!isWifiConnected())
        return null

    var ssid: String? = getCurrentSsidByWifiInfo()

    if (ssid.isNullOrEmpty() || ssid.contains("unknown ssid")) {
        ssid = getCurrentSsidByConfigured()
    }
    return ssid
}

@SuppressLint("MissingPermission")
fun Context.getCurrentSsidByConfigured(): String? {
    val wifiMgr =
        applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager ?: return null
    val wifiInfo = wifiMgr.connectionInfo ?: return null
    val networkId = wifiInfo.networkId
    return wifiMgr.configuredNetworks?.firstOrNull {
        it.networkId == networkId
    }?.SSID?.removeSsidQuot()
}

fun Context.getCurrentSsidByWifiInfo(): String? {
    return (applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager)?.connectionInfo?.ssid.removeSsidQuot()
}

/**
 * 移除ssid的引号
 */
private fun String?.removeSsidQuot(): String? {
    if (this != null && this.length > 2 && this[0] == '"' && this[this.length - 1] == '"') {
        return this.substring(1, this.length - 1)
    }
    return this
}

@RequiresPermission(value = Manifest.permission.ACCESS_NETWORK_STATE)
fun Context.getIPAddressPreferred(): String? {
    var ipAddress: String? = null
    val nettype = getNetworkType()
    if (nettype == NETWORK_TYPE_NONE)
        return null
    try {
        if (nettype == NETWORK_TYPE_WIFI) {
            ipAddress = getWifiIPAddress(this)
            Log.d("IPAddressPreferred", "getWifiIPAddress=$ipAddress")
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        Log.d("IPAddressPreferred", "getWifiIPAddress=$e")
    }

    try {
        if (!ipAddress.isValidIPV4) {
            ipAddress = getLocalIPAddress()
            Log.d("IPAddressPreferred", "getLocalIpAddress=$ipAddress")
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        Log.d("IPAddressPreferred", "getLocalMacAddressFromIp=$e")
    }

    if (!ipAddress.isValidIPV4) {
        return null
    }
    return ipAddress
}

/**
 * wifi状态下获取IP地址
 */
fun getWifiIPAddress(ctx: Context): String? {
    val dhcpInfo: DhcpInfo =
        (ctx.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager?)?.dhcpInfo
            ?: return null
    //DhcpInfo中的ipAddress是一个int型的变量，通过Formatter将其转化为字符串IP地址
    return Formatter.formatIpAddress(dhcpInfo.ipAddress)
}

/**
 * 获取本地IP
 */
fun getLocalIPAddress(): String? {
    try {
        val en: Enumeration<NetworkInterface> = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val nni: NetworkInterface = en.nextElement()
            val inetAddressEnumeration: Enumeration<InetAddress> = nni.inetAddresses
            while (inetAddressEnumeration.hasMoreElements()) {
                val inetAddress: InetAddress = inetAddressEnumeration.nextElement()
                Log.d(
                    "IPAddressPreferred",
                    "getLocalIpAddress=${inetAddress.hostAddress?.toString()}"
                )
                if (!inetAddress.isLoopbackAddress && !inetAddress.isLinkLocalAddress) {
                    return inetAddress.hostAddress?.toString()
                }
            }
        }
    } catch (ex: SocketException) {
        Log.e("IPAddress", ex.toString())
    }
    return null
}


@SuppressLint("MissingPermission")
fun Context.getMacAddressPreferred(): String? {
    var mac: String? = null
    try {
        mac = getLocalMacAddressFromBusybox()
        Log.d("MacAddressPreferred", "getLocalMacAddressFromBusybox=$mac")
    } catch (e: Throwable) {
        e.printStackTrace()
        Log.d("MacAddressPreferred", "getLocalMacAddressFromBusybox=$e")
    }
    try {
        if (!mac.isMacAddress) {
            mac = getLocalMacAddressFromIp(this)
            Log.d("IPAddressPreferred", "getLocalMacAddressFromIp=$mac")
        }
    } catch (e: Throwable) {
        e.printStackTrace()
        Log.d("IPAddressPreferred", "getLocalMacAddressFromIp=$e")
    }

    if (!mac.isMacAddress) {
        return null
    }
    return mac
}

/**
 * 根据Wifi信息获取本地Mac
 *
 */
@RequiresPermission(allOf = ["android.permission.LOCAL_MAC_ADDRESS", "android.permission.ACCESS_FINE_LOCATION"])
@Deprecated(message = "不推荐使用该方法，该方法需要系统权限")
fun getLocalMacAddressFromWifiInfo(context: Context): String? {
    return try {
        val wifi = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as? WifiManager
        wifi?.connectionInfo?.macAddress
    } catch (e: Throwable) {
        null
    }
}

/**
 * 根据本地Ip地址获取Mac地址
 */
@RequiresPermission(value = Manifest.permission.ACCESS_NETWORK_STATE)
fun getLocalMacAddressFromIp(context: Context): String? {
    val ip = context.getIPAddressPreferred()
    if (!ip.isValidIPV4)
        return null
    try {
        val ne = NetworkInterface.getByInetAddress(InetAddress.getByName(ip))
        return ne.hardwareAddress.toHex(":")
    } catch (e: java.lang.Exception) {
        e.printStackTrace()
    }
    return null
}

/**
 * 根据busybox获取本地Mac
 * 参考地址：https://cloud.tencent.com/developer/article/1931714
 */
fun getLocalMacAddressFromBusybox(): String? {
    val tag = "HWaddr"
    val value = callCmd("busybox ifconfig", tag)
    if (value.isNullOrEmpty()) {
        //如果返回的result == null，则说明网络不可取
        return null
    }
    //对该行数据进行解析
    //例如：eth0      Link encap:Ethernet  HWaddr 7C:52:59:8C:A4:75
    if (value.contains(tag)) {
        //前后可能有空格，去除空格
        return value.substring(value.indexOf(tag) + tag.length, value.length - 1).trim()
    }
    return null
}

private fun callCmd(cmd: String, filter: String): String? {
    var result = ""
    var line = ""
    try {
        val proc = Runtime.getRuntime().exec(cmd)
        val `is` = InputStreamReader(proc.inputStream)
        val br = BufferedReader(`is`)

        //执行命令cmd，只取结果中含有filter的这一行
        while (br.readLine().also { line = it } != null && !line.contains(filter)) {
            //result += line;
            Log.i("callCmd", "line: $line")
        }
        result = line
        Log.i("callCmd", "result: $result")
        return result
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

/**
 * 字节数组转16进制字符串
 */
private inline fun ByteArray.toHex(separator: CharSequence = ""): String =
    joinToString(separator = separator) { eachByte -> "%02x".format(eachByte) }

