package com.hartwig.actin.molecular.orange.datamodel.peach

import com.hartwig.hmftools.datamodel.peach.ImmutablePeachGenotype
import org.apache.logging.log4j.util.Strings

object TestPeachFactory {

    fun builder(): ImmutablePeachGenotype.Builder {
        return ImmutablePeachGenotype.builder()
            .gene(Strings.EMPTY)
            .haplotype(Strings.EMPTY)
            .function(Strings.EMPTY)
            .linkedDrugs(Strings.EMPTY)
            .urlPrescriptionInfo(Strings.EMPTY)
            .panelVersion(Strings.EMPTY)
            .repoVersion(Strings.EMPTY)
    }
}
