package com.hartwig.actin.molecular.datamodel.driver;

public enum VirusType {
    MERKEL_CELL_VIRUS,
    EPSTEIN_BARR_VIRUS,
    HUMAN_PAPILLOMA_VIRUS,
    HEPATITIS_B_VIRUS,
    HUMAN_HERPES_VIRUS_8,
    OTHER;

    public String toString() {
        switch (this) {
            case MERKEL_CELL_VIRUS:
                return "MCV";
            case EPSTEIN_BARR_VIRUS:
                return "EBV";
            case HUMAN_PAPILLOMA_VIRUS:
                return "HPV";
            case HEPATITIS_B_VIRUS:
                return "HBV";
            case HUMAN_HERPES_VIRUS_8:
                return "HHV8";
            default:
                return "Other";
        }
    }
}
