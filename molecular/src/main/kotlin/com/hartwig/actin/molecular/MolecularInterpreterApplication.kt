package com.hartwig.actin.molecular

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.EvidenceDatabaseFactory
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.orange.MolecularRecordAnnotator
import com.hartwig.actin.molecular.orange.interpretation.OrangeInterpreter
import com.hartwig.actin.molecular.priormoleculartest.PriorMolecularTestInterpreters
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

class MolecularInterpreterApplication(private val config: MolecularInterpreterConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading clinical json from {}", config.clinicalJson)
        val clinical = ClinicalRecordJson.read(config.clinicalJson)

        val orangeMolecularRecord = if (config.orangeJson != null) {

            LOGGER.info("Reading ORANGE json from {}", config.orangeJson)
            val orange = OrangeJson.getInstance().read(config.orangeJson)

            LOGGER.info("Loading evidence database for ORANGE")
            val (knownEvents, evidenceDatabase) = loadEvidence(clinical, orange.refGenomeVersion())

            LOGGER.info("Interpreting ORANGE record")
            val geneFilter = GeneFilterFactory.createFromKnownGenes(knownEvents.genes())
            val orangePipeline = MolecularInterpreter(OrangeInterpreter(geneFilter), MolecularRecordAnnotator(evidenceDatabase))

            orangePipeline.run(listOf(orange))
        } else {
            emptyList()
        }
        LOGGER.info("Loading evidence database for ARCHER")
        val (_, evidenceDatabase) = loadEvidence(clinical, OrangeRefGenomeVersion.V37)
        val clinicalMolecularTests = PriorMolecularTestInterpreters.create(evidenceDatabase).process(clinical.priorMolecularTests)

        val history = MolecularHistory(orangeMolecularRecord + clinicalMolecularTests)
        MolecularHistoryPrinter.printRecord(history)
        val patientRecord = PatientRecordFactory.fromInputs(clinical, history)
        PatientRecordJson.write(patientRecord, config.outputDirectory)

        LOGGER.info("Done!")
    }

    private fun loadEvidence(
        clinical: ClinicalRecord, orangeRefGenomeVersion: OrangeRefGenomeVersion
    ): Pair<KnownEvents, EvidenceDatabase> {
        val serveRefGenomeVersion = toServeRefGenomeVersion(orangeRefGenomeVersion)
        val knownEvents = KnownEventsLoader.readFromDir(config.serveDirectory, serveRefGenomeVersion)
        val evidenceDatabase = loadEvidenceDatabase(config.serveDirectory, config.doidJson, serveRefGenomeVersion, knownEvents, clinical)
        return Pair(knownEvents, evidenceDatabase)
    }

    companion object {
        val LOGGER: Logger = LogManager.getLogger(MolecularInterpreterApplication::class.java)
        const val APPLICATION: String = "ACTIN ORANGE Interpreter"
        private val VERSION = MolecularInterpreterApplication::class.java.getPackage().implementationVersion

        private fun loadEvidenceDatabase(
            serveDirectory: String, doidJson: String, serveRefGenomeVersion: RefGenome, knownEvents: KnownEvents, clinical: ClinicalRecord
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
        MolecularInterpreterApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(MolecularInterpreterApplication.APPLICATION, options)
        exitProcess(1)
    }

    MolecularInterpreterApplication(config).run()
}
