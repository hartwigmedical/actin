package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.clinical.datamodel.LabValue

class HasLimitedDerivedCreatinineClearance internal constructor(
    private val referenceYear: Int, private val method: CreatinineClearanceMethod,
    private val maxCreatinineClearance: Double
) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        return when (method) {
            CreatinineClearanceMethod.EGFR_MDRD -> evaluateMDRD(record, labValue)
            CreatinineClearanceMethod.EGFR_CKD_EPI -> evaluateCKDEPI(record, labValue)
            CreatinineClearanceMethod.COCKCROFT_GAULT -> evaluateCockcroftGault(record, labValue)
        }
    }

    private fun evaluateMDRD(record: PatientRecord, creatinine: LabValue): Evaluation {
        val mdrdValues = CreatinineFunctions.calcMDRD(
            record.clinical().patient().birthYear(),
            referenceYear,
            record.clinical().patient().gender(),
            creatinine
        )
        return evaluateValues("MDRD", mdrdValues, creatinine.comparator())
    }

    private fun evaluateCKDEPI(record: PatientRecord, creatinine: LabValue): Evaluation {
        val ckdepiValues = CreatinineFunctions.calcCKDEPI(
            record.clinical().patient().birthYear(),
            referenceYear,
            record.clinical().patient().gender(),
            creatinine
        )
        return evaluateValues("CKDEPI", ckdepiValues, creatinine.comparator())
    }

    private fun evaluateCockcroftGault(record: PatientRecord, creatinine: LabValue): Evaluation {
        val weight = CreatinineFunctions.determineWeight(record.clinical().bodyWeights())
        val cockcroftGault = CreatinineFunctions.calcCockcroftGault(
            record.clinical().patient().birthYear(),
            referenceYear,
            record.clinical().patient().gender(),
            weight,
            creatinine
        )
        var result = evaluateVersusMaxValue(cockcroftGault, creatinine.comparator(), maxCreatinineClearance)
        if (weight == null) {
            if (result == EvaluationResult.FAIL) {
                result = EvaluationResult.UNDETERMINED
            } else if (result == EvaluationResult.PASS) {
                result = EvaluationResult.WARN
            }
        }
        return toEvaluation(result, "Cockcroft-Gault")
    }

    private fun evaluateValues(code: String, values: List<Double>, comparator: String): Evaluation {
        val evaluations = values.map { evaluateVersusMaxValue(it, comparator, maxCreatinineClearance) }.toSet()
        return toEvaluation(CreatinineFunctions.interpretEGFREvaluations(evaluations), code)
    }

    companion object {
        private fun toEvaluation(result: EvaluationResult, code: String): Evaluation {
            val builder = recoverable().result(result)
            when (result) {
                EvaluationResult.FAIL -> {
                    builder.addFailSpecificMessages("$code is too high")
                    builder.addFailGeneralMessages("$code too high")
                }

                EvaluationResult.UNDETERMINED -> {
                    builder.addUndeterminedSpecificMessages("$code evaluation led to ambiguous results")
                    builder.addUndeterminedGeneralMessages("$code undetermined")
                }

                EvaluationResult.PASS -> {
                    builder.addPassSpecificMessages("limited $code")
                    builder.addPassGeneralMessages("limited $code")
                }

                EvaluationResult.WARN -> {
                    builder.addWarnSpecificMessages("limited $code")
                    builder.addWarnGeneralMessages("limited $code")
                }

                else -> {}
            }
            return builder.build()
        }
    }
}