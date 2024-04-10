package us.brainstormz.robotTwo


enum class Side {
    Left,
    Right;
    fun otherSide(): Side =
            when (this) {
                Left -> Right
                Right -> Left
            }
    interface ThingWithSides<T> {
        val left: T
        val right: T

        fun getBySide(side: Side): T = when (side) {
            Left -> left
            Right -> right
        }

//        companion object {
//            fun <T>createByRule(rule: (side: Side)->T):
//                    this(   left = rule(Left),
//                            right = rule(Right))
//        }
    }
}