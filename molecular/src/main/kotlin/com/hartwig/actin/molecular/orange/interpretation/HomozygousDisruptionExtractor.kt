package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.datamodel.driver.DriverLikelihood
import com.hartwig.actin.molecular.datamodel.driver.GeneRole
import com.hartwig.actin.molecular.datamodel.driver.HomozygousDisruption
import com.hartwig.actin.molecular.datamodel.driver.ProteinEffect
import com.hartwig.actin.molecular.filter.GeneFilter
import com.hartwig.actin.molecular.sort.driver.HomozygousDisruptionComparator
import com.hartwig.hmftools.datamodel.linx.LinxRecord

internal class HomozygousDisruptionExtractor(private val geneFilter: GeneFilter) {

    fun extractHomozygousDisruptions(linx: LinxRecord): MutableSet<HomozygousDisruption> {
        val relevantHomozygousDisruptions = relevantHomozygousDisruptions(linx)

        relevantHomozygousDisruptions.find { !geneFilter.include(it.gene()) }
            ?.let { homozygousDisruption ->
                throw IllegalStateException(
                    "Filtered a reported homozygous disruption through gene filtering: '${homozygousDisruption.gene()}'. "
                            + "Please make sure '${homozygousDisruption.gene()}' is configured as a known gene."
                )
            }

        return relevantHomozygousDisruptions.map { homozygousDisruption ->
//            val alteration = GeneAlterationFactory.convertAlteration(
//                homozygousDisruption.gene(), evidenceDatabase.geneAlterationForHomozygousDisruption(homozygousDisruption)
//            )
            HomozygousDisruption(
//                gene = alteration.gene,
//                geneRole = alteration.geneRole,
//                proteinEffect = alteration.proteinEffect,
//                isAssociatedWithDrugResistance = alteration.isAssociatedWithDrugResistance,
                gene = homozygousDisruption.gene(),
                geneRole = GeneRole.UNKNOWN,
                proteinEffect = ProteinEffect.UNKNOWN,
                isAssociatedWithDrugResistance = null,
                isReportable = true,
                event = DriverEventFactory.homozygousDisruptionEvent(homozygousDisruption),
                driverLikelihood = DriverLikelihood.HIGH,
//                evidence = ActionableEvidenceFactory.create(evidenceDatabase.evidenceForHomozygousDisruption(homozygousDisruption))!!
                evidence = ActionableEvidenceFactory.createNoEvidence(),
            )
        }
            .toSortedSet(HomozygousDisruptionComparator())
    }

    companion object {
        private fun relevantHomozygousDisruptions(linx: LinxRecord): Set<com.hartwig.hmftools.datamodel.linx.LinxHomozygousDisruption> {
            return listOfNotNull(linx.somaticHomozygousDisruptions(), linx.germlineHomozygousDisruptions()).flatten().toSet()
        }
    }
}
