package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.util.ResourceFile

class PriorPrimaryConfigFactory(private val curationDoidValidator: CurationDoidValidator) : CurationConfigFactory<PriorPrimaryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<PriorPrimaryConfig> {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        if (!ignore) {
            val (validatedTumorStatus, tumorStatusValidationErrors) = validateMandatoryEnum<TumorStatus>(
                CurationCategory.PRIOR_PRIMARY,
                input,
                "status",
                fields,
                parts
            ) { TumorStatus.valueOf(it) }
            val (validatedDoids, doidValidationErrors) = validateDoids(
                CurationCategory.PRIOR_PRIMARY,
                input,
                "doids",
                fields,
                parts
            ) { curationDoidValidator.isValidCancerDoidSet(it) }
            val curatedPriorPrimary =
                validatedTumorStatus?.let { validatedDoids?.let { doids -> curatedPriorPrimary(it, fields, parts, doids) } }
            return ValidatedCurationConfig(
                PriorPrimaryConfig(
                    input = input,
                    ignore = false,
                    curated = curatedPriorPrimary
                ), tumorStatusValidationErrors + doidValidationErrors
            )
        } else {
            return ValidatedCurationConfig(PriorPrimaryConfig(input = input, ignore = true, curated = null))
        }
    }

    private fun curatedPriorPrimary(
        tumorStatus: TumorStatus, fields: Map<String, Int>, parts: Array<String>, doids: Set<String>
    ): PriorPrimary {
        return PriorPrimary(
            tumorLocation = parts[fields["tumorLocation"]!!],
            tumorSubLocation = parts[fields["tumorSubLocation"]!!],
            tumorType = parts[fields["tumorType"]!!],
            tumorSubType = parts[fields["tumorSubType"]!!],
            doids = doids,
            diagnosedYear = ResourceFile.optionalInteger(parts[fields["diagnosedYear"]!!]),
            diagnosedMonth = ResourceFile.optionalInteger(parts[fields["diagnosedMonth"]!!]),
            treatmentHistory = parts[fields["treatmentHistory"]!!],
            lastTreatmentYear = ResourceFile.optionalInteger(parts[fields["lastTreatmentYear"]!!]),
            lastTreatmentMonth = ResourceFile.optionalInteger(parts[fields["lastTreatmentMonth"]!!]),
            status = tumorStatus
        )
    }
}