package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.TestResult
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasTripleNegativeBreastCancer(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tripleNegativeReceptors = setOf(ReceptorType.ER, ReceptorType.PR, ReceptorType.HER2)
        val tumorDoids = record.tumor.doids
        val expandedDoidSet = DoidEvaluationFunctions.createFullExpandedDoidTree(doidModel, tumorDoids)

        val anyIndicationForPositiveReceptor = tripleNegativeReceptors.map { receptor ->
            val targetMolecularTests = record.ihcTests.filter { it.item in receptor.display() }
            val ihcTestSummary = BreastCancerReceptorFunctions.summarizeTests(targetMolecularTests, receptor)
            val targetReceptorPositiveInDoids =
                expandedDoidSet.contains(BreastCancerReceptorFunctions.POSITIVE_DOID_MOLECULAR_COMBINATION[receptor])
            val targetReceptorNegativeInDoids =
                expandedDoidSet.contains(BreastCancerReceptorFunctions.NEGATIVE_DOID_MOLECULAR_COMBINATION[receptor])
            val positiveArguments = TestResult.POSITIVE in ihcTestSummary || targetReceptorPositiveInDoids
            val negativeArguments = TestResult.NEGATIVE in ihcTestSummary || targetReceptorNegativeInDoids
            when {
                positiveArguments && !negativeArguments -> true
                negativeArguments && !positiveArguments -> false
                else -> null
            }
        }

        return when {
            tumorDoids.isNullOrEmpty() -> {
                EvaluationFactory.undetermined("Undetermined if triple negative breast cancer (tumor doids missing)")
            }

            expandedDoidSet.contains(DoidConstants.TRIPLE_NEGATIVE_BREAST_CANCER_DOID) -> EvaluationFactory.pass("Has triple negative breast cancer")

            anyIndicationForPositiveReceptor.contains(true) -> EvaluationFactory.fail("Has no triple negative breast cancer")

            anyIndicationForPositiveReceptor.contains(null) -> EvaluationFactory.undetermined("Undetermined if triple negative breast cancer")

            else -> EvaluationFactory.fail("Has no triple negative breast cancer")
        }
    }
}
