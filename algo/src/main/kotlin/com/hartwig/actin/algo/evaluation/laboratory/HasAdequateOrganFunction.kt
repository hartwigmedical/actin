package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.othercondition.OtherConditionSelector
import com.hartwig.actin.clinical.interpretation.LabInterpreter
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.doid.DoidModel
import java.time.LocalDate

class HasAdequateOrganFunction(private val minValidDate: LocalDate, private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val interpretation = LabInterpreter.interpret(record.labValues)

        val valuesUnderLowerLimit = sequenceOf(
            LabMeasurement.HEMOGLOBIN,
            LabMeasurement.THROMBOCYTES_ABS,
            LabMeasurement.NEUTROPHILS_ABS,
            LabMeasurement.EGFR_MDRD,
            LabMeasurement.EGFR_CKD_EPI
        )
            .filter {
                evaluateVersusULN(
                    isMaxUln = false,
                    interpretation.mostRecentValue(it),
                    it
                ) == LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
            }
            .toSet()

        val valuesAboveUpperLimit = sequenceOf(
            LabMeasurement.ASPARTATE_AMINOTRANSFERASE,
            LabMeasurement.ALANINE_AMINOTRANSFERASE,
            LabMeasurement.TOTAL_BILIRUBIN,
            LabMeasurement.LACTATE_DEHYDROGENASE
        )
            .filter {
                evaluateVersusULN(
                    isMaxUln = true,
                    interpretation.mostRecentValue(it),
                    it
                ) == LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN
            }
            .toSet()

        val cardiovascularHistory = OtherConditionSelector.selectConditionsMatchingDoid(
            record.priorOtherConditions,
            DoidConstants.CARDIOVASCULAR_DISEASE_DOID,
            doidModel
        )

        val messageStart = "Possible inadequate organ function"

        return when {
            valuesUnderLowerLimit.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$messageStart (${Format.concatWithCommaAndAnd(valuesUnderLowerLimit.map { it.display() })} below LLN)"
                )
            }

            valuesAboveUpperLimit.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$messageStart (${Format.concatWithCommaAndAnd(valuesAboveUpperLimit.map { it.display() })} above ULN)"
                )
            }

            cardiovascularHistory.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$messageStart (cardiovascular disease present: ${cardiovascularHistory.joinToString(", ")})"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail("No indication of inadequate organ function")
            }
        }
    }

    private fun evaluateVersusULN(
        isMaxUln: Boolean,
        mostRecent: LabValue?,
        measurement: LabMeasurement
    ): LabEvaluation.LabEvaluationResult {
        return if (LabEvaluation.isValid(mostRecent, measurement, minValidDate) && mostRecent != null) {
            if (isMaxUln) LabEvaluation.evaluateVersusMaxULN(mostRecent, 1.0) else LabEvaluation.evaluateVersusMinLLN(mostRecent, 1.0)
        } else LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
    }
}