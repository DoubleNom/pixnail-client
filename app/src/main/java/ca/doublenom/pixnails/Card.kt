package ca.doublenom.pixnails

import org.json.JSONObject

class Card(json: JSONObject, var puddyness: Puddyness) {
    var set: String = ""
    var number: Int = 0
    var rarity: Rarity = Rarity.D

    init {
        this.set = json.getString("generationId")
        this.number = json.getInt("index")
        rarity = Generations.getCardRarity(set, number)
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if((other !is Card)) return false
        return this.toString() == other.toString()
    }

    override fun toString(): String {
        return "$set-$number-${puddyness.toString().lowercase()}"
    }
}