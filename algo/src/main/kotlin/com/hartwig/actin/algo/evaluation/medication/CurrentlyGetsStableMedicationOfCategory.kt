package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Medication

class CurrentlyGetsStableMedicationOfCategory(
    private val selector: MedicationSelector,
    private val categoriesToFind: Map<String, Set<AtcLevel>>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return medicationWhenProvidedEvaluation(record) { medications ->
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

            if (hasFoundOnePassingCategory) {
                EvaluationFactory.recoverablePass(
                    "Patient gets stable dosing of medication with category " + concatLowercaseWithAnd(categoryNamesToFind),
                    "Stable dosing of " + concatLowercaseWithAnd(categoryNamesToFind)
                )
            } else {
                EvaluationFactory.recoverableFail(
                    "Patient does not get stable dosing of medication with category " + concatLowercaseWithAnd(categoryNamesToFind),
                    "No stable dosing of " + concatLowercaseWithAnd(categoryNamesToFind)
                )
            }
        }
    }
}