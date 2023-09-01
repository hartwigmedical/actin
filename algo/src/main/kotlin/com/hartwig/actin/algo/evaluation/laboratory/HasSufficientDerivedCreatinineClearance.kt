package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.LabValue

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

        val result = evaluateVersusMinValue(cockcroftGault, creatinine.comparator(), minCreatinineClearance)
        return when {
            result == EvaluationResult.FAIL && weight == null -> EvaluationFactory.undetermined(
                "Cockcroft-Gault may be insufficient but weight of patient is not known",
                "Cockcroft-Gault may be insufficient but patient weight unknown"
            )

            result == EvaluationResult.FAIL -> EvaluationFactory.recoverableFail(
                "Cockcroft-Gault below minimum of $minCreatinineClearance",
                "Cockcroft-Gault below min of $minCreatinineClearance",
            )

            result == EvaluationResult.UNDETERMINED -> EvaluationFactory.undetermined(
                "Cockcroft-Gault evaluation led to ambiguous results",
                "Cockcroft-Gault evaluation ambiguous"
            )

            result == EvaluationResult.PASS && weight == null -> EvaluationFactory.notEvaluated(
                "Body weight is unknown but Cockcroft-Gault most likely above minimum of $minCreatinineClearance",
                "Cockcroft-Gault most likely below min of $minCreatinineClearance but weight unknown",
            )

            result == EvaluationResult.PASS -> EvaluationFactory.recoverablePass(
                "Cockcroft-Gault above minimum of $minCreatinineClearance",
                "Cockcroft-Gault above min of $minCreatinineClearance",
            )

            else -> recoverable().result(result).build()
        }
    }

    private fun evaluateValues(code: String, values: List<Double>, comparator: String): Evaluation {
        val evaluations = values.map { evaluateVersusMinValue(it, comparator, minCreatinineClearance) }.toSet()

        val result = CreatinineFunctions.interpretEGFREvaluations(evaluations)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages("$code below minimum of $minCreatinineClearance")
                builder.addFailGeneralMessages("$code below min of $minCreatinineClearance")
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages("$code evaluation led to ambiguous results")
                builder.addUndeterminedGeneralMessages("$code could not be determined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages("$code exceeds minimum of $minCreatinineClearance")
                builder.addPassGeneralMessages("$code exceeds min of $minCreatinineClearance")
            }

            else -> {}
        }
        return builder.build()
    }
}