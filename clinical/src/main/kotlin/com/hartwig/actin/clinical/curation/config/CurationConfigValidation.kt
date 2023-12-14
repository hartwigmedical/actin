package com.hartwig.actin.clinical.curation.config

data class CurationConfigValidationError(val message: String)
data class ValidatedCurationConfig<T : CurationConfig>(val config: T, val errors: List<CurationConfigValidationError> = emptyList())

fun validateBoolean(
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>
): Pair<Boolean?, List<CurationConfigValidationError>> {
    return validate(input, fields, fieldName, parts) { it.toValidatedBoolean() }
}

fun validateInteger(
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>
): Pair<Int?, List<CurationConfigValidationError>> {
    return validate(input, fields, fieldName, parts) { it.toIntOrNull() }
}

fun validateDouble(
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>
): Pair<Double?, List<CurationConfigValidationError>> {
    return validate(input, fields, fieldName, parts) { it.toDoubleOrNull() }
}

inline fun <reified T : Enum<T>> validateEnum(
    toValidate: String,
    enumCreator: (String) -> T
): Pair<T?, List<CurationConfigValidationError>> {
    val trimmedUppercase = toValidate.trim().uppercase()
    return if (enumContains<T>(trimmedUppercase)) {
        enumCreator.invoke(trimmedUppercase) to emptyList()
    } else {
        null to listOf(enumInvalid<T>(toValidate))
    }
}

private fun String.toValidatedBoolean(): Boolean? {
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
        "Invalid enum value '$name' for enum '${T::class.simpleName}'. Accepted values are " +
                "${enumValues<T>().map { it.name }}"
    )
}

private fun <T> validate(
    input: String,
    fields: Map<String, Int>,
    fieldName: String,
    parts: Array<String>,
    extractionFunction: (String) -> T?
): Pair<T?, List<CurationConfigValidationError>> {
    val fieldValue = parts[fields[fieldName]!!]
    return if (fieldValue.isNotEmpty()) {
        extractionFunction.invoke(fieldValue)?.let { it to emptyList() }
            ?: (null to listOf(CurationConfigValidationError("'$fieldName' had invalid value of '$fieldValue' for input '$input'")))
    } else {
        null to emptyList()
    }
}