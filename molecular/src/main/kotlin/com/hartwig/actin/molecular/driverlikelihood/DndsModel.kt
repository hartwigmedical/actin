package com.hartwig.actin.molecular.driverlikelihood

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.characteristics.TumorMutationalBurden
import com.hartwig.actin.datamodel.molecular.driver.GeneRole
import java.io.File
import org.apache.commons.math3.distribution.PoissonDistribution

enum class DndsDriverType {
    INDEL, NONSENSE, MISSENSE, SPLICE
}

data class DndsDatabaseEntry(
    val driversPerSample: Double = 0.0,
    val probabilityVariantNonDriver: Double = 0.0
)

data class DndsGeneEntry(
    val gene: String,
    val missenseVusDriversPerSample: Double,
    val missensePassengersPerMutation: Double,
    val nonsenseVusDriversPerSample: Double,
    val nonsensePassengersPerMutation: Double,
    val spliceVusDriversPerSample: Double,
    val splicePassengersPerMutation: Double,
    val indelVusDriversPerSample: Double,
    val indelPassengersPerMutation: Double
)

data class DndsDatabase(val oncoDndsGeneEntries: List<DndsGeneEntry>, val tsgDndsGeneEntries: List<DndsGeneEntry>) {
    companion object {
        fun create(oncoDndsPath: String, tsgDndsPath: String): DndsDatabase {
            val reader =
                CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(DndsGeneEntry::class.java)
                    .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))
            return DndsDatabase(
                reader.readValues<DndsGeneEntry>(File(oncoDndsPath)).readAll(),
                reader.readValues<DndsGeneEntry>(File(tsgDndsPath)).readAll()
            )
        }
    }
}

private const val ESTIMATED_TMB = 10.0
private const val ESTIMATED_SNV_TO_INDEL_RATIO = 30
private const val MB_PER_GENOME = 2859

data class VariantEstimates(val snvCount: Int, val indelCount: Int)

fun estimateVariants(tmb: Double, snvToIndelRatio: Int = ESTIMATED_SNV_TO_INDEL_RATIO): VariantEstimates {
    val estimatedTotalVariants = tmb * MB_PER_GENOME
    val indelCount = estimatedTotalVariants / (snvToIndelRatio + 1)
    val snvCount = estimatedTotalVariants - indelCount

    return VariantEstimates(snvCount.toInt(), indelCount.toInt())
}

class DndsModel(
    private val oncoGeneLookup: Map<String, Map<DndsDriverType, DndsDatabaseEntry>>,
    private val tsgGeneLookup: Map<String, Map<DndsDriverType, DndsDatabaseEntry>>,
) {

    fun find(gene: String, geneRole: GeneRole, driverType: DndsDriverType): DndsDatabaseEntry? {
        return when (geneRole) {
            GeneRole.ONCO -> oncoGeneLookup[gene]?.get(driverType)
            GeneRole.TSG -> tsgGeneLookup[gene]?.get(driverType)
            else -> throw IllegalArgumentException("Can only look up TSG or ONCO genes. Other gene roles are not supported")
        }
    }

    companion object {
        fun create(dndsDatabase: DndsDatabase, tumorMutationalBurden: TumorMutationalBurden?): DndsModel {
            val estimates = estimateVariants(tumorMutationalBurden?.score ?: ESTIMATED_TMB)
            return DndsModel(
                geneLookup(dndsDatabase.oncoDndsGeneEntries, estimates),
                geneLookup(dndsDatabase.tsgDndsGeneEntries, estimates)
            )
        }

        private fun geneLookup(
            entries: List<DndsGeneEntry>,
            estimates: VariantEstimates
        ) = entries.groupBy { it.gene }.mapValues {
            it.value.flatMap { geneEntry ->
                listOf(
                    DndsDriverType.NONSENSE to createEntry(
                        geneEntry.nonsenseVusDriversPerSample,
                        geneEntry.nonsensePassengersPerMutation,
                        estimates.snvCount
                    ),
                    DndsDriverType.INDEL to createEntry(
                        geneEntry.indelVusDriversPerSample,
                        geneEntry.indelPassengersPerMutation,
                        estimates.indelCount,
                    ),
                    DndsDriverType.MISSENSE to createEntry(
                        geneEntry.missenseVusDriversPerSample,
                        geneEntry.missensePassengersPerMutation,
                        estimates.snvCount,
                    ),
                    DndsDriverType.SPLICE to createEntry(
                        geneEntry.spliceVusDriversPerSample,
                        geneEntry.splicePassengersPerMutation,
                        estimates.snvCount
                    )
                )
            }.groupBy { databaseEntries -> databaseEntries.first }.mapValues { entry -> entry.value.first().second }
        }

        private fun createEntry(driversBySample: Double, passengersPerMutation: Double, estimatedMutationCount: Int) = DndsDatabaseEntry(
            driversBySample,
            probabilityVariantNonDriver(estimatedMutationCount, passengersPerMutation),
        )

        private fun probabilityVariantNonDriver(estimatedMutationCount: Int, passengersPerMutation: Double): Double {
            val samplePassengers = estimatedMutationCount * passengersPerMutation
            return if (samplePassengers > 0) 1 - PoissonDistribution(samplePassengers).cumulativeProbability(0) else 1.0
        }
    }
}
