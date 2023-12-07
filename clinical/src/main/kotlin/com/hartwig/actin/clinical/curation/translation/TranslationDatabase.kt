package com.hartwig.actin.clinical.curation.translation

class TranslationDatabase<T>(private val translations: Map<T, Translation<T>?>) {
    fun translate(input: T) = translations[input]
}