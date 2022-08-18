package com.hartwig.actin.algo.molecular;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood;
import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.TestAmplificationFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestHomozygousDisruptionFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestLossFactory;
import com.hartwig.actin.molecular.datamodel.driver.TestVariantFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class MolecularInterpretationTest {

    @Test
    public void canDetermineWhetherGeneIsAmplified() {
        assertFalse(MolecularInterpretation.hasGeneAmplified(TestMolecularFactory.createMinimalTestMolecularRecord(), "gene 1"));

        assertFalse(MolecularInterpretation.hasGeneAmplified(withAmplification("gene 2"), "gene 1"));
        assertTrue(MolecularInterpretation.hasGeneAmplified(withAmplification("gene 1"), "gene 1"));
    }

    @Test
    public void canDetermineWhetherGeneIsInactivated() {
        assertFalse(MolecularInterpretation.hasGeneInactivated(TestMolecularFactory.createMinimalTestMolecularRecord(), "gene 1"));

        assertFalse(MolecularInterpretation.hasGeneInactivated(withLoss("gene 2"), "gene 1"));
        assertTrue(MolecularInterpretation.hasGeneInactivated(withLoss("gene 1"), "gene 1"));

        assertFalse(MolecularInterpretation.hasGeneInactivated(withHomozygousDisruption("gene 2"), "gene 1"));
        assertTrue(MolecularInterpretation.hasGeneInactivated(withHomozygousDisruption("gene 1"), "gene 1"));

        assertFalse(MolecularInterpretation.hasGeneInactivated(withVariant("gene 2", DriverLikelihood.HIGH), "gene 1"));
        assertFalse(MolecularInterpretation.hasGeneInactivated(withVariant("gene 1", DriverLikelihood.LOW), "gene 1"));
        assertTrue(MolecularInterpretation.hasGeneInactivated(withVariant("gene 1", DriverLikelihood.HIGH), "gene 1"));
    }

    @NotNull
    private static MolecularRecord withAmplification(@NotNull String gene) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return ImmutableMolecularRecord.builder()
                .from(base)
                .drivers(ImmutableMolecularDrivers.builder()
                        .from(base.drivers())
                        .addAmplifications(TestAmplificationFactory.builder().gene(gene).build())
                        .build())
                .build();
    }

    @NotNull
    private static MolecularRecord withLoss(@NotNull String gene) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return ImmutableMolecularRecord.builder()
                .from(base)
                .drivers(ImmutableMolecularDrivers.builder()
                        .from(base.drivers())
                        .addLosses(TestLossFactory.builder().gene(gene).build())
                        .build())
                .build();
    }

    @NotNull
    private static MolecularRecord withHomozygousDisruption(@NotNull String gene) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return ImmutableMolecularRecord.builder()
                .from(base)
                .drivers(ImmutableMolecularDrivers.builder()
                        .from(base.drivers())
                        .addHomozygousDisruptions(TestHomozygousDisruptionFactory.builder().gene(gene).build())
                        .build())
                .build();
    }

    @NotNull
    private static MolecularRecord withVariant(@NotNull String gene, @NotNull DriverLikelihood driverLikelihood) {
        MolecularRecord base = TestMolecularFactory.createMinimalTestMolecularRecord();

        return ImmutableMolecularRecord.builder()
                .from(base)
                .drivers(ImmutableMolecularDrivers.builder()
                        .from(base.drivers())
                        .addVariants(TestVariantFactory.builder().gene(gene).driverLikelihood(driverLikelihood).build())
                        .build())
                .build();
    }
}