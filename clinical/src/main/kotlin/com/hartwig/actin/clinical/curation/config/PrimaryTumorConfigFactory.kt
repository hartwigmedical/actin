package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.clinical.curation.CurationValidator

class PrimaryTumorConfigFactory(private val curationValidator: CurationValidator) : CurationConfigFactory<PrimaryTumorConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): CurationConfigValidatedResponse<PrimaryTumorConfig> {
        val input = parts[fields["input"]!!]
        val doids = CurationUtil.toDOIDs(parts[fields["doids"]!!])

        return CurationConfigValidatedResponse(
            PrimaryTumorConfig(
                input = input,
                primaryTumorLocation = parts[fields["primaryTumorLocation"]!!],
                primaryTumorSubLocation = parts[fields["primaryTumorSubLocation"]!!],
                primaryTumorType = parts[fields["primaryTumorType"]!!],
                primaryTumorSubType = parts[fields["primaryTumorSubType"]!!],
                primaryTumorExtraDetails = parts[fields["primaryTumorExtraDetails"]!!],
                doids = doids
            ), if (!curationValidator.isValidCancerDoidSet(doids)) {
                listOf(CurationConfigValidationError("Primary tumor config with input '$input' contains at least one invalid doid: '$input'"))
            } else emptyList()
        )
    }
}