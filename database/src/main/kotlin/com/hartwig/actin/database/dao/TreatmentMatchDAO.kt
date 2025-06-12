package com.hartwig.actin.database.dao

import com.hartwig.actin.database.Tables
import com.hartwig.actin.datamodel.algo.CohortMatch
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.TreatmentMatch
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.trial.util.EligibilityFunctionDisplay
import org.jooq.DSLContext

class TreatmentMatchDAO(private val context: DSLContext) {

    fun clear(treatmentMatch: TreatmentMatch) {
        val patientId = treatmentMatch.patientId
        val treatmentMatchResults =
            context.select(Tables.TREATMENTMATCH.ID).from(Tables.TREATMENTMATCH).where(Tables.TREATMENTMATCH.PATIENTID.eq(patientId))
                .fetch()
        for (treatmentMatchResult in treatmentMatchResults) {
            val treatmentMatchId = treatmentMatchResult.getValue(Tables.TREATMENTMATCH.ID)
            val trialResults =
                context.select(Tables.TRIALMATCH.ID).from(Tables.TRIALMATCH).where(Tables.TRIALMATCH.TREATMENTMATCHID.eq(treatmentMatchId))
                    .fetch()
            for (trialResult in trialResults) {
                val trialMatchId = trialResult.getValue(Tables.TRIALMATCH.ID)
                context.delete(Tables.COHORTMATCH).where(Tables.COHORTMATCH.TRIALMATCHID.eq(trialMatchId)).execute()
                context.delete(Tables.EVALUATION).where(Tables.EVALUATION.TRIALMATCHID.eq(trialMatchId)).execute()
            }
            context.delete(Tables.TRIALMATCH).where(Tables.TRIALMATCH.TREATMENTMATCHID.eq(treatmentMatchId)).execute()
        }
        context.delete(Tables.TREATMENTMATCH).where(Tables.TREATMENTMATCH.PATIENTID.eq(patientId)).execute()
    }

    fun writeTreatmentMatch(treatmentMatch: TreatmentMatch) {
        val treatmentMatchId = context.insertInto(
            Tables.TREATMENTMATCH,
            Tables.TREATMENTMATCH.PATIENTID,
            Tables.TREATMENTMATCH.SAMPLEID,
            Tables.TREATMENTMATCH.REFERENCEDATE,
            Tables.TREATMENTMATCH.REFERENCEDATEISLIVE
        )
            .values(
                treatmentMatch.patientId,
                treatmentMatch.sampleId,
                treatmentMatch.referenceDate,
                treatmentMatch.referenceDateIsLive
            )
            .returning(Tables.TREATMENTMATCH.ID)
            .fetchOne()!!
            .getValue(Tables.TREATMENTMATCH.ID)
        for (trialMatch in treatmentMatch.trialMatches) {
            val trialMatchId = writeTrialMatch(treatmentMatchId, trialMatch)
            writeEvaluations(trialMatchId, null, trialMatch.evaluations)
            for (cohortMatch in trialMatch.cohorts) {
                val cohortMatchId = writeCohortMatch(trialMatchId, cohortMatch)
                writeEvaluations(trialMatchId, cohortMatchId, cohortMatch.evaluations)
            }
        }
    }

    private fun writeTrialMatch(treatmentMatchId: Int, trialMatch: TrialMatch): Int {
        return context.insertInto(
            Tables.TRIALMATCH,
            Tables.TRIALMATCH.TREATMENTMATCHID,
            Tables.TRIALMATCH.CODE,
            Tables.TRIALMATCH.OPEN,
            Tables.TRIALMATCH.ACRONYM,
            Tables.TRIALMATCH.TITLE,
            Tables.TRIALMATCH.ISELIGIBLE
        )
            .values(
                treatmentMatchId,
                trialMatch.identification.trialId,
                trialMatch.identification.open,
                trialMatch.identification.acronym,
                trialMatch.identification.title,
                trialMatch.isPotentiallyEligible
            )
            .returning(Tables.TRIALMATCH.ID)
            .fetchOne()!!
            .getValue(Tables.TRIALMATCH.ID)
    }

    private fun writeCohortMatch(trialMatchId: Int, cohortMatch: CohortMatch): Int {
        return context.insertInto(
            Tables.COHORTMATCH,
            Tables.COHORTMATCH.TRIALMATCHID,
            Tables.COHORTMATCH.CODE,
            Tables.COHORTMATCH.EVALUABLE,
            Tables.COHORTMATCH.OPEN,
            Tables.COHORTMATCH.SLOTSAVAILABLE,
            Tables.COHORTMATCH.IGNORE,
            Tables.COHORTMATCH.DESCRIPTION,
            Tables.TRIALMATCH.ISELIGIBLE
        )
            .values(
                trialMatchId,
                cohortMatch.metadata.cohortId,
                cohortMatch.metadata.evaluable,
                cohortMatch.metadata.open,
                cohortMatch.metadata.slotsAvailable,
                cohortMatch.metadata.ignore,
                cohortMatch.metadata.description,
                cohortMatch.isPotentiallyEligible
            )
            .returning(Tables.COHORTMATCH.ID)
            .fetchOne()!!
            .getValue(Tables.COHORTMATCH.ID)
    }

    private fun writeEvaluations(trialMatchId: Int, cohortMatchId: Int?, evaluations: Map<Eligibility, Evaluation>) {
        for ((key, evaluation) in evaluations) {
            val eligibility = EligibilityFunctionDisplay.format(key.function)
            context.insertInto(
                Tables.EVALUATION,
                Tables.EVALUATION.TRIALMATCHID,
                Tables.EVALUATION.COHORTMATCHID,
                Tables.EVALUATION.ELIGIBILITY,
                Tables.EVALUATION.RESULT,
                Tables.EVALUATION.RECOVERABLE,
                Tables.EVALUATION.INCLUSIONMOLECULAREVENTS,
                Tables.EVALUATION.EXCLUSIONMOLECULAREVENTS,
                Tables.EVALUATION.PASSMESSAGES,
                Tables.EVALUATION.WARNMESSAGES,
                Tables.EVALUATION.UNDETERMINEDMESSAGES,
                Tables.EVALUATION.FAILMESSAGES
            )
                .values(
                    trialMatchId,
                    cohortMatchId,
                    eligibility,
                    evaluation.result.toString(),
                    evaluation.recoverable,
                    DataUtil.concat(evaluation.inclusionMolecularEvents),
                    DataUtil.concat(evaluation.exclusionMolecularEvents),
                    DataUtil.concat(evaluation.passMessages.map { it.toString() }),
                    DataUtil.concat(evaluation.warnMessages.map { it.toString() }),
                    DataUtil.concat(evaluation.undeterminedMessages.map { it.toString() }),
                    DataUtil.concat(evaluation.failMessages.map { it.toString() })
                )
                .execute()
        }
    }
    
}