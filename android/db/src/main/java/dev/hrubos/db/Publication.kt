package dev.hrubos.db

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

data class Publication(
    var systemPath: String = "",

    var coverPath: String = "",
    var title: String = "",
    var description: String = "",
    var chapters: List<Chapter> = emptyList(),
    var lastChapterRead: Int = 0,
    var favourite: Boolean = false,
)

fun Publication.toRealmObject(): PublicationRO {
    val ro = PublicationRO()
    ro.systemPath = systemPath
    ro.coverPath = coverPath
    ro.title = title
    ro.description = description
    ro.chapters = realmListOf(*chapters.map { it.toRealmObject() }.toTypedArray())
    ro.lastChapterRead = lastChapterRead
    ro.favourite = favourite
    return ro
}

class PublicationRO : RealmObject {
    var systemPath = ""

    var coverPath = ""
    var title: String = ""
    var description: String = ""
    var chapters: RealmList<ChapterRO> = realmListOf()
    var lastChapterRead: Int = 0
    var favourite: Boolean = false
}

fun PublicationRO.toPublication(): Publication {
    return Publication(
        systemPath = systemPath,
        coverPath = coverPath,
        title = title,
        description = description,
        chapters = chapters.map { it.toChapter() },
        lastChapterRead = lastChapterRead,
        favourite = favourite
    )
}