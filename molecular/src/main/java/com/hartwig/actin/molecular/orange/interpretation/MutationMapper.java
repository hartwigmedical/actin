package com.hartwig.actin.molecular.orange.interpretation;

import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public interface MutationMapper {

    // TODO: Change to a set of strings and map everything.

    @NotNull
    String map(@NotNull TreatmentEvidence evidence);
}
