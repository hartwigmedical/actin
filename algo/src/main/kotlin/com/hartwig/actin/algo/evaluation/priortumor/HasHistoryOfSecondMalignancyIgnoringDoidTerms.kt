package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasHistoryOfSecondMalignancyIgnoringDoidTerms(
    private val doidModel: DoidModel, private val doidsToIgnore: List<String>, private val doidTermsToIgnore: List<String>): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorSecondPrimaries = record.priorSecondPrimaries
        val priorSecondPrimariesOfInterest =
            priorSecondPrimaries.flatMap { it.doids }.flatMap { doidModel.doidWithParents(it) }.filterNot { it in doidsToIgnore }

        return if (priorSecondPrimariesOfInterest.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has history of previous malignancy (${priorSecondPrimariesOfInterest.joinToString(", ")})",
                "History of other malignancy"
            )
        } else if (priorSecondPrimaries.isNotEmpty()) {
            EvaluationFactory.fail(
                "Patient has no history of previous malignancy other than ${doidTermsToIgnore.joinToString(", ")}",
                "No relevant history of other malignancy"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has no history of previous malignancy", "No history of other malignancy"
            )
        }
    }
}