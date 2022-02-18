package ca.doublenom.pixnails

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.text.format.DateUtils
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.util.*

class Money(context: AppCompatActivity, private val callback: Callback) {
    interface Callback {
        fun onShellsUpdated(shells: Int, silverShells: Int)
        fun requestRefresh()
    }

    private class Data {
        var lastUpdate = 0L
        var silverShells = 0
        var shells = 0

        var ceiling = 0
        var seconds = 0
        var quantity = 50
    }

    private var fullIn: Long = 0

    private var timer: Timer? = null
    private val data = Data()

    private var mainHandler = Handler(Looper.getMainLooper())

    private var regularView = context.findViewById<TextView>(R.id.shells_view_amount)
    private var dropTimerView = context.findViewById<TextView>(R.id.shells_view_drop_timer)
    private var fullView = context.findViewById<TextView>(R.id.shells_view_full_reminder)


    fun update(json: JSONObject) {
        val moneys = json.getJSONObject("moneys")
        val drop = json.getJSONObject("drop")
        data.lastUpdate = moneys.getLong("lastUpdateShells")
        data.shells = moneys.getInt("shells")
        data.silverShells = moneys.getInt("silverShells")

        data.ceiling = drop.getInt("ceiling")
        data.seconds = drop.getInt("seconds")
        data.quantity = drop.getInt("quantity")

        if (timer != null) {
            timer!!.cancel()
            timer!!.purge()
        }

        timer = Timer("shell")

        timer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                refresh()
            }
        }, 250, 250)

        refresh()

        mainHandler.post { callback.onShellsUpdated(data.shells, data.silverShells) }
    }


    @SuppressLint("SetTextI18n")
    private fun refresh() {
        val now = System.currentTimeMillis()
        val nextDrop = data.seconds * 1000 - (now - data.lastUpdate)
        fullIn =
            (((data.ceiling - data.shells) / data.quantity) * data.seconds * 1000) - (now - data.lastUpdate)

        if (nextDrop <= 0) {
            callback.requestRefresh()
        }
        mainHandler.post {
            regularView.text = "${data.shells}/${data.ceiling}"
            dropTimerView.text = DateUtils.formatElapsedTime(nextDrop / 1000)
            fullView.text = DateUtils.formatElapsedTime(fullIn / 1000)
        }
    }
}