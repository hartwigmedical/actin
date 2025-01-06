package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.PriorSecondPrimary
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHistoryOfSecondMalignancyIgnoringDoidTerms(
    private val doidModel: DoidModel, private val doidTermsToIgnore: List<String>, private val minDate: LocalDate?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidsToIgnore = doidTermsToIgnore.mapNotNull(doidModel::resolveDoidForTerm).toSet()
        val priorSecondPrimaries = record.priorSecondPrimaries
        val priorSecondPrimariesByDate = groupByDate(priorSecondPrimaries)

        val (priorSecondPrimaryDoidsOfInterest, otherSecondPrimaryDoids) =
            partitionDoidsOfInterest(priorSecondPrimariesByDate[true] ?: emptyList(), doidsToIgnore)

        val (priorSecondPrimaryDoidsOfInterestWithUnknownDate, _) =
            partitionDoidsOfInterest(priorSecondPrimariesByDate[null] ?: emptyList(), doidsToIgnore)

        val recentMessage = if (minDate != null) " recent" else ""

        return if (priorSecondPrimaryDoidsOfInterest.isNotEmpty()) {
            val priorPrimaryMessage = buildDoidTermList(priorSecondPrimaryDoidsOfInterest)
            EvaluationFactory.pass("History of$recentMessage previous malignancy$priorPrimaryMessage")
        } else if (priorSecondPrimaryDoidsOfInterestWithUnknownDate.isNotEmpty()) {
            val priorPrimaryMessage = buildDoidTermList(priorSecondPrimaryDoidsOfInterestWithUnknownDate)
            val dateMessage = "but undetermined if recent (date unknown)"
            EvaluationFactory.undetermined("History of previous malignancy$priorPrimaryMessage $dateMessage")
        } else if (otherSecondPrimaryDoids.isNotEmpty()) {
            val message = otherSecondPrimaryDoids.map { doidModel.resolveTermForDoid(it) }.joinToString(", ")
            EvaluationFactory.fail("No $recentMessage history of previous malignancy excluding $message")
        } else {
            EvaluationFactory.fail("No$recentMessage history of other malignancy")
        }
    }

    private fun partitionDoidsOfInterest(
        priorSecondPrimaries: List<PriorSecondPrimary>, doidsToIgnore: Set<String>
    ): Pair<List<String>, List<String>> {
        return priorSecondPrimaries.flatMap(PriorSecondPrimary::doids)
            .partition { doidModel.doidWithParents(it).none(doidsToIgnore::contains) }
    }

    private fun groupByDate(priorSecondPrimaries: List<PriorSecondPrimary>): Map<Boolean?, List<PriorSecondPrimary>> {
        return if (minDate == null) mapOf(true to priorSecondPrimaries) else {
            priorSecondPrimaries.groupBy { priorSecondPrimary ->
                val effectiveMinDate = if (priorSecondPrimary.lastTreatmentYear != null) minDate else minDate.minusYears(1)
                val year = priorSecondPrimary.lastTreatmentYear ?: priorSecondPrimary.diagnosedYear?.let { it + 1 }
                val month = priorSecondPrimary.lastTreatmentMonth ?: priorSecondPrimary.diagnosedMonth
                DateComparison.isAfterDate(effectiveMinDate, year, month)
            }
        }
    }

    private fun buildDoidTermList(doidList: List<String>): String {
        return doidList
            .mapNotNull(doidModel::resolveTermForDoid)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ", ", prefix = " (", postfix = ")") ?: ""
    }
}