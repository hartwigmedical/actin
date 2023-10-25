package com.hartwig.actin.molecular.orange

import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.orange.evidence.EvidenceDatabaseFactory
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMappingFile
import com.hartwig.actin.molecular.orange.interpretation.OrangeInterpreter
import com.hartwig.actin.molecular.serialization.MolecularRecordJson
import com.hartwig.actin.molecular.util.MolecularPrinter
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.serve.datamodel.ActionableEventsLoader
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.KnownEventsLoader
import com.hartwig.serve.datamodel.RefGenome
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import java.io.IOException
import java.util.*

class OrangeInterpreterApplication private constructor(private val config: OrangeInterpreterConfig) {
    @Throws(IOException::class)
    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)
        LOGGER.info("Reading ORANGE json from {}", config.orangeJson)
        val orange = OrangeJson.getInstance().read(config.orangeJson)
        LOGGER.info("Loading evidence database")
        val serveRefGenomeVersion = toServeRefGenomeVersion(orange.refGenomeVersion())
        val knownEvents = KnownEventsLoader.readFromDir(config.serveDirectory, serveRefGenomeVersion)
        val evidenceDatabase = loadEvidenceDatabase(config, serveRefGenomeVersion, knownEvents)
        LOGGER.info("Interpreting ORANGE record")
        val geneFilter = GeneFilterFactory.createFromKnownGenes(knownEvents.genes())
        val molecular = OrangeInterpreter(geneFilter, evidenceDatabase).interpret(orange)
        MolecularPrinter.printRecord(molecular)
        MolecularRecordJson.write(molecular, config.outputDirectory)
        LOGGER.info("Done!")
    }

    companion object {
        private val LOGGER = LogManager.getLogger(OrangeInterpreterApplication::class.java)
        private val APPLICATION: String? = "ACTIN ORANGE Interpreter"
        private val VERSION = OrangeInterpreterApplication::class.java.getPackage().implementationVersion

        @Throws(IOException::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val options: Options = OrangeInterpreterConfig.createOptions()
            var config: OrangeInterpreterConfig? = null
            try {
                config = OrangeInterpreterConfig.createConfig(DefaultParser().parse(options, args))
            } catch (exception: ParseException) {
                LOGGER.warn(exception)
                HelpFormatter().printHelp(APPLICATION, options)
                System.exit(1)
            }
            OrangeInterpreterApplication(config!!).run()
        }

        @Throws(IOException::class)
        private fun loadEvidenceDatabase(config: OrangeInterpreterConfig, serveRefGenomeVersion: RefGenome,
                                         knownEvents: KnownEvents): EvidenceDatabase {
            val actionableEvents = ActionableEventsLoader.readFromDir(config.serveDirectory, serveRefGenomeVersion)
            LOGGER.info("Loading external trial to ACTIN mapping TSV from {}", config.externalTrialMappingTsv)
            val mappings = ExternalTrialMappingFile.read(config.externalTrialMappingTsv)
            LOGGER.info(" Loaded {} mappings", mappings.size)
            LOGGER.info("Loading clinical json from {}", config.clinicalJson)
            val clinical = ClinicalRecordJson.read(config.clinicalJson)
            val tumorDoids = clinical.tumor().doids().orEmpty().filterNotNull().toMutableSet()
            if (tumorDoids.isEmpty()) {
                LOGGER.warn(" No tumor DOIDs configured in ACTIN clinical data for {}!", clinical.patientId())
            } else {
                LOGGER.info(" Tumor DOIDs determined to be: {}", concat(tumorDoids))
            }
            LOGGER.info("Loading DOID tree from {}", config.doidJson)
            val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
            LOGGER.info(" Loaded {} nodes", doidEntry.nodes().size)
            return EvidenceDatabaseFactory.create(knownEvents, actionableEvents, mappings, doidEntry, tumorDoids)
        }

        private fun toServeRefGenomeVersion(refGenomeVersion: OrangeRefGenomeVersion): RefGenome {
            return when (refGenomeVersion) {
                OrangeRefGenomeVersion.V37 -> {
                    RefGenome.V37
                }

                OrangeRefGenomeVersion.V38 -> {
                    RefGenome.V38
                }
            }
            throw IllegalStateException("Could not convert ORANGE ref genome version to SERVE ref genome version: $refGenomeVersion")
        }

        private fun concat(strings: MutableSet<String>): String {
            val joiner = StringJoiner(", ")
            for (string in strings) {
                joiner.add(string)
            }
            return joiner.toString()
        }
    }
}
