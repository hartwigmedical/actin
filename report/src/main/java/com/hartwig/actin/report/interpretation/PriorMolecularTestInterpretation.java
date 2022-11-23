package com.hartwig.actin.report.interpretation;

import java.util.Set;

import com.google.common.collect.Multimap;
import com.hartwig.actin.clinical.datamodel.PriorMolecularTest;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class, Nullable.class })
public abstract class PriorMolecularTestInterpretation {

    @NotNull
    public abstract Multimap<PriorMolecularTestKey, PriorMolecularTest> textBasedPriorTests();

    @NotNull
    public abstract Set<PriorMolecularTest> valueBasedPriorTests();
}
