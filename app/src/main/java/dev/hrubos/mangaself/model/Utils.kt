package dev.hrubos.mangaself.model

val DEFAULT_FUNC = { throw UnsupportedOperationException("this shouldn't be called, default argument for functions accepting function as parameter") }
val DEFAULT_FUNC_STRING: (String) -> Unit = { str -> throw UnsupportedOperationException("this shouldn't be called, default argument for functions accepting function as parameter") }

enum class ReadingMode(val text: String) {
    LEFTTORIGHT("Left to right"),
    RIGHTTOLEFT("Right to left"),
    LONGSTRIP("Long strip");
}

val readingModeOptions = ReadingMode.entries.map { it.text }