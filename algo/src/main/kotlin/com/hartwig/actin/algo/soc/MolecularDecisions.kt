package com.hartwig.actin.algo.soc

import com.hartwig.actin.datamodel.trial.EligibilityFunction
import com.hartwig.actin.datamodel.trial.FunctionParameter
import com.hartwig.actin.datamodel.trial.GeneParameter
import com.hartwig.actin.datamodel.trial.ManyProteinImpactsParameter
import com.hartwig.actin.trial.input.EligibilityRule

object MolecularDecisions {

    val brafV600EMutation = EligibilityFunction(
        EligibilityRule.MUTATION_IN_GENE_X_OF_ANY_PROTEIN_IMPACTS_Y.name,
        listOf(
            GeneParameter("BRAF"),
            ManyProteinImpactsParameter(setOf("V600E"))
        )
    )
    val rasWildTypeAndLeftSided = listOf("KRAS", "NRAS").map {
        FunctionParameter(
            EligibilityFunction(EligibilityRule.WILDTYPE_OF_GENE_X.name, listOf(GeneParameter(it)))
        )
    } + FunctionParameter(EligibilityFunction(EligibilityRule.HAS_LEFT_SIDED_COLORECTAL_TUMOR.name))
    val ntrkFusion = EligibilityFunction(
        EligibilityRule.OR.name,
        listOf("NTRK1", "NTRK2", "NTRK3").map {
            FunctionParameter(EligibilityFunction(EligibilityRule.FUSION_IN_GENE_X.name, listOf(GeneParameter(it))))
        }
    )

    val nonWildTypeMolecularDecisions = listOf(ntrkFusion, brafV600EMutation)
}
