package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.medicationWhenProvidedEvaluation
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.clinical.datamodel.Medication
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpretation
import com.hartwig.actin.clinical.interpretation.MedicationStatusInterpreter

class HasRecentlyReceivedCancerTherapyOfName(
    private val namesToFind: Set<String>, private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return medicationWhenProvidedEvaluation(record) { medications ->
            val lowercaseNamesToFind = namesToFind.map { it.lowercase() }.toSet()
            val namesFound = medications
                .filter {
                    lowercaseNamesToFind.contains(it.name.lowercase()) && interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE
                }
                .map(Medication::name)

            if (namesFound.isNotEmpty()) {
                EvaluationFactory.pass(
                    "Patient has recently received treatment with medication " + concat(namesFound) + " - pay attention to washout period",
                    "Has recently received treatment with medication " + concat(namesFound) + " - pay attention to washout period"
                )
            } else {
                EvaluationFactory.fail(
                    "Patient has not received recent treatments with name " + concat(namesToFind),
                    "Has not received recent treatments with name " + concat(namesToFind)
                )
            }
        }
    }
}