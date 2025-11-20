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

enum class ThemeStyle(val text: String) {
    DARK("Dark"),
    LIGHT("Light"),
}

val themeStyleOptions = ThemeStyle.entries.map { it.text }

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

private val numberRegex = Regex("\\d+")

/**
 * a comparator for DocumentFile directories that sorts by numeric parts first, then by lexicographic order.
 */
val numericComparator = Comparator<DocumentFile> { a, b ->
    val nameA = a.name?.lowercase(Locale.ROOT) ?: ""
    val nameB = b.name?.lowercase(Locale.ROOT) ?: ""

    val numA = numberRegex.findAll(nameA).map { it.value.toInt() }.toList()
    val numB = numberRegex.findAll(nameB).map { it.value.toInt() }.toList()

    val numCompare = compareNumberLists(numA, numB)
    if (numCompare != 0) numCompare else nameA.compareTo(nameB)
}

/**
 * filters and sorts a list of DocumentFiles to include only numeric chapter-like folders.
 */
fun List<DocumentFile>.filterAndSortChapters(): List<DocumentFile> =
    this.filter { it.isDirectory && it.name?.contains(Regex("\\d")) == true }
        .sortedWith(numericComparator)

/**
 * filters and sorts a list of DocumentFiles, putting unnumbered chapters first, then numeric chapters.
 */
fun List<DocumentFile>.filterAndSortChaptersIncludingUnnumbered(): List<DocumentFile> =
    this.filter { it.isDirectory }
        .sortedWith(Comparator { a, b ->
            val nameA = a.name?.lowercase(Locale.ROOT) ?: ""
            val nameB = b.name?.lowercase(Locale.ROOT) ?: ""

            val numA = numberRegex.findAll(nameA).map { it.value.toInt() }.toList()
            val numB = numberRegex.findAll(nameB).map { it.value.toInt() }.toList()

            // if a has no number and b does, a goes first
            if (numA.isEmpty() && numB.isNotEmpty()) return@Comparator -1
            // if b has no number and a does, b goes first
            if (numB.isEmpty() && numA.isNotEmpty()) return@Comparator 1
            // otherwise, compare numerically
            val numCompare = compareNumberLists(numA, numB)
            if (numCompare != 0) numCompare else nameA.compareTo(nameB)
        })

fun String.padNumbers(): String =
    replace(Regex("\\d+")) { it.value.padStart(10, '0') }