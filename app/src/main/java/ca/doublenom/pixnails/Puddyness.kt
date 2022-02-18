package ca.doublenom.pixnails

enum class Puddyness {
    Normal,
    Super,
    Giga,
    None;

    override fun toString(): String {
        return this.name
    }

    fun toUselessCorpRetardness(underscore: Boolean = true): String {
        return when {
            this == Normal -> "normal"
            this == Super -> "shiny"
            this == Giga -> if(underscore) "super_shiny" else "superShiny"
            else -> ""
        }
    }
}