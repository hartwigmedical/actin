package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.datamodel.driver.ImmutableMolecularDrivers;
import com.hartwig.actin.molecular.datamodel.driver.Loss;
import com.hartwig.actin.molecular.datamodel.driver.MolecularDrivers;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;

import org.jetbrains.annotations.NotNull;

final class DriverExtraction {

    private DriverExtraction() {
    }

    @NotNull
    public static MolecularDrivers extract(@NotNull OrangeRecord record) {
        // In case purple contains no tumor cells, we wipe all drivers.
        if (!record.purple().containsTumorCells()) {
            return ImmutableMolecularDrivers.builder().build();
        }

        Set<Loss> losses = CopyNumberExtraction.extractLosses(record.purple());

        return ImmutableMolecularDrivers.builder()
                .variants(VariantExtraction.extract(record.purple()))
                .amplifications(CopyNumberExtraction.extractAmplifications(record.purple()))
                .losses(losses)
                .homozygousDisruptions(DisruptionExtraction.extractHomozygousDisruptions(record.linx()))
                .disruptions(DisruptionExtraction.extractDisruptions(record.linx(), losses))
                .fusions(FusionExtraction.extract(record.linx()))
                .viruses(VirusExtraction.extract(record.virusInterpreter()))
                .build();
    }
}
