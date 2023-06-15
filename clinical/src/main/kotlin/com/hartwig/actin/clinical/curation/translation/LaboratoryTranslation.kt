package com.hartwig.actin.clinical.curation.translation

data class LaboratoryTranslation(
    val code: String,
    val translatedCode: String,
    val name: String,
    val translatedName: String
) : Translation