package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.icd.IcdModel

data class ValidatedCurationConfig<T : CurationConfig>(val config: T, val errors: List<CurationConfigValidationError> = emptyList())

fun validateDoids(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>,
    doidValidator: (Set<String>) -> Boolean,
): Pair<Set<String>?, List<CurationConfigValidationError>> {
    val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
    return if (doidValidator.invoke(doids)) {
        doids to emptyList()
    } else {
        null to listOf(
            CurationConfigValidationError(
                curationCategory,
                input,
                fieldName,
                doids.toString(),
                "doids"
            )
        )
    }
}

fun validateIcd(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>,
    icdModel: IcdModel,
): Pair<Set<IcdCode>, List<CurationConfigValidationError>> {
    val titlesByCode = CurationUtil.toIcdTitles(parts[fields["icd"]!!]).groupBy(icdModel::resolveCodeForTitle)
    val errors = titlesByCode[null]?.map { title ->
        CurationConfigValidationError(
            curationCategory,
            input,
            fieldName,
            title,
            "icd",
            "ICD title \"$title\" is not known - check for existence in ICD model"
        )
    } ?: emptyList()
    val codes = titlesByCode.keys.filterNotNull().toSet()
    return codes to errors
}

fun validateBoolean(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>
): Pair<Boolean?, List<CurationConfigValidationError>> {
    return validate(curationCategory, input, fieldName, Boolean::class.java.simpleName, fields, parts) { it.toValidatedBoolean() }
}

fun validateInteger(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>
): Pair<Int?, List<CurationConfigValidationError>> {
    return validate(curationCategory, input, fieldName, Integer::class.java.simpleName, fields, parts) { it.toIntOrNull() }
}

fun validateDouble(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>
): Pair<Double?, List<CurationConfigValidationError>> {
    return validate(curationCategory, input, fieldName, Double::class.java.simpleName, fields, parts) { it.toDoubleOrNull() }
}

inline fun <reified T : Enum<T>> validateOptionalEnum(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>,
    enumCreator: (String) -> T
): Pair<T?, List<CurationConfigValidationError>> {
    val fieldValue = parts[fields[fieldName]!!]
    return if (fieldValue.trim().isEmpty()) {
        null to emptyList()
    } else {
        return validateEnum<T>(curationCategory, input, fieldName, fieldValue, enumCreator)
    }
}

inline fun <reified T : Enum<T>> validateMandatoryEnum(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fields: Map<String, Int>,
    parts: Array<String>,
    enumCreator: (String) -> T,
): Pair<T?, List<CurationConfigValidationError>> {
    val fieldValue = parts[fields[fieldName]!!]
    return validateEnum<T>(curationCategory, input, fieldName, fieldValue, enumCreator)
}

inline fun <reified T : Enum<T>> validateEnum(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    fieldValue: String,
    enumCreator: (String) -> T,
): Pair<T?, List<CurationConfigValidationError>> {
    val trimmedUppercase = fieldValue.trim().replace(" ".toRegex(), "_").uppercase()
    return if (enumContains<T>(trimmedUppercase)) {
        enumCreator.invoke(trimmedUppercase) to emptyList()
    } else {
        return null to listOf(
            CurationConfigValidationError(
                curationCategory,
                input,
                fieldName,
                fieldValue,
                T::class.java.simpleName,
                "Accepted values are ${enumValues<T>().map { it.name }}"
            )
        )
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

private fun <T> validate(
    curationCategory: CurationCategory,
    input: String,
    fieldName: String,
    validType: String,
    fields: Map<String, Int>,
    parts: Array<String>,
    extractionFunction: (String) -> T?
): Pair<T?, List<CurationConfigValidationError>> {
    val fieldValue = parts[fields[fieldName]!!]
    return if (fieldValue.isNotEmpty()) {
        extractionFunction.invoke(fieldValue)?.let { it to emptyList() }
            ?: (null to listOf(
                CurationConfigValidationError(curationCategory, input, fieldName, fieldValue, validType.lowercase())
            ))
    } else {
        null to emptyList()
    }
}