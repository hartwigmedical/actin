package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationValidator
import com.hartwig.actin.clinical.datamodel.ImmutablePriorSecondPrimary
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.util.ResourceFile
import org.apache.logging.log4j.LogManager

class SecondPrimaryConfigFactory(private val curationValidator: CurationValidator) : CurationConfigFactory<SecondPrimaryConfig> {
    override fun create(fields: Map<String?, Int?>, parts: Array<String>): SecondPrimaryConfig {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        return ImmutableSecondPrimaryConfig.builder()
            .input(input)
            .ignore(ignore)
            .curated(if (!ignore) curatedPriorSecondPrimary(fields, input, parts) else null)
            .build()
    }

    private fun curatedPriorSecondPrimary(
        fields: Map<String?, Int?>, input: String,
        parts: Array<String>
    ): PriorSecondPrimary {
        val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
        if (!curationValidator.isValidCancerDoidSet(doids)) {
            LOGGER.warn("Second primary config with input '{}' contains at least one invalid doid: '{}'", input, doids)
        }
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
            .isActive(ResourceFile.bool(parts[fields["isActive"]!!]))
            .build()
    }

    companion object {
        private val LOGGER = LogManager.getLogger(SecondPrimaryConfigFactory::class.java)
    }
}