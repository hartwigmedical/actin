package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import java.time.LocalDate

class SurgeryConfigFactory : CurationConfigFactory<SurgeryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SurgeryConfig> {
        val name = parts[fields["name"]!!]
        val ignore = CurationUtil.isIgnoreString(name)
        val endDateText = parts[fields["endDate"]!!]
        val surgeryStatusText = parts[fields["status"]!!]
        return ValidatedCurationConfig(
            SurgeryConfig(
                input = parts[fields["input"]!!],
                ignore = ignore,
                name = name,
                endDate = if (endDateText.isNotEmpty()) LocalDate.parse(endDateText) else null,
                status = if (surgeryStatusText.isNotEmpty()) SurgeryStatus.valueOf(surgeryStatusText) else null
            )
        )
    }
}


