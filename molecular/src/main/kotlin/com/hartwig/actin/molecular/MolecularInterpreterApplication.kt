package com.hartwig.actin.molecular

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.clinical.datamodel.PriorIHCTest
import com.hartwig.actin.clinical.datamodel.PriorSequencingTest
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.EvidenceDatabaseFactory
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.orange.MolecularRecordAnnotator
import com.hartwig.actin.molecular.orange.interpretation.OrangeExtractor
import com.hartwig.actin.molecular.paver.PaveRefGenomeVersion
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.priormoleculartest.PanelAnnotator
import com.hartwig.actin.molecular.priormoleculartest.PriorMolecularTestInterpreters
import com.hartwig.actin.molecular.priormoleculartest.PriorSequencingExtractor
import com.hartwig.actin.molecular.util.MolecularHistoryPrinter
import com.hartwig.actin.tools.ensemblcache.EnsemblDataLoader
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.transvar.TransvarVariantAnnotatorFactory
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

        val tumorDoids = clinical.tumor.doids.orEmpty().toSet()
        if (tumorDoids.isEmpty()) {
            LOGGER.warn(" No tumor DOIDs configured in ACTIN clinical data for {}!", clinical.patientId)
        } else {
            LOGGER.info(" Tumor DOIDs determined to be: {}", tumorDoids.joinToString(", "))
        }

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)

        val orangeMolecularTests = interpretOrangeRecord(config, doidEntry, tumorDoids)
        val clinicalMolecularTests =
            interpretClinicalMolecularTests(config, clinical.priorIHCTests, clinical.priorSequencingTests, doidEntry, tumorDoids)

        val history = MolecularHistory(orangeMolecularTests + clinicalMolecularTests)
        MolecularHistoryPrinter.printRecord(history)

        val patientRecord = PatientRecordFactory.fromInputs(clinical, history)
        PatientRecordJson.write(patientRecord, config.outputDirectory)

        LOGGER.info("Done!")
    }

    private fun interpretOrangeRecord(
        config: MolecularInterpreterConfig,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): List<MolecularTest> {
        return if (config.orangeJson != null) {
            LOGGER.info("Reading ORANGE json from {}", config.orangeJson)
            val orange = OrangeJson.getInstance().read(config.orangeJson)

            LOGGER.info("Loading evidence database for ORANGE")
            val (knownEvents, evidenceDatabase) = loadEvidence(orange.refGenomeVersion(), doidEntry, tumorDoids)

            LOGGER.info("Interpreting ORANGE record")
            val geneFilter = GeneFilterFactory.createFromKnownGenes(knownEvents.genes())
            val orangeRecordMolecularRecordMolecularInterpreter =
                MolecularInterpreter(OrangeExtractor(geneFilter), MolecularRecordAnnotator(evidenceDatabase))

            orangeRecordMolecularRecordMolecularInterpreter.run(listOf(orange))
        } else {
            emptyList()
        }
    }

    private fun interpretClinicalMolecularTests(
        config: MolecularInterpreterConfig,
        priorIHCTests: List<PriorIHCTest>,
        priorSequencingTests: List<PriorSequencingTest>,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): List<MolecularTest> {
        LOGGER.info("Loading evidence database for clinical molecular tests")
        val (_, evidenceDatabase) = loadEvidence(OrangeRefGenomeVersion.V37, doidEntry, tumorDoids)

        LOGGER.info("Loading ensemble cache from ${config.ensemblCachePath}")
        val ensemblDataCache = EnsemblDataLoader.load(config.ensemblCachePath, com.hartwig.actin.tools.ensemblcache.RefGenome.V37)

        LOGGER.info(
            "Loading dnds database for driver likelihood annotation from " +
                    "${config.oncoDndsDatabasePath} and ${config.tsgDndsDatabasePath}"
        )
        val dndsDatabase = DndsDatabase.create(config.oncoDndsDatabasePath, config.tsgDndsDatabasePath)

        LOGGER.info("Interpreting clinical molecular tests")
        val geneDriverLikelihoodModel = GeneDriverLikelihoodModel(dndsDatabase)
        val variantAnnotator = TransvarVariantAnnotatorFactory.withRefGenome(
            com.hartwig.actin.tools.ensemblcache.RefGenome.V37,
            config.referenceGenomeFastaPath,
            ensemblDataCache
        )
        val paver = Paver(
            config.ensemblCachePath, config.referenceGenomeFastaPath, PaveRefGenomeVersion.V37,
            config.driverGenePanelPath, config.tempDir
        )
        val paveLite = PaveLite(ensemblDataCache, false)
        val clinicalMolecularTests = PriorMolecularTestInterpreters.create(
            evidenceDatabase,
            geneDriverLikelihoodModel,
            variantAnnotator,
            paver,
            paveLite
        ).process(priorIHCTests)
        val sequencingMolecularTests = MolecularInterpreter(
            PriorSequencingExtractor(),
            PanelAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite),
        ).run(priorSequencingTests)
        LOGGER.info(" Completed interpretation of {} clinical molecular tests", clinicalMolecularTests.size)

        return clinicalMolecularTests + sequencingMolecularTests
    }

    private fun loadEvidence(
        orangeRefGenomeVersion: OrangeRefGenomeVersion,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): Pair<KnownEvents, EvidenceDatabase> {
        val serveRefGenomeVersion = toServeRefGenomeVersion(orangeRefGenomeVersion)
        val serveDirectoryWithGenome = "${config.serveDirectory}/${serveRefGenomeVersion.name.lowercase().replace("v", "")}"
        val knownEvents = KnownEventsLoader.readFromDir(
            serveDirectoryWithGenome,
            serveRefGenomeVersion
        )
        val evidenceDatabase = loadEvidenceDatabase(serveDirectoryWithGenome, doidEntry, serveRefGenomeVersion, knownEvents, tumorDoids)
        return Pair(knownEvents, evidenceDatabase)
    }

    private fun loadEvidenceDatabase(
        serveDirectoryWithGenome: String,
        doidEntry: DoidEntry,
        serveRefGenomeVersion: RefGenome,
        knownEvents: KnownEvents,
        tumorDoids: Set<String>
    ): EvidenceDatabase {
        val actionableEvents = ActionableEventsLoader.readFromDir(serveDirectoryWithGenome, serveRefGenomeVersion)

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

    companion object {
        val LOGGER: Logger = LogManager.getLogger(MolecularInterpreterApplication::class.java)
        const val APPLICATION: String = "ACTIN Molecular Interpreter"
        private val VERSION = MolecularInterpreterApplication::class.java.getPackage().implementationVersion
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
