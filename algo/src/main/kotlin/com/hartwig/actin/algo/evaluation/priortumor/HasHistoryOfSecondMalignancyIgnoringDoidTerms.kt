package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.clinical.datamodel.PriorSecondPrimary
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHistoryOfSecondMalignancyIgnoringDoidTerms(
    private val doidModel: DoidModel, private val doidTermsToIgnore: List<String>, private val minDate: LocalDate?
): EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidsToIgnore = doidTermsToIgnore.map { doidModel.resolveDoidForTerm(it) }
        val priorSecondPrimaries = record.priorSecondPrimaries
        val priorSecondPrimariesByDate = groupByDate(priorSecondPrimaries, minDate)

        val (priorSecondPrimaryDoidsOfInterest, otherSecondPrimaryDoids) =
            filterDoidsOfInterest(priorSecondPrimariesByDate.first, doidModel, doidsToIgnore)

        val (priorSecondPrimaryDoidsOfInterestWithUnknownDate, _) =
            filterDoidsOfInterest(priorSecondPrimariesByDate.third, doidModel, doidsToIgnore)

        val recentMessage = if (minDate != null) " recent" else ""

        return if (priorSecondPrimaryDoidsOfInterest.isNotEmpty()) {
            val priorPrimaryMessage = buildDoidTermList(priorSecondPrimaryDoidsOfInterest, doidModel)
            EvaluationFactory.pass(
                "Patient has history of$recentMessage previous malignancy$priorPrimaryMessage",
                "History of$recentMessage previous malignancy$priorPrimaryMessage"
            )
        } else if (priorSecondPrimaryDoidsOfInterestWithUnknownDate.isNotEmpty()) {
            val priorPrimaryMessage = buildDoidTermList(priorSecondPrimaryDoidsOfInterestWithUnknownDate, doidModel)
            val dateMessage = "but undetermined if recent (date unknown)"
            EvaluationFactory.undetermined(
                "Patient has history of previous malignancy$priorPrimaryMessage $dateMessage",
                "History of previous malignancy$priorPrimaryMessage $dateMessage"
            )
        } else if (otherSecondPrimaryDoids.isNotEmpty()) {
            val message = otherSecondPrimaryDoids.map { doidModel.resolveTermForDoid(it) }.joinToString(", ")
            EvaluationFactory.fail(
                "Patient has no$recentMessage history of previous malignancy excluding $message",
                "No relevant$recentMessage history of other malignancy"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has no$recentMessage history of previous malignancy", "No$recentMessage history of other malignancy"
            )
        }
    }

    companion object {
        private fun filterDoidsOfInterest(
            priorSecondPrimaries: List<PriorSecondPrimary>, doidModel: DoidModel, doidsToIgnore: List<String?>
        ): Pair<List<String>, List<String>> {
            return priorSecondPrimaries.flatMap(PriorSecondPrimary::doids)
                .partition { doidModel.doidWithParents(it).none(doidsToIgnore::contains) }
        }

        private fun groupByDate(
            priorSecondPrimaries: List<PriorSecondPrimary>, minDate: LocalDate?
        ): Triple<List<PriorSecondPrimary>, List<PriorSecondPrimary>, List<PriorSecondPrimary>> {
            if (minDate == null) return Triple(priorSecondPrimaries, emptyList(), emptyList()) else {
                val insideDateRange = mutableListOf<PriorSecondPrimary>()
                val outsideDateRange = mutableListOf<PriorSecondPrimary>()
                val unknownDate = mutableListOf<PriorSecondPrimary>()

                priorSecondPrimaries.forEach { priorSecondPrimary ->
                    val effectiveMinDate = if (priorSecondPrimary.lastTreatmentYear != null) minDate else minDate.minusYears(1)
                    val year = priorSecondPrimary.lastTreatmentYear ?: priorSecondPrimary.diagnosedYear
                    val month = priorSecondPrimary.lastTreatmentMonth ?: priorSecondPrimary.diagnosedMonth

                    when (DateComparison.isAfterDate(effectiveMinDate, year, month)) {
                        true -> insideDateRange.add(priorSecondPrimary)
                        false -> outsideDateRange.add(priorSecondPrimary)
                        else -> unknownDate.add(priorSecondPrimary)
                    }
                }
                return Triple(insideDateRange.toList(), outsideDateRange.toList(), unknownDate.toList())
            }
        }

        private fun buildDoidTermList(doidList: List<String>, doidModel: DoidModel): String {
            return doidList
                .map { doidModel.resolveTermForDoid(it) }
                .takeIf { it.isNotEmpty() }
                ?.joinToString(separator = ", ", prefix = " (", postfix = ")") ?: ""
        }
    }
}