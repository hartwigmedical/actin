package com.hartwig.actin.clinical.datamodel;

import org.immutables.value.Value;

@Value.Immutable
public interface CypInteraction {

    enum Type {
        INDUCER,
        INHIBITOR,
        SENSITIVE_SUBSTRATE
    }

    enum Strength {
        NONE,
        STRONG,
        MODERATE,
        WEAK
    }

    enum Enzyme {
        CYP
    }

    Type type();

    Strength strength();

    Enzyme enzyme();

    String cyp();
}
