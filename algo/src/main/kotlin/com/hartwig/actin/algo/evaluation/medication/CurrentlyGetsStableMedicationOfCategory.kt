package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.Medication

class CurrentlyGetsStableMedicationOfCategory(
    private val selector: MedicationSelector,
    private val categoriesToFind: Map<String, Set<AtcLevel>>,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val categoryNamesToFind = categoriesToFind.keys
        var hasFoundOnePassingCategory = false
        for (categoryToFind in categoriesToFind) {
            var hasActiveAndStableMedication = false
            var referenceDosing: Medication? = null
            val filtered =
                selector.active(medications)
                    .filter { (it.allLevels() intersect categoryToFind.value).isNotEmpty() }
            for (medication in filtered) {
                if (referenceDosing != null) {
                    if (!MedicationDosage.hasMatchingDosing(medication.dosage, referenceDosing.dosage)) {
                        hasActiveAndStableMedication = false
                    }
                } else {
                    hasActiveAndStableMedication = true
                    referenceDosing = medication
                }
            }
            if (hasActiveAndStableMedication) {
                hasFoundOnePassingCategory = true
            }
        }

        return if (hasFoundOnePassingCategory) {
            EvaluationFactory.recoverablePass(labels.currentlyGetsStableMedicationOfCategoryRecoverablePass(concatLowercaseWithAnd(categoryNamesToFind)))
        } else {
            EvaluationFactory.recoverableFail(labels.currentlyGetsStableMedicationOfCategoryRecoverableFail(concatLowercaseWithAnd(categoryNamesToFind)))
        }
    }
}