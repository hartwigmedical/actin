package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import java.time.LocalDate

data class SurgeryConfig(
    override val input: String,
    override val ignore: Boolean = false,
    val name: String,
    val endDate: LocalDate? = null,
    val status: SurgeryStatus? = null
) : CurationConfig