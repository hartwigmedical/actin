package com.hartwig.actin.algo.soc

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.EligibilityRule

object MolecularDecisions {
    val brafV600EMutation = EligibilityFunction(EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y, listOf("BRAF", "V600E"))
    val rasWildTypeAndLeftSided =
        listOf("KRAS", "NRAS").map { EligibilityFunction(EligibilityRule.WILDTYPE_OF_GENE_X, listOf(it)) } + EligibilityFunction(
            EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR
        )
    val ntrkFusion = EligibilityFunction(EligibilityRule.OR, listOf("NTRK1", "NTRK2", "NTRK3").map {
        EligibilityFunction(EligibilityRule.FUSION_IN_GENE_X, listOf(it))
    })

    val nonWildTypeMolecularDecisions = listOf(ntrkFusion,brafV600EMutation)
}