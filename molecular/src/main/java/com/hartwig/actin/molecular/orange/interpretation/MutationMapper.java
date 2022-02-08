package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.orange.datamodel.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;

public interface MutationMapper {

    @NotNull
    Set<String> map(@NotNull TreatmentEvidence evidence);
}
