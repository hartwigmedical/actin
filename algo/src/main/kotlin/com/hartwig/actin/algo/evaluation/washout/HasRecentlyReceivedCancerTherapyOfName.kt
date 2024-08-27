package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import java.time.LocalDate

class HasRecentlyReceivedCancerTherapyOfName(
    private val namesToFind: Set<Drug>, private val interpreter: MedicationStatusInterpreter, private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val lowercaseNamesToFind = namesToFind.map { it.name.lowercase() }.toSet()
        val namesFound = medications
            .filter {
                lowercaseNamesToFind.contains(it.name.lowercase()) && interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE
            }
            .map(Medication::name)

        val drugInTreatmentHistoryEvaluation = TreatmentSinceDateFunctions.evaluateTreatmentMatchingPredicateSinceDate(
            record, minDate, "containing '${Format.concatItemsWithOr(namesToFind)}'"
        ) { it is DrugTreatment && it.drugs.intersect(namesToFind).isNotEmpty() }

        return when {
            namesFound.isNotEmpty() || drugInTreatmentHistoryEvaluation.result == EvaluationResult.PASS -> {
                EvaluationFactory.pass(
                    "Patient has recently received treatment with medication " + Format.concatItemsWithOr(namesToFind) + " - pay attention to washout period",
                    "Has recently received treatment with medication " + Format.concatItemsWithOr(namesToFind) + " - pay attention to washout period"
                )
            }

            drugInTreatmentHistoryEvaluation.result == EvaluationResult.UNDETERMINED -> {
                return drugInTreatmentHistoryEvaluation
            }

            else -> {
                EvaluationFactory.fail(
                "Patient has not received recent treatments with name " + Format.concatItemsWithOr(namesToFind),
                "Has not received recent treatments with name " + Format.concatItemsWithOr(namesToFind)
            )
            }
            }
    }
}