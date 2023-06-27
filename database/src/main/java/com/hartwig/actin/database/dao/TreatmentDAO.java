package com.hartwig.actin.database.dao;

import static com.hartwig.actin.database.Tables.COHORT;
import static com.hartwig.actin.database.Tables.ELIGIBILITY;
import static com.hartwig.actin.database.Tables.ELIGIBILITYREFERENCE;
import static com.hartwig.actin.database.Tables.REFERENCE;
import static com.hartwig.actin.database.Tables.TRIAL;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.hartwig.actin.treatment.datamodel.Cohort;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.CriterionReference;
import com.hartwig.actin.treatment.datamodel.Eligibility;
import com.hartwig.actin.treatment.datamodel.EligibilityFunction;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;
import com.hartwig.actin.treatment.input.composite.CompositeRules;
import com.hartwig.actin.treatment.util.EligibilityFunctionDisplay;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jooq.DSLContext;

class TreatmentDAO {

    @NotNull
    private final DSLContext context;

    public TreatmentDAO(@NotNull final DSLContext context) {
        this.context = context;
    }

    public void clear() {
        context.execute("SET FOREIGN_KEY_CHECKS = 0;");
        context.truncate(TRIAL).execute();
        context.truncate(COHORT).execute();
        context.truncate(ELIGIBILITY).execute();
        context.truncate(REFERENCE).execute();
        context.truncate(ELIGIBILITYREFERENCE).execute();
        context.execute("SET FOREIGN_KEY_CHECKS = 1;");
    }

    public void writeTrial(@NotNull Trial trial) {
        int trialId = writeTrialIdentification(trial.identification());

        Map<CriterionReference, Integer> referenceIdMap = writeReferences(trial);
        writeEligibilities(trialId, null, trial.generalEligibility(), referenceIdMap);

        for (Cohort cohort : trial.cohorts()) {
            int cohortId = writeCohortMetadata(trialId, cohort.metadata());
            writeEligibilities(trialId, cohortId, cohort.eligibility(), referenceIdMap);
        }
    }

    private int writeTrialIdentification(@NotNull TrialIdentification identification) {
        return context.insertInto(TRIAL, TRIAL.CODE, TRIAL.OPEN, TRIAL.ACRONYM, TRIAL.TITLE)
                .values(identification.trialId(), identification.open(), identification.acronym(), identification.title())
                .returning(TRIAL.ID)
                .fetchOne()
                .getValue(TRIAL.ID);
    }

    @NotNull
    private Map<CriterionReference, Integer> writeReferences(@NotNull Trial trial) {
        Set<CriterionReference> references = Sets.newHashSet();
        for (Eligibility eligibility : trial.generalEligibility()) {
            references.addAll(eligibility.references());
        }

        for (Cohort cohort : trial.cohorts()) {
            for (Eligibility eligibility : cohort.eligibility()) {
                references.addAll(eligibility.references());
            }
        }

        Map<CriterionReference, Integer> referenceIdMap = Maps.newHashMap();
        for (CriterionReference reference : references) {
            int id = context.insertInto(REFERENCE, REFERENCE.CODE, REFERENCE.TEXT)
                    .values(reference.id(), reference.text())
                    .returning(REFERENCE.ID)
                    .fetchOne()
                    .getValue(REFERENCE.ID);

            referenceIdMap.put(reference, id);
        }

        return referenceIdMap;
    }

    private int writeCohortMetadata(int trialId, @NotNull CohortMetadata metadata) {
        return context.insertInto(COHORT,
                        COHORT.TRIALID,
                        COHORT.CODE,
                        COHORT.EVALUABLE,
                        COHORT.OPEN,
                        COHORT.SLOTSAVAILABLE,
                        COHORT.BLACKLIST,
                        COHORT.DESCRIPTION)
                .values(trialId,
                        metadata.cohortId(),
                        metadata.evaluable(),
                        metadata.open(),
                        metadata.slotsAvailable(),
                        metadata.blacklist(),
                        metadata.description())
                .returning(COHORT.ID)
                .fetchOne()
                .getValue(COHORT.ID);
    }

    private void writeEligibilities(int trialId, @Nullable Integer cohortId, @NotNull List<Eligibility> eligibilities,
            @NotNull Map<CriterionReference, Integer> referenceIdMap) {
        for (Eligibility eligibility : eligibilities) {
            int id = writeEligibilityFunction(trialId, cohortId, null, eligibility.function());

            for (CriterionReference reference : eligibility.references()) {
                context.insertInto(ELIGIBILITYREFERENCE, ELIGIBILITYREFERENCE.ELIGIBILITYID, ELIGIBILITYREFERENCE.REFERENCEID)
                        .values(id, referenceIdMap.get(reference))
                        .execute();
            }
        }
    }

    private int writeEligibilityFunction(int trialId, @Nullable Integer cohortId, @Nullable Integer parentId,
            @NotNull EligibilityFunction function) {
        boolean isComposite = CompositeRules.isComposite(function.rule());
        String parameters = isComposite ? Strings.EMPTY : DataUtil.concatObjects(function.parameters());

        int id = context.insertInto(ELIGIBILITY,
                        ELIGIBILITY.TRIALID,
                        ELIGIBILITY.COHORTID,
                        ELIGIBILITY.PARENTID,
                        ELIGIBILITY.RULE,
                        ELIGIBILITY.PARAMETERS,
                        ELIGIBILITY.DISPLAY)
                .values(trialId, cohortId, parentId, function.rule().toString(), parameters, EligibilityFunctionDisplay.format(function))
                .returning(ELIGIBILITY.ID)
                .fetchOne()
                .getValue(ELIGIBILITY.ID);

        if (isComposite) {
            for (Object input : function.parameters()) {
                writeEligibilityFunction(trialId, cohortId, id, (EligibilityFunction) input);
            }
        }

        return id;
    }
}
