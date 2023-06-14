package com.hartwig.actin.clinical.curation.translation

interface TranslationFactory<T : Translation?> {
    fun create(fields: Map<String, Int>, parts: Array<String>): T
}