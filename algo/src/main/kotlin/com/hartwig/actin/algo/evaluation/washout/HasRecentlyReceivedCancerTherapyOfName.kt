package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import java.time.LocalDate

class HasRecentlyReceivedCancerTherapyOfName(
    private val namesToFind: Set<Drug>, private val interpreter: MedicationStatusInterpreter, private val minDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val lowercaseNamesToFind = namesToFind.map { it.name.lowercase() }.toSet()
        val medicationsFound = medications
            .filter {
                lowercaseNamesToFind.contains(it.name.lowercase()) && interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE
            }
            .map { it.drug?.name ?: it.name }.toSet()

        val treatmentDrugsFound = mutableSetOf<String>()
        val matchingTreatments = record.oncologicalHistory
            .mapNotNull { entry ->
                TreatmentHistoryEntryFunctions.portionOfTreatmentHistoryEntryMatchingPredicate(entry) {
                    it is DrugTreatment && it.drugs.intersect(
                        namesToFind
                    ).isNotEmpty()
                }
            }
        matchingTreatments.forEach {
            it.treatments.forEach { treatment ->
                if (treatment is DrugTreatment && treatment.drugs.intersect(namesToFind).isNotEmpty()) {
                    treatment.drugs.forEach { treatmentDrugsFound.add(it.name) }
                }
            }
        }

        val namesFound = medicationsFound + treatmentDrugsFound

        return when {
            medicationsFound.isNotEmpty() || matchingTreatments.any {
                TreatmentSinceDateFunctions.treatmentSinceMinDate(
                    it,
                    minDate,
                    false
                )
            } -> {
                EvaluationFactory.pass(
                    "Patient has recently received treatment with medication " + concat(namesFound) + " - pay attention to washout period",
                    "Has recently received treatment with medication " + concat(namesFound) + " - pay attention to washout period"
                )
            }

            matchingTreatments.any { TreatmentSinceDateFunctions.treatmentSinceMinDate(it, minDate, true) } -> {
                EvaluationFactory.undetermined(
                    "Treatment containing '${Format.concatItemsWithOr(namesToFind)}' administered with unknown date",
                    "Matching treatment with unknown date"
                )
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