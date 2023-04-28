package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.LabValue
import org.apache.logging.log4j.LogManager

class HasSufficientDerivedCreatinineClearance internal constructor(
    private val referenceYear: Int, private val method: CreatinineClearanceMethod,
    private val minCreatinineClearance: Double
) : LabEvaluationFunction {
    //TODO: Implement logics for method = "measured"
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        return when (method) {
            CreatinineClearanceMethod.EGFR_MDRD -> evaluateMDRD(record, labValue)
            CreatinineClearanceMethod.EGFR_CKD_EPI -> evaluateCKDEPI(record, labValue)
            CreatinineClearanceMethod.COCKCROFT_GAULT -> evaluateCockcroftGault(record, labValue)
            else -> {
                LOGGER.warn("No creatinine clearance function implemented for '{}'", method)
                recoverable().result(EvaluationResult.NOT_IMPLEMENTED).build()
            }
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
        var result = evaluateVersusMinValue(cockcroftGault, creatinine.comparator(), minCreatinineClearance)
        if (weight == null) {
            if (result == EvaluationResult.FAIL) {
                result = EvaluationResult.UNDETERMINED
            } else if (result == EvaluationResult.PASS) {
                result = EvaluationResult.WARN
            }
        }
        val builder = recoverable().result(result)
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages("Cockcroft-Gault is insufficient")
            builder.addFailGeneralMessages("Cockcroft-Gault insufficient")
        } else if (result == EvaluationResult.UNDETERMINED) {
            if (weight == null) {
                builder.addUndeterminedSpecificMessages("Cockcroft-Gault is likely insufficient, but weight of patient is not known")
                builder.addUndeterminedGeneralMessages("Cockcroft-Gault evaluation weight unknown")
            } else {
                builder.addUndeterminedSpecificMessages("Cockcroft-Gault evaluation led to ambiguous results")
                builder.addUndeterminedGeneralMessages("Cockcroft-Gault evaluation ambiguous")
            }
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages("Cockcroft-Gault is sufficient")
            builder.addPassGeneralMessages("Cockcroft-Gault sufficient")
        } else if (result == EvaluationResult.WARN) {
            builder.addWarnSpecificMessages("Cockcroft-Gault is likely sufficient, but body weight of patient is not known")
            builder.addWarnGeneralMessages("Cockcroft-Gault evaluation weight unknown")
        }
        return builder.build()
    }

    private fun evaluateValues(code: String, values: List<Double>, comparator: String): Evaluation {
        val evaluations = values.map { evaluateVersusMinValue(it, comparator, minCreatinineClearance) }.toSet()

        val result = CreatinineFunctions.interpretEGFREvaluations(evaluations)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages("$code is insufficient")
                builder.addFailGeneralMessages("$code insufficient")
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages("$code evaluation led to ambiguous results")
                builder.addUndeterminedGeneralMessages("$code undetermined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages("$code is sufficient")
                builder.addPassGeneralMessages("$code sufficient")
            }

            else -> {}
        }
        return builder.build()
    }

    companion object {
        private val LOGGER = LogManager.getLogger(
            HasSufficientDerivedCreatinineClearance::class.java
        )
    }
}