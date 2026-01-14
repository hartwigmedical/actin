package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.TestResult
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasHER2ExpressionByIhc(private val ihcResultToFind: TestResult) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create("HER2", record.ihcTests)
        val geneERBB2IsAmplified = geneIsAmplifiedForPatient("ERBB2", record)
        val her2TestResults = ihcTestEvaluation.filteredTests.map(::classifyHer2Test).toSet()

        val ihcResultString = ihcResultToFind.toString().lowercase()
        val warnInclusionEvent = setOf("Potential IHC HER2 $ihcResultString")
        val erbb2AmplifiedMessage = if (geneERBB2IsAmplified) " (but ERBB2 amplification detected)" else ""

        return when {
            her2TestResults.isEmpty() -> {
                val undeterminedMessage = "No IHC HER2 expression test available"
                if (geneERBB2IsAmplified) {
                    EvaluationFactory.warn(
                        "$undeterminedMessage$erbb2AmplifiedMessage",
                        inclusionEvents = warnInclusionEvent,
                        isMissingMolecularResultForEvaluation = true,
                    )
                } else {
                    EvaluationFactory.undetermined(undeterminedMessage, isMissingMolecularResultForEvaluation = true)
                }
            }

            her2TestResults.all { it == ihcResultToFind } -> {
                if (geneERBB2IsAmplified && (ihcResultToFind == TestResult.NEGATIVE || ihcResultToFind == TestResult.LOW)) {
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
                if (geneERBB2IsAmplified && ihcResultToFind == TestResult.POSITIVE) {
                    EvaluationFactory.recoverableFail("$failMessage$erbb2AmplifiedMessage")
                } else {
                    EvaluationFactory.fail(failMessage)
                }
            }

            her2TestResults.all { it == TestResult.BORDERLINE } && ihcResultToFind in setOf(TestResult.POSITIVE, TestResult.LOW) -> {
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

    private val failingResultsByTargetResult = mapOf(
        TestResult.POSITIVE to setOf(TestResult.NEGATIVE, TestResult.LOW),
        TestResult.LOW to setOf(TestResult.NEGATIVE, TestResult.POSITIVE),
        TestResult.NEGATIVE to setOf(TestResult.LOW, TestResult.BORDERLINE, TestResult.POSITIVE),
    )
}