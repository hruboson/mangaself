package dev.hrubos.db

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject

class Publication : RealmObject {
    var systemPath = ""

    var coverPath = ""
    var title: String = ""
    var description: String = ""
    var chapters: RealmList<Chapter> = realmListOf()
    var lastChapterRead: Int = 0
    var favourite: Boolean = false
}