package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.TestResult
import com.hartwig.actin.algo.evaluation.molecular.IhcTestClassificationFunctions.classifyHer2Test
import com.hartwig.actin.algo.evaluation.molecular.MolecularRuleEvaluator.geneIsAmplifiedForPatient
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class HasPositiveHER2ExpressionByIhc(private val maxTestAge: LocalDate? = null) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ihcTestEvaluation = IhcTestEvaluation.create("HER2", record.ihcTests)
        val geneERBB2IsAmplified = geneIsAmplifiedForPatient("ERBB2", record, maxTestAge)
        val her2TestResults = ihcTestEvaluation.filteredTests.map(::classifyHer2Test).toSet()

        val warnInclusionEvent = setOf("Potential IHC HER2 positive")

        return when {
            her2TestResults.isEmpty() -> {
                val undeterminedMessage = "No IHC HER2 expression test available"
                if (geneERBB2IsAmplified) {
                    EvaluationFactory.warn(
                        "$undeterminedMessage (but ERBB2 amplification detected)",
                        inclusionEvents = warnInclusionEvent,
                        isMissingMolecularResultForEvaluation = true,
                    )
                } else {
                    EvaluationFactory.undetermined(undeterminedMessage, isMissingMolecularResultForEvaluation = true)
                }
            }

            her2TestResults.all { it == TestResult.POSITIVE } -> {
                EvaluationFactory.pass(
                    "Has positive HER2 IHC result",
                    inclusionEvents = setOf("IHC HER2 positive")
                )
            }

            her2TestResults.all { it == TestResult.NEGATIVE } -> {
                val failMessage = "Has no positive HER2 IHC result"
                if (geneERBB2IsAmplified) {
                    EvaluationFactory.recoverableFail("$failMessage (but ERBB2 amplification detected)")
                } else {
                    EvaluationFactory.fail(failMessage)
                }
            }

            her2TestResults.all { it == TestResult.BORDERLINE } -> {
                EvaluationFactory.warn(
                    "Undetermined if IHC HER2 score value(s) is considered positive",
                    inclusionEvents = warnInclusionEvent,
                )
            }

            else -> {
                val erbb2AmplifiedMessage = if (geneERBB2IsAmplified) " (but ERBB2 amplification detected)" else ""
                EvaluationFactory.warn(
                    "Undetermined if HER2 IHC test results indicate positive HER2 status$erbb2AmplifiedMessage",
                    inclusionEvents = warnInclusionEvent
                )
            }
        }
    }
}