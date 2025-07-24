package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest

class MolecularResultsAreKnownForPromoterOfGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminatePriorTests, validPriorTests) = record.ihcTests
            .filter { it.item.contains(gene) && it.item.lowercase().contains("promoter") }
            .partition(IhcTest::impliesPotentialIndeterminateStatus)

        if (validPriorTests.isNotEmpty()) {
            return EvaluationFactory.pass("Results for $gene promoter are available by IHC")
        } else if (indeterminatePriorTests.isNotEmpty()) {
            return EvaluationFactory.warn("Test for $gene promoter was done by IHC but indeterminate status")
        }
        return EvaluationFactory.undetermined("$gene promoter status not tested", isMissingMolecularResultForEvaluation = true)
    }
}