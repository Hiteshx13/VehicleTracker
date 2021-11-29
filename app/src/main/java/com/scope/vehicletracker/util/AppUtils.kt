package com.scope.vehicletracker.util

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import com.scope.vehicletracker.R
import java.text.SimpleDateFormat
import java.util.*


object AppUtils {

    /**
     * open application settings
     */
    fun openAppSettings(activity: Activity) {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", activity.packageName, null)
        )
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.startActivity(intent)
    }

    /**
     * show dialog to grant location  permission from application settings
     */
    fun showDialog(mContext: Context, listener: SettingsDialogClickListener) {
        val builder = AlertDialog.Builder(mContext)
        builder.setMessage(R.string.message_location_permission)
            .setPositiveButton(
                R.string.settings
            ) { dialog, id ->
                listener.onSettingClicked()
                dialog.dismiss()
            }
            .setNegativeButton(
                R.string.cancel
            ) { dialog, id ->
                dialog.dismiss()
            }
        builder.create().show()
    }

    /**
     * remove extra string from image url
     * **/
    fun getFormattedImageUrl(str: String): String {
        val indexEnd: Int
        val strUrl = str.lowercase()
        var subStrUrl = strUrl.subSequence(0, strUrl.length)
        if (strUrl.contains(".jpg", true)) {
            indexEnd = strUrl.indexOf(".jpg")
            subStrUrl = strUrl.substring(0, indexEnd + 4)
        } else if (strUrl.contains(".jpeg", true)) {
            indexEnd = strUrl.indexOf(".jpeg")
            subStrUrl = strUrl.substring(0, indexEnd + 5)
        }
        return subStrUrl.toString()
    }


    fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
        val formatter = SimpleDateFormat(format, locale)
        return formatter.format(this)
    }

    fun getCurrentDateTime(): Date {
        return Calendar.getInstance().time
    }

}