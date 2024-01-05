package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.AtcLevel

class CurrentlyGetsMedicationOfAtcLevel(
    private val selector: MedicationSelector,
    private val categoryName: String,
    private val categoryAtcLevels: Set<AtcLevel>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = selector.active(record.clinical.medications)
            .filter { (it.allLevels() intersect categoryAtcLevels).isNotEmpty() }

        val foundMedicationNames = medications.map { it.name }.filter { it.isNotEmpty() }

        return if (medications.isNotEmpty()) {
            val foundMedicationString = if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
            EvaluationFactory.recoverablePass(
                "Patient currently gets medication$foundMedicationString which belong(s) to category '$categoryName'",
                "$categoryName medication use$foundMedicationString"
            )
        } else
            EvaluationFactory.recoverableFail(
                "Patient currently does not get medication of category '$categoryName'",
                "No $categoryName medication use"
            )
    }
}