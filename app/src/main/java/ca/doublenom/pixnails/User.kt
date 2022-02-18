package ca.doublenom.pixnails

import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class User(context: AppCompatActivity, private val callback: User.Callback) {

    private val queue = HTTPClient.getInstance(context)

    private val cards = Cards()
    private val money = Money(context, object : Money.Callback {
        override fun onShellsUpdated(shells: Int, silverShells: Int) {
            callback.onMoneyUpdated(shells, silverShells)
        }

        override fun requestRefresh() {
            fetch()
        }
    })

    fun fetch() {
        queue.addToRequestQueueObject(
            "/user",
            {
                money.update(it)
            },
            {
                Log.e("User", it.toString())
            }
        )

        queue.addToRequestQueueArray(
            "/user/cards",
            {
                cards.update(it)
            },
            {
                Log.e("User", it.toString())
            }
        )
    }

    fun findNewCards(cards: Array<Card>) : Array<Card> {
        return this.cards.findNewCards(cards)
    }

    interface Callback {
        fun onMoneyUpdated(shells: Int, silverShells: Int)
    }
}