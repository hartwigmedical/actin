package com.hartwig.actin.molecular.sort.driver

import com.hartwig.actin.molecular.datamodel.driver.Fusion

class FusionComparator : Comparator<Fusion> {
    override fun compare(fusion1: Fusion, fusion2: Fusion): Int {
        val driverCompare = DRIVER_COMPARATOR.compare(fusion1, fusion2)
        if (driverCompare != 0) {
            return driverCompare
        }
        val geneStartCompare = fusion1.geneStart().compareTo(fusion2.geneStart())
        if (geneStartCompare != 0) {
            return geneStartCompare
        }
        val geneEndCompare = fusion1.geneEnd().compareTo(fusion2.geneEnd())
        if (geneEndCompare != 0) {
            return geneEndCompare
        }
        val geneTranscriptStartCompare = fusion1.geneTranscriptStart().compareTo(fusion2.geneTranscriptStart())
        return if (geneTranscriptStartCompare != 0) {
            geneTranscriptStartCompare
        } else fusion1.geneTranscriptEnd().compareTo(fusion2.geneTranscriptEnd())
    }

    companion object {
        private val DRIVER_COMPARATOR = DriverComparator()
    }
}
