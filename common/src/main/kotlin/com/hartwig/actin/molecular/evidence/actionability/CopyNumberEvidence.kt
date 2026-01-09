package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class CopyNumberEvidence {

    companion object {
        private val AMPLIFICATION_EVENTS = setOf(GeneEvent.AMPLIFICATION, GeneEvent.OVEREXPRESSION, GeneEvent.PRESENCE_OF_PROTEIN)
        private val DELETION_EVENTS =
            setOf(GeneEvent.DELETION, GeneEvent.UNDEREXPRESSION, GeneEvent.ABSENCE_OF_PROTEIN, GeneEvent.INACTIVATION)

        fun isAmplificationEvent(geneEvent: GeneEvent): Boolean {
            return AMPLIFICATION_EVENTS.contains(geneEvent)
        }

        fun isDeletionEvent(gene: ActionableGene): Boolean {
            return DELETION_EVENTS.contains(gene.event())
        }

        fun isAmplificationMatch(actionableGene: ActionableGene, copyNumber: CopyNumber): Boolean {
            return (copyNumber.canonicalImpact.type.isGain) && copyNumber.gene == actionableGene.gene()
        }

        fun isDeletionMatch(actionableGene: ActionableGene, copyNumber: CopyNumber): Boolean {
            return (copyNumber.canonicalImpact.type.isDeletion) && copyNumber.gene == actionableGene.gene()
        }
    }
}
