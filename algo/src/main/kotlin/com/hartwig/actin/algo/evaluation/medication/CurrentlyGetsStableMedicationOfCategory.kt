package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.AtcLevel


class CurrentlyGetsStableMedicationOfCategory(
    private val selector: MedicationSelector,
    private val categoriesToFind: Map<String, Set<AtcLevel>>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val categoryNamesToFind = categoriesToFind.keys
        var hasFoundOnePassingCategory = false
        for (categoryToFind in categoriesToFind) {
            var hasActiveAndStableMedication = false
            var referenceDosing: Medication? = null
            val filtered =
                selector.active(record.clinical().medications())
                    .filter { (allLevels(it) intersect categoryToFind.value).isNotEmpty() }
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
                "Patient gets stable dosing of medication with category " + concatLowercaseWithAnd(categoryNamesToFind),
                "Stable dosing of " + concatLowercaseWithAnd(categoryNamesToFind)
            )
        } else {
            EvaluationFactory.fail(
                "Patient does not get stable dosing of medication with category " + concatLowercaseWithAnd(categoryNamesToFind),
                "No stable dosing of " + concatLowercaseWithAnd(categoryNamesToFind)
            )
        }
    }

    private fun allLevels(it: Medication) = it.atc()?.allLevels() ?: emptySet<AtcLevel>()
}