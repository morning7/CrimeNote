package com.android.study.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Crime(@PrimaryKey var id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = ""){

    @Ignore
    constructor():this(UUID.randomUUID(), "", Date(), false, "")

    val photoFileName
        get() = "IMG_$id.jpg"
}