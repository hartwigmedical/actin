package com.hartwig.actin.algo.datamodel;

import org.jetbrains.annotations.NotNull;

public enum EvaluationResult {
    PASS,
    PASS_BUT_WARN,
    FAIL,
    UNDETERMINED,
    NOT_EVALUATED,
    NOT_IMPLEMENTED;

    public boolean isPass() {
        return this == PASS || this == PASS_BUT_WARN || this == NOT_EVALUATED;
    }

    public boolean isWorseThan(@NotNull EvaluationResult otherResult) {
        switch (otherResult) {
            case FAIL: {
                return false;
            }case UNDETERMINED: {
                return this == FAIL;
            } case NOT_IMPLEMENTED: {
                return this == FAIL || this == UNDETERMINED;
            } case NOT_EVALUATED: {
                return this == FAIL || this == UNDETERMINED || this == NOT_IMPLEMENTED;
            } case PASS_BUT_WARN: {
                return this == FAIL || this == UNDETERMINED || this == NOT_IMPLEMENTED || this == NOT_EVALUATED;
            } case PASS: {
                return this == FAIL || this == UNDETERMINED || this == NOT_IMPLEMENTED || this == NOT_EVALUATED || this == PASS_BUT_WARN;
            } default: {
                throw new IllegalStateException("Cannot compare evaluation result with " + otherResult);
            }
        }
    }
}
