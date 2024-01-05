package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.clinical.datamodel.BodyWeight
import com.hartwig.actin.clinical.datamodel.Gender
import com.hartwig.actin.clinical.datamodel.LabValue
import java.time.LocalDate
import kotlin.math.pow

internal object CreatinineFunctions {
    private const val DEFAULT_MIN_WEIGHT_FEMALE = 50.0
    private const val DEFAULT_MIN_WEIGHT_MALE = 65.0
    fun calcMDRD(birthYear: Int, referenceYear: Int, gender: Gender, creatinine: LabValue): List<Double> {
        val age = referenceYear - birthYear
        val base = 175 * (creatinine.value / 88.4).pow(-1.154) * age.toDouble().pow(-0.203)
        val adjusted = if (gender == Gender.FEMALE) base * 0.742 else base

        return listOf(adjusted, adjusted * 1.212)
    }

    fun calcCKDEPI(birthYear: Int, referenceYear: Int, gender: Gender, creatinine: LabValue): List<Double> {
        val age = referenceYear - birthYear
        val isFemale = gender == Gender.FEMALE
        val correction = if (isFemale) 61.9 else 79.6
        val power = if (isFemale) -0.329 else -0.411
        val factor1 = (creatinine.value / correction).coerceAtMost(1.0).pow(power)
        val factor2 = (creatinine.value / correction).coerceAtLeast(1.0).pow(-1.209)
        val base = 141 * factor1 * factor2 * 0.993.pow(age.toDouble())
        val adjusted = if (isFemale) base * 1.018 else base
        return listOf(adjusted, adjusted * 1.159)
    }

    fun interpretEGFREvaluations(evaluations: Set<EvaluationResult>): EvaluationResult {
        return if (evaluations.contains(EvaluationResult.FAIL)) {
            if (evaluations.contains(EvaluationResult.PASS)) EvaluationResult.UNDETERMINED else EvaluationResult.FAIL
        } else if (evaluations.contains(EvaluationResult.UNDETERMINED)) {
            EvaluationResult.UNDETERMINED
        } else {
            EvaluationResult.PASS
        }
    }

    fun calcCockcroftGault(
        birthYear: Int, referenceYear: Int, gender: Gender, weight: Double?,
        creatinine: LabValue
    ): Double {
        val isFemale = gender == Gender.FEMALE
        val effectiveWeight = weight ?: if (isFemale) DEFAULT_MIN_WEIGHT_FEMALE else DEFAULT_MIN_WEIGHT_MALE
        val age = referenceYear - birthYear
        val base = (140 - age) * effectiveWeight / (0.81 * creatinine.value)
        return if (isFemale) base * 0.85 else base
    }

    fun determineWeight(bodyWeights: List<BodyWeight>): Double? {
        var weight: Double? = null
        var mostRecentDate: LocalDate? = null
        for (bodyWeight in bodyWeights) {
            if (mostRecentDate == null || bodyWeight.date.isAfter(mostRecentDate)) {
                weight = bodyWeight.value
                mostRecentDate = bodyWeight.date
            }
        }
        return weight
    }
}