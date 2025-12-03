package com.hartwig.actin.molecular.util

object GeneConstants {
    val HR_GENES = setOf("BRCA1", "BRCA2", "RAD51C", "PALB2")
    val MMR_GENES = setOf("EPCAM", "MLH1", "MSH2", "MSH6", "PMS2")

    val IHC_FUSION_EVALUABLE_GENES = setOf("ALK", "ROS1")
    val IHC_LOSS_EVALUABLE_GENES = setOf("MLH1", "MSH2", "MSH6", "PMS2", "MTAP")

    val IHC_AMP_EVALUABLE_GENES_TO_PROTEINS = mapOf("ERBB2" to "HER2")

    val IHC_EVALUABLE_GENES = IHC_FUSION_EVALUABLE_GENES + IHC_LOSS_EVALUABLE_GENES + IHC_AMP_EVALUABLE_GENES_TO_PROTEINS.keys

    fun returnProteinForGene(gene: String): String =
        IHC_AMP_EVALUABLE_GENES_TO_PROTEINS.getOrDefault(gene, gene)
}