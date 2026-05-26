package com.hartwig.actin.algo.evaluation.comorbidity

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.toxicity.ToxicityFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.StaticMessage
import com.hartwig.actin.datamodel.clinical.IcdCode
import com.hartwig.actin.datamodel.clinical.Intolerance
import com.hartwig.actin.datamodel.clinical.Toxicity
import com.hartwig.actin.datamodel.clinical.ToxicitySource
import com.hartwig.actin.icd.IcdModel
import java.time.LocalDate

class HasHadComorbidityWithIcdCode(
    private val icdModel: IcdModel,
    private val targetIcdCodes: Set<IcdCode>,
    private val diseaseDescription: String,
    private val referenceDate: LocalDate
) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val (relevantToxicitiesUnknownGrade, relevantToxicities) = ToxicityFunctions.selectRelevantToxicities(
            record,
            icdModel,
            referenceDate = referenceDate,
            emptyList()
        )
            .filter { toxicity -> toxicity.grade == null || toxicity.grade!! >= 2 || toxicity.source == ToxicitySource.QUESTIONNAIRE }
            .partition { toxicity -> toxicity.grade == null }

        val icdMatches = icdModel.findInstancesMatchingAnyIcdCode(
            record.comorbidities.filter { it !is Toxicity } + relevantToxicities,
            targetIcdCodes
        )

        val icdMatchesUnknownGrade = icdModel.findInstancesMatchingAnyIcdCode(relevantToxicitiesUnknownGrade, targetIcdCodes)

        return when {
            icdMatches.fullMatches.isNotEmpty() -> {
                val (intolerances, other) = icdMatches.fullMatches.partition { it is Intolerance }
                val passMessages = listOfNotNull(
                    intolerances.takeIf { it.isNotEmpty() }
                        ?.let { icdMatch -> "Has intolerance to ${Format.concat(icdMatch.map { it.display() })}" },
                    other.takeIf { it.isNotEmpty() }
                        ?.let { icdMatch -> "Has history of ${Format.concat(icdMatch.map { it.display() })}" }
                )
                Evaluation(
                    result = EvaluationResult.PASS,
                    recoverable = false,
                    passMessages = passMessages.map { StaticMessage(it) }.toSet()
                )
            }

            icdMatches.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${Format.concatItemsWithAnd(icdMatches.mainCodeMatchesWithUnknownExtension, true)} " +
                        "but undetermined if history of $diseaseDescription"
            )

            icdMatchesUnknownGrade.fullMatches.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${Format.concatItemsWithAnd(icdMatchesUnknownGrade.fullMatches, true)} " +
                        "but grade of toxicity unknown"
            )

            icdMatchesUnknownGrade.mainCodeMatchesWithUnknownExtension.isNotEmpty() -> EvaluationFactory.undetermined(
                "Has history of ${Format.concatItemsWithAnd(icdMatchesUnknownGrade.mainCodeMatchesWithUnknownExtension, true)} " +
                        "but undetermined if history of $diseaseDescription and grade unknown"
            )

            else -> EvaluationFactory.fail("Has no comorbidity belonging to category $diseaseDescription")
        }
    }
}