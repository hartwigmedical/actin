package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.composite.Fallback
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabUnit
import java.time.LocalDate

internal class MeasuredCreatinineClearanceEvaluator(
    private val minCreatinineClearance: Double,
    private val minValidDate: LocalDate,
    private val minPassDate: LocalDate,
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val direct = LabMeasurementEvaluator(
            SingleLabValueSelector(LabMeasurement.CREATININE_CLEARANCE_24H, false),
            HasSufficientLabValue(minCreatinineClearance, LabMeasurement.CREATININE_CLEARANCE_24H, LabUnit.MILLILITERS_PER_MINUTE),
            minValidDate, minPassDate
        )
        val dailyTotal = LabMeasurementEvaluator(
            SameDateLabValueSelector(setOf(LabMeasurement.CREATININE_24U, LabMeasurement.CREATININE)),
            HasSufficientMeasuredCreatinineClearance(minCreatinineClearance, MeasuredCreatinineClearanceMethod.DAILY_TOTAL),
            minValidDate, minPassDate
        )
        val concentration = LabMeasurementEvaluator(
            SameDateLabValueSelector(setOf(LabMeasurement.CREATININE_URINE, LabMeasurement.URINE_VOLUME_24H, LabMeasurement.CREATININE)),
            HasSufficientMeasuredCreatinineClearance(minCreatinineClearance, MeasuredCreatinineClearanceMethod.URINE_CONCENTRATION),
            minValidDate, minPassDate
        )
        return Fallback(direct, Fallback(dailyTotal, concentration)).evaluate(record)
    }
}
