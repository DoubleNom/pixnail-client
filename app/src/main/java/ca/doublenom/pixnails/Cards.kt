package ca.doublenom.pixnails

import org.json.JSONArray

class Cards {
    private val cards = HashMap<Card, Int>()

    fun update(json: JSONArray) {
        for (i in 0 until json.length()) {
            val e = json.getJSONObject(i)
            val q = e.getJSONObject("quantities")
            cards[Card(e, Puddyness.Normal)] = q.getInt(Puddyness.Normal.toUselessCorpRetardness())
            cards[Card(e, Puddyness.Super)] = q.getInt(Puddyness.Super.toUselessCorpRetardness())
            cards[Card(e, Puddyness.Giga)] = q.getInt(Puddyness.Giga.toUselessCorpRetardness(false))
        }
    }

    fun findNewCards(draw: Array<Card>) : Array<Card> {
        val newCards = ArrayList<Card>()
        for(card in draw) {
            if (cards[card] == 0) newCards.add(card)
        }
        return newCards.toTypedArray()
    }

}