package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.Fusion
import com.hartwig.actin.datamodel.molecular.driver.FusionDriverType
import com.hartwig.actin.molecular.evidence.matching.FusionMatching
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MolecularCriterium
import com.hartwig.serve.datamodel.molecular.fusion.ActionableFusion
import com.hartwig.serve.datamodel.molecular.gene.ActionableGene
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import com.hartwig.serve.datamodel.trial.ActionableTrial
import java.util.function.Predicate

class FusionEvidence {

    companion object {

        fun isPromiscuousFusionEvent(geneEvent: GeneEvent): Boolean {
            return geneEvent == GeneEvent.FUSION || geneEvent == GeneEvent.ACTIVATION || geneEvent == GeneEvent.ANY_MUTATION
        }

        fun isFusionMatch(actionable: ActionableFusion, fusion: Fusion): Boolean {
            return fusion.isReportable && FusionMatching.isGeneMatch(actionable, fusion) && FusionMatching.isExonMatch(actionable, fusion)
        }

        fun isPromiscuousMatch(actionable: ActionableGene, fusion: Fusion): Boolean {
            if (!fusion.isReportable) {
                return false
            }

            return when (fusion.driverType) {
                FusionDriverType.PROMISCUOUS_3 -> {
                    actionable.gene() == fusion.geneEnd
                }

                FusionDriverType.PROMISCUOUS_5 -> {
                    actionable.gene() == fusion.geneStart
                }

                else -> {
                    actionable.gene() == fusion.geneStart || actionable.gene() == fusion.geneEnd
                }
            }
        }
    }
}
