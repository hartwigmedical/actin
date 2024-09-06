package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest

interface MolecularEvaluationFunction : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val relevantMolecularTests = record.molecularHistory.molecularTestsForTrialMatching()
        return if (relevantMolecularTests.isEmpty()) {
            noMolecularRecordEvaluation() ?: EvaluationFactory.undetermined("No molecular data", "No molecular data")
        } else {

            if (genes().isNotEmpty() && genes().none { relevantMolecularTests.any { t -> t.testsGene(it) } })
                return EvaluationFactory.undetermined(
                    "Gene(s) ${genes().joinToString { it }} not tested in molecular data",
                    "Gene(s) ${genes().joinToString { it }} not tested"
                )
            val testEvaluation =
                relevantMolecularTests.mapNotNull { evaluate(it)?.let { eval -> MolecularEvaluation(it, eval) } }
            if (testEvaluation.isNotEmpty()) {
                return MolecularEvaluation.combine(testEvaluation)
            }

            evaluate(record.molecularHistory)
                ?: record.molecularHistory.latestOrangeMolecularRecord()?.let(::evaluate)
                ?: noMolecularRecordEvaluation()
                ?: EvaluationFactory.undetermined("Insufficient molecular data", "Insufficient molecular data")
        }
    }

    fun noMolecularRecordEvaluation(): Evaluation? = null
    fun evaluate(molecularHistory: MolecularHistory): Evaluation? = null
    fun evaluate(molecular: MolecularRecord): Evaluation? = null

    fun evaluate(test: MolecularTest): Evaluation? = null

    fun genes(): List<String> = emptyList()

    fun evaluationPrecedence(): (Map<EvaluationResult, List<MolecularEvaluation>>) -> List<MolecularEvaluation>? =
        { MolecularEvaluation.defaultEvaluationPrecedence(it) }
}