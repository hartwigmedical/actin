package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import java.time.LocalDate

abstract class MolecularEvaluationFunction(
    maxTestAge: LocalDate? = null,
    useInsufficientQualityRecords: Boolean = false,
    open val gene: String? = null,
    val targetCoveragePredicate: TargetCoveragePredicate = any(),
) : EvaluationFunction {

    private val molecularTestFilter = MolecularTestFilter(maxTestAge, useInsufficientQualityRecords)

    override fun evaluate(record: PatientRecord): Evaluation {
        val recentMolecularTests = molecularTestFilter.apply(record.molecularTests)

        return if (recentMolecularTests.isEmpty()) {
            noMolecularTestEvaluation() ?: EvaluationFactory.undetermined(
                "No molecular results of sufficient quality",
                isMissingMolecularResultForEvaluation = true
            )
        } else {
            if (gene?.let { g -> recentMolecularTests.any { t -> t.testsGene(g, targetCoveragePredicate) } } == false)
                return Evaluation(
                    recoverable = false,
                    result = EvaluationResult.UNDETERMINED,
                    undeterminedMessages = setOf(targetCoveragePredicate.message(gene!!)),
                    isMissingMolecularResultForEvaluation = true
                )

            val testEvaluation = recentMolecularTests.mapNotNull { evaluate(it, record)?.let { eval -> MolecularEvaluation(it, eval) } }
            if (testEvaluation.isNotEmpty()) {
                return MolecularEvaluation.combine(testEvaluation, evaluationPrecedence())
            }

            return noMolecularTestEvaluation() ?: EvaluationFactory.undetermined(
                "Insufficient molecular data",
                isMissingMolecularResultForEvaluation = true
            )
        }
    }

    open fun noMolecularTestEvaluation(): Evaluation? = null
    open fun evaluate(test: MolecularTest): Evaluation? = null
    open fun evaluate(test: MolecularTest, record: PatientRecord): Evaluation? = evaluate(test)

    open fun evaluationPrecedence(): (Map<EvaluationResult, List<MolecularEvaluation>>) -> List<MolecularEvaluation>? =
        { MolecularEvaluation.defaultEvaluationPrecedence(it) }
}