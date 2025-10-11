package dev.hrubos.db

import io.realm.kotlin.types.RealmObject

class Chapter : RealmObject {
    var title: String = ""
    var description: String = ""
    var pages: Int = 0
    var pageLastRead: Int = 0
    var read: Boolean = false
}