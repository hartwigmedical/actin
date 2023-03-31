package com.hartwig.actin.algo.evaluation.othercondition;

import static java.lang.String.format;

import com.hartwig.actin.algo.evaluation.util.Format;

final class PriorConditionMessages {

    enum Characteristic {
        CONDITION,
        TOXICITY,
        COMPLICATION
    }

    static String failSpecific(String doidTerm) {
        return "Patient has no other condition belonging to category " + doidTerm;
    }

    static String failGeneral() {
        return "No relevant non-oncological condition";
    }

    static String passGeneral(String doidTerm) {
        return "Relevant non-oncological condition of category " + doidTerm;
    }

    static String passSpecific(Characteristic characteristic, Iterable<String> matches, String doidTerm) {
        return format("Patient has %s(s) %s, which is indicative of %s",
                characteristic.toString().toLowerCase(),
                Format.concat(matches),
                doidTerm);
    }
}
