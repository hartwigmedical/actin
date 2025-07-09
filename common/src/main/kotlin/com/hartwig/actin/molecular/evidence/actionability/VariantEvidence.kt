package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent

class VariantEvidence {

    companion object {
        private val VARIANT_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun isGeneEventEligible(gene: ActionableGene): Boolean =
            VARIANT_GENE_EVENTS.contains(gene.event()) || MmrFunctions.isMmrAbsenceOfProteinEvent(gene.event(), gene.gene())

        fun isVariantEligible(variant: Variant): Boolean =
            variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH
    }
}