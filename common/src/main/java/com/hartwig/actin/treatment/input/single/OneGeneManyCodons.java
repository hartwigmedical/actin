package com.hartwig.actin.treatment.input.single;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;

@Value.Immutable
@Value.Style(passAnnotations = { NotNull.class })
public abstract class OneGeneManyCodons {

    @NotNull
    public abstract String geneName();

    @NotNull
    public abstract List<String> codons();
}
