package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants.ARTERY_DISEASE_BLOCK
import com.hartwig.actin.algo.icd.IcdConstants.HEART_DISEASE_SET
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.clinical.LabValue
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasAdequateOrganFunction(private val minValidDate: LocalDate, private val icdModel: IcdModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val interpretation = LabInterpretation.interpret(record.labValues)

        val llnEvaluation = sequenceOf(
            LabMeasurement.HEMOGLOBIN,
            LabMeasurement.THROMBOCYTES_ABS,
            LabMeasurement.NEUTROPHILS_ABS,
            LabMeasurement.EGFR_MDRD,
            LabMeasurement.EGFR_CKD_EPI
        ).map {
            it to evaluateVersusLimit(
                isMaxUln = false,
                interpretation.mostRecentValue(it, false),
                it
            )
        }.toSet()

        val ulnEvaluation = sequenceOf(
            LabMeasurement.ASPARTATE_AMINOTRANSFERASE,
            LabMeasurement.ALANINE_AMINOTRANSFERASE,
            LabMeasurement.TOTAL_BILIRUBIN,
            LabMeasurement.LACTATE_DEHYDROGENASE
        ).map {
            it to evaluateVersusLimit(
                isMaxUln = true,
                interpretation.mostRecentValue(it),
                it
            )
        }.toSet()

        val (valuesUnderLowerLimit, valuesAboveUpperLimit) = listOf(
            llnEvaluation,
            ulnEvaluation
        ).map { it.filter { e -> e.second == LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN } }

        val undeterminedLabValues = (ulnEvaluation + llnEvaluation)
            .filter { it.second == LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED }
            .map { it.first }

        val significantCardiovascularHistory = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities, (HEART_DISEASE_SET + ARTERY_DISEASE_BLOCK).map { IcdCode(it) }.toSet()
        ).fullMatches

        val messageStart = "Possible inadequate organ function"

        return when {
            valuesUnderLowerLimit.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$messageStart (${Format.concat(valuesUnderLowerLimit.map { it.first.display })} below LLN)"
                )
            }

            valuesAboveUpperLimit.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$messageStart (${Format.concat(valuesAboveUpperLimit.map { it.first.display() })} above ULN)"
                )
            }

            significantCardiovascularHistory.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "$messageStart (cardiovascular disease present: ${significantCardiovascularHistory.joinToString(", ") { it.display() }})"
                )
            }

            undeterminedLabValues.isNotEmpty() -> {
                EvaluationFactory.recoverableUndetermined(
                    "Undetermined if adequate organ function " +
                            "(lab value(s) (${Format.concat(undeterminedLabValues.map { it.display })}) undetermined)"
                )
            }

            else -> {
                EvaluationFactory.recoverablePass("No indication of inadequate organ function")
            }
        }
    }

    private fun evaluateVersusLimit(
        isMaxUln: Boolean,
        mostRecent: LabValue?,
        measurement: LabMeasurement
    ): LabEvaluation.LabEvaluationResult {
        return if (LabEvaluation.isValid(mostRecent, measurement, minValidDate) && mostRecent != null) {
            val limit =
                if (measurement in listOf(LabMeasurement.ASPARTATE_AMINOTRANSFERASE, LabMeasurement.ALANINE_AMINOTRANSFERASE)) 2.0 else 1.0
            if (isMaxUln) LabEvaluation.evaluateVersusMaxULN(mostRecent, limit) else LabEvaluation.evaluateVersusMinLLN(mostRecent, limit)
        } else LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED
    }
}