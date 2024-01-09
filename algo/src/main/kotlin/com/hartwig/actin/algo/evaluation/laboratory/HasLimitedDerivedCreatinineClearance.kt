package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions
import com.hartwig.actin.algo.evaluation.vitalfunction.BodyWeightFunctions.selectMedianBodyWeightPerDay
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasLimitedDerivedCreatinineClearance internal constructor(
    private val referenceYear: Int, private val method: CreatinineClearanceMethod,
    private val maxCreatinineClearance: Double
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
        val weight = selectMedianBodyWeightPerDay(record)?.let { BodyWeightFunctions.determineMedianBodyWeight(it) }
        val cockcroftGault = CreatinineFunctions.calcCockcroftGault(
            record.clinical().patient().birthYear(),
            referenceYear,
            record.clinical().patient().gender(),
            weight,
            creatinine
        )

        val result = evaluateVersusMaxValue(cockcroftGault, creatinine.comparator(), maxCreatinineClearance)

        return when {
            result == EvaluationResult.FAIL && weight == null -> EvaluationFactory.undetermined(
                "Cockcroft-Gault may be above maximum but weight of patient is not known",
                "Cockcroft-Gault may be above max but patient weight unknown"
            )

            result == EvaluationResult.FAIL -> EvaluationFactory.recoverableFail(
                "Cockcroft-Gault above maximum of $maxCreatinineClearance",
                "Cockcroft-Gault above max of $maxCreatinineClearance",
            )

            result == EvaluationResult.UNDETERMINED -> EvaluationFactory.undetermined(
                "Cockcroft-Gault evaluation led to ambiguous results",
                "Cockcroft-Gault evaluation ambiguous"
            )

            result == EvaluationResult.PASS && weight == null -> EvaluationFactory.notEvaluated(
                "Body weight is unknown but Cockcroft-Gault is most likely below maximum of $maxCreatinineClearance",
                "Cockcroft-Gault most likely below max of $maxCreatinineClearance but weight unknown",
            )

            result == EvaluationResult.PASS -> EvaluationFactory.recoverablePass(
                "Cockcroft-Gault is below maximum of $maxCreatinineClearance",
                "Cockcroft-Gault below max of $maxCreatinineClearance",
            )

            else -> recoverable().result(result).build()
        }
    }

    private fun evaluateValues(code: String, values: List<Double>, comparator: String): Evaluation {
        val evaluations = values.map { evaluateVersusMaxValue(it, comparator, maxCreatinineClearance) }.toSet()

        val result = CreatinineFunctions.interpretEGFREvaluations(evaluations)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages("$code exceeds maximum of $maxCreatinineClearance")
                builder.addFailGeneralMessages("$code exceeds max of $maxCreatinineClearance")
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages("$code evaluation led to ambiguous results")
                builder.addUndeterminedGeneralMessages("$code could not be determined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages("$code below maximum of $maxCreatinineClearance")
                builder.addPassGeneralMessages("$code below max of $maxCreatinineClearance")
            }

            else -> {}
        }
        return builder.build()
    }
}