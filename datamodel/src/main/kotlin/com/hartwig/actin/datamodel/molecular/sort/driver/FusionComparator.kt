package com.hartwig.actin.datamodel.molecular.sort.driver

import com.hartwig.actin.datamodel.molecular.Fusion

class FusionComparator : Comparator<Fusion> {

    private val comparator = Comparator.comparing<Fusion, Fusion>({ it }, DriverComparator())
        .thenComparing(Fusion::geneStart)
        .thenComparing(Fusion::geneEnd)
        .thenComparing { fusion -> fusion.geneTranscriptStart.orEmpty() }
        .thenComparing { fusion -> fusion.geneTranscriptEnd.orEmpty() }

    override fun compare(fusion1: Fusion, fusion2: Fusion): Int {
        return comparator.compare(fusion1, fusion2)
    }
}
