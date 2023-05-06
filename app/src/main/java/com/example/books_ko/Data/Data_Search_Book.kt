import android.os.Parcel
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Data_Search_Book(
    var unique_book_value: String = "",
    var title: String = "",
    var authors: String = "",
    var publisher: String = "",
    var thumbnail: String = "",
    var contents: String = "",
    var from_: String = "",
    var isbn: String = "",
    var url: String = ""
) : Parcelable
