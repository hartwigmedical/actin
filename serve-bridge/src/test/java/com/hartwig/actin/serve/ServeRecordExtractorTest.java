package com.hartwig.actin.serve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.serve.datamodel.ServeRecord;
import com.hartwig.actin.treatment.datamodel.CohortMetadata;
import com.hartwig.actin.treatment.datamodel.EligibilityRule;
import com.hartwig.actin.treatment.datamodel.ImmutableCohort;
import com.hartwig.actin.treatment.datamodel.ImmutableCohortMetadata;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibility;
import com.hartwig.actin.treatment.datamodel.ImmutableEligibilityFunction;
import com.hartwig.actin.treatment.datamodel.ImmutableTrial;
import com.hartwig.actin.treatment.datamodel.ImmutableTrialIdentification;
import com.hartwig.actin.treatment.datamodel.Trial;
import com.hartwig.actin.treatment.datamodel.TrialIdentification;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ServeRecordExtractorTest {

    @Test
    public void canExtractServeRecords() {
        List<Trial> trials = createTestTrials();

        List<ServeRecord> records = ServeRecordExtractor.extract(trials);

        assertEquals(4, records.size());

        ServeRecord first = find(records, EligibilityRule.ACTIVATING_FUSION_IN_GENE_X);
        assertEquals("trial 1", first.trialId());
        assertNull(first.cohortId());
        assertEquals(Lists.newArrayList("gene 1"), first.parameters());

        ServeRecord second = find(records, EligibilityRule.TMB_OF_AT_LEAST_X);
        assertEquals("trial 1", second.trialId());
        assertNull(second.cohortId());
        assertEquals(Lists.newArrayList("450"), second.parameters());

        ServeRecord third = find(records, EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y);
        assertEquals("trial 2", third.trialId());
        assertNull(third.cohortId());
        assertEquals(Lists.newArrayList("gene 2", "coding"), third.parameters());

        ServeRecord fourth = find(records, EligibilityRule.INACTIVATING_MUTATION_IN_GENE_X);
        assertEquals("trial 2", fourth.trialId());
        assertEquals("cohort 2", fourth.cohortId());
        assertEquals(Lists.newArrayList("gene 3"), fourth.parameters());
    }

    @NotNull
    private static ServeRecord find(@NotNull List<ServeRecord> records, @NotNull EligibilityRule rule) {
        for (ServeRecord record : records) {
            if (record.rule() == rule) {
                return record;
            }
        }

        throw new IllegalStateException("Could not find record with rule '" + rule + "'");
    }

    @NotNull
    private static List<Trial> createTestTrials() {
        List<Trial> trials = Lists.newArrayList();
        trials.add(ImmutableTrial.builder()
                .identification(withTrialId("trial 1"))
                .addGeneralEligibility(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.AND)
                                .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.ACTIVATING_FUSION_IN_GENE_X)
                                        .addParameters("gene 1")
                                        .build())
                                .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.TMB_OF_AT_LEAST_X)
                                        .addParameters("450")
                                        .build())
                                .build())
                        .build())
                .build());

        trials.add(ImmutableTrial.builder()
                .identification(withTrialId("trial 2"))
                .addGeneralEligibility(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.NOT)
                                .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y)
                                        .addParameters("gene 2")
                                        .addParameters("coding")
                                        .build())
                                .build())
                        .build())
                .addGeneralEligibility(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder().rule(EligibilityRule.IS_PREGNANT).build())
                        .build())
                .addCohorts(ImmutableCohort.builder().metadata(withCohortId("cohort 1")).build())
                .addCohorts(ImmutableCohort.builder()
                        .metadata(withCohortId("cohort 2"))
                        .addEligibility(ImmutableEligibility.builder()
                                .function(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.INACTIVATING_MUTATION_IN_GENE_X)
                                        .addParameters("gene 3")
                                        .build())
                                .build())
                        .build())
                .build());

        return trials;
    }

    @NotNull
    private static TrialIdentification withTrialId(@NotNull String trialId) {
        return ImmutableTrialIdentification.builder().trialId(trialId).acronym(Strings.EMPTY).title(Strings.EMPTY).build();
    }

    @NotNull
    private static CohortMetadata withCohortId(@NotNull String cohortId) {
        return ImmutableCohortMetadata.builder().cohortId(cohortId).open(false).description(Strings.EMPTY).build();
    }
}