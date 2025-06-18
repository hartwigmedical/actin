package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.*
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import kotlin.reflect.full.memberProperties


class ComorbidityConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {
    private val requiredFields = mapOf(
        "toxicity" to setOf("name", "grade", "icd"),
        "complication" to setOf("impliesUnknownComplicationState", "name", "icd", "year", "month"),
        "other_condition" to setOf("name", "year", "month", "icd", "isLVEF", "lvefValue"),
        "intolerance" to setOf("name", "icd"),
        "ecg" to setOf("interpretation", "icd", "isQTCF", "isJTC", "qtcfValue", "qtcfUnit", "jtcValue", "jtcUnit"),
        "infection" to setOf("interpretation", "icd")
    )

    private val fieldProcessingFunction = mapOf(
        "name" to ::validateString,
        "interpretation" to ::validateString,
        "icd" to ::partialValidateIcd,
        "year" to ::partialValidateInteger,
        "month" to ::partialValidateInteger,
        "grade" to ::partialValidateInteger,
        "impliesUnknownComplicationState" to ::partialValidateBoolean,
        "isLVEF" to ::partialValidateBoolean,
        "lvefValue" to ::partialValidateDouble,
    )

    internal fun <T> partialValidate(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>,
        validateFn: (CurationCategory, String, String, Map<String, Int>, Array<String>) -> Pair<T, List<CurationConfigValidationError>>
    ): Pair<T, List<CurationConfigValidationError>> {
        return validateFn(CurationCategory.COMORBIDITY, parts[fields["input"]!!], fieldName, fields, parts)
    }

    internal fun partialValidateBoolean(fieldName: String, fields: Map<String, Int>, parts: Array<String>) =
        partialValidate(fieldName, fields, parts, ::validateBoolean)

    internal fun partialValidateInteger(fieldName: String, fields: Map<String, Int>, parts: Array<String>) =
        partialValidate(fieldName, fields, parts, ::validateInteger)

    internal fun partialValidateDouble(fieldName: String, fields: Map<String, Int>, parts: Array<String>) =
        partialValidate(fieldName, fields, parts, ::validateDouble)

    internal fun partialValidateIcd(fieldName: String, fields: Map<String, Int>, parts: Array<String>) =
        partialValidate(fieldName, fields, parts) { category, inputValue, fName, flds, prts ->
            validateIcd(category, inputValue, fName, flds, prts, icdModel)
        }

    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val type = parts[fields["type"]!!]
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        if (type !in requiredFields.keys) {
            return generateNoTypeError(type, input, ignore)
        }
        return generateConfig(input, ignore, type, fields, parts)
    }


    private fun generateConfig(
        input: String,
        ignore: Boolean,
        type: String,
        fields: Map<String, Int>,
        parts: Array<String>
    ): ValidatedCurationConfig<ComorbidityConfig> {
        val retval = requiredFields[type]!!.map { it ->
            it to fieldProcessingFunction[it]!!.call(it, fields, parts)
        }.toMap()
        val fieldValues = retval.mapValues { it.value.first }
        val validationErrors = retval.flatMap { it.value.second }
        val allErrors = validationErrors.fold(emptyList<CurationConfigValidationError>()) { acc, current ->
            acc + current
        }
        var curated: Comorbidity? = when (type) {
            "complication" ->
                Complication(
                    name = fieldValues["name"]!! as String?,
                    icdCodes = fieldValues["icd"]!! as? Set<IcdCode> ?: emptySet(),
                    year = fieldValues["year"]!! as Int?,
                    month = fieldValues["month"]!! as Int?
                )

            "intolerance" ->
                Intolerance(
                    name = fieldValues["name"]!! as String?,
                    icdCodes = fieldValues["icd"]!! as? Set<IcdCode> ?: emptySet()
                )

            "other_condition" ->
                OtherCondition(
                    name = fieldValues["name"]!! as String?,
                    icdCodes = fieldValues["icd"]!! as? Set<IcdCode> ?: emptySet(),
                    year = fieldValues["year"]!! as Int?,
                    month = fieldValues["month"]!! as Int?,
                )

            "toxicity" ->
                ToxicityCuration(
                    name = fieldValues["name"]!! as String?,
                    grade = fieldValues["grade"]!! as Int?,
                    icdCodes = fieldValues["icd"]!! as? Set<IcdCode> ?: emptySet()
                )

            "ecg" -> Ecg(
                name = fieldValues["interpretation"]!! as String?,
                icdCodes = fieldValues["icd"]!! as? Set<IcdCode> ?: emptySet(),
                qtcfMeasure = if (fieldValues["isQTCF"]!! as Boolean? == true) {
                    EcgMeasure(
                        value = fieldValues["qtcfValue"]!! as Int,
                        unit = parts[fields["qtcfUnit"]!!]
                    )
                } else null,
                jtcMeasure = if (fieldValues["isJTC"]!! as Boolean? == true) {
                    EcgMeasure(
                        value = fieldValues["jtcValue"]!! as Int,
                        unit = parts[fields["jtcUnit"]!!]
                    )
                } else null
            )

            "infection" ->
                OtherCondition(
                    name = fieldValues["interpretation"]!! as String?,
                    icdCodes = fieldValues["icd"]!! as? Set<IcdCode> ?: emptySet()
                )

            else -> null
        }

        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
//                lvef = if (retval["isLVEF"]!!.first as Boolean? == true) retval["lvefValue"]!!.first as Double? else null,
                curated = if (!ignore) curated else null
            ), allErrors
        )
    }

    fun validateString(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>,
    ): Pair<String?, List<CurationConfigValidationError>> {
        return Pair(parts[fields[fieldName]!!].trim().ifEmpty { null }, emptyList())
    }

    fun generateNoTypeError(
        type: String,
        input: String,
        ignore: Boolean
    ): ValidatedCurationConfig<ComorbidityConfig> {
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
                curated = null
            ), listOf(
                CurationConfigValidationError(
                    CurationCategory.COMORBIDITY,
                    input,
                    "type",
                    type,
                    String::class.java.simpleName,
                    "Unknown comorbidity type: $type. Expected one of: complication, other_condition, infection, intolerance, ecg, toxicity."
                )
            )
        )
    }
}