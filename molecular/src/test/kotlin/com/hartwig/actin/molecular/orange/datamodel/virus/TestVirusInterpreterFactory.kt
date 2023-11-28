package com.hartwig.actin.molecular.orange.datamodel.virus

import com.hartwig.hmftools.datamodel.virus.ImmutableVirusInterpreterEntry
import com.hartwig.hmftools.datamodel.virus.VirusBreakendQCStatus
import com.hartwig.hmftools.datamodel.virus.VirusLikelihoodType
import org.apache.logging.log4j.util.Strings

object TestVirusInterpreterFactory {

    fun builder(): ImmutableVirusInterpreterEntry.Builder {
        return ImmutableVirusInterpreterEntry.builder()
            .reported(true)
            .name(Strings.EMPTY)
            .qcStatus(VirusBreakendQCStatus.NO_ABNORMALITIES)
            .integrations(0)
            .driverLikelihood(VirusLikelihoodType.LOW)
            .percentageCovered(0.0)
            .meanCoverage(0.0)
    }
}
