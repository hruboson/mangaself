package dev.hrubos.api

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.*

class Profile : RealmObject {
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
}