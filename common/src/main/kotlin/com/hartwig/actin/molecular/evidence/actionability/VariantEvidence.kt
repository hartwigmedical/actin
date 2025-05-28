package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.molecular.evidence.matching.GeneMatching
import com.hartwig.actin.molecular.evidence.matching.HotspotMatching
import com.hartwig.actin.molecular.evidence.matching.RangeMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class VariantEvidence {

    companion object {

        private val VARIANT_GENE_EVENTS = setOf(GeneEvent.ACTIVATION, GeneEvent.INACTIVATION, GeneEvent.ANY_MUTATION)

        fun isGeneEventEligible(gene: ActionableGene): Boolean =
            VARIANT_GENE_EVENTS.contains(gene.event())

        fun isVariantEligible(variant: Variant): Boolean =
            variant.isReportable && variant.driverLikelihood == DriverLikelihood.HIGH
    }
}