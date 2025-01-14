package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.molecular.filter.MolecularTestFilter
import java.time.LocalDate

abstract class MolecularEvaluationFunction(maxTestAge: LocalDate? = null, useInsufficientQualityRecords: Boolean = false) :
    EvaluationFunction {

    private val molecularTestFilter = MolecularTestFilter(maxTestAge, useInsufficientQualityRecords)

    override fun evaluate(record: PatientRecord): Evaluation {
        val recentMolecularTests = molecularTestFilter.apply(record.molecularHistory.molecularTests)

        return if (recentMolecularTests.isEmpty()) {
            noMolecularRecordEvaluation() ?: EvaluationFactory.undetermined(
                "No molecular results of sufficient quality",
                missingGenesForEvaluation = true
            )
        } else {

            if (genes().isNotEmpty() && genes().none { recentMolecularTests.any { t -> t.testsGene(it) } })
                return EvaluationFactory.undetermined(
                    "Gene(s) ${genes().joinToString { it }} not tested",
                    missingGenesForEvaluation = true
                )

            val testEvaluation =
                recentMolecularTests.mapNotNull { evaluate(it)?.let { eval -> MolecularEvaluation(it, eval) } }
            if (testEvaluation.isNotEmpty()) {
                return MolecularEvaluation.combine(testEvaluation)
            }

            evaluate(record.molecularHistory)
                ?: record.molecularHistory.latestOrangeMolecularRecord()?.let(::evaluate)
                ?: noMolecularRecordEvaluation()
                ?: EvaluationFactory.undetermined(
                    "Insufficient molecular data",
                    missingGenesForEvaluation = true
                )
        }
    }

    open fun noMolecularRecordEvaluation(): Evaluation? = null
    open fun evaluate(molecularHistory: MolecularHistory): Evaluation? = null
    open fun evaluate(molecular: MolecularRecord): Evaluation? = null
    open fun evaluate(test: MolecularTest): Evaluation? = null
    open fun genes(): List<String> = emptyList()
    open fun evaluationPrecedence(): (Map<EvaluationResult, List<MolecularEvaluation>>) -> List<MolecularEvaluation>? =
        { MolecularEvaluation.defaultEvaluationPrecedence(it) }
}