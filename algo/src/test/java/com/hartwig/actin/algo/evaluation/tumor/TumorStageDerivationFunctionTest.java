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
    public void shouldReturnEmptySetOfDerivationWhenNoDoidsConfigured() {
        assertThat(victim.apply(ImmutableTumorDetails.builder().stage(null).doids(null).build())).isEmpty();
    }

    @Test
    public void shouldReturnEmptySetOfDerivationWhenNoLesionDetailsConfigured() {
        assertThat(victim.apply(tumorBuilderWithNoStage().build())).isEmpty();
    }

    @Test
    public void stageIAndIIWhenNoLesions() {
        assertThat(victim.apply(tumorBuilderWithNoStage().hasBoneLesions(false)
                .hasBrainLesions(false)
                .hasLiverLesions(false)
                .hasLymphNodeLesions(false)
                .hasBoneLesions(false)
                .hasCnsLesions(false)
                .hasLungLesions(false)
                .build())).containsOnly(TumorStage.I, TumorStage.II);
    }

    @Test
    public void stageIIIAndIVWhenOneCategorizedLocation() {
        assertThat(victim.apply(tumorBuilderWithNoStage().hasLymphNodeLesions(true).build())).containsOnly(TumorStage.III, TumorStage.IV);
    }

    @Test
    public void stageIVWhenMultipleLesions() {
        assertThat(victim.apply(tumorBuilderWithNoStage().hasBoneLesions(true).hasBrainLesions(true).build())).containsOnly(TumorStage.IV);
    }

    private static ImmutableTumorDetails.Builder tumorBuilderWithNoStage() {
        return TumorTestFactory.builder().stage(null).doids(List.of(DoidConstants.BREAST_CANCER_DOID));
    }
}