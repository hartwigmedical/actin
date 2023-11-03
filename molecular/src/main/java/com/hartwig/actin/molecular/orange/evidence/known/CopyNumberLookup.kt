package com.hartwig.actin.molecular.orange.evidence.known

import com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.gene.KnownCopyNumber

internal object CopyNumberLookup {
    fun findForCopyNumber(knownCopyNumbers: Iterable<KnownCopyNumber>, gainLoss: PurpleGainLoss): KnownCopyNumber? {
        for (knownCopyNumber in knownCopyNumbers) {
            val geneMatches = knownCopyNumber.gene() == gainLoss.gene()
            val interpretationMatches = interpretationMatchesEvent(gainLoss.interpretation(), knownCopyNumber.event())
            if (geneMatches && interpretationMatches) {
                return knownCopyNumber
            }
        }
        return null
    }

    private fun interpretationMatchesEvent(interpretation: CopyNumberInterpretation, event: GeneEvent): Boolean {
        return when (interpretation) {
            CopyNumberInterpretation.FULL_GAIN, CopyNumberInterpretation.PARTIAL_GAIN -> {
                event == GeneEvent.AMPLIFICATION
            }

            CopyNumberInterpretation.FULL_LOSS, CopyNumberInterpretation.PARTIAL_LOSS -> {
                event == GeneEvent.DELETION
            }

            else -> {
                false
            }
        }
    }

    fun findForHomozygousDisruption(knownCopyNumbers: Iterable<KnownCopyNumber>,
                                    linxHomozygousDisruption: LinxHomozygousDisruption): KnownCopyNumber? {
        for (knownCopyNumber in knownCopyNumbers) {
            if (knownCopyNumber.event() == GeneEvent.DELETION && knownCopyNumber.gene() == linxHomozygousDisruption.gene()) {
                return knownCopyNumber
            }
        }
        return null
    }
}
