package com.hartwig.actin.molecular

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.archer.ArcherAnnotator
import com.hartwig.actin.molecular.archer.ArcherInterpreter
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.EvidenceDatabaseFactory
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.orange.MolecularRecordAnnotator
import com.hartwig.actin.molecular.orange.interpretation.OrangeInterpreter
import com.hartwig.actin.molecular.util.MolecularHistoryPrinter
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.serve.datamodel.ActionableEventsLoader
import com.hartwig.serve.datamodel.KnownEvents
import com.hartwig.serve.datamodel.KnownEventsLoader
import com.hartwig.serve.datamodel.RefGenome
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class OrangeInterpreterApplication(private val config: MolecularInterpreterConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical json from {}", config.clinicalJson)
        val clinical = ClinicalRecordJson.read(config.clinicalJson)

        val orangeMolecularRecord = if (config.orangeJson != null) {
            if (config.serveDirectory == null) {
                throw IllegalArgumentException("SERVE directory must be provided when interpreting ORANGE record!")
            }

            LOGGER.info("Reading ORANGE json from {}", config.orangeJson)
            val orange = OrangeJson.getInstance().read(config.orangeJson)

            LOGGER.info("Loading evidence database")
            val serveRefGenomeVersion = toServeRefGenomeVersion(orange.refGenomeVersion())
            val knownEvents = KnownEventsLoader.readFromDir(config.serveDirectory, serveRefGenomeVersion)
            val evidenceDatabase =
                loadEvidenceDatabase(config.serveDirectory, config.doidJson, serveRefGenomeVersion, knownEvents, clinical)

            LOGGER.info("Interpreting ORANGE record")
            val geneFilter = GeneFilterFactory.createFromKnownGenes(knownEvents.genes())
            val orangePipeline = MolecularPipeline(OrangeInterpreter(geneFilter), MolecularRecordAnnotator(evidenceDatabase))

            orangePipeline.run(listOf(orange))
        } else {
            emptyList()
        }
        val archerMolecularRecord = MolecularPipeline(ArcherInterpreter(), ArcherAnnotator()).run(clinical.priorMolecularTests)

        val history = MolecularHistory(orangeMolecularRecord + archerMolecularRecord)
        MolecularHistoryPrinter.printRecord(history)
        val patientRecord = PatientRecordFactory.fromInputs(clinical, history)
        PatientRecordJson.write(patientRecord, config.outputDirectory)

        LOGGER.info("Done!")
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(OrangeInterpreterApplication::class.java)
        const val APPLICATION: String = "ACTIN ORANGE Interpreter"
        private val VERSION = OrangeInterpreterApplication::class.java.getPackage().implementationVersion

        private fun loadEvidenceDatabase(
            serveDirectory: String, doidJson: String, serveRefGenomeVersion: RefGenome,
            knownEvents: KnownEvents, clinical: ClinicalRecord
        ): EvidenceDatabase {
            val actionableEvents = ActionableEventsLoader.readFromDir(serveDirectory, serveRefGenomeVersion)

            val tumorDoids = clinical.tumor.doids.orEmpty().toSet()
            if (tumorDoids.isEmpty()) {
                LOGGER.warn(" No tumor DOIDs configured in ACTIN clinical data for {}!", clinical.patientId)
            } else {
                LOGGER.info(" Tumor DOIDs determined to be: {}", tumorDoids.joinToString(", "))
            }

            LOGGER.info("Loading DOID tree from {}", doidJson)
            val doidEntry = DoidJson.readDoidOwlEntry(doidJson)

            LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
            return EvidenceDatabaseFactory.create(knownEvents, actionableEvents, doidEntry, tumorDoids)
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
        }
    }
}

fun main(args: Array<String>) {
    val options: Options = MolecularInterpreterConfig.createOptions()
    val config: MolecularInterpreterConfig?
    try {
        config = MolecularInterpreterConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        OrangeInterpreterApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(OrangeInterpreterApplication.APPLICATION, options)
        exitProcess(1)
    }

    OrangeInterpreterApplication(config).run()
}
