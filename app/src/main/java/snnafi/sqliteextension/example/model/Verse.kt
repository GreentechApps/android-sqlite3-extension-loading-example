package snnafi.sqliteextension.example.model

data class Verse(
    var rowId: Int = -1,
    var sura: Int = -1,
    var ayah: Int = -1,
    var text: String? = "",
    var rank : String = ""
) {
    override fun toString(): String {
        return "Rank: $rank , $rowId - $sura - $ayah >  $text"
    }
}