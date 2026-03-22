package dev.tanvd.kcli.view.model

enum class Color(val id: String) {
    BLACK("black"),
    RED("red"),
    GREEN("green"),
    YELLOW("yellow"),
    BLUE("blue"),
    MAGENTA("magenta"),
    CYAN("cyan"),
    WHITE("white");

    data class RGB(val r: Int, val g: Int, val b: Int)

    interface Palette {
        fun color(color: Color): RGB

        companion object {
            val current = Light
        }

        object Light : Palette {
            override fun color(color: Color): RGB = when (color) {
                BLACK -> RGB(40, 44, 52)
                RED -> RGB(224, 108, 117)
                GREEN -> RGB(80, 161, 79)
                YELLOW -> RGB(229, 192, 123)
                BLUE -> RGB(97, 175, 239)
                MAGENTA -> RGB(198, 120, 221)
                CYAN -> RGB(86, 182, 194)
                WHITE -> RGB(220, 223, 228)
            }
        }

        object Dark : Palette {
            override fun color(color: Color): RGB = when (color) {
                BLACK -> RGB(171, 178, 191)
                RED -> RGB(224, 108, 117)
                GREEN -> RGB(152, 195, 121)
                YELLOW -> RGB(229, 192, 123)
                BLUE -> RGB(97, 175, 239)
                MAGENTA -> RGB(198, 120, 221)
                CYAN -> RGB(86, 182, 194)
                WHITE -> RGB(40, 44, 52)
            }
        }
    }
}
