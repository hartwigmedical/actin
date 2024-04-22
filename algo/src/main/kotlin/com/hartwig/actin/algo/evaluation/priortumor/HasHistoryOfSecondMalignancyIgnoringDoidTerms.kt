package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHistoryOfSecondMalignancyIgnoringDoidTerms(
    private val doidModel: DoidModel, private val doidTermsToIgnore: List<String>, private val minDate: LocalDate?
): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidsToIgnore = doidTermsToIgnore.map { doidModel.resolveDoidForTerm(it) }
        val priorSecondPrimaries = record.priorSecondPrimaries

        val (priorSecondPrimariesOfInterest, otherSecondPrimaries) =
            priorSecondPrimaries.partition { it.doids.all { doid -> doidModel.doidWithParents(doid).none(doidsToIgnore::contains) } }

        val (insideDateRange, _) =
            if (minDate != null) {
                priorSecondPrimariesOfInterest.partition { priorSecondPrimary ->
                    val (effectiveMinDate, secondPrimaryYear, secondPrimaryMonth) = if (priorSecondPrimary.lastTreatmentYear != null) {
                        Triple(minDate, priorSecondPrimary.lastTreatmentYear, priorSecondPrimary.lastTreatmentMonth)
                    } else {
                        Triple(minDate.minusYears(1), priorSecondPrimary.diagnosedYear, priorSecondPrimary.diagnosedMonth)
                    }
                    if (secondPrimaryYear != null) {
                        val match = !LocalDate.of(secondPrimaryYear, secondPrimaryMonth ?: 1, 1).isBefore(effectiveMinDate)
                        val potentialMatch = secondPrimaryYear == effectiveMinDate.year && secondPrimaryMonth == null
                        match || potentialMatch
                    } else {
                        false
                    }
                }
            } else Pair(priorSecondPrimaries, emptyList())

        val anyOfInterestHasUnknownDate = priorSecondPrimariesOfInterest.any { it.lastTreatmentYear == null && it.diagnosedYear == null }

        val priorSecondPrimaryDoidsOfInterest = insideDateRange.flatMap(PriorSecondPrimary::doids)
        val otherSecondPrimaryDoids = otherSecondPrimaries.flatMap(PriorSecondPrimary::doids)

        val recentMessage = if (minDate != null) "recent" else ""

        val priorPrimaryMessage = priorSecondPrimaryDoidsOfInterest
            .mapNotNull { doidModel.resolveTermForDoid(it) }
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ", ", prefix = " (", postfix = ")") ?: ""

        return if (priorSecondPrimaryDoidsOfInterest.isNotEmpty()) {
            EvaluationFactory.pass(
                "Patient has history of $recentMessage previous malignancy$priorPrimaryMessage",
                "History of $recentMessage previous malignancy$priorPrimaryMessage"
            )
        } else if (anyOfInterestHasUnknownDate) {
            val dateMessage = if (minDate != null) " but undetermined if recent (date unknown)" else ""
            EvaluationFactory.undetermined(
                "Patient has history of previous malignancy$priorPrimaryMessage$dateMessage",
                "History of previous malignancy$priorPrimaryMessage$dateMessage"
            )
        } else if (otherSecondPrimaries.isNotEmpty()) {
            val message = otherSecondPrimaryDoids.map { doidModel.resolveTermForDoid(it) }.joinToString(", ")
            EvaluationFactory.fail(
                "Patient has no $recentMessage history of previous malignancy excluding $message",
                "No relevant $recentMessage history of other malignancy"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has no $recentMessage history of previous malignancy", "No $recentMessage history of other malignancy"
            )
        }
    }
}