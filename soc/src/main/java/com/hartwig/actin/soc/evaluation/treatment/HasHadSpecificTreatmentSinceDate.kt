package com.hartwig.actin.soc.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment
import com.hartwig.actin.soc.evaluation.EvaluationFactory
import com.hartwig.actin.soc.evaluation.EvaluationFunction
import com.hartwig.actin.soc.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.soc.evaluation.util.Format
import com.hartwig.actin.util.ApplicationConfig
import java.time.LocalDate

class HasHadSpecificTreatmentSinceDate internal constructor(treatmentName: String, minDate: LocalDate) : EvaluationFunction {

    private val query: String
    private val minDate: LocalDate

    init {
        query = treatmentName.lowercase(ApplicationConfig.LOCALE)
        this.minDate = minDate
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments: List<PriorTumorTreatment> = record.clinical().priorTumorTreatments()
                .filter { it.name().lowercase(ApplicationConfig.LOCALE).contains(query) }

        return when {
            matchingTreatments.any { treatment: PriorTumorTreatment -> treatmentSinceMinDate(treatment, false) } ->
                EvaluationFactory.pass(java.lang.String.format("Treatment matching '%s' administered since %s", query, Format.date(minDate)),
                        "Matching treatment since date")
            matchingTreatments.any { treatmentSinceMinDate(it, true) } ->
                EvaluationFactory.undetermined(String.format("Treatment matching '%s' administered with unknown date", query),
                        "Matching treatment with unknown date")
            matchingTreatments.isNotEmpty() ->
                EvaluationFactory.fail(java.lang.String.format("All treatments matching '%s' administered before %s", query, Format.date(minDate)),
                        "Matching treatment with earlier date")
            else ->
                EvaluationFactory.fail(String.format("No treatments matching '%s' in prior tumor history", query),
                        "No matching treatments found")
        }
    }

    private fun treatmentSinceMinDate(treatment: PriorTumorTreatment, includeUnknown: Boolean): Boolean {
        return isAfterDate(minDate, treatment.stopYear(), treatment.stopMonth()) ?: isAfterDate(minDate, treatment.startYear(),
                treatment.startMonth()) ?: includeUnknown
    }
}