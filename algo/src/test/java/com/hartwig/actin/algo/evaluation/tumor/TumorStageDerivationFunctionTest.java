package com.hartwig.actin.algo.evaluation.tumor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.hartwig.actin.algo.doid.DoidConstants;
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;
import com.hartwig.actin.doid.TestDoidModelFactory;

import org.junit.Before;
import org.junit.Test;

public class TumorStageDerivationFunctionTest {

    private TumorStageDerivationFunction victim;

    @Before
    public void setUp() {
        victim = TumorStageDerivationFunction.create(TestDoidModelFactory.createMinimalTestDoidModel());
    }

    @Test
    public void existingStageIsReturnedWithoutDerivation() {
        for (TumorStage value : TumorStage.values()) {
            assertThat(victim.apply(tumorBuilder(value).build())).containsOnly(value);
        }
    }

    @Test
    public void noDoidsConfiguredReturnsEmptySetOfDerivation() {
        assertThat(victim.apply(ImmutableTumorDetails.builder().stage(null).doids(null).build())).isEmpty();
    }

    @Test
    public void stageIAndIIWhenNoLesions() {
        assertThat(victim.apply(tumorBuilder(null).build())).containsOnly(TumorStage.I, TumorStage.II);
    }

    @Test
    public void stageIIIAndIVWhenOneCategorizedLocation() {
        assertThat(victim.apply(tumorBuilder(null).hasLymphNodeLesions(true).build())).containsOnly(TumorStage.III, TumorStage.IV);
    }

    @Test
    public void stageIVWhenMultipleLesions() {
        assertThat(victim.apply(tumorBuilder(null).hasBoneLesions(true).hasBrainLesions(true).build())).containsOnly(TumorStage.IV);
    }

    private static ImmutableTumorDetails.Builder tumorBuilder(final TumorStage stage) {
        return TumorTestFactory.builder().stage(stage).doids(List.of(DoidConstants.BREAST_CANCER_DOID));
    }
}