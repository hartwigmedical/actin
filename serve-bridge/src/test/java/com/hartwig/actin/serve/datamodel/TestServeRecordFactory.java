package com.hartwig.actin.serve.datamodel;

import com.hartwig.actin.treatment.datamodel.EligibilityRule;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestServeRecordFactory {

    private TestServeRecordFactory() {
    }

    @NotNull
    public static ImmutableServeRecord.Builder builder() {
        return ImmutableServeRecord.builder()
                .trial(Strings.EMPTY)
                .rule(EligibilityRule.AMPLIFICATION_OF_GENE_X)
                .gene(Strings.EMPTY)
                .isUsedAsInclusion(false);
    }
}
