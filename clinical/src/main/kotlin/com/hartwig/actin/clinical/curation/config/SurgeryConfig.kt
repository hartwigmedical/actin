package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType

data class SurgeryConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val name: String,
    val treatmentType: OtherTreatmentType
) : CurationConfig