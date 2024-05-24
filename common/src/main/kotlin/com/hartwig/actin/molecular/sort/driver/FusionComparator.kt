package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.wgs.driver.WgsFusion

class FusionComparator : Comparator<WgsFusion> {

    private val comparator = Comparator.comparing<WgsFusion, WgsFusion>({ it }, DriverComparator())
        .thenComparing(WgsFusion::geneStart)
        .thenComparing(WgsFusion::geneEnd)
        .thenComparing(WgsFusion::geneTranscriptStart)
        .thenComparing(WgsFusion::geneTranscriptEnd)
    
    override fun compare(fusion1: WgsFusion, fusion2: WgsFusion): Int {
        return comparator.compare(fusion1, fusion2)
    }
}
