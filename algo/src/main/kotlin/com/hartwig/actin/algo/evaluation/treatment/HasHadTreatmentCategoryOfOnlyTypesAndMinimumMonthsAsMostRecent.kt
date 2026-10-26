package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import java.time.YearMonth
import java.time.temporal.ChronoUnit

private const val MIN_MONTH = 1
private const val MAX_MONTH = 12

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecent(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val months: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val description =
            "${category.display()} of only type(s) ${Format.concatItemsWithOr(types)} for at least $months months as most recent line"
        val mostRecent = SystemicTreatmentAnalyser.lastSystemicTreatment(record.oncologicalHistory)
        val (minDuration, maxDuration) = mostRecent?.let { durationRange(it) } ?: (null to null)

        return when {
            mostRecent == null || !matchesCategoryAndTypes(mostRecent) -> EvaluationFactory.fail("No $description")

            minDuration != null && minDuration >= months -> EvaluationFactory.pass("Has had $description")

            maxDuration != null && maxDuration < months -> EvaluationFactory.fail("No $description (duration too short)")

            else -> EvaluationFactory.undetermined("Undetermined if $description (dates incomplete)")
        }
    }

    private fun durationRange(treatment: TreatmentHistoryEntry): Pair<Long?, Long?> {
        val (startMin, startMax) = monthRange(treatment.startYear, treatment.startMonth)
        val stopYear = treatment.stopYear()

        val (stopMin, stopMax) = if (stopYear != null) {
            monthRange(stopYear, treatment.stopMonth())
        } else {
            val end = YearMonth.from(referenceDate)
            end to end
        }

        return if (startMin != null && startMax != null && stopMin != null && stopMax != null) {
            ChronoUnit.MONTHS.between(startMax, stopMin) to ChronoUnit.MONTHS.between(startMin, stopMax)
        } else {
            null to null
        }
    }

    private fun matchesCategoryAndTypes(entry: TreatmentHistoryEntry): Boolean =
        entry.allTreatments().any {
            it.isSystemic && it.categories().contains(category) && it.types().isNotEmpty() && it.types().all { type -> type in types }
        }

    private fun monthRange(year: Int?, month: Int?): Pair<YearMonth?, YearMonth?> =
        when {
            year == null -> null to null
            month == null -> YearMonth.of(year, MIN_MONTH) to YearMonth.of(year, MAX_MONTH)
            else -> YearMonth.of(year, month).let { it to it }
        }
}

