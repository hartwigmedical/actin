package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import kotlin.math.roundToInt

private const val WEEKS_PER_MONTH = 52.0 / 12

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumMonthsAsMostRecent(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val months: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val mostRecent =
            record.oncologicalHistory.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())?.let(::assumeOngoingIfNoStopDate)

        return TreatmentDurationEvaluator(
            ::matchesCategoryAndOnlyTypes,
            { category in it.categories() && !it.hasTypeConfigured() },
            setOf(category),
            "${Format.concatItemsWithOr(types)} ${category.display()} as most recent line",
            TreatmentDurationType.SUFFICIENT,
            (months * WEEKS_PER_MONTH).roundToInt()
        ).evaluate(record.copy(oncologicalHistory = listOfNotNull(mostRecent)))
    }

    private fun matchesCategoryAndOnlyTypes(treatment: Treatment): Boolean =
        category in treatment.categories() && treatment.types().isNotEmpty() && treatment.types().all { it in types }

    private fun assumeOngoingIfNoStopDate(entry: TreatmentHistoryEntry): TreatmentHistoryEntry {
        return if (entry.stopYear() != null) {
            entry
        } else {
            val details = entry.treatmentHistoryDetails ?: TreatmentHistoryDetails()
            entry.copy(treatmentHistoryDetails = details.copy(stopYear = referenceDate.year, stopMonth = referenceDate.monthValue))
        }
    }
}
