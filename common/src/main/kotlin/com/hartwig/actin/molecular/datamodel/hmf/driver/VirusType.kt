package com.hartwig.actin.molecular.datamodel.hmf.driver

enum class VirusType {
    MERKEL_CELL_VIRUS,
    EPSTEIN_BARR_VIRUS,
    HUMAN_PAPILLOMA_VIRUS,
    HEPATITIS_B_VIRUS,
    HUMAN_HERPES_VIRUS_8,
    OTHER;

    override fun toString(): String {
        return when (this) {
            MERKEL_CELL_VIRUS -> "MCV"
            EPSTEIN_BARR_VIRUS -> "EBV"
            HUMAN_PAPILLOMA_VIRUS -> "HPV"
            HEPATITIS_B_VIRUS -> "HBV"
            HUMAN_HERPES_VIRUS_8 -> "HHV8"
            else -> "Other"
        }
    }
}
