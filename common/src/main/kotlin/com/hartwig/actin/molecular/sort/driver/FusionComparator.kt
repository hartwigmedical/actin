package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Fusion

class FusionComparator : Comparator<Fusion> {

    private val comparator = Comparator.comparing<Fusion, Fusion>({ it }, DriverComparator())
        .thenComparing(Fusion::geneStart)
        .thenComparing(Fusion::geneEnd)
        .thenComparing(Fusion::geneTranscriptStart)
        .thenComparing(Fusion::geneTranscriptEnd)
    
    override fun compare(fusion1: Fusion, fusion2: Fusion): Int {
        return comparator.compare(fusion1, fusion2)
    }
}
