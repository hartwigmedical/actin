package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IhcTest

class MolecularResultsAreKnownForPromoterOfGene(private val gene: String, private val labels: EvaluationLabels.Molecular) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (indeterminatePriorTests, determinatePriorTests) = record.ihcTests
            .filter { it.item.contains(gene) && it.item.lowercase().contains("promoter") }
            .partition(IhcTest::impliesPotentialIndeterminateStatus)

        return when {
            determinatePriorTests.isNotEmpty() -> EvaluationFactory.pass(labels.molecularResultsAreKnownForPromoterOfGenePass(gene))

            indeterminatePriorTests.isNotEmpty() -> EvaluationFactory.warn(labels.molecularResultsAreKnownForPromoterOfGeneWarn(gene))

            else -> EvaluationFactory.recoverableFail(labels.molecularResultsAreKnownForPromoterOfGeneRecoverableFail(gene))
        }
    }
}