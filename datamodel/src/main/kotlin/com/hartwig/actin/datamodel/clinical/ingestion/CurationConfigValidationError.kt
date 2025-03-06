package com.hartwig.actin.datamodel.clinical.ingestion

data class CurationConfigValidationError(
    val categoryName: String,
    val input: String,
    val fieldName: String,
    val invalidValue: String,
    val validType: String,
    val additionalMessage: String? = null
) : Comparable<CurationConfigValidationError> {

    override fun compareTo(other: CurationConfigValidationError): Int {
        return Comparator.comparing(CurationConfigValidationError::categoryName)
            .thenComparing(CurationConfigValidationError::fieldName)
            .thenComparing(CurationConfigValidationError::input)
            .thenComparing(CurationConfigValidationError::invalidValue)
            .thenComparing(CurationConfigValidationError::validType)
            .compare(this, other)
    }
}