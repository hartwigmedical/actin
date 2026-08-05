package com.hartwig.actin.algo.evaluation.cardiacfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.icd.IcdConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.icd.IcdModel

class HasEcgAberration(private val icdModel: IcdModel, private val labels: EvaluationLabels.CardiacFunction) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val ecgsIcdCodes = record.ecgs.flatMap { it.icdCodes }
        val cardiacArrhythmiaComorbidities = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities,
            listOf(IcdCode(IcdConstants.CARDIAC_ARRHYTHMIA_BLOCK))
        ).fullMatches.filterNot { it.icdCodes.any { icdCode -> icdCode in ecgsIcdCodes } }

        val aberrations = Format.concat(record.ecgs.map { it.name ?: "details unknown" })

        return when {
            record.ecgs.isNotEmpty() && cardiacArrhythmiaComorbidities.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.hasEcgAberrationRecoverablePassBoth(
                        aberrations, Format.concatItemsWithAnd(cardiacArrhythmiaComorbidities)
                    )
                )
            }

            record.ecgs.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(labels.hasEcgAberrationRecoverablePassEcgOnly(aberrations))
            }

            cardiacArrhythmiaComorbidities.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.hasEcgAberrationRecoverablePassArrhythmiaOnly(Format.concatItemsWithAnd(cardiacArrhythmiaComorbidities))
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.hasEcgAberrationRecoverableFail())
            }
        }
    }
}