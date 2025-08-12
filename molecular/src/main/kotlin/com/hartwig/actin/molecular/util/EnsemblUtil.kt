package com.hartwig.actin.molecular.util

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.hmftools.common.ensemblcache.EnsemblDataCache

object EnsemblUtil {
    fun canonicalTranscriptIdForGeneOrFail(ensembleDataCache: EnsemblDataCache, gene: String): String {
        val geneData = ensembleDataCache.getGeneDataByName(gene)
            ?: throw IllegalArgumentException("No gene data found for gene $gene")
        return ensembleDataCache.getCanonicalTranscriptData(geneData.GeneId)?.TransName
            ?: throw IllegalStateException("No canonical transcript found for gene $gene")
    }

    fun toHmfRefGenomeVersion(actinRefGenomeVersion: RefGenomeVersion): com.hartwig.hmftools.common.genome.refgenome.RefGenomeVersion {
        return when (actinRefGenomeVersion) {
            RefGenomeVersion.V37 -> com.hartwig.hmftools.common.genome.refgenome.RefGenomeVersion.V37
            RefGenomeVersion.V38 -> com.hartwig.hmftools.common.genome.refgenome.RefGenomeVersion.V38
        }
    }
}