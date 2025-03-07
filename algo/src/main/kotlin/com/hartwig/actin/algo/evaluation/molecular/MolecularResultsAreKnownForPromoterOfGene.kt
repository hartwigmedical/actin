package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.PriorIHCTest

class MolecularResultsAreKnownForPromoterOfGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminatePriorTests, validPriorTests) = record.priorIHCTests
            .filter { it.item?.contains(gene) ?: false && it.item?.lowercase()?.contains(PROMOTER) ?: false }
            .partition(PriorIHCTest::impliesPotentialIndeterminateStatus)

        if (validPriorTests.isNotEmpty()) {
            return EvaluationFactory.pass("$gene promoter tested in prior molecular test")
        } else if (indeterminatePriorTests.isNotEmpty()) {
            return EvaluationFactory.undetermined("$gene promoter tested in prior molecular test but indeterminate status")
        }
        return EvaluationFactory.recoverableFail("$gene not tested", isMissingMolecularResultForEvaluation = true)
    }

    companion object {
        const val PROMOTER = "promoter"
    }
}