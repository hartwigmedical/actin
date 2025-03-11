package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.OtherCondition
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.icd.IcdModel

class OtherConditionConfigFactory(private val icdModel: IcdModel) : CurationConfigFactory<ComorbidityConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<ComorbidityConfig> {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val (lvefValue, lvefValueValidationErrors) = validatedLvefValue(input, ignore, parts, fields)
        val (otherCondition, otherConditionValidationErrors) = toCuratedOtherCondition(ignore, fields, input, parts)
        return ValidatedCurationConfig(
            ComorbidityConfig(
                input = input,
                ignore = ignore,
                lvef = lvefValue,
                curated = otherCondition.takeUnless { ignore }
            ), otherConditionValidationErrors + lvefValueValidationErrors
        )
    }

    private fun validatedLvefValue(
        input: String,
        ignore: Boolean,
        parts: Array<String>,
        fields: Map<String, Int>
    ): Pair<Double?, List<CurationConfigValidationError>> {
        return if (!ignore && isLVEF(fields, parts)) {
            validateDouble(CurationCategory.NON_ONCOLOGICAL_HISTORY, input, "lvefValue", fields, parts)
        } else {
             null to emptyList()
        }
    }


    private fun toCuratedOtherCondition(
        ignore: Boolean,
        fields: Map<String, Int>,
        input: String,
        parts: Array<String>
    ): Pair<OtherCondition?, List<CurationConfigValidationError>> {
        return if (!ignore && !isLVEF(fields, parts)) {
            val (icdCodes, icdValidationErrors) = validateIcd(CurationCategory.NON_ONCOLOGICAL_HISTORY, input, "icd", fields, parts, icdModel)
            val (year, yearValidationErrors) = validateInteger(CurationCategory.NON_ONCOLOGICAL_HISTORY, input, "year", fields, parts)
            val (month, monthValidationErrors) = validateInteger(CurationCategory.NON_ONCOLOGICAL_HISTORY, input, "month", fields, parts)

            OtherCondition(
                name = parts[fields["name"]!!].trim().ifEmpty { null },
                icdCodes = icdCodes,
                year = year,
                month = month
            ) to icdValidationErrors + yearValidationErrors + monthValidationErrors
        } else {
            null to emptyList()
        }
    }

    private fun isLVEF(fields: Map<String, Int>, parts: Array<String>): Boolean {
        return parts[fields["isLVEF"]!!] == "1"
    }
}