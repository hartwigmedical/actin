package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.HomozygousDisruption
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class HomozygousDisruptionEvidence {

    companion object {
        private val HOMOZYGOUS_DISRUPTION_EVENTS = setOf(GeneEvent.DELETION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun isHomozygousDisruptionEvent(geneEvent: GeneEvent): Boolean {
            return HOMOZYGOUS_DISRUPTION_EVENTS.contains(geneEvent)
        }

        fun isHomozygousDisruptionMatch(actionableGene: ActionableGene, disruption: HomozygousDisruption): Boolean {
            // TODO we don't check isReportable here, should we?
            return disruption.gene == actionableGene.gene()
        }
    }
}