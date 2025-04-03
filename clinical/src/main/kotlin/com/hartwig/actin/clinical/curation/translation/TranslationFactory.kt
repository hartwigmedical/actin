package com.hartwig.actin.clinical.curation.translation

interface TranslationFactory<T> {

    fun create(fields: Map<String, Int>, parts: Array<String>): T
}