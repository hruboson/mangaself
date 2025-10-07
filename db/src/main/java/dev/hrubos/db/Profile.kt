package dev.hrubos.db

import io.realm.kotlin.types.RealmObject
import java.util.UUID

class Profile : RealmObject {
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
}