package dev.hrubos.db

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.util.UUID

data class Profile(
    var id: String = UUID.randomUUID().toString(),
    var name: String = "",
    var readingMode: String = "",
    var associatedPublications: List<Publication> = emptyList(),
)

fun Profile.toRealmObject(): ProfileRO {
    val ro = ProfileRO()
    ro.id = id
    ro.name = name
    ro.readingMode = readingMode

    val realmPubs = associatedPublications.map { it.toRealmObject() }
    ro.associatedPublications = realmListOf(*realmPubs.toTypedArray())

    return ro
}

class ProfileRO : RealmObject {
    @PrimaryKey
    var id: String = UUID.randomUUID().toString()
    var name: String = ""
    var readingMode: String = ""
    var associatedPublications: RealmList<PublicationRO> = realmListOf()
}

fun ProfileRO.toProfile(): Profile {
    return Profile(
        id = id,
        name = name,
        readingMode = readingMode,
        associatedPublications = associatedPublications.map { it.toPublication() } //TODO { it.toPublication() } later!
    )
}