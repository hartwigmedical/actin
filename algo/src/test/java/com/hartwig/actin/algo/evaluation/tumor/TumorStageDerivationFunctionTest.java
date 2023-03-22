package com.hartwig.actin.algo.evaluation.tumor;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails;
import com.hartwig.actin.clinical.datamodel.TumorStage;

import org.junit.Before;
import org.junit.Test;

public class TumorStageDerivationFunctionTest {

    private static final String DOID = "doid";
    private TumorStageDerivationFunction victim;

    @Before
    public void setUp() {
        victim = TumorStageDerivationFunction.defaultRules(null);
    }

    @Test
    public void returnsConfiguredStageWhenNotNull() {
        assertThat(victim.apply(ImmutableTumorDetails.builder().stage(TumorStage.IV).doids(List.of(DOID)).build())).isEqualTo(TumorStage.IV);
    }

    @Test
    public void returnsNullWhenNoDoidsConfigured() {
        assertThat(victim.apply(ImmutableTumorDetails.builder().stage(null).doids(null).build())).isNull();
    }

    @Test
    public void returnsStageIIWhenNoLesions() {
        assertThat(victim.apply(ImmutableTumorDetails.builder().stage(null).doids(List.of(DOID)).build())).isEqualTo(TumorStage.II);
    }

}