package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.doid.DoidModel

class HasHistoryOfSecondMalignancyIgnoringDoidTerms(
    private val doidModel: DoidModel, private val doidTermsToIgnore: List<String>
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidsToIgnore = doidTermsToIgnore.map { doidModel.resolveDoidForTerm(it) }
        val priorSecondPrimaries = record.priorSecondPrimaries
        val (priorSecondPrimaryDoidsOfInterest, otherSecondPrimaryDoids) = priorSecondPrimaries.flatMap(PriorSecondPrimary::doids)
            .partition { doidModel.doidWithParents(it).none(doidsToIgnore::contains) }

        return if (priorSecondPrimaryDoidsOfInterest.isNotEmpty()) {
            val priorPrimaryMessage = priorSecondPrimaryDoidsOfInterest
                .mapNotNull { doidModel.resolveTermForDoid(it) }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = " (", postfix = ")") ?: ""
            EvaluationFactory.pass(
                "Patient has history of previous malignancy$priorPrimaryMessage",
                "History of previous malignancy$priorPrimaryMessage"
            )
        } else if (priorSecondPrimaries.isNotEmpty()) {
            val message = otherSecondPrimaryDoids.map { doidModel.resolveTermForDoid(it) }.joinToString(", ")
            EvaluationFactory.fail(
                "Patient has no history of previous malignancy excluding $message",
                "No relevant history of other malignancy"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has no history of previous malignancy", "No history of other malignancy"
            )
        }
    }
}