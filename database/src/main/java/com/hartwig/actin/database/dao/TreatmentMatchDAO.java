package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.COHORTMATCH;
import static com.hartwig.actin.database.Tables.EVALUATION;
import static com.hartwig.actin.database.Tables.TRIALMATCH;

import java.util.Map;

import com.hartwig.actin.algo.datamodel.CohortEligibility;
import com.hartwig.actin.algo.datamodel.Evaluation;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.algo.datamodel.TrialEligibility;
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
        String sampleId = treatmentMatch.sampleId();

        Result<Record1<Integer>> result = context.select(TRIALMATCH.ID).from(TRIALMATCH).where(TRIALMATCH.SAMPLEID.eq(sampleId)).fetch();
        for (Record record : result) {
            int trialMatchId = record.getValue(TRIALMATCH.ID);
            context.delete(COHORTMATCH).where(COHORTMATCH.TRIALMATCHID.eq(trialMatchId)).execute();
            context.delete(EVALUATION).where(EVALUATION.TRIALMATCHID.eq(trialMatchId)).execute();
        }

        context.delete(TRIALMATCH).where(TRIALMATCH.SAMPLEID.eq(sampleId)).execute();
    }

    public void writeTreatmentMatch(@NotNull TreatmentMatch treatmentMatch) {
        for (TrialEligibility trialEligibility : treatmentMatch.trialMatches()) {
            int trialMatchId = writeTrialMatch(treatmentMatch.sampleId(), trialEligibility);

            writeEvaluations(trialMatchId, null, trialEligibility.evaluations());
            for (CohortEligibility cohortEligibility : trialEligibility.cohorts()) {
                int cohortMatchId = writeCohortMatch(trialMatchId, cohortEligibility);
                writeEvaluations(trialMatchId, cohortMatchId, cohortEligibility.evaluations());
            }
        }
    }

    private int writeTrialMatch(@NotNull String sampleId, @NotNull TrialEligibility trialEligibility) {
        return context.insertInto(TRIALMATCH, TRIALMATCH.SAMPLEID, TRIALMATCH.CODE, TRIALMATCH.ACRONYM, TRIALMATCH.ISELIGIBLE)
                .values(sampleId,
                        trialEligibility.identification().trialId(),
                        trialEligibility.identification().acronym(),
                        DataUtil.toByte(trialEligibility.isPotentiallyEligible()))
                .returning(TRIALMATCH.ID)
                .fetchOne()
                .getValue(TRIALMATCH.ID);
    }

    private int writeCohortMatch(int trialMatchId, @NotNull CohortEligibility cohortEligibility) {
        return context.insertInto(COHORTMATCH, COHORTMATCH.TRIALMATCHID, COHORTMATCH.CODE, COHORTMATCH.DESCRIPTION, TRIALMATCH.ISELIGIBLE)
                .values(trialMatchId,
                        cohortEligibility.metadata().cohortId(),
                        cohortEligibility.metadata().description(),
                        DataUtil.toByte(cohortEligibility.isPotentiallyEligible()))
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
