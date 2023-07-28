package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;

@Value.Immutable
public interface AtcLevel {

    String code();

    String name();
}