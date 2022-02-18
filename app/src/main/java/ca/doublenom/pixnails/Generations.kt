package ca.doublenom.pixnails

import android.content.Context
import android.telecom.Call
import android.util.Log
import org.json.JSONArray

object Generations {
    private val generations: HashMap<String, Array<Rarity>> = HashMap()

    var isLoaded: Boolean = false

    interface Callback {
        fun onGenerationLoaded()
    }

    fun requestGenerations(context: Context, callback: Callback) {
        isLoaded = false
        HTTPClient.getInstance(context).addToRequestQueueArray(
            "/generations",
            {
                onGenerations(it)
                isLoaded = true
                callback.onGenerationLoaded()
            },
            {
                Log.e("Generations", it.toString())
            },
            false
        )

    }

    private fun onGenerations(json: JSONArray) {
        for (i in 0 until json.length()) {
            val element = json.getJSONObject(i)
            val name = element.getString("name")
            val cards = element.getJSONArray("cards")
            val array = Array(cards.length()) { Rarity.None }
            for (j in 0 until cards.length()) {
                val card = cards.getJSONObject(j)
                val index = card.getInt("index")
                val rarity = card.getString("rarity")
                array[index] = Rarity.fromString(rarity)
            }
            generations[name] = array
        }
    }

    fun getCardRarity(set: String, cardIndex: Int): Rarity {
        return generations[set]?.get(cardIndex) ?: Rarity.None
    }
}