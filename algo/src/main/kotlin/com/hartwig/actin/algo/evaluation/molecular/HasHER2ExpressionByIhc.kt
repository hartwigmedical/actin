package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTestResult

class HasHER2ExpressionByIhc(private val ihcResultToFind: IhcTestResult) : EvaluationFunction {

    private val failingResultsByTargetResult = mapOf(
        IhcTestResult.POSITIVE to setOf(IhcTestResult.NEGATIVE, IhcTestResult.LOW),
        IhcTestResult.LOW to setOf(IhcTestResult.NEGATIVE, IhcTestResult.POSITIVE),
        IhcTestResult.NEGATIVE to setOf(IhcTestResult.LOW, IhcTestResult.BORDERLINE, IhcTestResult.POSITIVE),
    )

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create("HER2", record.ihcTests)
        val erbb2AmpResult = GeneIsAmplified("ERBB2", null).evaluate(record).result
        val erbb2IsAmplified = erbb2AmpResult == EvaluationResult.PASS
        val her2TestResults = ihcTestEvaluation.filteredTests.map(::classifyHer2Test).toSet()

        val ihcResultString = ihcResultToFind.toString().lowercase()
        val warnInclusionEvent = setOf("Potential IHC HER2 $ihcResultString")
        val erbb2AmplifiedMessage = if (erbb2IsAmplified) " (but ERBB2 amplification detected)" else ""
        val erbb2NotAmplifiedMessage = if (erbb2AmpResult == EvaluationResult.FAIL) " (but no ERBB2 amplification found in DNA)" else ""

        return when {
            her2TestResults.isEmpty() -> {
                val undeterminedMessage = "No IHC HER2 expression test available"
                if (erbb2IsAmplified) {
                    val message = "$undeterminedMessage$erbb2AmplifiedMessage"
                    if (ihcResultToFind == IhcTestResult.POSITIVE) {
                        EvaluationFactory.warn(message, inclusionEvents = warnInclusionEvent, isMissingMolecularResultForEvaluation = true)
                    } else {
                        EvaluationFactory.undetermined(message, isMissingMolecularResultForEvaluation = true)
                    }
                } else {
                    val noAmpClarification = if (ihcResultToFind == IhcTestResult.POSITIVE) erbb2NotAmplifiedMessage else ""
                    EvaluationFactory.undetermined("$undeterminedMessage$noAmpClarification", isMissingMolecularResultForEvaluation = true)
                }
            }

            her2TestResults.all { it == ihcResultToFind } -> {
                if (erbb2IsAmplified && (ihcResultToFind == IhcTestResult.NEGATIVE || ihcResultToFind == IhcTestResult.LOW)) {
                    EvaluationFactory.warn(
                        "Undetermined if HER2 IHC test results indicate $ihcResultString HER2 status$erbb2AmplifiedMessage",
                        inclusionEvents = warnInclusionEvent
                    )
                } else {
                    EvaluationFactory.pass(
                        "Has $ihcResultString HER2 IHC result",
                        inclusionEvents = setOf("IHC HER2 $ihcResultString")
                    )
                }
            }

            failingResultsByTargetResult[ihcResultToFind]?.let { failResults -> her2TestResults.all { it in failResults } } == true -> {
                val failMessage = "Has no $ihcResultString HER2 IHC result"
                if (erbb2IsAmplified && ihcResultToFind == IhcTestResult.POSITIVE) {
                    EvaluationFactory.recoverableFail("$failMessage$erbb2AmplifiedMessage")
                } else {
                    EvaluationFactory.fail(failMessage)
                }
            }

            her2TestResults.all { it == IhcTestResult.BORDERLINE } && ihcResultToFind in setOf(
                IhcTestResult.POSITIVE,
                IhcTestResult.LOW
            ) -> {
                EvaluationFactory.undetermined(
                    "Undetermined if IHC HER2 score value(s) is considered $ihcResultString",
                    isMissingMolecularResultForEvaluation = true
                )
            }

            else -> {
                EvaluationFactory.warn(
                    "Undetermined if HER2 IHC test results indicate $ihcResultString HER2 status$erbb2AmplifiedMessage",
                    inclusionEvents = warnInclusionEvent
                )
            }
        }
    }
}