package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.util.GeneConstants
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

object MmrFunctions {

    fun isMmrAbsenceOfProteinEvent(geneEvent: GeneEvent, gene: String): Boolean {
        return geneEvent == GeneEvent.ABSENCE_OF_PROTEIN && GeneConstants.MMR_GENES.contains(gene)
    }
}