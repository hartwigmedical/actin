package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.datamodel.ImmutablePriorOtherCondition
import com.hartwig.actin.clinical.datamodel.PriorOtherCondition
import com.hartwig.actin.util.ResourceFile

class NonOncologicalHistoryConfigFactory(private val curationDoidValidator: CurationDoidValidator) :
    CurationConfigFactory<NonOncologicalHistoryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<NonOncologicalHistoryConfig> {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val lvefValue = parts[fields["lvefValue"]!!].toDoubleOrNull()
        val (priorOtherCondition, validationError) = toCuratedPriorOtherCondition(fields, input, parts)
        return ValidatedCurationConfig(
            NonOncologicalHistoryConfig(
                input = input,
                ignore = ignore,
                lvef = if (!ignore) {
                    toCuratedLVEF(fields, parts, lvefValue)
                } else null,
                priorOtherCondition = if (!ignore) {
                    priorOtherCondition
                } else null
            ), validationError + if (lvefValue == null) {
                listOf(CurationConfigValidationError("LVEF value had an unparseable input of '${parts[fields["lvefValue"]!!]}"))
            } else {
                emptyList()
            }
        )
    }

    private fun toCuratedPriorOtherCondition(
        fields: Map<String, Int>,
        input: String,
        parts: Array<String>
    ): Pair<PriorOtherCondition?, List<CurationConfigValidationError>> {
        return if (!isLVEF(fields, parts)) {
            val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
            val isContraindicationForTherapy = parts[fields["isContraindicationForTherapy"]!!].toValidatedBoolean()
            ImmutablePriorOtherCondition.builder()
                .name(parts[fields["name"]!!])
                .year(ResourceFile.optionalInteger(parts[fields["year"]!!]))
                .month(ResourceFile.optionalInteger(parts[fields["month"]!!]))
                .doids(doids)
                .category(parts[fields["category"]!!])
                .isContraindicationForTherapy(isContraindicationForTherapy ?: false)
                .build() to validationErrors(doids, isContraindicationForTherapy, input)
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


    private fun toCuratedLVEF(fields: Map<String, Int>, parts: Array<String>, lvefValue: Double?): Double? {
        return if (isLVEF(fields, parts)) lvefValue else null
    }

    private fun isLVEF(fields: Map<String, Int>, parts: Array<String>): Boolean {
        return parts[fields["isLVEF"]!!] == "1"
    }
}