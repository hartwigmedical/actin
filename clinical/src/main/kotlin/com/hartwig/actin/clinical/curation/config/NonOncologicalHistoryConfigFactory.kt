package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition

class NonOncologicalHistoryConfigFactory(private val curationDoidValidator: CurationDoidValidator) :
    CurationConfigFactory<NonOncologicalHistoryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<NonOncologicalHistoryConfig> {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val (lvefValue, lvefValueValidationErrors) = validatedLvefValue(input, ignore, parts, fields)
        val (priorOtherCondition, priorOtherConditionValidationErrors) = toCuratedPriorOtherCondition(ignore, fields, input, parts)
        return ValidatedCurationConfig(
            NonOncologicalHistoryConfig(
                input = input,
                ignore = ignore,
                lvef = lvefValue,
                priorOtherCondition = if (!ignore) {
                    priorOtherCondition
                } else null
            ), priorOtherConditionValidationErrors + lvefValueValidationErrors
        )
    }

    private fun validatedLvefValue(
        input: String,
        ignore: Boolean,
        parts: Array<String>,
        fields: Map<String, Int>
    ): Pair<Double?, List<CurationConfigValidationError>> {

        if (ignore) return null to emptyList()

        return when (isLVEF(fields, parts)) {
            true -> validateDouble(CurationCategory.NON_ONCOLOGICAL_HISTORY, input, "lvefValue", fields, parts)
            false -> null to emptyList()
        }
    }


    private fun toCuratedPriorOtherCondition(
        ignore: Boolean,
        fields: Map<String, Int>,
        input: String,
        parts: Array<String>
    ): Pair<PriorOtherCondition?, List<CurationConfigValidationError>> {
        return if (!ignore && !isLVEF(fields, parts)) {
            val (doids, doidValidationErrors) = validateDoids(
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                input,
                "doids",
                fields,
                parts
            ) { curationDoidValidator.isValidDiseaseDoidSet(it) }
            val (isContraindicationForTherapy, isContraindicationForTherapyValidationErrors) = validateBoolean(
                CurationCategory.NON_ONCOLOGICAL_HISTORY,
                input,
                "isContraindicationForTherapy",
                fields,
                parts
            )
            val (year, yearValidationErrors) = validateInteger(CurationCategory.NON_ONCOLOGICAL_HISTORY, input, "year", fields, parts)
            val (month, monthValidationErrors) = validateInteger(CurationCategory.NON_ONCOLOGICAL_HISTORY, input, "month", fields, parts)

            ImmutablePriorOtherCondition.builder()
                .name(parts[fields["name"]!!])
                .year(year)
                .month(month)
                .doids(doids ?: emptySet())
                .category(parts[fields["category"]!!])
                .isContraindicationForTherapy(isContraindicationForTherapy ?: false)
                .build() to doidValidationErrors + isContraindicationForTherapyValidationErrors + yearValidationErrors + monthValidationErrors
        } else {
            null to emptyList()
        }
    }

    private fun isLVEF(fields: Map<String, Int>, parts: Array<String>): Boolean {
        return parts[fields["isLVEF"]!!] == "1"
    }
}