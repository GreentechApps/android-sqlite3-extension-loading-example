package snnafi.sqliteextension.example.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "verses")
data class Verse(

    @PrimaryKey
    @ColumnInfo(name = "rowid")
    var rowId: Int = -1,
    @ColumnInfo(name = "sura")
    var sura: Int = -1,
    @ColumnInfo(name = "ayah")
    var ayah: Int = -1,
    @ColumnInfo(name = "text")
    var text: String? = "",
) {
    override fun toString(): String {
        return "$rowId - $sura - $ayah $text"
    }
}