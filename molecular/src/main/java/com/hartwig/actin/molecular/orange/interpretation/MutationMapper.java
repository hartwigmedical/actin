package com.hartwig.actin.molecular.orange.interpretation;

import java.util.Set;

import com.hartwig.actin.molecular.orange.datamodel.protect.ProtectEvidence;

import org.jetbrains.annotations.NotNull;

public interface MutationMapper {

    @NotNull
    Set<String> map(@NotNull ProtectEvidence evidence);
}
