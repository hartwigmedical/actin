package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.algo.medication.MedicationStatusInterpretation
import com.hartwig.actin.algo.medication.MedicationStatusInterpreter
import com.hartwig.actin.util.ApplicationConfig

class HasRecentlyReceivedCancerTherapyOfName internal constructor(
    private val namesToFind: Set<String>,
    private val interpreter: MedicationStatusInterpreter
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseNamesToFind = namesToFind.map { it.lowercase(ApplicationConfig.LOCALE) }.toSet()
        val namesFound = record.clinical().medications()
            .filter {
                lowercaseNamesToFind.contains(it.name().lowercase(ApplicationConfig.LOCALE)) &&
                        interpreter.interpret(it) == MedicationStatusInterpretation.ACTIVE
            }
            .map { it.name() }

        return if (namesFound.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has recently received treatment with medication " + concat(namesFound),
                "Washout period requirements " + concat(namesToFind)
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not received recent treatments with name " + concat(namesToFind),
                "Washout period requirements " + concat(namesToFind)
            )
        }
    }
}