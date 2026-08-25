package com.maaz.befit.data.database

import androidx.room.TypeConverter
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

class Converters {

    /*
    * This is conversion is used to convert date datatype to String,
    * and String dataType to date datatype.
    * room db do not save date datatype in tables, because db don't have access of that datatype.
    * so we store date by converting in String, and later fetch that string and convert that into
    * date datatype(so we can perform operation on date)
    * */

    @TypeConverter
    fun fromLocalDate(date : LocalDate?) : String? {
        return date?.toString()
    }

    @TypeConverter
    fun toLocalDate(dateString : String?) : LocalDate? {
        return dateString?.let { LocalDate.parse(it) }
    }

    @TypeConverter
    fun fromLocalDateTime(dateTime : LocalDateTime?) : String? {
        return dateTime?.toString()
    }

    @TypeConverter
    fun toLocalDateTime(dateTimeString : String?) : LocalDateTime? {
        return dateTimeString?.let { LocalDateTime.parse(it) }
    }
}