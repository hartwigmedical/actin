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
        val (validatedTumorStatus, tumorStatusValidationErrors) = if (!ignore) validateEnum<TumorStatus>(parts[fields["status"]!!], input) {
            TumorStatus.valueOf(
                it
            )
        } else null to emptyList()
        val curatedPriorSecondPrimary = validatedTumorStatus?.let { curatedPriorSecondPrimary(ignore, it, fields, parts, doids) }
        return ValidatedCurationConfig(
            SecondPrimaryConfig(
                input = input,
                ignore = ignore,
                curated = curatedPriorSecondPrimary
            ), tumorStatusValidationErrors + if (!ignore && !curationDoidValidator.isValidCancerDoidSet(doids)) {
                listOf(CurationConfigValidationError("Second primary config with input '$input' contains at least one invalid doid: '$doids'"))
            } else emptyList()
        )
    }

    private fun curatedPriorSecondPrimary(
        ignore: Boolean,
        tumorStatus: TumorStatus,
        fields: Map<String, Int>,
        parts: Array<String>,
        doids: Set<String>
    ): PriorSecondPrimary? {
        return if (!ignore) {
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
                .status(tumorStatus)
                .build()
        } else {
            null
        }
    }
}