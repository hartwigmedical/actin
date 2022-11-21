package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.hartwig.serve.datamodel.ActionableEvent;

import org.jetbrains.annotations.NotNull;

public interface EvidenceMatcher<T> {

    @NotNull
    List<ActionableEvent> findMatches(T event);
}
