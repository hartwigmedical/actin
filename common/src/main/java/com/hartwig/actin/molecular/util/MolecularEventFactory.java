package com.hartwig.actin.molecular.util;

import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptEffect;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;

public final class MolecularEventFactory {

    public static final String MICROSATELLITE_UNSTABLE = "MSI";
    public static final String MICROSATELLITE_POTENTIALLY_UNSTABLE = "Potential MSI";
    public static final String HOMOLOGOUS_REPAIR_DEFICIENT = "HRD";
    public static final String HOMOLOGOUS_REPAIR_POTENTIALLY_DEFICIENT = "Potential HRD";
    public static final String HIGH_TUMOR_MUTATIONAL_BURDEN = "TMB High";
    public static final String ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN = "Almost sufficient TMB";
    public static final String HIGH_TUMOR_MUTATIONAL_LOAD = "TML High";
    public static final String ADEQUATE_TUMOR_MUTATIONAL_LOAD = "TML High";
    public static final String ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD = "Almost sufficient TML";

    private MolecularEventFactory() {
    }

    @NotNull
    public static String event(@NotNull Driver driver) {
        if (driver instanceof Variant) {
            return variantEvent((Variant) driver);
        } else if (driver instanceof Amplification) {
            return amplificationEvent((Amplification) driver);
        } else if (driver instanceof Loss) {
            return lossEvent((Loss) driver);
        } else if (driver instanceof HomozygousDisruption) {
            return homozygousDisruptionEvent((HomozygousDisruption) driver);
        } else if (driver instanceof Disruption) {
            return disruptionEvent((Disruption) driver);
        } else if (driver instanceof Fusion) {
            return fusionEvent((Fusion) driver);
        } else if (driver instanceof Virus) {
            return virusEvent((Virus) driver);
        }

        throw new IllegalStateException("Cannot generate event for driver: " + driver);
    }

    @NotNull
    private static String variantEvent(@NotNull Variant variant) {
        return variant.gene() + " " + transcriptImpactEvent(variant.canonicalImpact());
    }

    @NotNull
    private static String transcriptImpactEvent(@NotNull TranscriptImpact impact) {
        if (!impact.hgvsProteinImpact().isEmpty() && !impact.hgvsProteinImpact().equals("p.?")) {
            return impact.hgvsProteinImpact();
        }

        if (!impact.hgvsCodingImpact().isEmpty()) {
            return impact.codingEffect() == CodingEffect.SPLICE ? impact.hgvsCodingImpact() + " splice" : impact.hgvsCodingImpact();
        }

        StringJoiner effectJoiner = new StringJoiner("&");
        for (TranscriptEffect effect : impact.effects()) {
            effectJoiner.add(effect.toString().toLowerCase());
        }
        return effectJoiner.toString();
    }

    @NotNull
    private static String amplificationEvent(@NotNull Amplification amplification) {
        return amplification.gene() + " amp";
    }

    @NotNull
    private static String lossEvent(@NotNull Loss loss) {
        return loss.gene() + " del";
    }

    @NotNull
    private static String homozygousDisruptionEvent(@NotNull HomozygousDisruption homozygousDisruption) {
        return homozygousDisruption.gene() + " hom disruption";
    }

    @NotNull
    private static String disruptionEvent(@NotNull Disruption disruption) {
        return disruption.gene() + " disruption";
    }

    @NotNull
    private static String fusionEvent(@NotNull Fusion fusion) {
        return fusion.geneStart() + " - " + fusion.geneEnd() + " fusion";
    }

    @NotNull
    private static String virusEvent(@NotNull Virus virus) {
        String interpretation = virus.interpretation();
        return interpretation != null ? interpretation + " positive" : virus.name() + " positive";
    }
}
