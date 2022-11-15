package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.datamodel.driver.Disruption;
import com.hartwig.actin.molecular.datamodel.driver.Fusion;
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption;
import com.hartwig.actin.molecular.datamodel.driver.Virus;

import org.jetbrains.annotations.NotNull;

final class EventFactory {

    private EventFactory() {
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
