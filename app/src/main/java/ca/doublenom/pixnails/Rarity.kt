package ca.doublenom.pixnails

enum class Rarity {
    S,
    A,
    B,
    C,
    D,
    None;

    override fun toString(): String {
        return this.name
    }

    companion object {
        fun fromString(letter: String): Rarity {
            return when (letter.lowercase()) {
                "s" -> S
                "a" -> A
                "b" -> B
                "c" -> C
                "d" -> D
                else -> None
            }
        }
    }
}
