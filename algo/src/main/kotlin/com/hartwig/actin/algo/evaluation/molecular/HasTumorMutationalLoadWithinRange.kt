package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.util.MolecularCharacteristicEvents

class HasTumorMutationalLoadWithinRange internal constructor(
    private val minTumorMutationalLoad: Int,
    private val maxTumorMutationalLoad: Int?
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorMutationalLoad = record.molecular().characteristics().tumorMutationalLoad()
            ?: return unrecoverable()
                .result(EvaluationResult.FAIL)
                .addFailSpecificMessages("Unknown tumor mutational load (TML)")
                .addFailGeneralMessages("TML unknown")
                .build()
        val meetsMinTumorLoad = tumorMutationalLoad >= minTumorMutationalLoad
        val meetsMaxTumorLoad = maxTumorMutationalLoad == null || tumorMutationalLoad <= maxTumorMutationalLoad
        val tumorMutationalLoadIsAllowed = meetsMinTumorLoad && meetsMaxTumorLoad
        if (tumorMutationalLoadIsAllowed) {
            return if (maxTumorMutationalLoad == null) {
                unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                        "Tumor mutational load (TML) of sample " + tumorMutationalLoad + " is higher than requested minimal TML of "
                                + minTumorMutationalLoad
                    )
                    .addPassGeneralMessages("Adequate TML")
                    .addInclusionMolecularEvents(MolecularCharacteristicEvents.HIGH_TUMOR_MUTATIONAL_LOAD)
                    .build()
            } else {
                unrecoverable()
                    .result(EvaluationResult.PASS)
                    .addPassSpecificMessages(
                        "Tumor mutational load (TML) of sample " + tumorMutationalLoad + " is between requested TML range of " + minTumorMutationalLoad
                                + " - " + maxTumorMutationalLoad
                    )
                    .addPassGeneralMessages("Adequate TML")
                    .addInclusionMolecularEvents(MolecularCharacteristicEvents.ADEQUATE_TUMOR_MUTATIONAL_LOAD)
                    .build()
            }
        }
        val tumorMutationalLoadIsAlmostAllowed = minTumorMutationalLoad - tumorMutationalLoad <= 5
        return if (tumorMutationalLoadIsAlmostAllowed && record.molecular().hasSufficientQuality() && !record.molecular()
                .hasSufficientQualityAndPurity()
        ) {
            unrecoverable()
                .result(EvaluationResult.WARN)
                .addWarnSpecificMessages(
                    "Tumor mutational load (TML) of sample " + tumorMutationalLoad + " almost exceeds " + minTumorMutationalLoad
                            + " while purity is low: perhaps a few mutations are missed and TML is adequate"
                )
                .addWarnGeneralMessages("TML almost sufficient with low purity")
                .addInclusionMolecularEvents(MolecularCharacteristicEvents.ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD)
                .build()
        } else unrecoverable()
            .result(EvaluationResult.FAIL)
            .addFailSpecificMessages("Tumor mutational load (TML) of sample $tumorMutationalLoad is not within specified range")
            .addFailGeneralMessages("Inadequate TML")
            .build()
    }
}