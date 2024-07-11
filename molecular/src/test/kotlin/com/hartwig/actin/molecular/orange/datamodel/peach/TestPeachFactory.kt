package com.hartwig.actin.molecular.orange.datamodel.peach

import com.hartwig.hmftools.datamodel.peach.ImmutablePeachGenotype

object TestPeachFactory {

    fun builder(): ImmutablePeachGenotype.Builder {
        return ImmutablePeachGenotype.builder()
            .gene("")
            .haplotype("")
            .function("")
            .linkedDrugs("")
            .urlPrescriptionInfo("")
            .panelVersion("")
            .repoVersion("")
    }
}
