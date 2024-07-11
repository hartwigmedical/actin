package com.hartwig.actin.molecular.orange.datamodel.virus

import com.hartwig.hmftools.datamodel.virus.ImmutableVirusInterpreterEntry
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType

object TestVirusInterpreterFactory {

    fun builder(): ImmutableVirusInterpreterEntry.Builder {
        return ImmutableVirusInterpreterEntry.builder()
            .reported(true)
            .name("")
            .qcStatus(VirusBreakendQCStatus.NO_ABNORMALITIES)
            .integrations(0)
            .driverLikelihood(VirusLikelihoodType.LOW)
            .percentageCovered(0.0)
            .meanCoverage(0.0)
    }
}
