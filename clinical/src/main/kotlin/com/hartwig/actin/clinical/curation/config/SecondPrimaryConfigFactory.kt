package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationCategory
import com.hartwig.actin.clinical.curation.CurationDoidValidator
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import com.hartwig.actin.datamodel.clinical.TumorStatus
import com.hartwig.actin.util.ResourceFile

class SecondPrimaryConfigFactory(private val curationDoidValidator: CurationDoidValidator) : CurationConfigFactory<SecondPrimaryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SecondPrimaryConfig> {
        val input = parts[fields["input"]!!]
        val ignore = CurationUtil.isIgnoreString(parts[fields["name"]!!])
        if (!ignore) {
            val (validatedTumorStatus, tumorStatusValidationErrors) = validateMandatoryEnum<TumorStatus>(
                CurationCategory.SECOND_PRIMARY,
                input,
                "status",
                fields,
                parts
            ) { TumorStatus.valueOf(it) }
            val (validatedDoids, doidValidationErrors) = validateDoids(
                CurationCategory.SECOND_PRIMARY,
                input,
                "doids",
                fields,
                parts
            ) { curationDoidValidator.isValidCancerDoidSet(it) }
            val curatedPriorSecondPrimary =
                validatedTumorStatus?.let { validatedDoids?.let { doids -> curatedPriorSecondPrimary(it, fields, parts, doids) } }
            return ValidatedCurationConfig(
                SecondPrimaryConfig(
                    input = input,
                    ignore = false,
                    curated = curatedPriorSecondPrimary
                ), tumorStatusValidationErrors + doidValidationErrors
            )
        } else {
            return ValidatedCurationConfig(SecondPrimaryConfig(input = input, ignore = true, curated = null))
        }
    }

    private fun curatedPriorSecondPrimary(
        tumorStatus: TumorStatus, fields: Map<String, Int>, parts: Array<String>, doids: Set<String>
    ): PriorSecondPrimary {
        return PriorSecondPrimary(
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