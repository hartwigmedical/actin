package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class IsEligibleForFirstLinePalliativeChemotherapy(
    private val hasMetastaticCancer: HasMetastaticCancer,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val palliativeTreatments = record.oncologicalHistory.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val categoriesList = Format.concatItemsWithAnd(palliativeTreatments.flatMap { it.categories() })
        val hasMetastaticCancerResult = hasMetastaticCancer.evaluate(record).result

        return when {
            hasMetastaticCancerResult == EvaluationResult.FAIL -> {
                EvaluationFactory.fail(labels.isEligibleForFirstLinePalliativeChemotherapyFailNoMetastatic())
            }

            palliativeTreatments.any { treatment -> treatment.categories().contains(TreatmentCategory.CHEMOTHERAPY) } -> {
                EvaluationFactory.fail(labels.isEligibleForFirstLinePalliativeChemotherapyFailHadChemo())
            }

            palliativeTreatments.isNotEmpty() && hasMetastaticCancerResult == EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(
                    labels.isEligibleForFirstLinePalliativeChemotherapyUndeterminedHadPalliative(categoriesList)
                )
            }

            hasMetastaticCancerResult == EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(labels.isEligibleForFirstLinePalliativeChemotherapyUndeterminedMetastatic())
            }

            else -> {
                EvaluationFactory.undetermined(labels.isEligibleForFirstLinePalliativeChemotherapyUndetermined())
            }
        }
    }
}