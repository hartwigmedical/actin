package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.treatment.SystemicTreatmentAnalyser.TimingEvaluatedEntry
import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions.treatmentStoppedDueToPD
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import java.time.LocalDate

class PlatinumProgressionAnalysis(val firstPlatinumTreatment: TimingEvaluatedEntry?, val lastPlatinumTreatment: TimingEvaluatedEntry?) {

    fun hasProgressionDuringPlatinumOrWithinMonths(platinumTreatment: TimingEvaluatedEntry?) =
        platinumTreatment?.let {
            treatmentStoppedDueToPD(it.entry) == true || treatmentResultedInPDWithTiming(
                it, setOf(SystemicTreatmentAnalyser.TreatmentTiming.WITHIN)
            )
        }

    fun hasProgressionOrUnknownProgressionOnPlatinum(platinumTreatment: TimingEvaluatedEntry?) =
        platinumTreatment?.let { treatmentStoppedDueToOrBestResponsePD(it.entry) != false }

    private fun treatmentResultedInPDWithTiming(treatment: TimingEvaluatedEntry, timing: Set<SystemicTreatmentAnalyser.TreatmentTiming>) =
        treatmentStoppedDueToOrBestResponsePD(treatment.entry) == true && treatment.timing in timing

    private fun treatmentStoppedDueToOrBestResponsePD(treatment: TreatmentHistoryEntry): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails?.bestResponse
        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE || treatmentStoppedDueToPD(treatment) == true -> true
            treatmentStoppedDueToPD(treatment) == false && bestResponse != null -> false
            else -> null
        }
    }

    companion object {
        fun create(record: PatientRecord, referenceDate: LocalDate, minMonths: Int): PlatinumProgressionAnalysis {
            val platinumTreatments = record.oncologicalHistory.filter { it.isOfType(DrugType.PLATINUM_COMPOUND) == true }
            val timingEvaluatedHistory =
                SystemicTreatmentAnalyser.evaluateTreatmentTimingRelativeToNextLine(record.oncologicalHistory, minMonths, referenceDate)
            val firstPlatinumTreatment =
                timingEvaluatedHistory.find { it.entry == SystemicTreatmentAnalyser.firstSystemicTreatment(platinumTreatments) }
            val lastPlatinumTreatment =
                timingEvaluatedHistory.find { it.entry == SystemicTreatmentAnalyser.lastSystemicTreatment(platinumTreatments) }
            return PlatinumProgressionAnalysis(firstPlatinumTreatment, lastPlatinumTreatment)
        }
    }
}