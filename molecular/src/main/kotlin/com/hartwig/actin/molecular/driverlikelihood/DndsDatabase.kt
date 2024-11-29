package com.hartwig.actin.molecular.driverlikelihood

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.molecular.GeneRole
import org.apache.commons.math3.distribution.PoissonDistribution
import java.io.File

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

private const val ESTIMATED_MUTATION_COUNT_SNV = 30000
private const val ESTIMATED_MUTATION_COUNT_FRAMESHIFT = 1000

class DndsDatabase(
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
        fun create(oncoDndsFilePath: String, tsgDndsFilePath: String): DndsDatabase {
            val reader =
                CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(DndsGeneEntry::class.java)
                    .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))
            val oncoGeneLookup = geneLookup(reader, oncoDndsFilePath)
            val tsgGeneLookup = geneLookup(reader, tsgDndsFilePath)
            return DndsDatabase(oncoGeneLookup, tsgGeneLookup)
        }

        private fun geneLookup(
            reader: ObjectReader,
            oncoDndFilePath: String
        ) = reader.readValues<DndsGeneEntry>(File(oncoDndFilePath)).readAll().groupBy { it.gene }.mapValues {
            it.value.flatMap { geneEntry ->
                listOf(
                    DndsDriverType.NONSENSE to createEntry(
                        geneEntry.nonsenseVusDriversPerSample,
                        geneEntry.nonsensePassengersPerMutation,
                        ESTIMATED_MUTATION_COUNT_SNV
                    ),
                    DndsDriverType.INDEL to createEntry(
                        geneEntry.indelVusDriversPerSample,
                        geneEntry.indelPassengersPerMutation,
                        ESTIMATED_MUTATION_COUNT_FRAMESHIFT,
                    ),
                    DndsDriverType.MISSENSE to createEntry(
                        geneEntry.missenseVusDriversPerSample,
                        geneEntry.missensePassengersPerMutation,
                        ESTIMATED_MUTATION_COUNT_SNV,
                    ),
                    DndsDriverType.SPLICE to createEntry(
                        geneEntry.spliceVusDriversPerSample,
                        geneEntry.splicePassengersPerMutation,
                        ESTIMATED_MUTATION_COUNT_SNV
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
