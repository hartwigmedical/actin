package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasLiverAndOrLymphNodeAndOrLungMetastasesOnly : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {

            val hasLiverAndOrLymphNodeAndOrLungMetastases = hasLiverLesions == true || hasLymphNodeLesions == true || hasLungLesions == true
            val hasNoMetastasesOutsideLiverAndLymphNodeAndLung =
                hasCnsLesions == false && hasBrainLesions == false && hasBoneLesions == false && otherLesions?.isEmpty() == true &&
                        hasSuspectedCnsLesions != true && hasSuspectedBrainLesions != true && hasSuspectedBoneLesions != true &&
                        otherSuspectedLesions?.isNotEmpty() == false

            val hasNoLiverAndNoLymphNodeAndNoLungMetastases =
                hasLiverLesions == false && hasLymphNodeLesions == false && hasLungLesions == false
            val hasMetastasesOutsideLiverAndLymphNodeAndLung =
                hasCnsLesions == true || hasBrainLesions == true || hasBoneLesions == true || otherLesions?.isNotEmpty() == true

            val hasSuspectedMetastasesOutsideLiverAndLymphNodeAndLung =
                hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true || hasSuspectedBoneLesions == true ||
                        otherSuspectedLesions?.isNotEmpty() == true

            return when {
                hasLiverAndOrLymphNodeAndOrLungMetastases && hasNoMetastasesOutsideLiverAndLymphNodeAndLung -> {
                    EvaluationFactory.pass("Has only liver and/or lymph node and/or lung metastases")
                }

                hasNoLiverAndNoLymphNodeAndNoLungMetastases || hasMetastasesOutsideLiverAndLymphNodeAndLung -> {
                    EvaluationFactory.fail("Does not have only liver and/or lymph node and/or lung metastases")
                }

                hasSuspectedMetastasesOutsideLiverAndLymphNodeAndLung -> {
                    EvaluationFactory.undetermined("Undetermined if patient has only liver and/or lymph node and/or lung metastases (suspected lesions presence and/or missing lesion data)")
                }

                else -> {
                    EvaluationFactory.undetermined("Undetermined if patient has only liver and/or lymph node and/or lung metastases (missing lesion data)")
                }
            }
        }
    }
}