package com.hartwig.actin.clinical.curation.config

import com.fasterxml.jackson.databind.ser.Serializers.Base
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.*
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import kotlin.reflect.full.memberProperties


class ComorbidityConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {

    internal fun lazyValidateBoolean(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>,
        allErrors: MutableList<List<CurationConfigValidationError>>
    ): Boolean? {
        val result = validateBoolean(
            CurationCategory.COMORBIDITY, parts[fields["input"]!!], fieldName, fields, parts
        )
        allErrors.add(result.second)
        return result.first
    }

    internal fun lazyValidateInteger(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>,
        allErrors: MutableList<List<CurationConfigValidationError>>
    ): Int? {
        val result = validateInteger(
            CurationCategory.COMORBIDITY, parts[fields["input"]!!], fieldName, fields, parts
        )
        allErrors.add(result.second)
        return result.first
    }

    internal fun lazyValidateDouble(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>,
        allErrors: MutableList<List<CurationConfigValidationError>>
    ): Double? {
        val result = validateDouble(
            CurationCategory.COMORBIDITY, parts[fields["input"]!!], fieldName, fields, parts
        )
        allErrors.add(result.second)
        return result.first
    }

    internal fun lazyValidateIcd(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>,
        allErrors: MutableList<List<CurationConfigValidationError>>
    ): Set<IcdCode> {
        val result = validateIcd(
            CurationCategory.COMORBIDITY, parts[fields["input"]!!], fieldName, fields, parts, icdModel
        )
        allErrors.add(result.second)
        return result.first
    }

    internal fun lazyValidateString(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>
    ): String? =
        parts[fields[fieldName]!!].trim().ifEmpty { null }

    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val type = parts[fields["type"]!!]
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        return generateConfig(input, ignore, type, fields, parts)
    }


    private fun generateConfig(
        input: String,
        ignore: Boolean,
        type: String,
        fields: Map<String, Int>,
        parts: Array<String>
    ): ValidatedCurationConfig<ComorbidityConfig> {
        val allErrors = mutableListOf(emptyList<CurationConfigValidationError>())

        val name = parts[fields["name"]!!].trim().ifEmpty { null }
        val year by lazy { lazyValidateInteger("year", fields, parts, allErrors) }
        val month by lazy { lazyValidateInteger("month", fields, parts, allErrors) }
        val icdCode by lazy { lazyValidateIcd("icd", fields, parts, allErrors) }
        val grade by lazy { lazyValidateInteger("grade", fields, parts, allErrors) }
        val impliesUnknownComplicationState by lazy {
            lazyValidateBoolean("impliesUnknownComplicationState", fields, parts, allErrors)
        }
        val isLVEF by lazy { lazyValidateBoolean("isLVEF", fields, parts, allErrors) }
        val lvefValue by lazy { lazyValidateDouble("lvefValue", fields, parts, allErrors) }
        val isQTCF by lazy { lazyValidateBoolean("isQTCF", fields, parts, allErrors) }
        val isJTC by lazy { lazyValidateBoolean("isJTC", fields, parts, allErrors) }
        val qtcfValue by lazy { lazyValidateInteger("qtcfValue", fields, parts, allErrors) }
        val qtcfUnit by lazy { lazyValidateString("qtcfUnit", fields, parts) }
        val jtcValue by lazy { lazyValidateInteger("jtcValue", fields, parts, allErrors) }
        val jtcUnit by lazy { lazyValidateString("jtcUnit", fields, parts) }
        val interpretation by lazy { lazyValidateString("interpretation", fields, parts) }

        val baseComorbidity = BaseComorbidity(name, icdCode, year, month)

        val curated: Comorbidity? = when (type) {
            "complication" -> Complication(baseComorbidity)
            "intolerance" -> Intolerance(baseComorbidity)
            "other_condition" -> OtherCondition(baseComorbidity)
            "toxicity" -> ToxicityCuration(baseComorbidity, grade)

            "ecg" -> Ecg(
                name = interpretation,
                icdCodes = icdCode,
                qtcfMeasure = if (isQTCF == true) {
                    EcgMeasure(
                        value = qtcfValue as Int,
                        unit = qtcfUnit as String
                    )
                } else null,
                jtcMeasure = if (isJTC == true) {
                    EcgMeasure(
                        value = jtcValue as Int,
                        unit = jtcUnit as String
                    )
                } else null
            )
            "infection" ->
                OtherCondition(
                    name = interpretation,
                    icdCodes = icdCode
                )

            else -> null
        }

        return if (curated == null) {
            generateNoTypeError(type, input, ignore)
        } else ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
                lvef = if (isLVEF == true) lvefValue else null,
                curated = if (!ignore) curated else null
            ), allErrors.fold(emptyList()) { acc, errors -> acc + errors }
        )
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