package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.orange.driver.ExtendedFusion

class FusionComparator : Comparator<ExtendedFusion> {

    private val comparator = Comparator.comparing<ExtendedFusion, ExtendedFusion>({ it }, DriverComparator())
        .thenComparing(ExtendedFusion::geneStart)
        .thenComparing(ExtendedFusion::geneEnd)
        .thenComparing(ExtendedFusion::geneTranscriptStart)
        .thenComparing(ExtendedFusion::geneTranscriptEnd)
    
    override fun compare(fusion1: ExtendedFusion, fusion2: ExtendedFusion): Int {
        return comparator.compare(fusion1, fusion2)
    }
}
