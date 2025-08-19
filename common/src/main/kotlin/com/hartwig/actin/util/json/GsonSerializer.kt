package com.hartwig.actin.util.json

import com.google.gson.GsonBuilder
import java.time.LocalDate
import java.time.LocalDateTime

object GsonSerializer {

    fun createBuilder(): GsonBuilder = GsonBuilder().serializeNulls()
        .enableComplexMapKeySerialization()
        .serializeSpecialFloatingPointValues()
        .registerTypeAdapter(LocalDateTime::class.java, GsonLocalDateTimeAdapter())
        .registerTypeAdapter(LocalDate::class.java, GsonLocalDateAdapter())
        .registerTypeHierarchyAdapter(Set::class.java, GsonSetAdapter<Any>())
}