package com.hartwig.actin.algo.evaluation.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularTest

interface MolecularEvaluationFunction : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        return if (!record.molecularHistory.hasMolecularData()) {
            noMolecularRecordEvaluation() ?: EvaluationFactory.undetermined("No molecular data", "No molecular data")
        } else {

            if (genes().any { record.molecularHistory.molecularTests.any { t -> t.testsGene(it) } })
                return EvaluationFactory.undetermined(
                    "Gene(s) ${genes()} not tested in molecular data",
                    "Gene(s) ${genes()} not tested"
                )
            val testEvaluation =
                record.molecularHistory.molecularTests.mapNotNull { evaluate(it)?.let { eval -> MolecularEvaluation(it, eval) } }
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
}