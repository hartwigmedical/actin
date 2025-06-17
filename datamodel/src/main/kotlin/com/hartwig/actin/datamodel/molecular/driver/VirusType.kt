package com.hartwig.actin.datamodel.molecular.driver

enum class VirusType {
    EPSTEIN_BARR_VIRUS,
    HEPATITIS_B_VIRUS,
    HUMAN_HERPES_VIRUS_8,
    HUMAN_PAPILLOMA_VIRUS,
    MERKEL_CELL_VIRUS,
    OTHER;

    override fun toString(): String {
        return when (this) {
            EPSTEIN_BARR_VIRUS -> "EBV"
            HEPATITIS_B_VIRUS -> "HBV"
            HUMAN_HERPES_VIRUS_8 -> "HHV8"
            HUMAN_PAPILLOMA_VIRUS -> "HPV"
            MERKEL_CELL_VIRUS -> "MCV"
            else -> "Other"
        }
    }
}
