package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.datamodel.immunology.ImmutableMolecularImmunology;
import com.hartwig.actin.molecular.datamodel.immunology.MolecularImmunology;
import com.hartwig.actin.molecular.orange.datamodel.OrangeRecord;

import org.jetbrains.annotations.NotNull;

final class ImmunologyExtraction {

    private ImmunologyExtraction() {
    }

    @NotNull
    public static MolecularImmunology extract(@NotNull OrangeRecord record) {
        return ImmutableMolecularImmunology.builder().build();
    }
}
