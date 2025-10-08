package dev.hrubos.mangaself.model

enum class ReadingMode(val text: String) {
    LEFTTORIGHT("Left to right"),
    RIGHTTOLEFT("Right to left"),
    LONGSTRIP("Long strip");
}

val readingModeOptions = ReadingMode.entries.map { it.text }

object Configuration {
    var useLocalDB: Boolean = true
    var readingMode: ReadingMode = ReadingMode.LONGSTRIP
}