package com.hartwig.actin.algo.evaluation.molecular;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.algo.datamodel.EvaluationResult;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularUtilTest {

    @Test
    public void canEvaluateNoMatch() {
        assertEquals(EvaluationResult.UNDETERMINED, MolecularUtil.noMatchFound(fail()).result());
        assertEquals(EvaluationResult.FAIL, MolecularUtil.noMatchFound(pass()).result());
    }

    @NotNull
    private static MolecularRecord pass() {
        return withQuality(true);
    }

    @NotNull
    private static MolecularRecord fail() {
        return withQuality(false);
    }

    @NotNull
    private static MolecularRecord withQuality(boolean hasReliableQuality) {
        return ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .hasReliableQuality(hasReliableQuality)
                .build();
    }
}