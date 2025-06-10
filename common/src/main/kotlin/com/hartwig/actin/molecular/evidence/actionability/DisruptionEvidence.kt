package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.Disruption
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class DisruptionEvidence {
    
    companion object {
        private val DISRUPTION_EVENTS = setOf(GeneEvent.ANY_MUTATION)

        fun isDisruptionEvent(geneEvent: GeneEvent): Boolean {
            return DISRUPTION_EVENTS.contains(geneEvent)
        }

        fun isDisruptionMatch(actionableGene: ActionableGene, disruption: Disruption): Boolean {
            return disruption.isReportable && disruption.gene == actionableGene.gene() && disruption.geneRole != GeneRole.TSG
        }
    }
}
