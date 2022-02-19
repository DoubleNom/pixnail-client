package ca.doublenom.pixnails

import org.json.JSONObject

class Card {
    var set: String = ""
    var number: Int = 0
    var rarity: Rarity = Rarity.D
    var puddyness: Puddyness = Puddyness.None

    constructor(json: JSONObject, puddyness: Puddyness) {
        this.set = json.getString("generationId")
        this.number = json.getInt("index")
        rarity = Generations.getCardRarity(set, number)
        this.puddyness = puddyness
    }

    constructor(set: String, number: Int, puddyness: Puddyness) {
        this.set = set
        this.number = number
        this.puddyness = puddyness
    }

    override fun hashCode(): Int {
        return this.toString().hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if ((other !is Card)) return false
        return this.toString() == other.toString()
    }

    override fun toString(): String {
        return "$set-$number-${puddyness.toString().lowercase()}"
    }
}