package com.hartwig.actin.algo.datamodel;

public enum Evaluation {
    PASS,
    PASS_BUT_WARN,
    FAIL,
    UNDETERMINED,
    IGNORED,
    NOT_IMPLEMENTED;

    public boolean isPass() {
        return this == PASS || this == PASS_BUT_WARN || this == IGNORED;
    }
}
