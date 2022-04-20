package com.hartwig.actin.algo.datamodel;

import org.jetbrains.annotations.NotNull;

public enum EvaluationResult {
    PASS,
    WARN,
    FAIL,
    UNDETERMINED,
    NOT_EVALUATED,
    NOT_IMPLEMENTED;

    public boolean isWorseThan(@NotNull EvaluationResult otherResult) {
        switch (otherResult) {
            case NOT_IMPLEMENTED: {
                return false;
            }
            case FAIL: {
                return this == NOT_IMPLEMENTED;
            }
            case WARN: {
                return this == NOT_IMPLEMENTED || this == FAIL;
            }
            case UNDETERMINED: {
                return this == NOT_IMPLEMENTED || this == FAIL || this == WARN;
            }
            case PASS: {
                return this == NOT_IMPLEMENTED || this == FAIL || this == WARN || this == UNDETERMINED;
            }
            case NOT_EVALUATED: {
                return this == NOT_IMPLEMENTED || this == FAIL || this == WARN || this == UNDETERMINED || this == PASS;
            }
            default: {
                throw new IllegalStateException("Cannot compare evaluation result with " + otherResult);
            }
        }
    }
}
