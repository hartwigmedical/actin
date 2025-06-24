package com.hartwig.actin.clinical.curation.config

import SurgeryConfig
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.ingestion.CurationConfigValidationError
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class SurgeryConfigFactory : CurationConfigFactory<SurgeryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SurgeryConfig> {
        val input = parts[fields["input"]!!]
        val name = parts[fields["name"]!!]

        val (treatmentType, validationErrors) = validateOptionalEnum(CurationCategory.SURGERY, input, "type", fields, parts) {
            OtherTreatmentType.valueOf(it)
        }.let { (treatmentType, error) ->
            treatmentType?.let { type ->
                (type to error).takeIf { type.category == TreatmentCategory.SURGERY }
                    ?: (null to listOf(
                        CurationConfigValidationError(
                            category = CurationCategory.SURGERY,
                            input = input,
                            fieldName = "type",
                            invalidValue = name,
                            validType = OtherTreatmentType::class.java.simpleName,
                            additionalMessage = "Accepted values are ${enumValues<OtherTreatmentType>()
                                .filter { it.category == TreatmentCategory.SURGERY }
                                .map { it.name }}"
                        )
                    ))
            } ?: (null to error)
        }
        return ValidatedCurationConfig(
            SurgeryConfig(
                input = input,
                ignore = CurationUtil.isIgnoreString(name),
                name = name,
                treatmentType = treatmentType ?: OtherTreatmentType.OTHER_SURGERY,
            ), validationErrors
        )
    }
}


