package com.hartwig.actin.molecular.orange.interpretation;

import java.util.StringJoiner;

import com.hartwig.actin.molecular.datamodel.driver.Amplification;
import com.hartwig.actin.molecular.datamodel.driver.CodingEffect;
import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Driver;
import com.hartwig.actin.molecular.datamodel.driver.Effect;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.TranscriptImpact;
import com.hartwig.actin.molecular.datamodel.driver.Variant;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;

final class EventFactory {

    private EventFactory() {
    }

    // TODO Refactor to be used in ORANGE datamodel and use.
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
        for (Effect effect : impact.effects()) {
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
