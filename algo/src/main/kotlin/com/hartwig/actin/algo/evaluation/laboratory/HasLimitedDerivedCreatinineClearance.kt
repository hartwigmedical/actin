package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.selectMedianBodyWeightPerDay
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedDerivedCreatinineClearance internal constructor(
    private val referenceYear: Int, private val method: CreatinineClearanceMethod,
    private val maxCreatinineClearance: Double, private val minimumDateForBodyWeights: LocalDate
) : LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        return when (method) {
            CreatinineClearanceMethod.EGFR_MDRD -> evaluateMDRD(record, labValue)
            CreatinineClearanceMethod.EGFR_CKD_EPI -> evaluateCKDEPI(record, labValue)
            CreatinineClearanceMethod.COCKCROFT_GAULT -> evaluateCockcroftGault(record, labValue)
        }
    }

    private fun evaluateMDRD(record: PatientRecord, creatinine: LabValue): Evaluation {
        val mdrdValues = CreatinineFunctions.calcMDRD(
            record.patient.birthYear,
            referenceYear,
            record.patient.gender,
            creatinine
        )
        return evaluateValues("MDRD", mdrdValues, creatinine.comparator)
    }

    private fun evaluateCKDEPI(record: PatientRecord, creatinine: LabValue): Evaluation {
        val ckdepiValues = CreatinineFunctions.calcCKDEPI(
            record.patient.birthYear,
            referenceYear,
            record.patient.gender,
            creatinine
        )
        return evaluateValues("CKDEPI", ckdepiValues, creatinine.comparator)
    }

    private fun evaluateCockcroftGault(record: PatientRecord, creatinine: LabValue): Evaluation {
        val weight = selectMedianBodyWeightPerDay(record, minimumDateForBodyWeights)
            ?.let { BodyWeightFunctions.determineMedianBodyWeight(it) }
        val cockcroftGault = CreatinineFunctions.calcCockcroftGault(
            record.patient.birthYear,
            referenceYear,
            record.patient.gender,
            weight,
            creatinine
        )

        val result = evaluateVersusMaxValue(cockcroftGault, creatinine.comparator, maxCreatinineClearance)

        return when {
            result == EvaluationResult.FAIL && weight == null -> EvaluationFactory.recoverableUndetermined(
                "Cockcroft-Gault may be above maximum but weight of patient is not known",
                "Cockcroft-Gault may be above max but patient weight unknown"
            )

            result == EvaluationResult.FAIL -> EvaluationFactory.recoverableFail(
                "Cockcroft-Gault above maximum of $maxCreatinineClearance",
                "Cockcroft-Gault above max of $maxCreatinineClearance",
            )

            result == EvaluationResult.UNDETERMINED -> EvaluationFactory.recoverableUndetermined(
                "Cockcroft-Gault evaluation led to ambiguous results",
                "Cockcroft-Gault evaluation ambiguous"
            )

            result == EvaluationResult.PASS && weight == null -> EvaluationFactory.recoverableUndetermined(
                "Body weight is unknown but Cockcroft-Gault is most likely below maximum of $maxCreatinineClearance",
                "Cockcroft-Gault most likely below max of $maxCreatinineClearance but weight unknown",
            )

            result == EvaluationResult.PASS -> EvaluationFactory.recoverablePass(
                "Cockcroft-Gault is below maximum of $maxCreatinineClearance",
                "Cockcroft-Gault below max of $maxCreatinineClearance",
            )

            else -> Evaluation(result = result, recoverable = true)
        }
    }

    private fun evaluateValues(code: String, values: List<Double>, comparator: String): Evaluation {
        val evaluations = values.map { evaluateVersusMaxValue(it, comparator, maxCreatinineClearance) }.toSet()

        return when (val result = CreatinineFunctions.interpretEGFREvaluations(evaluations)) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "$code exceeds maximum of $maxCreatinineClearance", "$code exceeds max of $maxCreatinineClearance"
                )
            }
            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "$code evaluation led to ambiguous results", "$code could not be determined"
                )
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "$code below maximum of $maxCreatinineClearance", "$code below max of $maxCreatinineClearance"
                )
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}