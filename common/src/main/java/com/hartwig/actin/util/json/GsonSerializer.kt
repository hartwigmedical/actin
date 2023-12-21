package com.hartwig.actin.util.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import java.time.LocalDate

object GsonSerializer {

    fun create(): Gson {
        // If we don't register an explicit type adapter for LocalDate, GSON using reflection internally to create serialize these objects
        return GsonBuilder().serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(LocalDate::class.java, GsonLocalDateAdapter())
            .registerTypeHierarchyAdapter(Set::class.java, GsonSetAdapter<Comparable<Any>>())
            .create()
    }
}
