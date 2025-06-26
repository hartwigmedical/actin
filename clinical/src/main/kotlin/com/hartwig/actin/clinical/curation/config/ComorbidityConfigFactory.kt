package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.*
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.icd.IcdModel


class ComorbidityConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {

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
        val name = parts[fields["name"]!!].trim().ifEmpty { null }
        val yearResult by lazy {
            validateInteger(
                CurationCategory.COMORBIDITY,
                input,
                "year",
                fields,
                parts
            )
        }
        val monthResult by lazy {
            validateInteger(
                CurationCategory.COMORBIDITY,
                input,
                "month",
                fields,
                parts
            )
        }
        val icdCodeResult by lazy {
            validateIcd(
                CurationCategory.COMORBIDITY,
                input,
                "icd",
                fields,
                parts,
                icdModel
            )
        }
        val gradeResult by lazy {
            validateInteger(
                CurationCategory.COMORBIDITY,
                input,
                "grade",
                fields,
                parts
            )
        }
        val impliesUnknownComplicationStateResult by lazy {
            validateBoolean(
                CurationCategory.COMORBIDITY,
                input,
                "impliesUnknownComplicationState",
                fields,
                parts
            )
        }
        val isLVEFResult by lazy {
            validateBoolean(
                CurationCategory.COMORBIDITY,
                input,
                "isLVEF",
                fields,
                parts
            )
        }
        val lvefValueResult by lazy {
            validateDouble(
                CurationCategory.COMORBIDITY,
                input,
                "lvefValue",
                fields,
                parts
            )
        }
        val isQTCFResult by lazy {
            validateBoolean(
                CurationCategory.COMORBIDITY,
                input,
                "isQTCF",
                fields,
                parts
            )
        }
        val isJTCResult by lazy {
            validateBoolean(
                CurationCategory.COMORBIDITY,
                input,
                "isJTC",
                fields,
                parts
            )
        }
        val qtcfValueResult by lazy {
            validateInteger(
                CurationCategory.COMORBIDITY,
                input,
                "qtcfValue",
                fields,
                parts
            )
        }
        val qtcfUnitResult by lazy { extractString("qtcfUnit", fields, parts) }
        val jtcValueResult by lazy {
            validateInteger(
                CurationCategory.COMORBIDITY,
                input,
                "jtcValue",
                fields,
                parts
            )
        }
        val jtcUnitResult by lazy { extractString("jtcUnit", fields, parts) }
        val interpretationResult by lazy { extractString("interpretation", fields, parts) }

        val curatedPair: Pair<Comorbidity?, List<CurationConfigValidationError>> = when (type.uppercase()) {
            ComorbidityClass.COMPLICATION.name ->
                Pair(
                    Complication(
                        name = name,
                        icdCodes = icdCodeResult.first,
                        year = yearResult.first,
                        month = monthResult.first,
                    ),
                    icdCodeResult.second + yearResult.second + monthResult.second
                )

            ComorbidityClass.INTOLERANCE.name ->
                Pair(
                    Intolerance(
                        name = name,
                        icdCodes = icdCodeResult.first
                    ),
                    icdCodeResult.second
                )

            ComorbidityClass.OTHER_CONDITION.name ->
                Pair(
                    OtherCondition(
                        name = name,
                        icdCodes = icdCodeResult.first,
                        year = yearResult.first,
                        month = monthResult.first
                    ),
                    icdCodeResult.second + yearResult.second + monthResult.second
                )

            ComorbidityClass.TOXICITY.name ->
                Pair(
                    ToxicityCuration(
                        name = name,
                        icdCodes = icdCodeResult.first,
                        grade = gradeResult.first
                    ),
                    icdCodeResult.second + gradeResult.second
                )

            ComorbidityClass.ECG.name ->
                Pair(
                    Ecg(
                        name = interpretationResult.first,
                        icdCodes = icdCodeResult.first,
                        qtcfMeasure = if (isQTCFResult.first == true) {
                            val value = qtcfValueResult.first
                            val unit = qtcfUnitResult.first
                            if (value != null && unit != null) EcgMeasure(value, unit) else null
                        } else null,

                        jtcMeasure = if (isJTCResult.first == true) {
                            val value = jtcValueResult.first
                            val unit = jtcUnitResult.first
                            if (value != null && unit != null) EcgMeasure(value, unit) else null
                        } else null
                    ),
                    icdCodeResult.second + qtcfValueResult.second + jtcValueResult.second
                )

            ComorbidityClass.INFECTION.name ->
                Pair(
                    OtherCondition(
                        name = interpretationResult.first,
                        icdCodes = icdCodeResult.first
                    ),
                    icdCodeResult.second
                )

            else -> Pair(null, emptyList())
        }

        val curated = curatedPair.first
        val allErrors = curatedPair.second
        return if (curated == null) {
            generateNoTypeError(type, input, ignore)
        } else ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
                lvef = if (isLVEFResult.first == true) lvefValueResult.first else null,
                curated = if (!ignore) curated else null
            ), allErrors
        )
    }

    private fun generateNoTypeError(
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

    private fun extractString(
        fieldName: String,
        fields: Map<String, Int>,
        parts: Array<String>
    ): Pair<String?, List<CurationConfigValidationError>> {
        return parts[fields[fieldName]!!].trim().ifEmpty { null } to emptyList()
    }
}