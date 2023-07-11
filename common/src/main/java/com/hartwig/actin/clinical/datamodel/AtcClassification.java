package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;

@Value.Immutable
public interface AtcClassification {

    AtcLevel anatomicalMainGroup();

    AtcLevel therapeuticSubGroup();

    AtcLevel pharmacologicalSubGroup();

    AtcLevel chemicalSubGroup();

    AtcLevel chemicalSubstance();
}
