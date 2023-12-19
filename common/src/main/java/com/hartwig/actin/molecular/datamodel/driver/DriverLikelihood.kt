package com.hartwig.actin.molecular.datamodel.driver;

public enum DriverLikelihood {
    HIGH,
    MEDIUM,
    LOW;

    public String toString() {
        return name().substring(0, 1).toUpperCase() + name().substring(1).toLowerCase();
    }
}
