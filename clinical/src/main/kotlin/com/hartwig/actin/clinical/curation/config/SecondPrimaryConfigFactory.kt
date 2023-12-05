package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationValidator
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.TumorStatus
import com.hartwig.actin.util.ResourceFile

class SecondPrimaryConfigFactory(private val curationValidator: CurationValidator) : CurationConfigFactory<SecondPrimaryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SecondPrimaryConfig> {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])

        return ValidatedCurationConfig(
            SecondPrimaryConfig(
                input = input,
                ignore = ignore,
                curated = if (!ignore) curatedPriorSecondPrimary(fields, parts, doids) else null
            ), if (!curationValidator.isValidCancerDoidSet(doids)) {
                listOf(CurationConfigValidationError("Second primary config with input '$input' contains at least one invalid doid: '$doids')"))
            } else emptyList()
        )
    }

    private fun curatedPriorSecondPrimary(fields: Map<String, Int>, parts: Array<String>, doids: Set<String>): PriorSecondPrimary {

        return ImmutablePriorSecondPrimary.builder()
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
            .status(TumorStatus.valueOf(parts[fields["status"]!!]))
            .build()
    }
}