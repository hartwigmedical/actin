package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent

class IsEligibleForFirstLinePalliativeChemotherapy : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val palliativeTreatments = record.oncologicalHistory.filter { it.intents?.any { intent -> intent == Intent.PALLIATIVE } == true }
        val categoriesList = Format.concatItemsWithAnd(palliativeTreatments.flatMap { it.categories() }.toSet())
        val hasUnresectableOrMetastaticCancer = record.tumor.stage == TumorStage.IV

        return when {
            palliativeTreatments.any { treatment -> treatment.categories().contains(TreatmentCategory.CHEMOTHERAPY) } -> {
                EvaluationFactory.fail(
                    "Patient has already had palliative chemotherapy and is hence not eligible for first line palliative chemotherapy",
                    "Patient has already had palliative chemotherapy"
                )
            }

            palliativeTreatments.isNotEmpty() && hasUnresectableOrMetastaticCancer -> {
                EvaluationFactory.undetermined(
                    "Patient has already had palliative $categoriesList and hence may not be eligible for first line palliative chemotherapy",
                    "Patient has already had palliative $categoriesList (hence may not be eligible for first line palliative chemotherapy)"
                )
            }

            hasUnresectableOrMetastaticCancer -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient with metastatic disease is eligible for first line palliative chemotherapy",
                    "Undetermined eligibility for first line palliative chemotherapy"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has no metastatic/unresectable cancer and is hence not eligible for first line palliative chemotherapy",
                    "No metastatic/unresectable cancer and hence no eligibility for first line palliative chemotherapy"
                )
            }
        }
    }
}