package unics.okdroid.tools.ui

import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import androidx.annotation.StyleRes
import androidx.appcompat.app.AlertDialog
import unics.okcore.lang.mapAs

/**
 * 默认对话框实现:基于系统SDK实现
 */
object SystemDialogUI : DialogUI {

    private const val UNSET = 0

    /**
     * 默认对话框主题样式
     */
    @StyleRes
    var defaultThemeResId: Int = UNSET

    private fun newAlertDialogBuilder(context: Context): AlertDialog.Builder {
        return if (defaultThemeResId == UNSET) {
            AlertDialog.Builder(context)
        } else {
            AlertDialog.Builder(context, defaultThemeResId)
        }
    }

    override fun showLoading(
        ctx: Context,
        message: CharSequence
    ): unics.okdroid.tools.ui.ProgressDialog {
        val dialog = if (defaultThemeResId == UNSET) {
            ProgressDialog(ctx, defaultThemeResId)
        } else {
            ProgressDialog(ctx)
        }
        dialog.setMessage(message)
        dialog.setCancelable(false)
        dialog.setCanceledOnTouchOutside(false)
        dialog.show()
        return wrapperProgressDialog(dialog)
    }

    override fun showAlertDialog(
        ctx: Context,
        message: CharSequence,
        positiveBtnText: CharSequence
    ): unics.okdroid.tools.ui.Dialog {
        val dialog = newAlertDialogBuilder(ctx)
            .setMessage(message)
            .setPositiveButton(positiveBtnText, null)
            .show()
        return wrapper(dialog)
    }

    override fun showAlertDialog(
        ctx: Context,
        messageId: Int,
        okPair: Pair<Int, DialogInterface.OnClickListener?>,
        cancelPair: Pair<Int, DialogInterface.OnClickListener?>?,
        cancelable: Boolean
    ): unics.okdroid.tools.ui.Dialog {
        return showAlertDialog(ctx, ctx.getText(messageId), okPair.mapAs {
            Pair(ctx.getText(this.first), this.second)
        }, cancelPair?.mapAs {
            Pair(ctx.getText(this.first), this.second)
        }, cancelable)
    }

    override fun showAlertDialog(
        ctx: Context,
        message: CharSequence,
        okPair: Pair<CharSequence, DialogInterface.OnClickListener?>,
        cancelPair: Pair<CharSequence, DialogInterface.OnClickListener?>?,
        cancelable: Boolean
    ): unics.okdroid.tools.ui.Dialog {
        val dialog = newAlertDialogBuilder(ctx)
            .setMessage(message)
            .setPositiveButton(okPair.first, okPair.second)
            .apply {
                if (cancelPair != null)
                    setNegativeButton(cancelPair!!.first, cancelPair.second)
            }
            .setCancelable(cancelable)
            .show()

        return wrapper(dialog)
    }

    //包装对话框
    private fun wrapper(dialog: Dialog): unics.okdroid.tools.ui.Dialog {
        return object : unics.okdroid.tools.ui.Dialog {
            override fun dismiss() {
                dialog.dismiss()
            }

            override val isShowing: Boolean
                get() = dialog.isShowing

            override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
                dialog.setOnDismissListener(listener)
            }

            override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
                dialog.setOnCancelListener(listener)
            }
        }
    }

    private fun wrapperProgressDialog(dialog: ProgressDialog): unics.okdroid.tools.ui.ProgressDialog {
        return object : unics.okdroid.tools.ui.ProgressDialog {
            override fun setMessage(msg: String) {
                dialog.setMessage(msg)
            }

            override fun setOnDismissListener(listener: DialogInterface.OnDismissListener?) {
                dialog.setOnDismissListener(listener)
            }

            override fun setOnCancelListener(listener: DialogInterface.OnCancelListener?) {
                dialog.setOnCancelListener(listener)
            }

            override fun dismiss() {
                dialog.dismiss()
            }

            override val isShowing: Boolean
                get() = dialog.isShowing

        }
    }
}