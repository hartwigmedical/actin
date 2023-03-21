package com.hartwig.actin.algo.evaluation.cardiacfunction;

enum ECGUnit {
    MILLISECONDS("ms");

    private final String symbol;

    ECGUnit(final String symbol) {
        this.symbol = symbol;
    }

    public String symbol() {
        return symbol;
    }
}
