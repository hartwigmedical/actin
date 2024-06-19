package com.hartwig.actin.molecular.driverlikelihood

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.hartwig.actin.molecular.datamodel.GeneRole
import java.io.File
import org.apache.commons.math3.distribution.PoissonDistribution

enum class DndsDriverType {
    INDEL, NONESENSE, MISSENSE, SPLICE
}

data class DndsDatabaseEntry(
    val driversPerSample: Double = 0.0,
    val passengersPerMutation: Double = 0.0,
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

class DndsDatabase(
    private val oncoGeneLookup: Map<String, Map<DndsDriverType, DndsDatabaseEntry>>,
    private val tsgGeneLookup: Map<String, Map<DndsDriverType, DndsDatabaseEntry>>,
) {
    fun find(gene: String, geneRole: GeneRole, driverType: DndsDriverType): DndsDatabaseEntry? {
        return when (geneRole) {
            GeneRole.ONCO -> oncoGeneLookup[gene]?.get(driverType)
            GeneRole.TSG -> tsgGeneLookup[gene]?.get(driverType)
            else -> throw IllegalArgumentException("Can only look up TSG ")
        }
    }

    companion object {
        fun create(oncoDndFilePath: String, tsgDndFilePath: String): DndsDatabase {
            val reader = CsvMapper().apply {
                setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
            }.readerFor(DndsGeneEntry::class.java).with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))

            val oncoGeneLookup = geneLookup(reader, oncoDndFilePath)
            val tsgGeneLookup = geneLookup(reader, tsgDndFilePath)

            return DndsDatabase(oncoGeneLookup, tsgGeneLookup)
        }

        private fun geneLookup(
            reader: ObjectReader,
            oncoDndFilePath: String
        ) = reader.readValues<DndsGeneEntry>(File(oncoDndFilePath)).readAll().groupBy { it.gene }.mapValues {
            it.value.flatMap { geneEntry ->
                listOf(
                    DndsDriverType.NONESENSE to listOf(
                        DndsDatabaseEntry(
                            geneEntry.nonsenseVusDriversPerSample,
                            geneEntry.nonsensePassengersPerMutation,
                            probabilityVariantNonDriver(30000, geneEntry.nonsensePassengersPerMutation)
                        )
                    ),
                    DndsDriverType.INDEL to listOf(
                        DndsDatabaseEntry(
                            geneEntry.indelVusDriversPerSample,
                            geneEntry.indelPassengersPerMutation,
                            probabilityVariantNonDriver(1000, geneEntry.indelPassengersPerMutation)
                        )
                    ),
                    DndsDriverType.MISSENSE to listOf(
                        DndsDatabaseEntry(
                            geneEntry.missenseVusDriversPerSample,
                            geneEntry.missensePassengersPerMutation,
                            probabilityVariantNonDriver(30000, geneEntry.missensePassengersPerMutation)
                        )
                    ),
                    DndsDriverType.SPLICE to listOf(
                        DndsDatabaseEntry(
                            geneEntry.spliceVusDriversPerSample,
                            geneEntry.splicePassengersPerMutation,
                            probabilityVariantNonDriver(30000, geneEntry.splicePassengersPerMutation)
                        )
                    )
                )
            }.groupBy { databaseEntries -> databaseEntries.first }.mapValues { entry -> entry.value.first().second.first() }
        }

        private fun probabilityVariantNonDriver(estimatedMutationCount: Int, passengersPerMutation: Double): Double {
            return 1 - PoissonDistribution(estimatedMutationCount * passengersPerMutation).probability(0)
        }
    }
}
