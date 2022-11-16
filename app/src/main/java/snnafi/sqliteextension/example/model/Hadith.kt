package snnafi.sqliteextension.example.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "hadith")
data class Hadith(

    @PrimaryKey
    @ColumnInfo(name = "urn")
    var urn: Int = -1,
    @ColumnInfo(name = "collection_id")
    var collectionId: Int = -1,
    @ColumnInfo(name = "book_id")
    var bookId: Int = -1,
    @ColumnInfo(name = "chapter_id")
    var chapterId: Int = -1,
    @ColumnInfo(name = "display_number")
    var displayNumber: Int = -1,
    @ColumnInfo(name = "order_in_book")
    var orderInBook: Int = -1,
    @ColumnInfo(name = "narrator_prefix")
    var narratorPrefix: String? = "",
    @ColumnInfo(name = "content")
    var content: String = "",
    @ColumnInfo(name = "narrator_postfix")
    var narratorPostfix: String? = ""

) {
    override fun toString(): String {
        return "$urn - $collectionId $bookId $chapterId $displayNumber $orderInBook"
    }
}