package com.hartwig.actin.serve;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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

        assertEquals(5, records.size());

        ServeRecord first = find(records, EligibilityRule.FUSION_IN_GENE_X);
        assertEquals("trial 1", first.trial());
        assertEquals("gene 1", first.gene());
        assertNull(first.mutation());
        assertTrue(first.isUsedAsInclusion());

        ServeRecord second = find(records, EligibilityRule.MUTATION_IN_GENE_X_OF_TYPE_Y);
        assertEquals("trial 2", second.trial());
        assertEquals("gene 2", second.gene());
        assertEquals("coding", second.mutation());
        assertFalse(second.isUsedAsInclusion());

        ServeRecord third = find(records, EligibilityRule.INACTIVATION_OF_GENE_X);
        assertEquals("trial 2", third.trial());
        assertEquals("gene 3", third.gene());
        assertNull(third.mutation());
        assertTrue(third.isUsedAsInclusion());

        ServeRecord fourth = find(records, EligibilityRule.TMB_OF_AT_LEAST_X);
        assertEquals("trial 1", fourth.trial());
        assertNull(fourth.gene());
        assertEquals("TMB >= 10", fourth.mutation());
        assertTrue(fourth.isUsedAsInclusion());

        ServeRecord fifth = find(records, EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X);
        assertEquals("trial 3", fifth.trial());
        assertEquals("gene 3", fifth.gene());
        assertNull(fifth.mutation());
        assertFalse(fifth.isUsedAsInclusion());
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
                .identification(withTrialAcronym("trial 1"))
                .addGeneralEligibility(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.AND)
                                .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.FUSION_IN_GENE_X)
                                        .addParameters("gene 1")
                                        .build())
                                .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.TMB_OF_AT_LEAST_X)
                                        .addParameters("10")
                                        .build())
                                .build())
                        .build())
                .build());

        trials.add(ImmutableTrial.builder()
                .identification(withTrialAcronym("trial 2"))
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
                                        .rule(EligibilityRule.INACTIVATION_OF_GENE_X)
                                        .addParameters("gene 3")
                                        .build())
                                .build())
                        .build())
                .build());

        trials.add(ImmutableTrial.builder()
                .identification(withTrialAcronym("trial 3"))
                .addGeneralEligibility(ImmutableEligibility.builder()
                        .function(ImmutableEligibilityFunction.builder()
                                .rule(EligibilityRule.WARN_IF)
                                .addParameters(ImmutableEligibilityFunction.builder()
                                        .rule(EligibilityRule.ACTIVATING_MUTATION_IN_GENE_X)
                                        .addParameters("gene 3")
                                        .build())
                                .build())
                        .build())
                .build());

        return trials;
    }

    @NotNull
    private static TrialIdentification withTrialAcronym(@NotNull String trialAcronym) {
        return ImmutableTrialIdentification.builder().trialId(Strings.EMPTY).acronym(trialAcronym).title(Strings.EMPTY).build();
    }

    @NotNull
    private static CohortMetadata withCohortId(@NotNull String cohortId) {
        return ImmutableCohortMetadata.builder()
                .cohortId(cohortId)
                .open(false)
                .slotsAvailable(false)
                .blacklist(false)
                .description(Strings.EMPTY)
                .build();
    }
}