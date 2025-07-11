package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.util.GeneConstants
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

object MmrActionabilityFunctions {

    fun isMmrAbsenceOfProteinEvent(gene: ActionableGene): Boolean {
        return gene.event() == GeneEvent.ABSENCE_OF_PROTEIN && GeneConstants.MMR_GENES.contains(gene.gene())
    }
}