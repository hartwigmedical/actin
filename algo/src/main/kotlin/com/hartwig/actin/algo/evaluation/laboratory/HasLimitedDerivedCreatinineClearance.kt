package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.selectMedianBodyWeightPerDay
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.clinical.LabValue
import java.time.LocalDate

class HasLimitedDerivedCreatinineClearance internal constructor(
    private val referenceYear: Int, private val method: CreatinineClearanceMethod,
    private val maxCreatinineClearance: Double, private val minimumDateForBodyWeights: LocalDate,
    private val labels: EvaluationLabels.Laboratory
) : SingleLabValueEvaluationFunction {

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
                labels.hasLimitedDerivedCreatinineClearanceRecoverableUndeterminedUnknownWeightFail()
            )

            result == EvaluationResult.FAIL -> EvaluationFactory.recoverableFail(
                labels.hasLimitedDerivedCreatinineClearanceRecoverableFail("Cockcroft-Gault", maxCreatinineClearance)
            )

            result == EvaluationResult.UNDETERMINED -> EvaluationFactory.recoverableUndetermined(
                labels.hasLimitedDerivedCreatinineClearanceRecoverableUndetermined("Cockcroft-Gault")
            )

            result == EvaluationResult.PASS && weight == null -> EvaluationFactory.recoverableUndetermined(
                labels.hasLimitedDerivedCreatinineClearanceRecoverableUndeterminedUnknownWeightPass(maxCreatinineClearance)
            )

            result == EvaluationResult.PASS -> EvaluationFactory.recoverablePass(
                labels.hasLimitedDerivedCreatinineClearancePass("Cockcroft-Gault", maxCreatinineClearance)
            )

            else -> Evaluation(result = result, recoverable = true)
        }
    }

    private fun evaluateValues(code: String, values: List<Double>, comparator: String): Evaluation {
        val evaluations = values.map { evaluateVersusMaxValue(it, comparator, maxCreatinineClearance) }.toSet()

        return when (val result = CreatinineFunctions.interpretEGFREvaluations(evaluations)) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(labels.hasLimitedDerivedCreatinineClearanceRecoverableFail(code, maxCreatinineClearance))
            }

            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(labels.hasLimitedDerivedCreatinineClearanceRecoverableUndetermined(code))
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(labels.hasLimitedDerivedCreatinineClearancePass(code, maxCreatinineClearance))
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}