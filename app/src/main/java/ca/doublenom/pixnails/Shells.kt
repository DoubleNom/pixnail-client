package ca.doublenom.pixnails

import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class Shells(context: AppCompatActivity, private val callback: Callback) {
    interface Callback {
        fun onShellsUpdated(shells: Int, silverShells: Int)
    }

    private class Data {
        var lastUpdate = 0L
        var silverShells = 0
        var shells = 0

        var ceiling = 0
        var seconds = 0
        var quantity = 50
    }

    var fullIn: Long = 0
        private set

    private val timer = Timer("Shells")
    private val data = Data()

    private var mainHandler = Handler(Looper.getMainLooper())
    private var queue = HTTPClient.getInstance(context)

    private var regularView = context.findViewById<TextView>(R.id.shells_view_amount)
    private var dropTimerView = context.findViewById<TextView>(R.id.shells_view_drop_timer)
    private var fullView = context.findViewById<TextView>(R.id.shells_view_full_reminder)

    init {
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                refresh()
            }
        }, 250, 250)
    }

    fun fetch() {
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

                refresh()

                mainHandler.post { callback.onShellsUpdated(data.shells, data.silverShells) }
            },
            {
                Log.d("Shells", "Error: $it")
            }
        )
    }

    private fun refresh() {
        val now = System.currentTimeMillis()
        val nextDrop = data.seconds * 1000 - (now - data.lastUpdate)
        fullIn =
            (((data.ceiling - data.shells) / data.quantity) * data.seconds * 1000) - (now - data.lastUpdate)

        if (nextDrop <= 0) {
            fetch()
        }
        mainHandler.post {

            regularView.text = "${data.shells}/${data.ceiling}"
            dropTimerView.text = DateUtils.formatElapsedTime(nextDrop / 1000)
            fullView.text = DateUtils.formatElapsedTime(fullIn / 1000)
        }
    }
}