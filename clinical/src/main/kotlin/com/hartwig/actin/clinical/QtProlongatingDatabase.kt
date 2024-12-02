package com.hartwig.actin.clinical

import com.fasterxml.jackson.dataformat.csv.CsvMapper
import com.fasterxml.jackson.dataformat.csv.CsvSchema
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.hartwig.actin.datamodel.clinical.QTProlongatingRisk
import java.io.File

data class QTProlongatingEntry(
    val name: String,
    val risk: String
)

class QtProlongatingDatabase(private val qtProlongatingRisksPerDrug: Map<String, QTProlongatingRisk>) {

    fun annotateWithQTProlongating(
        medicationName: String
    ): QTProlongatingRisk {
        return qtProlongatingRisksPerDrug[medicationName.lowercase()] ?: QTProlongatingRisk.NONE
    }

    companion object {
        fun read(tsv: String): QtProlongatingDatabase {
            val reader = CsvMapper().apply { registerModule(KotlinModule.Builder().build()) }.readerFor(QTProlongatingEntry::class.java)
                .with(CsvSchema.emptySchema().withHeader().withColumnSeparator('\t'))
            val qtProlongatingRisksPerDrug =
                reader.readValues<QTProlongatingEntry>(File(tsv)).readAll().groupBy { it.name.lowercase() }.map { (name, risks) ->
                    if (risks.size > 1) {
                        throw IllegalStateException(
                            "Multiple risk configurations found for one medication name [$name]. " +
                                    "Check the qt_prolongating.tsv for a duplicate"
                        )
                    } else {
                        val trimmedUppercase = risks.first().risk.trim().replace(" ".toRegex(), "_").uppercase()
                        val qtRisk = try {
                            QTProlongatingRisk.valueOf(trimmedUppercase)
                        } catch (e: IllegalArgumentException) {
                            throw IllegalStateException("Invalid QTProlongatingRisk value: $trimmedUppercase", e)
                        }
                        name.lowercase() to qtRisk
                    }
                }.toMap()
            return QtProlongatingDatabase(qtProlongatingRisksPerDrug)
        }
    }
}