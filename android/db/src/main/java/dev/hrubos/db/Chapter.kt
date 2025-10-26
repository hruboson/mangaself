package dev.hrubos.db

import io.realm.kotlin.types.RealmObject

class Chapter : RealmObject {
    var title: String = ""

    var systemPath = ""
    var description: String = ""
    var position: Int = 0
    var pages: Int = 0
    var pageLastRead: Int = 0
    var read: Boolean = false
}