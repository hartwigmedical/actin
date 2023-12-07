package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus
import com.hartwig.actin.util.ResourceFile

class SecondPrimaryConfigFactory(private val curationDoidValidator: CurationDoidValidator) : CurationConfigFactory<SecondPrimaryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SecondPrimaryConfig> {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
        val (curatedPriorSecondPrimary, priorSecondPrimaryValidations) = curatedPriorSecondPrimary(ignore, fields, parts, doids)
        return ValidatedCurationConfig(
            SecondPrimaryConfig(
                input = input,
                ignore = ignore,
                curated = curatedPriorSecondPrimary
            ), priorSecondPrimaryValidations + if (!curationDoidValidator.isValidCancerDoidSet(doids)) {
                listOf(CurationConfigValidationError("Second primary config with input '$input' contains at least one invalid doid: '$doids'"))
            } else emptyList()
        )
    }

    private fun curatedPriorSecondPrimary(
        ignore: Boolean,
        fields: Map<String, Int>,
        parts: Array<String>,
        doids: Set<String>
    ): Pair<PriorSecondPrimary?, List<CurationConfigValidationError>> {
        return if (!ignore) {
            val (validatedTumorStatus, tumorStatusValidationErrors) = validatedTumorStatus(parts, fields)
            validatedTumorStatus?.let {
                ImmutablePriorSecondPrimary.builder()
                    .tumorLocation(parts[fields["tumorLocation"]!!])
                    .tumorSubLocation(parts[fields["tumorSubLocation"]!!])
                    .tumorType(parts[fields["tumorType"]!!])
                    .tumorSubType(parts[fields["tumorSubType"]!!])
                    .doids(doids)
                    .diagnosedYear(ResourceFile.optionalInteger(parts[fields["diagnosedYear"]!!]))
                    .diagnosedMonth(ResourceFile.optionalInteger(parts[fields["diagnosedMonth"]!!]))
                    .treatmentHistory(parts[fields["treatmentHistory"]!!])
                    .lastTreatmentYear(ResourceFile.optionalInteger(parts[fields["lastTreatmentYear"]!!]))
                    .lastTreatmentMonth(ResourceFile.optionalInteger(parts[fields["lastTreatmentMonth"]!!]))
                    .status(validatedTumorStatus)
                    .build() to tumorStatusValidationErrors
            } ?: (null to tumorStatusValidationErrors)
        } else {
            null to emptyList()
        }
    }

    private fun validatedTumorStatus(
        parts: Array<String>,
        fields: Map<String, Int>
    ): Pair<TumorStatus?, List<CurationConfigValidationError>> {
        val statusInput = parts[fields["status"]!!]
        val statusInputTrimmed = statusInput.trim().uppercase()
        return if (enumContains<TumorStatus>(statusInputTrimmed)) {
            Pair(TumorStatus.valueOf(statusInputTrimmed), emptyList())
        } else {
            null to listOf(enumInvalid<TumorStatus>(statusInput))
        }
    }
}