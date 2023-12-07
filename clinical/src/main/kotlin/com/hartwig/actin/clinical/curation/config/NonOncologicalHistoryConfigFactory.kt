package com.hartwig.actin.clinical.curation.config

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
            true -> validateLvefValue(input, parts, fields)
            false -> null to emptyList()
        }
    }

    private fun validateLvefValue(
        input: String,
        parts: Array<String>,
        fields: Map<String, Int>
    ): Pair<Double?, List<CurationConfigValidationError>> {
        val lvefInput = parts[fields["lvefValue"]!!]
        val lvefValue = lvefInput.toDoubleOrNull()
        return lvefValue to (lvefValue?.let { emptyList() }
            ?: listOf(CurationConfigValidationError("lvefValue was not a valid double '$lvefInput' for input '$input'")))
    }

    private fun toCuratedPriorOtherCondition(
        ignore: Boolean,
        fields: Map<String, Int>,
        input: String,
        parts: Array<String>
    ): Pair<PriorOtherCondition?, List<CurationConfigValidationError>> {
        return if (!ignore && !isLVEF(fields, parts)) {
            val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
            val isContraindicationForTherapy = parts[fields["isContraindicationForTherapy"]!!].toValidatedBoolean()
            val (year, yearValidationErrors) = validateInteger("year", fields, parts)
            val (month, monthValidationErrors) = validateInteger("month", fields, parts)

            ImmutablePriorOtherCondition.builder()
                .name(parts[fields["name"]!!])
                .year(year)
                .month(month)
                .doids(doids)
                .category(parts[fields["category"]!!])
                .isContraindicationForTherapy(isContraindicationForTherapy ?: false)
                .build() to validationErrors(doids, isContraindicationForTherapy, input) + yearValidationErrors + monthValidationErrors
        } else {
            null to emptyList()
        }
    }

    private fun validationErrors(
        doids: Set<String>,
        isContraindicationForTherapy: Boolean?,
        input: String
    ) = if (!curationDoidValidator.isValidDiseaseDoidSet(doids)) {
        listOf(CurationConfigValidationError("Non-oncological history config with input '$input' contains at least one invalid doid: '$doids'"))
    } else emptyList<CurationConfigValidationError>() + if (isContraindicationForTherapy == null) {
        listOf(CurationConfigValidationError("isContraindicationForTherapy was not a valid boolean in input '$input'"))
    } else emptyList()


    private fun isLVEF(fields: Map<String, Int>, parts: Array<String>): Boolean {
        return parts[fields["isLVEF"]!!] == "1"
    }
}