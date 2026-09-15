package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.medication.MedicationToTreatmentConverter

class HasHadAnyCancerTreatment(
    private val categoriesToIgnore: Set<TreatmentCategory>,
    private val atcLevelsToFind: Set<AtcLevel>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistoryWithoutTrialMedication = MedicationToTreatmentConverter.convertAndCombine(
            record.medications?.filter { (it.allLevels() intersect atcLevelsToFind).isNotEmpty() },
            record.oncologicalHistory
        )

        val hasHadPriorCancerTreatment =
            if (categoriesToIgnore.isEmpty()) {
                effectiveTreatmentHistoryWithoutTrialMedication.isNotEmpty()
            } else {
                effectiveTreatmentHistoryWithoutTrialMedication.any { it.categories().any { category -> category !in categoriesToIgnore } }
            }

        val hasHadTrial =
            effectiveTreatmentHistoryWithoutTrialMedication.any { it.isTrial } || record.medications?.any { it.isTrialMedication } == true

        return when {
            hasHadPriorCancerTreatment -> {
                EvaluationFactory.pass("Prior cancer treatment(s) in provided treatments")
            }

            hasHadTrial -> {
                EvaluationFactory.undetermined("Undetermined if trial treatment in provided treatments included cancer treatment")
            }

            else -> {
                EvaluationFactory.fail("No prior cancer treatment in provided treatments")
            }
        }
    }
}