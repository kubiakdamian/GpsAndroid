package pl.dmcs.utils

import android.content.Context
import android.widget.Toast

class ToastCaller {
    companion object {
        fun call(context: Context, message: String) {
            Toast.makeText(
                context,
                message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}