package com.hartwig.actin.datamodel.molecular.characteristics

object MolecularCharacteristicEvents {
    const val MICROSATELLITE_UNSTABLE = "MSI"
    const val MICROSATELLITE_STABLE = "MSS"

    const val HOMOLOGOUS_RECOMBINATION_DEFICIENT = "HRD"
    const val HOMOLOGOUS_RECOMBINATION_PROFICIENT = "HRP"

    const val HIGH_TUMOR_MUTATIONAL_BURDEN = "TMB High"
    const val LOW_TUMOR_MUTATIONAL_BURDEN = "TMB Low"
    const val ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_BURDEN = "Almost sufficient TMB"

    const val HIGH_TUMOR_MUTATIONAL_LOAD = "TML High"
    const val LOW_TUMOR_MUTATIONAL_LOAD = "TML Low"
    const val ADEQUATE_TUMOR_MUTATIONAL_LOAD = "TML High"
    const val ALMOST_SUFFICIENT_TUMOR_MUTATIONAL_LOAD = "Almost sufficient TML"
}