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
        val statusPerReceptor = receptorsToConsider.associateWith { receptor ->
            val targetMolecularTests = record.ihcTests.filter { it.item == receptor.display() }
            val ihcTestSummary = breastCancerReceptorsEvaluator.summarizeTests(targetMolecularTests, receptor)
            val positiveArguments = breastCancerReceptorsEvaluator.positiveArguments(ihcTestSummary, tumorDoids!!, receptor)
            val negativeArguments = breastCancerReceptorsEvaluator.negativeArguments(ihcTestSummary, tumorDoids, receptor)
            breastCancerReceptorsEvaluator.receptorIsPositive(positiveArguments, negativeArguments)
        }

        val erbb2Amplified = geneIsAmplifiedForPatient("ERBB2", record)
        val prAndErNotPositive = (statusPerReceptor[ReceptorType.ER] != true) && (statusPerReceptor[ReceptorType.PR] != true)

        return when {
            !breastCancerReceptorsEvaluator.isBreastCancer(tumorDoids!!) -> EvaluationFactory.fail("Has no triple negative breast cancer")

            prAndErNotPositive && erbb2Amplified && statusPerReceptor[ReceptorType.HER2] == false -> EvaluationFactory.undetermined("Undetermined if triple negative breast cancer (DOID/IHC data inconsistent with ERBB2 gene amp)")

            prAndErNotPositive && erbb2Amplified && statusPerReceptor[ReceptorType.HER2] == null -> EvaluationFactory.undetermined("Undetermined if triple negative breast cancer (IHC HER2 data missing but ERBB2 amp so probably not triple negative)")

            statusPerReceptor.values.all { it == false } -> EvaluationFactory.pass("Has triple negative breast cancer")

            statusPerReceptor.values.contains(true) -> EvaluationFactory.fail("Has no triple negative breast cancer")

            else -> EvaluationFactory.undetermined("Undetermined if triple negative breast cancer")
        }
    }
}