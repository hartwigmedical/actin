package com.hartwig.actin.molecular.datamodel;

public enum ExperimentType {
    TARGETED {
        @Override
        public String toString() {
            return "Panel analysis";
        }
    },
    WHOLE_GENOME {
        @Override
        public String toString() {
            return "WGS";
        }
    }
}