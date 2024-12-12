package com.hartwig.actin.molecular

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.clinical.serialization.ClinicalRecordJson
import com.hartwig.actin.datamodel.clinical.ClinicalRecord
import com.hartwig.actin.datamodel.clinical.PriorIHCTest
import com.hartwig.actin.datamodel.clinical.PriorSequencingTest
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.doid.datamodel.DoidEntry
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.molecular.driverlikelihood.DndsDatabase
import com.hartwig.actin.molecular.driverlikelihood.GeneDriverLikelihoodModel
import com.hartwig.actin.molecular.evidence.EvidenceDatabaseFactory
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.evidence.matching.EvidenceDatabase
import com.hartwig.actin.molecular.evidence.orange.MolecularRecordAnnotator
import com.hartwig.actin.molecular.filter.GeneFilterFactory
import com.hartwig.actin.molecular.orange.OrangeExtractor
import com.hartwig.actin.molecular.panel.IHCAnnotator
import com.hartwig.actin.molecular.panel.IHCExtractor
import com.hartwig.actin.molecular.panel.PanelAnnotator
import com.hartwig.actin.molecular.panel.PanelCopyNumberAnnotator
import com.hartwig.actin.molecular.panel.PanelFusionAnnotator
import com.hartwig.actin.molecular.panel.PanelVariantAnnotator
import com.hartwig.actin.molecular.paver.PaveRefGenomeVersion
import com.hartwig.actin.molecular.paver.Paver
import com.hartwig.actin.molecular.util.MolecularHistoryPrinter
import com.hartwig.actin.tools.ensemblcache.EnsemblDataLoader
import com.hartwig.actin.tools.pave.PaveLite
import com.hartwig.actin.tools.transvar.TransvarVariantAnnotatorFactory
import com.hartwig.hmftools.common.fusion.KnownFusionCache
import com.hartwig.hmftools.datamodel.OrangeJson
import com.hartwig.hmftools.datamodel.orange.OrangeRefGenomeVersion
import com.hartwig.serve.datamodel.RefGenome
import com.hartwig.serve.datamodel.molecular.KnownEvents
import com.hartwig.serve.datamodel.serialization.ServeJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import kotlin.system.exitProcess

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
        val clinicalMolecularTests = interpretClinicalMolecularTests(config, clinical, doidEntry, tumorDoids)

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
        clinical: ClinicalRecord,
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

        LOGGER.info("Loading known fusions from " + config.knownFusionsPath)
        val knownFusionCache = KnownFusionCache()
        if (!knownFusionCache.loadFromFile(config.knownFusionsPath)) {
            throw IllegalArgumentException("Failed to load known fusions from ${config.knownFusionsPath}")
        }

        LOGGER.info("Interpreting {} prior sequencing tests without orange results", clinical.priorSequencingTests.size)
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

        val panelVariantAnnotator = PanelVariantAnnotator(evidenceDatabase, geneDriverLikelihoodModel, variantAnnotator, paver, paveLite)
        val panelFusionAnnotator = PanelFusionAnnotator(evidenceDatabase, knownFusionCache, ensemblDataCache)
        val panelCopyNumberAnnotator = PanelCopyNumberAnnotator(evidenceDatabase, ensemblDataCache)

        val sequencingMolecularTests = interpretPriorSequencingMolecularTests(
            clinical.priorSequencingTests,
            evidenceDatabase,
            panelVariantAnnotator,
            panelFusionAnnotator,
            panelCopyNumberAnnotator
        )

        val ihcMolecularTests = interpretPriorIHCMolecularTests(
            clinical.priorIHCTests,
            panelFusionAnnotator
        )

        LOGGER.info(" Completed interpretation of {} clinical molecular tests", sequencingMolecularTests.size)
        return sequencingMolecularTests + ihcMolecularTests
    }

    private fun interpretPriorSequencingMolecularTests(
        priorSequencingTests: List<PriorSequencingTest>,
        evidenceDatabase: EvidenceDatabase,
        panelVariantAnnotator: PanelVariantAnnotator,
        panelFusionAnnotator: PanelFusionAnnotator,
        panelCopyNumberAnnotator: PanelCopyNumberAnnotator
    ): List<MolecularTest> {
        return MolecularInterpreter(
            extractor = object : MolecularExtractor<PriorSequencingTest, PriorSequencingTest> {
                override fun extract(input: List<PriorSequencingTest>): List<PriorSequencingTest> {
                    return input
                }
            },
            annotator = PanelAnnotator(
                evidenceDatabase,
                panelVariantAnnotator,
                panelFusionAnnotator,
                panelCopyNumberAnnotator
            ),
        ).run(priorSequencingTests)
    }

    private fun interpretPriorIHCMolecularTests(
        priorIHCTests: List<PriorIHCTest>,
        panelFusionAnnotator: PanelFusionAnnotator
    ): List<MolecularTest> {
        return MolecularInterpreter(
            extractor = IHCExtractor(),
            annotator = IHCAnnotator(panelFusionAnnotator),
        ).run(priorIHCTests)
    }

    private fun loadEvidence(
        orangeRefGenomeVersion: OrangeRefGenomeVersion,
        doidEntry: DoidEntry,
        tumorDoids: Set<String>
    ): Pair<KnownEvents, EvidenceDatabase> {
        val serveRefGenomeVersion = toServeRefGenomeVersion(orangeRefGenomeVersion)
        val filePath = ServeJson.jsonFilePath(config.serveDirectory)

        LOGGER.info("Loading SERVE from {}", filePath)
        val (knownEvents, actionableEvents) = ServeLoader.loadServe(filePath, serveRefGenomeVersion)
        val evidenceDatabase = EvidenceDatabaseFactory.create(knownEvents, actionableEvents, doidEntry, tumorDoids)

        return Pair(knownEvents, evidenceDatabase)
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
        const val APPLICATION: String = "ACTIN Molecular Interpreter"

        val LOGGER: Logger = LogManager.getLogger(MolecularInterpreterApplication::class.java)
        private val VERSION = MolecularInterpreterApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = MolecularInterpreterConfig.createOptions()
    val config: MolecularInterpreterConfig
    try {
        config = MolecularInterpreterConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        MolecularInterpreterApplication.LOGGER.warn(exception)
        HelpFormatter().printHelp(MolecularInterpreterApplication.APPLICATION, options)
        exitProcess(1)
    }

    MolecularInterpreterApplication(config).run()
}
