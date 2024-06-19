package com.hartwig.actin.molecular.driverlikelihood

import com.fasterxml.jackson.databind.ObjectReader
import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.molecular.datamodel.GeneRole
import java.io.File
import org.apache.commons.math3.distribution.PoissonDistribution

enum class DndsDriverType {
    INDEL, NONESENSE, MISSENSE, SPLICE
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
            val reader =
                CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(DndsGeneEntry::class.java)
                    .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))
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
                    DndsDriverType.NONESENSE to createEntry(
                        geneEntry.nonsenseVusDriversPerSample,
                        geneEntry.nonsensePassengersPerMutation,
                        30000
                    ),
                    DndsDriverType.INDEL to createEntry(
                        geneEntry.indelVusDriversPerSample,
                        geneEntry.indelPassengersPerMutation,
                        1000,
                    ),
                    DndsDriverType.MISSENSE to createEntry(
                        geneEntry.missenseVusDriversPerSample,
                        geneEntry.missensePassengersPerMutation,
                        30000,
                    ),
                    DndsDriverType.SPLICE to createEntry(
                        geneEntry.spliceVusDriversPerSample,
                        geneEntry.splicePassengersPerMutation,
                        30000
                    )
                )
            }.groupBy { databaseEntries -> databaseEntries.first }.mapValues { entry -> entry.value.first().second }
        }

        private fun createEntry(driversBySample: Double, passengersPerMutation: Double, estimatedMutationCount: Int) = DndsDatabaseEntry(
            driversBySample,
            probabilityVariantNonDriver(estimatedMutationCount, passengersPerMutation),
        )

        private fun probabilityVariantNonDriver(estimatedMutationCount: Int, passengersPerMutation: Double): Double {
            return 1 - PoissonDistribution(estimatedMutationCount * passengersPerMutation).probability(0)
        }
    }
}
