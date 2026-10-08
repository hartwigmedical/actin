package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.PriorPrimary
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasHistoryOfSecondMalignancyIgnoringDoidTerms(
    private val doidModel: DoidModel, private val doidsToIgnore: Set<String>, private val minDate: LocalDate?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorPrimaries = record.priorPrimaries
        val priorPrimariesByDate = groupByDate(priorPrimaries)

        val (priorPrimariesOfInterest, otherSecondPrimaries) =
            partitionPriorPrimariesOfInterest(priorPrimariesByDate[true] ?: emptyList(), doidsToIgnore)

        val (priorPrimariesOfInterestWithUnknownDate, _) =
            partitionPriorPrimariesOfInterest(priorPrimariesByDate[null] ?: emptyList(), doidsToIgnore)

        val recentMessage = if (minDate != null) " recent" else ""

        return if (priorPrimariesOfInterest.isNotEmpty()) {
            val priorPrimaryMessage = buildNameList(priorPrimariesOfInterest)
            EvaluationFactory.pass("Has history of$recentMessage previous malignancy$priorPrimaryMessage")
        } else if (priorPrimariesOfInterestWithUnknownDate.isNotEmpty()) {
            val priorPrimaryMessage = buildNameList(priorPrimariesOfInterestWithUnknownDate)
            val dateMessage = "but undetermined if recent (date unknown)"
            EvaluationFactory.undetermined("Has history of previous malignancy$priorPrimaryMessage $dateMessage")
        } else if (otherSecondPrimaries.isNotEmpty()) {
            val names = otherSecondPrimaries.map(PriorPrimary::name).filter(String::isNotBlank)
            val excludingMessage = if (names.isEmpty()) "" else " excluding ${names.joinToString(", ")}"
            EvaluationFactory.fail("No$recentMessage history of previous malignancy$excludingMessage")
        } else {
            EvaluationFactory.fail("No$recentMessage history of other malignancy")
        }
    }

    private fun partitionPriorPrimariesOfInterest(
        priorPrimaries: List<PriorPrimary>, doidsToIgnore: Set<String>
    ): Pair<List<PriorPrimary>, List<PriorPrimary>> {
        return priorPrimaries.filter { it.doids.isNotEmpty() }
            .partition { priorPrimary ->
                priorPrimary.doids.any { doidModel.doidWithParents(it).none(doidsToIgnore::contains) }
            }
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

    private fun buildNameList(priorPrimaries: List<PriorPrimary>): String {
        return priorPrimaries
            .map(PriorPrimary::name)
            .filter(String::isNotBlank)
            .takeIf { it.isNotEmpty() }
            ?.joinToString(separator = ", ", prefix = " (", postfix = ")") ?: ""
    }
}