package com.hartwig.actin.algo.doid.datamodel;

import java.util.List;

import org.immutables.value.Value;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Value.Immutable
@Value.Style(allParameters = true,
             passAnnotations = { NotNull.class, Nullable.class })
public abstract class Metadata {

    @Nullable
    public abstract Definition definition();

    @Nullable
    public abstract List<String> subsets();

    @Nullable
    public abstract List<Xref> xrefs();

    @Nullable
    public abstract List<Synonym> synonyms();

    @Nullable
    public abstract List<BasicPropertyValue> basicPropertyValues();

    @Nullable
    public abstract String snomedConceptId();

}
