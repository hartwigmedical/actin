package com.hartwig.actin.molecular.util;

import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;

public final class MolecularEventFactory {

    public static final String MICROSATELLITE_UNSTABLE = "MSI";
    public static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HRD";
    public static final String HIGH_TUMOR_MUTATIONAL_BURDEN = "TMB High";
    public static final String HIGH_TUMOR_MUTATIONAL_LOAD = "TML High";

    private MolecularEventFactory() {
    }

    @NotNull
    public static String variantEvent(@NotNull Variant variant) {
        return variant.gene() + " " + variant.canonicalImpact().proteinImpact();
    }

    @NotNull
    public static String amplificationEvent(@NotNull Amplification amplification) {
        return amplification.gene() + " amp";
    }

    @NotNull
    public static String lossEvent(@NotNull Loss loss) {
        return loss.gene() + " del";
    }

    @NotNull
    public static String homozygousDisruptionEvent(@NotNull HomozygousDisruption homozygousDisruption) {
        return homozygousDisruption.gene() + " hom disruption";
    }

    @NotNull
    public static String disruptionEvent(@NotNull Disruption disruption) {
        return disruption.gene() + " disruption";
    }

    @NotNull
    public static String fusionEvent(@NotNull Fusion fusion) {
        return fusion.geneStart() + "-" + fusion.geneEnd() + " fusion";
    }

    @NotNull
    public static String virusEvent(@NotNull Virus virus) {
        return virus.name();
    }
}
