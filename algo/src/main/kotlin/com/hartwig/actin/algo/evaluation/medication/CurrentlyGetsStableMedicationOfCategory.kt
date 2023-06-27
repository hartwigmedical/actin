package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.Medication

class CurrentlyGetsStableMedicationOfCategory internal constructor(
    private val selector: MedicationSelector,
    private val categoriesToFind: Set<String>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasFoundOnePassingCategory = false
        for (categoryToFind in categoriesToFind) {
            var hasActiveAndStableMedication = false
            var referenceDosing: Medication? = null
            val filtered = selector.activeWithExactCategory(record.clinical().medications(), categoryToFind)
            for (medication in filtered) {
                if (referenceDosing != null) {
                    if (!MedicationDosage.hasMatchingDosing(medication.dosage(), referenceDosing.dosage())) {
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
            EvaluationFactory.pass(
                "Patient gets stable dosing of medication with category " + concat(categoriesToFind),
                "Stable dosing of " + concat(categoriesToFind)
            )
        } else {
            EvaluationFactory.fail(
                "Patient does not get stable dosing of medication with category " + concat(categoriesToFind),
                "No stable dosing of " + concat(categoriesToFind)
            )
        }
    }
}