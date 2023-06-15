package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationValidator
import org.apache.logging.log4j.LogManager

class PrimaryTumorConfigFactory(private val curationValidator: CurationValidator) : CurationConfigFactory<PrimaryTumorConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): PrimaryTumorConfig {
        val input = parts[fields["input"]!!]
        val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])
        if (!curationValidator.isValidCancerDoidSet(doids)) {
            LOGGER.warn("Primary tumor config with input '{}' contains at least one invalid doid: '{}'", input, doids)
        }
        return PrimaryTumorConfig(
            input = input,
            primaryTumorLocation = parts[fields["primaryTumorLocation"]!!],
            primaryTumorSubLocation = parts[fields["primaryTumorSubLocation"]!!],
            primaryTumorType = parts[fields["primaryTumorType"]!!],
            primaryTumorSubType = parts[fields["primaryTumorSubType"]!!],
            primaryTumorExtraDetails = parts[fields["primaryTumorExtraDetails"]!!],
            doids = doids
        )
    }

    companion object {
        private val LOGGER = LogManager.getLogger(PrimaryTumorConfigFactory::class.java)
    }
}