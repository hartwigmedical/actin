package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.HasMetastaticCancer
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent

class IsEligibleForFirstLinePalliativeChemotherapy(private val hasMetastaticCancer: HasMetastaticCancer) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val palliativeTreatments = record.oncologicalHistory.filter { it.intents?.contains(Intent.PALLIATIVE) == true }
        val categoriesList = Format.concatItemsWithAnd(palliativeTreatments.flatMap { it.categories() })
        val hasMetastaticCancerResult = hasMetastaticCancer.evaluate(record).result

        return when {
            hasMetastaticCancerResult == EvaluationResult.FAIL -> {
                EvaluationFactory.fail(
                    "Patient has no metastatic cancer and is hence not eligible for first line palliative chemotherapy",
                    "No metastatic cancer and hence no eligibility for first line palliative chemotherapy"
                )
            }

            palliativeTreatments.any { treatment -> treatment.categories().contains(TreatmentCategory.CHEMOTHERAPY) } -> {
                EvaluationFactory.fail(
                    "Patient has already had palliative chemotherapy and is hence not eligible for first line palliative chemotherapy",
                    "Patient already had palliative chemotherapy"
                )
            }

            palliativeTreatments.isNotEmpty() && hasMetastaticCancerResult == EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(
                    "Patient has had palliative $categoriesList and hence may not be considered eligible for first line palliative chemotherapy",
                    "Patient had palliative $categoriesList (hence may not be considered eligible for first line palliative chemotherapy)"
                )
            }

            hasMetastaticCancerResult == EvaluationResult.PASS -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient with metastatic disease is considered eligible for first line palliative chemotherapy",
                    "Undetermined if considered eligible for first line palliative chemotherapy"
                )
            }

            else -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has metastatic cancer and hence may not be eligible for first line palliative chemotherapy",
                    "Undetermined if metastatic cancer (hence may not be eligible for first line palliative chemotherapy)"
                )
            }
        }
    }
}