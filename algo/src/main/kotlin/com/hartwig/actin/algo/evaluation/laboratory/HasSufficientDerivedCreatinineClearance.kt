package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import java.time.LocalDate

class HasSufficientDerivedCreatinineClearance internal constructor(
    private val referenceYear: Int, private val method: CreatinineClearanceMethod,
    private val minCreatinineClearance: Double, private val minimumDateForBodyWeights: LocalDate
) : LabEvaluationFunction {

    //TODO: Implement logics for method = "measured"
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
        val weight = BodyWeightFunctions.selectMedianBodyWeightPerDay(record, minimumDateForBodyWeights)
            ?.let { BodyWeightFunctions.determineMedianBodyWeight(it) }
        val cockcroftGault = CreatinineFunctions.calcCockcroftGault(
            record.patient.birthYear,
            referenceYear,
            record.patient.gender,
            weight,
            creatinine
        )

        val result = evaluateVersusMinValue(cockcroftGault, creatinine.comparator, minCreatinineClearance)
        val unit = LabMeasurement.CREATININE.defaultUnit.display()

        return when {
            result == EvaluationResult.FAIL && weight == null -> EvaluationFactory.recoverableUndetermined(
                "eGFR (Cockcroft-Gault) may be insufficient based on creatinine level ($unit) but weight of patient is not known",
                "eGFR (CG) may be insufficient based on creatinine level ($unit) but patient weight unknown"
            )

            result == EvaluationResult.FAIL -> EvaluationFactory.recoverableFail(
                "eGFR (Cockcroft-Gault) below minimum of $minCreatinineClearance",
                "eGFR (Cockcroft-Gault) below min of $minCreatinineClearance",
            )

            result == EvaluationResult.UNDETERMINED -> EvaluationFactory.recoverableUndetermined(
                "eGFR (Cockcroft-Gault) evaluation led to ambiguous results",
                "eGFR (Cockcroft-Gault) evaluation ambiguous"
            )

            result == EvaluationResult.PASS && weight == null -> EvaluationFactory.notEvaluated(
                "Body weight unknown but eGFR (Cockcroft-Gault) based on creatinine level ($unit) most likely above min of $minCreatinineClearance",
                "eGFR (CG) based on creatinine level ($unit) most likely above min of $minCreatinineClearance but weight unknown",
            )

            result == EvaluationResult.PASS -> EvaluationFactory.recoverablePass(
                "eGFR (Cockcroft-Gault) above minimum of $minCreatinineClearance",
                "eGFR (Cockcroft-Gault) above min of $minCreatinineClearance",
            )

            else -> Evaluation(result = result, recoverable = true)
        }
    }

    private fun evaluateValues(code: String, values: List<Double>, comparator: String): Evaluation {
        val evaluations = values.map { evaluateVersusMinValue(it, comparator, minCreatinineClearance) }.toSet()

        return when (val result = CreatinineFunctions.interpretEGFREvaluations(evaluations)) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "eGFR ($code) below minimum of $minCreatinineClearance", "eGFR ($code) below min of $minCreatinineClearance"
                )
            }
            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "eGFR ($code) evaluation led to ambiguous results", "eGFR ($code) could not be determined"
                )
            }
            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "eGFR ($code) above minimum of $minCreatinineClearance", "eGFR ($code) above min of $minCreatinineClearance"
                )
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}