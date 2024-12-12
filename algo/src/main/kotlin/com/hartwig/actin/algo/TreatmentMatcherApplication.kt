package com.hartwig.actin.algo

import com.hartwig.actin.PatientPrinter
import com.hartwig.actin.PatientRecordJson
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProviderFactory
import com.hartwig.actin.algo.ckb.EfficacyEntryFactory
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.serialization.TreatmentMatchJson
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.algo.util.TreatmentMatchPrinter
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.molecular.RefGenomeVersion
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.serialization.ServeJson
import java.time.Period
import kotlin.system.exitProcess
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading patient record from {}", config.patientRecordJson)
        val patient = PatientRecordJson.read(config.patientRecordJson)
        PatientPrinter.printRecord(patient)

        LOGGER.info("Loading trials from {}", config.trialDatabaseDirectory)
        val trials = TrialJson.readFromDir(config.trialDatabaseDirectory).filterNot { it.identification.source == TrialSource.LKO }
        LOGGER.info(" Loaded {} trials", trials.size)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, config.runHistorically)
        LOGGER.info("Matching patient to available trials")

        // We assume we never check validity of a gene inside algo.
        val molecularInputChecker = MolecularInputChecker.createAnyGeneValid()
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val functionInputResolver =
            FunctionInputResolver(doidModel, molecularInputChecker, treatmentDatabase, MedicationCategories.create(atcTree))
        val configuration = EnvironmentConfiguration.create(config.overridesYaml).algo
        LOGGER.info(" Loaded algo config: $configuration")

        val maxMolecularTestAge = configuration.maxMolecularTestAgeInDays?.let { referenceDateProvider.date().minus(Period.ofDays(it)) }
        val resources = RuleMappingResources(
            referenceDateProvider,
            doidModel,
            functionInputResolver,
            atcTree,
            treatmentDatabase,
            config.personalizationDataPath,
            configuration,
            maxMolecularTestAge
        )
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        LOGGER.info("Loading evidence database for resistance evidence")
        val tumorDoids = patient.tumor.doids.orEmpty().toSet()
        val evidences = loadEvidence(patient.molecularHistory.latestOrangeMolecularRecord()?.refGenomeVersion ?: RefGenomeVersion.V37)
        val resistanceEvidenceMatcher =
            ResistanceEvidenceMatcher.create(doidModel, tumorDoids, evidences, treatmentDatabase, patient.molecularHistory)
        val match = TreatmentMatcher.create(resources, trials, evidenceEntries, resistanceEvidenceMatcher, maxMolecularTestAge)
            .evaluateAndAnnotateMatchesForPatient(patient)

        TreatmentMatchPrinter.printMatch(match)
        TreatmentMatchJson.write(match, config.outputDirectory)
        LOGGER.info("Done!")
    }

    private fun loadEvidence(refGenomeVersion: RefGenomeVersion): List<EfficacyEvidence> {
        val jsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)
        LOGGER.info("Loading SERVE database from {}", jsonFilePath)
        val serveRecord = ServeLoader.loadServeRecord(jsonFilePath, refGenomeVersion)
        LOGGER.info(" Loaded {} evidences", serveRecord.evidences().size)

        return serveRecord.evidences()
    }

    companion object {
        const val APPLICATION = "ACTIN Treatment Matcher"

        val LOGGER: Logger = LogManager.getLogger(TreatmentMatcherApplication::class.java)
        private val VERSION = TreatmentMatcherApplication::class.java.getPackage().implementationVersion ?: "UNKNOWN VERSION"
    }
}

fun main(args: Array<String>) {
    val options: Options = TreatmentMatcherConfig.createOptions()
    val config: TreatmentMatcherConfig
    try {
        config = TreatmentMatcherConfig.createConfig(DefaultParser().parse(options, args))
    } catch (exception: ParseException) {
        TreatmentMatcherApplication.LOGGER.error(exception)
        HelpFormatter().printHelp(TreatmentMatcherApplication.APPLICATION, options)
        exitProcess(1)
    }

    TreatmentMatcherApplication(config).run()
}