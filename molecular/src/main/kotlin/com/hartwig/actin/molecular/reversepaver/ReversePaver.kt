package com.hartwig.actin.molecular.reversepaver

import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.hmftools.common.ensemblcache.EnsemblDataCache
import com.hartwig.hmftools.common.genome.refgenome.RefGenomeSource
import com.hartwig.hmftools.pavereverse.BaseSequenceVariants
import com.hartwig.hmftools.pavereverse.ReversePave
import htsjdk.samtools.reference.IndexedFastaSequenceFile
import java.io.File

class ReversePaver(private val reversePave: ReversePave) {

    fun resolve(gene: String, transcript: String?, hgvsImpact: String): BaseSequenceChange {

        val baseSequenceChange = when (hgvsImpact.firstOrNull()) {
            'c' -> {
                val baseSequenceChange = reversePave.calculateDnaVariant(gene, transcript, hgvsImpact)
                baseSequenceChange?.let {
                    BaseSequenceChange(
                        chromosome = it.Chromosome,
                        position = it.Position,
                        ref = it.Ref,
                        alt = it.Alt
                    )
                }
            }

            'p' -> {
                val baseSequenceVariants = reversePave.calculateProteinVariant(gene, transcript, hgvsImpact)
                selectBaseSequenceChange(baseSequenceVariants)
            }

            else -> throw IllegalArgumentException("Invalid HGVS format: $hgvsImpact")
        }

        return baseSequenceChange ?: throw IllegalStateException("Unable to resolve variant '$gene $hgvsImpact' in variant annotator.")
    }

    private fun selectBaseSequenceChange(baseSequenceVariants: BaseSequenceVariants): BaseSequenceChange? {
        return baseSequenceVariants.changes()
            .sortedWith(
                compareBy<com.hartwig.hmftools.pavereverse.BaseSequenceChange>(
                    { kotlin.math.abs(it.Ref.length - it.Alt.length) },
                    { it.Position },
                    { it.Ref },
                    { it.Alt }
                )
            )
            .firstOrNull()
            ?.let { change ->
                BaseSequenceChange(
                    chromosome = change.Chromosome,
                    position = change.Position,
                    ref = change.Ref,
                    alt = change.Alt
                )
            }
    }
}

private fun toHmfRefGenomeVersion(refGenomeVersion: RefGenomeVersion): com.hartwig.hmftools.common.genome.refgenome.RefGenomeVersion {
    return when (refGenomeVersion) {
        RefGenomeVersion.V37 -> {
            com.hartwig.hmftools.common.genome.refgenome.RefGenomeVersion.V37
        }

        RefGenomeVersion.V38 -> {
            com.hartwig.hmftools.common.genome.refgenome.RefGenomeVersion.V38
        }
    }
}

fun reversePaverFactory(referenceGenomeFastaPath: String, ensemblCachePath: String, refGenomeVersion: RefGenomeVersion): ReversePaver {
    val refGenomeSource = RefGenomeSource(IndexedFastaSequenceFile(File(referenceGenomeFastaPath)))
    val ensemblDataCache = EnsemblDataCache(ensemblCachePath, toHmfRefGenomeVersion(refGenomeVersion))
    val reversePave = ReversePave(ensemblDataCache, ensemblCachePath, refGenomeSource)
    return ReversePaver(reversePave)
}