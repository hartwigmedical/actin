package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasTripleNegativeBreastCancer(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined if triple negative breast cancer (tumor doids missing)")
        }

        val breastCancerReceptorsEvaluator = BreastCancerReceptorsEvaluator(doidModel)
        val receptorsToConsider = listOf(ReceptorType.ER, ReceptorType.PR, ReceptorType.HER2)
        val evaluationPerReceptor = receptorsToConsider.associateWith { receptor ->
            breastCancerReceptorsEvaluator.evaluate(tumorDoids!!, record.ihcTests, receptor)
        }

        val erbb2Amplified = geneIsAmplifiedForPatient("ERBB2", record)
        val prAndErNotPositive =
            (evaluationPerReceptor[ReceptorType.ER] != BreastCancerReceptorEvaluation.POSITIVE) && (evaluationPerReceptor[ReceptorType.PR] != BreastCancerReceptorEvaluation.POSITIVE)
        val hasNoTripleNegativeBreastCancer =
            evaluationPerReceptor.values.contains(BreastCancerReceptorEvaluation.NOT_BREAST_CANCER) || evaluationPerReceptor.values.contains(
                BreastCancerReceptorEvaluation.POSITIVE
            )
        val allReceptorsNegative = evaluationPerReceptor.values.all { it == BreastCancerReceptorEvaluation.NEGATIVE }

        return when {
            hasNoTripleNegativeBreastCancer -> EvaluationFactory.fail("Has no triple negative breast cancer")

            allReceptorsNegative && erbb2Amplified -> EvaluationFactory.undetermined("Undetermined if triple negative breast cancer (DOID/IHC data inconsistent with ERBB2 gene amp)")

            allReceptorsNegative -> EvaluationFactory.pass("Has triple negative breast cancer")

            prAndErNotPositive && erbb2Amplified && evaluationPerReceptor[ReceptorType.HER2] != BreastCancerReceptorEvaluation.NEGATIVE -> EvaluationFactory.undetermined(
                "Undetermined if triple negative breast cancer (IHC HER2 data missing but ERBB2 amp so potentially not triple negative)"
            )

            else -> EvaluationFactory.undetermined("Undetermined if triple negative breast cancer")
        }
    }
}