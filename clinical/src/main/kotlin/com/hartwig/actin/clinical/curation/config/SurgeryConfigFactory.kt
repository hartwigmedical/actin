package com.hartwig.actin.clinical.curation.config

import com.hartwig.actin.clinical.curation.CurationUtil
import com.hartwig.actin.datamodel.clinical.SurgeryStatus
import java.time.LocalDate

class SurgeryConfigFactory : CurationConfigFactory<SurgeryConfig> {
    override fun create(fields: Map<String, Int>, parts: Array<String>): ValidatedCurationConfig<SurgeryConfig> {
        val name = parts[fields["name"]!!]
        val ignore = CurationUtil.isIgnoreString(name)
        return ValidatedCurationConfig(
            SurgeryConfig(
                input = parts[fields["input"]!!],
                ignore = ignore,
                name = name,
                endDate = if (!ignore) LocalDate.parse(parts[fields["endDate"]!!]) else null,
                status = if (!ignore) SurgeryStatus.valueOf(parts[fields["status"]!!]) else null
            )
        )
    }
}


