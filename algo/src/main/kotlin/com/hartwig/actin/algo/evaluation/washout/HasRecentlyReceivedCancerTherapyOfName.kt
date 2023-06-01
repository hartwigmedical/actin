package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter

class HasRecentlyReceivedCancerTherapyOfName(
    private val namesToFind: Set<String>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val namesFound = record.clinical().medications()
            .filter { stringCaseInsensitivelyMatchesQueryCollection(it.name(), namesToFind) }
            .filter { interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE }
            .map { it.name() }

        return if (namesFound.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has recently received treatment with medication " + concat(namesFound),
                "Has recently received treatment with medication " + concat(namesFound)
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received recent treatments with name " + concat(namesToFind),
                "Has not received recent treatments with name " + concat(namesToFind)
            )
        }
    }
}