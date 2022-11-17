package snnafi.sqliteextension.example.model

data class Verse(
    var rowId: Int = -1,
    var sura: Int = -1,
    var ayah: Int = -1,
    var text: String? = "",
) {
    override fun toString(): String {
        return "$rowId - $sura - $ayah >  $text"
    }
}