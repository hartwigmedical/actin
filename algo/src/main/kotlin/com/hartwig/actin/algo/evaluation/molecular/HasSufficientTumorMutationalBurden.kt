package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class HasSufficientTumorMutationalBurden internal constructor(private val minTumorMutationalBurden: Double) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorMutationalBurden = record.molecular().characteristics().tumorMutationalBurden()
            ?: return unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Unknown tumor mutational burden (TMB)")
                .addFailGeneralMessages("Unknown TMB")
                .build()
        val tumorMutationalBurdenIsAllowed = tumorMutationalBurden >= minTumorMutationalBurden
        if (tumorMutationalBurdenIsAllowed) {
            return unrecoverable()
                .result(EvaluationResult.PASS)
                .addPassSpecificMessages("TMB of sample $tumorMutationalBurden is sufficient")
                .addPassGeneralMessages("Adequate TMB")
                .addInclusionMolecularEvents(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_BURDEN)
                .build()
        }
        val tumorMutationalBurdenIsAlmostAllowed = minTumorMutationalBurden - tumorMutationalBurden <= 0.5
        val hasSufficientQuality = record.molecular().hasSufficientQuality()
        return if (tumorMutationalBurdenIsAlmostAllowed && !hasSufficientQuality) {
            unrecoverable()
                .result(EvaluationResult.WARN)
                .addWarnSpecificMessages(
                    "TMB of sample " + tumorMutationalBurden + " almost exceeds " + minTumorMutationalBurden
                            + " while data quality is insufficient (perhaps a few mutations are missed)"
                )
                .addWarnGeneralMessages("Inadequate TMB")
                .addInclusionMolecularEvents(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN)
                .build()
        } else unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("TMB of sample $tumorMutationalBurden is not within specified range")
            .addFailGeneralMessages("Inadequate TMB")
            .build()
    }
}