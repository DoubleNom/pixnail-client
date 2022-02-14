package ca.doublenom.pixnails

import android.text.format.DateUtils
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*

class Shells(context: AppCompatActivity) {
    private class Data {
        var lastUpdate = 0L
        var silverShells = 0
        var shells = 0

        var ceiling = 0
        var seconds = 0
        var quantity = 0
    }
    private val data = Data()

    private var queue = HTTPClient.getInstance(context)

    private var regularView = context.findViewById<TextView>(R.id.shells_view_amount)
    private var dropTimerView = context.findViewById<TextView>(R.id.shells_view_drop_timer)
    private var fullView = context.findViewById<TextView>(R.id.shells_view_full_reminder)

    fun refresh() {
        queue.addToRequestQueue(
            "/user",
            {
                val moneys = it.getJSONObject("moneys")
                val drop = it.getJSONObject("drop")
                data.lastUpdate = moneys.getLong("lastUpdateShells")
                data.shells = moneys.getInt("shells")
                data.silverShells = moneys.getInt("silverShells")

                data.ceiling = drop.getInt("ceiling")
                data.seconds = drop.getInt("seconds")
                data.quantity = drop.getInt("quantity")

                updateView()
            },
            {
                Log.d("Shells", "Error: $it")
            }
        )
    }

    private fun updateView() {
        val now = System.currentTimeMillis()
        val nextDrop = data.seconds * 1000 - (now - data.lastUpdate)
        val fullIn = (((data.ceiling - data.shells) / data.quantity) * data.seconds * 1000) - (now - data.lastUpdate)

        regularView.text = "${data.shells}/${data.ceiling}"
        dropTimerView.text = DateUtils.formatElapsedTime(nextDrop / 1000)
        fullView.text = DateUtils.formatElapsedTime(fullIn / 1000)
    }

}