package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHistoryOfSecondMalignancyIgnoringDoidTerms(
    private val doidModel: DoidModel, private val doidTermsToIgnore: List<String>, private val minDate: LocalDate?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val doidsToIgnore = doidTermsToIgnore.mapNotNull(doidModel::resolveDoidForTerm).toSet()
        val priorPrimaries = record.priorPrimaries
        val priorPrimariesByDate = groupByDate(priorPrimaries)

        val (priorPrimaryDoidsOfInterest, otherSecondPrimaryDoids) =
            partitionDoidsOfInterest(priorPrimariesByDate[true] ?: emptyList(), doidsToIgnore)

        val (priorPrimaryDoidsOfInterestWithUnknownDate, _) =
            partitionDoidsOfInterest(priorPrimariesByDate[null] ?: emptyList(), doidsToIgnore)

        val recentMessage = if (minDate != null) " recent" else ""

        return if (priorPrimaryDoidsOfInterest.isNotEmpty()) {
            val priorPrimaryMessage = buildDoidTermList(priorPrimaryDoidsOfInterest)
            EvaluationFactory.pass("Has history of$recentMessage previous malignancy$priorPrimaryMessage")
        } else if (priorPrimaryDoidsOfInterestWithUnknownDate.isNotEmpty()) {
            val priorPrimaryMessage = buildDoidTermList(priorPrimaryDoidsOfInterestWithUnknownDate)
            val dateMessage = "but undetermined if recent (date unknown)"
            EvaluationFactory.undetermined("Has history of previous malignancy$priorPrimaryMessage $dateMessage")
        } else if (otherSecondPrimaryDoids.isNotEmpty()) {
            val message = otherSecondPrimaryDoids.map { doidModel.resolveTermForDoid(it) }.joinToString(", ")
            EvaluationFactory.fail("No$recentMessage history of previous malignancy excluding $message")
        } else {
            EvaluationFactory.fail("No$recentMessage history of other malignancy")
        }
    }

    private fun partitionDoidsOfInterest(
        priorPrimaries: List<PriorPrimary>, doidsToIgnore: Set<String>
    ): Pair<List<String>, List<String>> {
        return priorPrimaries.flatMap(PriorPrimary::doids)
            .partition { doidModel.doidWithParents(it).none(doidsToIgnore::contains) }
    }

    private fun groupByDate(priorPrimaries: List<PriorPrimary>): Map<Boolean?, List<PriorPrimary>> {
        return if (minDate == null) mapOf(true to priorPrimaries) else {
            priorPrimaries.groupBy { priorPrimary ->
                val effectiveMinDate = if (priorPrimary.lastTreatmentYear != null) minDate else minDate.minusYears(1)
                val year = priorPrimary.lastTreatmentYear ?: priorPrimary.diagnosedYear?.let { it + 1 }
                val month = priorPrimary.lastTreatmentMonth ?: priorPrimary.diagnosedMonth
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