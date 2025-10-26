package dev.hrubos.db

import io.realm.kotlin.types.RealmObject

data class Chapter(
    val title: String = "",
    val systemPath: String = "",
    val description: String = "",
    val position: Int = 0,
    val pages: Int = 0,
    val pageLastRead: Int = 0,
    val read: Boolean = false,
)

fun Chapter.toRealmObject(): ChapterRO {
    val ro = ChapterRO()
    ro.title = title
    ro.systemPath = systemPath
    ro.description = description
    ro.position = position
    ro.pages = pages
    ro.pageLastRead = pageLastRead
    ro.read = read
    return ro
}

class ChapterRO : RealmObject {
    var title: String = ""

    var systemPath: String = ""
    var description: String = ""
    var position: Int = 0
    var pages: Int = 0
    var pageLastRead: Int = 0
    var read: Boolean = false
}

fun ChapterRO.toChapter(): Chapter {
    return Chapter(
        title = title,
        systemPath = systemPath,
        description = description,
        position = position,
        pages = pages,
        pageLastRead = pageLastRead,
        read = read
    )
}