package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.ReceptorType
import com.hartwig.actin.doid.DoidModel

class HasTripleNegativeBreastCancer(
    private val doidModel: DoidModel,
    private val molecularLabels: EvaluationLabels.Molecular,
    private val tumorLabels: EvaluationLabels.Tumor
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(tumorLabels.hasTripleNegativeBreastCancerUndeterminedMissingDoids())
        }

        val breastCancerReceptorsEvaluator = BreastCancerReceptorsEvaluator(doidModel)
        val receptorsToConsider = listOf(ReceptorType.ER, ReceptorType.PR, ReceptorType.HER2)
        val evaluationPerReceptor = receptorsToConsider.associateWith { receptor ->
            breastCancerReceptorsEvaluator.evaluate(tumorDoids!!, record.ihcTests, receptor)
        }

        val erbb2Amplified = geneIsAmplifiedForPatient("ERBB2", record, molecularLabels)
        val prAndErNotPositive =
            (evaluationPerReceptor[ReceptorType.ER] != BreastCancerReceptorEvaluation.POSITIVE) && (evaluationPerReceptor[ReceptorType.PR] != BreastCancerReceptorEvaluation.POSITIVE)
        val hasNoTripleNegativeBreastCancer =
            evaluationPerReceptor.values.contains(BreastCancerReceptorEvaluation.NOT_BREAST_CANCER) || evaluationPerReceptor.values.contains(
                BreastCancerReceptorEvaluation.POSITIVE
            )
        val her2NegativeOrLow = breastCancerReceptorsEvaluator.isNegativeOrLow(evaluationPerReceptor[ReceptorType.HER2]!!)
        val allReceptorsNegativeOrLow = evaluationPerReceptor.values.all { breastCancerReceptorsEvaluator.isNegativeOrLow(it) }
        val allReceptorsNegativeOrHer2Low = listOf(
            ReceptorType.ER,
            ReceptorType.PR
        ).all { evaluationPerReceptor[it] == BreastCancerReceptorEvaluation.NEGATIVE } && her2NegativeOrLow

        return when {
            hasNoTripleNegativeBreastCancer -> EvaluationFactory.fail(tumorLabels.hasTripleNegativeBreastCancerFail())

            allReceptorsNegativeOrLow && erbb2Amplified -> EvaluationFactory.undetermined(
                tumorLabels.hasTripleNegativeBreastCancerUndeterminedErbb2Inconsistent()
            )

            allReceptorsNegativeOrHer2Low && !erbb2Amplified -> EvaluationFactory.pass(tumorLabels.hasTripleNegativeBreastCancerPass())

            allReceptorsNegativeOrLow -> EvaluationFactory.undetermined(tumorLabels.hasTripleNegativeBreastCancerUndeterminedIhcLow())

            prAndErNotPositive && erbb2Amplified && evaluationPerReceptor[ReceptorType.HER2] != BreastCancerReceptorEvaluation.NEGATIVE -> EvaluationFactory.undetermined(
                tumorLabels.hasTripleNegativeBreastCancerUndeterminedHer2Missing()
            )

            else -> EvaluationFactory.undetermined(tumorLabels.hasTripleNegativeBreastCancerUndetermined())
        }
    }
}