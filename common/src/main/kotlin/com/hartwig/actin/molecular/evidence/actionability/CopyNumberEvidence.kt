package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class CopyNumberEvidence {
    
    companion object {
        private val AMPLIFICATION_EVENTS = setOf(GeneEvent.AMPLIFICATION)
        private val DELETION_EVENTS = setOf(GeneEvent.DELETION)

        fun isAmplificationEvent(geneEvent: GeneEvent): Boolean {
            return AMPLIFICATION_EVENTS.contains(geneEvent)
        }

        fun isDeletionEvent(geneEvent: GeneEvent): Boolean {
            return DELETION_EVENTS.contains(geneEvent)
        }

        fun isAmplificationMatch(actionableGene: ActionableGene, copyNumber: CopyNumber): Boolean {
            return (copyNumber.canonicalImpact.type == CopyNumberType.FULL_GAIN
                    || copyNumber.canonicalImpact.type == CopyNumberType.PARTIAL_GAIN)
                    && copyNumber.gene == actionableGene.gene()
        }

        fun isDeletionMatch(actionableGene: ActionableGene, copyNumber: CopyNumber): Boolean {
            return copyNumber.canonicalImpact.type == CopyNumberType.DEL
                    && copyNumber.gene == actionableGene.gene()
        }
    }
}
