package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest

class MolecularResultsAreKnownForPromoterOfGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminatePriorTests, determinatePriorTests) = record.ihcTests
            .filter { it.item.contains(gene) && it.item.lowercase().contains("promoter") }
            .partition(IhcTest::impliesPotentialIndeterminateStatus)

        return when {
            determinatePriorTests.isNotEmpty() -> EvaluationFactory.pass("Results for $gene promoter are available by IHC")

            indeterminatePriorTests.isNotEmpty() -> EvaluationFactory.warn("Test for $gene promoter was done by IHC but indeterminate status")

            else -> EvaluationFactory.recoverableFail("$gene promoter status not tested", isMissingMolecularResultForEvaluation = true)
        }
    }
}