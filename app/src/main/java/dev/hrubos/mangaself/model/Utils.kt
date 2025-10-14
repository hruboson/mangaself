package dev.hrubos.mangaself.model

import androidx.documentfile.provider.DocumentFile
import java.util.Locale

val DEFAULT_FUNC = { throw UnsupportedOperationException("this shouldn't be called, default argument for functions accepting function as parameter") }
val DEFAULT_FUNC_STRING: (String) -> Unit = { str -> throw UnsupportedOperationException("this shouldn't be called, default argument for functions accepting function as parameter") }

enum class ReadingMode(val text: String) {
    LEFTTORIGHT("Left to right"),
    RIGHTTOLEFT("Right to left"),
    LONGSTRIP("Long strip");
}

val readingModeOptions = ReadingMode.entries.map { it.text }

/**
 * compare two lists of integers numerically (e.g. [1,2] < [1,10])
 */
fun compareNumberLists(a: List<Int>, b: List<Int>): Int {
    val minLength = minOf(a.size, b.size)
    for (i in 0 until minLength) {
        if (a[i] != b[i]) return a[i] - b[i]
    }
    return a.size - b.size
}

/**
 * regex to extract numeric parts from folder names, like "ch1.5" -> [1, 5]
 */
private val numberRegex = Regex("(\\d+(?:\\.\\d+)*)")

/**
 * a comparator for DocumentFile directories that sorts by numeric parts first, then by lexicographic order.
 */
val chapterDirectoryComparator = Comparator<DocumentFile> { a, b ->
    val nameA = a.name?.lowercase(Locale.ROOT) ?: ""
    val nameB = b.name?.lowercase(Locale.ROOT) ?: ""

    val numA = numberRegex.find(nameA)
        ?.value?.split('.')?.mapNotNull { it.toIntOrNull() } ?: emptyList()
    val numB = numberRegex.find(nameB)
        ?.value?.split('.')?.mapNotNull { it.toIntOrNull() } ?: emptyList()

    val numCompare = compareNumberLists(numA, numB)
    if (numCompare != 0) numCompare else nameA.compareTo(nameB)
}

/**
 * filters and sorts a list of DocumentFiles to include only numeric chapter-like folders.
 */
fun List<DocumentFile>.filterAndSortChapters(): List<DocumentFile> =
    this.filter { it.isDirectory && it.name?.contains(Regex("\\d")) == true }
        .sortedWith(chapterDirectoryComparator)