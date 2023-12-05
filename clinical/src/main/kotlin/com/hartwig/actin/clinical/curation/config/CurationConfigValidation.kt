package com.hartwig.actin.clinical.curation.config

data class CurationConfigValidationError(val message: String)
data class ValidatedCurationConfig<T : CurationConfig>(val config: T, val errors: List<CurationConfigValidationError> = emptyList())

fun String.toValidatedBoolean(): Boolean? {
    return if (this == "1") {
        true
    } else if (this == "0") {
        false
    } else {
        null
    }
}

inline fun <reified T : Enum<T>> enumContains(name: String): Boolean {
    return enumValues<T>().any { it.name == name }
}

inline fun <reified T : Enum<T>> enumInvalid(name: String): CurationConfigValidationError {
    return CurationConfigValidationError(
        "Invalid enum value [$name] for enum [${T::class.simpleName}]. Accepted values are " +
                "[${enumValues<T>().any { it.name == name }}]"
    )
}