package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.hmf.driver.ExhaustiveFusion

class FusionComparator : Comparator<ExhaustiveFusion> {

    private val comparator = Comparator.comparing<ExhaustiveFusion, ExhaustiveFusion>({ it }, DriverComparator())
        .thenComparing(ExhaustiveFusion::geneStart)
        .thenComparing(ExhaustiveFusion::geneEnd)
        .thenComparing(ExhaustiveFusion::geneTranscriptStart)
        .thenComparing(ExhaustiveFusion::geneTranscriptEnd)
    
    override fun compare(fusion1: ExhaustiveFusion, fusion2: ExhaustiveFusion): Int {
        return comparator.compare(fusion1, fusion2)
    }
}
