package com.hartwig.actin.molecular.evidence.known

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.molecular.gene.KnownCopyNumber

internal object CopyNumberLookup {

    fun findForCopyNumber(knownCopyNumbers: Iterable<KnownCopyNumber>, copyNumber: CopyNumber): KnownCopyNumber? {
        for (knownCopyNumber in knownCopyNumbers) {
            val geneMatches = knownCopyNumber.gene() == copyNumber.gene
            val interpretationMatches = interpretationMatchesEvent(copyNumber.canonicalImpact.type, knownCopyNumber.event())
            if (geneMatches && interpretationMatches) {
                return knownCopyNumber
            }
        }
        return null
    }

    private fun interpretationMatchesEvent(copyNumberType: CopyNumberType, event: GeneEvent): Boolean {
        return when (copyNumberType) {
            CopyNumberType.FULL_GAIN, CopyNumberType.PARTIAL_GAIN -> {
                event == GeneEvent.AMPLIFICATION
            }

            CopyNumberType.DEL -> {
                event == GeneEvent.DELETION
            }

            else -> {
                false
            }
        }
    }

    fun findForHomozygousDisruption(
        knownCopyNumbers: Iterable<KnownCopyNumber>,
        homozygousDisruption: HomozygousDisruption
    ): KnownCopyNumber? {
        for (knownCopyNumber in knownCopyNumbers) {
            if (knownCopyNumber.event() == GeneEvent.DELETION && knownCopyNumber.gene() == homozygousDisruption.gene) {
                return knownCopyNumber
            }
        }
        return null
    }
}
