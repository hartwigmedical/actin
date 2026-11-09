package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadTreatmentCategoryOfOnlyTypesAndMinimumWeeksAsMostRecent(
    private val category: TreatmentCategory,
    private val types: Set<TreatmentType>,
    private val weeks: Int,
    private val referenceDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val mostRecent = record.oncologicalHistory.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())?.let(::assumeOngoingIfNoStopDate)

        return TreatmentDurationEvaluator(
            { category in it.categories() && it.types().isNotEmpty() && it.types().all(types::contains) },
            { category in it.categories() && !it.hasTypeConfigured() },
            setOf(category),
            "${Format.concatItemsWithOr(types)} ${category.display()} as most recent line",
            TreatmentDurationType.SUFFICIENT,
            weeks
        ).evaluate(record.copy(oncologicalHistory = listOfNotNull(mostRecent)))
    }

    private fun assumeOngoingIfNoStopDate(entry: TreatmentHistoryEntry): TreatmentHistoryEntry {
        return if (entry.stopYear() != null) {
            entry
        } else {
            val details = entry.treatmentHistoryDetails ?: TreatmentHistoryDetails()
            entry.copy(treatmentHistoryDetails = details.copy(stopYear = referenceDate.year, stopMonth = referenceDate.monthValue))
        }
    }
}
