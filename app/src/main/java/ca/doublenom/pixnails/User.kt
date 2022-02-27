package ca.doublenom.pixnails

import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class User(context: AppCompatActivity, private val callback: Callback) {

    enum class Fetch {
        Money,
        Cards,
        All
    }

    private val queue = HTTPClient.getInstance(context)

    private val cards = Cards(context)
    private val money = Money(context, object : Money.Callback {
        override fun onShellsUpdated(shells: Int, silverShells: Int) {
            callback.onMoneyUpdated(shells, silverShells)
        }

        override fun requestRefresh() {
            fetch()
        }
    })



    fun fetch(fetch: Fetch = Fetch.All) {
        if(fetch == Fetch.Money || fetch == Fetch.All) {
            queue.addToRequestQueueObject(
                "/user",
                {
                    money.update(it)
                },
                {
                    Log.e("User", it.toString())
                }
            )
        }

        if(fetch == Fetch.Cards || fetch == Fetch.All) {
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
    }

    fun findNewCards(cards: Array<Card>) : Array<Card> {
        return this.cards.findNewCards(cards)
    }

    interface Callback {
        fun onMoneyUpdated(shells: Int, silverShells: Int)
    }
}