package dev.hrubos.db

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

class Publication : RealmObject {
    @PrimaryKey
    var systemPath = ""

    var title: String = ""
    var description: String = ""
    var chapters: RealmList<Chapter> = realmListOf()
    var favourite: Boolean = false
}