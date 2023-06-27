package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.COHORTMATCH;
import static com.hartwig.actin.database.Tables.EVALUATION;
import static com.hartwig.actin.database.Tables.TREATMENTMATCH;
import static com.hartwig.actin.database.Tables.TRIALMATCH;

import java.util.Map;

import com.hartwig.actin.algo.datamodel.CohortMatch;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialMatch;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Result;

public class TreatmentMatchDAO {

    @NotNull
    private final DSLContext context;

    public TreatmentMatchDAO(@NotNull final DSLContext context) {
        this.context = context;
    }

    public void clear(@NotNull TreatmentMatch treatmentMatch) {
        String patientId = treatmentMatch.patientId();

        Result<Record1<Integer>> treatmentMatchResults =
                context.select(TREATMENTMATCH.ID).from(TREATMENTMATCH).where(TREATMENTMATCH.PATIENTID.eq(patientId)).fetch();
        for (Record treatmentMatchResult : treatmentMatchResults) {
            int treatmentMatchId = treatmentMatchResult.getValue(TREATMENTMATCH.ID);
            Result<Record1<Integer>> trialResults =
                    context.select(TRIALMATCH.ID).from(TRIALMATCH).where(TRIALMATCH.TREATMENTMATCHID.eq(treatmentMatchId)).fetch();
            for (Record trialResult : trialResults) {
                int trialMatchId = trialResult.getValue(TRIALMATCH.ID);
                context.delete(COHORTMATCH).where(COHORTMATCH.TRIALMATCHID.eq(trialMatchId)).execute();
                context.delete(EVALUATION).where(EVALUATION.TRIALMATCHID.eq(trialMatchId)).execute();
            }
            context.delete(TRIALMATCH).where(TRIALMATCH.TREATMENTMATCHID.eq(treatmentMatchId)).execute();
        }

        context.delete(TREATMENTMATCH).where(TREATMENTMATCH.PATIENTID.eq(patientId)).execute();
    }

    public void writeTreatmentMatch(@NotNull TreatmentMatch treatmentMatch) {
        int treatmentMatchId = context.insertInto(TREATMENTMATCH,
                        TREATMENTMATCH.PATIENTID,
                        TREATMENTMATCH.SAMPLEID,
                        TREATMENTMATCH.REFERENCEDATE,
                        TREATMENTMATCH.REFERENCEDATEISLIVE)
                .values(treatmentMatch.patientId(),
                        treatmentMatch.sampleId(),
                        treatmentMatch.referenceDate(),
                        treatmentMatch.referenceDateIsLive())
                .returning(TREATMENTMATCH.ID)
                .fetchOne()
                .getValue(TREATMENTMATCH.ID);

        for (TrialMatch trialMatch : treatmentMatch.trialMatches()) {
            int trialMatchId = writeTrialMatch(treatmentMatchId, trialMatch);

            writeEvaluations(trialMatchId, null, trialMatch.evaluations());
            for (CohortMatch cohortMatch : trialMatch.cohorts()) {
                int cohortMatchId = writeCohortMatch(trialMatchId, cohortMatch);
                writeEvaluations(trialMatchId, cohortMatchId, cohortMatch.evaluations());
            }
        }
    }

    private int writeTrialMatch(int treatmentMatchId, @NotNull TrialMatch trialMatch) {
        return context.insertInto(TRIALMATCH,
                        TRIALMATCH.TREATMENTMATCHID,
                        TRIALMATCH.CODE,
                        TRIALMATCH.OPEN,
                        TRIALMATCH.ACRONYM,
                        TRIALMATCH.TITLE,
                        TRIALMATCH.ISELIGIBLE)
                .values(treatmentMatchId,
                        trialMatch.identification().trialId(),
                        trialMatch.identification().open(),
                        trialMatch.identification().acronym(),
                        trialMatch.identification().title(),
                        trialMatch.isPotentiallyEligible())
                .returning(TRIALMATCH.ID)
                .fetchOne()
                .getValue(TRIALMATCH.ID);
    }

    private int writeCohortMatch(int trialMatchId, @NotNull CohortMatch cohortMatch) {
        return context.insertInto(COHORTMATCH,
                        COHORTMATCH.TRIALMATCHID,
                        COHORTMATCH.CODE,
                        COHORTMATCH.EVALUABLE,
                        COHORTMATCH.OPEN,
                        COHORTMATCH.SLOTSAVAILABLE,
                        COHORTMATCH.BLACKLIST,
                        COHORTMATCH.DESCRIPTION,
                        TRIALMATCH.ISELIGIBLE)
                .values(trialMatchId,
                        cohortMatch.metadata().cohortId(),
                        cohortMatch.metadata().evaluable(),
                        cohortMatch.metadata().open(),
                        cohortMatch.metadata().slotsAvailable(),
                        cohortMatch.metadata().blacklist(),
                        cohortMatch.metadata().description(),
                        cohortMatch.isPotentiallyEligible())
                .returning(COHORTMATCH.ID)
                .fetchOne()
                .getValue(COHORTMATCH.ID);
    }

    private void writeEvaluations(int trialMatchId, @Nullable Integer cohortMatchId, @NotNull Map<Eligibility, Evaluation> evaluations) {
        for (Map.Entry<Eligibility, Evaluation> entry : evaluations.entrySet()) {
            String eligibility = EligibilityFunctionDisplay.format(entry.getKey().function());
            Evaluation evaluation = entry.getValue();

            context.insertInto(EVALUATION,
                            EVALUATION.TRIALMATCHID,
                            EVALUATION.COHORTMATCHID,
                            EVALUATION.ELIGIBILITY,
                            EVALUATION.RESULT,
                            EVALUATION.RECOVERABLE,
                            EVALUATION.INCLUSIONMOLECULAREVENTS,
                            EVALUATION.EXCLUSIONMOLECULAREVENTS,
                            EVALUATION.PASSSPECIFICMESSAGES,
                            EVALUATION.PASSGENERALMESSAGES,
                            EVALUATION.WARNSPECIFICMESSAGES,
                            EVALUATION.WARNGENERALMESSAGES,
                            EVALUATION.UNDETERMINEDSPECIFICMESSAGES,
                            EVALUATION.UNDETERMINEDGENERALMESSAGES,
                            EVALUATION.FAILSPECIFICMESSAGES,
                            EVALUATION.FAILGENERALMESSAGES)
                    .values(trialMatchId,
                            cohortMatchId,
                            eligibility,
                            evaluation.result().toString(),
                            evaluation.recoverable(),
                            DataUtil.concat(evaluation.inclusionMolecularEvents()),
                            DataUtil.concat(evaluation.exclusionMolecularEvents()),
                            DataUtil.concat(evaluation.passSpecificMessages()),
                            DataUtil.concat(evaluation.passGeneralMessages()),
                            DataUtil.concat(evaluation.warnSpecificMessages()),
                            DataUtil.concat(evaluation.warnGeneralMessages()),
                            DataUtil.concat(evaluation.undeterminedSpecificMessages()),
                            DataUtil.concat(evaluation.undeterminedGeneralMessages()),
                            DataUtil.concat(evaluation.failSpecificMessages()),
                            DataUtil.concat(evaluation.failGeneralMessages()))
                    .execute();
        }
    }
}
