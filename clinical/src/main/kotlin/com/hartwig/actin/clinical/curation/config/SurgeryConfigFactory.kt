package com.hartwig.actin.clinical.curation.config

import SurgeryConfig
import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.ingestion.CurationCategory
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class SurgeryConfigFactory : CurationConfigFactory<SurgeryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SurgeryConfig> {
        val input = parts[fields["input"]!!]
        val name = parts[fields["name"]!!]

        val (treatmentType, validationErrors) = validateOptionalEnum(
            CurationCategory.SURGERY,
            input,
            "type",
            fields,
            parts,
            OtherTreatmentType.entries.filterNot { it.category == TreatmentCategory.SURGERY }.toSet()
        ) {
            OtherTreatmentType.valueOf(it)
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


