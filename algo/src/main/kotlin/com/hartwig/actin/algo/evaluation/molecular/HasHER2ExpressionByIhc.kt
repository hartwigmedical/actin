package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.IhcTestResult

class HasHER2ExpressionByIhc(private val ihcResultToFind: IhcTestResult, private val labels: EvaluationLabels.Molecular) :
    EvaluationFunction {

    private val failingResultsByTargetResult = mapOf(
        IhcTestResult.POSITIVE to setOf(IhcTestResult.NEGATIVE, IhcTestResult.LOW),
        IhcTestResult.LOW to setOf(IhcTestResult.NEGATIVE, IhcTestResult.POSITIVE),
        IhcTestResult.NEGATIVE to setOf(IhcTestResult.LOW, IhcTestResult.BORDERLINE, IhcTestResult.POSITIVE),
    )

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create("HER2", record.ihcTests)
        val erbb2AmpResult = GeneIsAmplified("ERBB2", null, labels).evaluate(record).result
        val erbb2IsAmplified = erbb2AmpResult == EvaluationResult.PASS
        val her2TestResults = ihcTestEvaluation.filteredTests.map(::classifyHer2Test).toSet()

        val ihcResultString = ihcResultToFind.toString().lowercase()
        val warnInclusionEvent = setOf("Potential IHC HER2 $ihcResultString")
        val erbb2AmplifiedMessage = if (erbb2IsAmplified) labels.hasHer2ExpressionByIhcSuffixErbb2Amplified() else ""
        val erbb2NotAmplifiedMessage =
            if (erbb2AmpResult == EvaluationResult.FAIL) labels.hasHer2ExpressionByIhcSuffixErbb2NotAmplified() else ""

        return when {
            her2TestResults.isEmpty() -> {
                val undeterminedMessage = labels.hasHer2ExpressionByIhcUndeterminedNoTest()
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
                        labels.hasHer2ExpressionByIhcWarnStatus(ihcResultString, erbb2AmplifiedMessage),
                        inclusionEvents = warnInclusionEvent
                    )
                } else {
                    EvaluationFactory.pass(
                        labels.hasHer2ExpressionByIhcPass(ihcResultString),
                        inclusionEvents = setOf("IHC HER2 $ihcResultString")
                    )
                }
            }

            failingResultsByTargetResult[ihcResultToFind]?.let { failResults -> her2TestResults.all { it in failResults } } == true -> {
                val failMessage = labels.hasHer2ExpressionByIhcFail(ihcResultString)
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
                    labels.hasHer2ExpressionByIhcUndeterminedScore(ihcResultString),
                    isMissingMolecularResultForEvaluation = true
                )
            }

            else -> {
                EvaluationFactory.warn(
                    labels.hasHer2ExpressionByIhcWarnStatus(ihcResultString, erbb2AmplifiedMessage),
                    inclusionEvents = warnInclusionEvent
                )
            }
        }
    }
}