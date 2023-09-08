package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.Medication

class CurrentlyGetsMedicationOfAtcLevel(
    private val selector: MedicationSelector,
    private val categoryName: String,
    private val categoryAtcLevels: Set<AtcLevel>
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        val medications =
            selector.active(record.clinical().medications())
                .filter { (allLevels(it) intersect categoryAtcLevels).isNotEmpty() }

        val foundMedicationNames = medications.map { it.name() }.filter { it.isNotEmpty() }

        return if (medications.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
            EvaluationFactory.pass(
                "Patient currently gets medication$foundMedicationString which belong(s) to category $categoryName",
                "$categoryName medication use"
            )
        } else
            EvaluationFactory.fail(
                "Patient currently does not get medication of category $categoryName",
                "No $categoryName medication use"
            )
    }

    private fun allLevels(it: Medication) = it.atc()?.allLevels() ?: emptySet<AtcLevel>()
}