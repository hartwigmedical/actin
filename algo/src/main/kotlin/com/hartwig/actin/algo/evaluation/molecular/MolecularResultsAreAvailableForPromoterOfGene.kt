package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest

class MolecularResultsAreAvailableForPromoterOfGene(private val gene: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminatePriorTests, validPriorTests) = record.molecularHistory.allIHCTests()
            .filter { it.item.contains(gene) && it.item.lowercase().contains(PROMOTER) }
            .partition(PriorMolecularTest::impliesPotentialIndeterminateStatus)

        if (validPriorTests.isNotEmpty()) {
            return EvaluationFactory.pass("$gene promoter has been tested in a prior molecular test", "$gene promoter tested before")
        } else if (indeterminatePriorTests.isNotEmpty()) {
            return EvaluationFactory.undetermined(
                "$gene promoter has been tested in a prior molecular test but with indeterminate status",
                "$gene promoter tested before but indeterminate status"
            )
        }
        return EvaluationFactory.recoverableFail("$gene has not been tested", "$gene not tested")
    }

    companion object {
        const val PROMOTER = "promoter"
    }
}