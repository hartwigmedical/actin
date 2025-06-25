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
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.icd.IcdModel
import com.hartwig.actin.icd.serialization.CsvReader
import com.hartwig.actin.icd.serialization.IcdDeserializer
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.evidence.ServeLoader
import com.hartwig.actin.molecular.evidence.actionability.ActionabilityMatcher
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.actin.trial.serialization.TrialJson
import com.hartwig.serve.datamodel.serialization.ServeJson
import org.apache.commons.cli.DefaultParser
import org.apache.commons.cli.HelpFormatter
import org.apache.commons.cli.Options
import org.apache.commons.cli.ParseException
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.time.Period
import kotlin.system.exitProcess

class TreatmentMatcherApplication(private val config: TreatmentMatcherConfig) {

    fun run() {
        LOGGER.info("Running {} v{}", APPLICATION, VERSION)

        LOGGER.info("Loading patient record from {}", config.patientRecordJson)
        val patient = PatientRecordJson.read(config.patientRecordJson)
        PatientPrinter.printRecord(patient)

        LOGGER.info("Loading trials from {}", config.trialDatabaseDirectory)
        val trials = TrialJson.readFromDir(config.trialDatabaseDirectory)
        LOGGER.info(" Loaded {} trials", trials.size)

        LOGGER.info("Loading DOID tree from {}", config.doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(config.doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ICD-11 tree from file {}", config.icdTsv)
        val icdNodes = IcdDeserializer.deserialize(CsvReader.readFromFile(config.icdTsv))
        LOGGER.info(" Loaded {} nodes", icdNodes.size)
        val icdModel = IcdModel.create(icdNodes)

        LOGGER.info("Creating ATC tree from file {}", config.atcTsv)
        val atcTree = AtcTree.createFromFile(config.atcTsv)

        val referenceDateProvider = ReferenceDateProviderFactory.create(patient, config.runHistorically)
        LOGGER.info("Matching patient to available trials")

        // We assume we never check validity of a gene inside algo.
        val molecularInputChecker = MolecularInputChecker.createAnyGeneValid()
        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(config.treatmentDirectory)
        val functionInputResolver =
            FunctionInputResolver(doidModel, icdModel, molecularInputChecker, treatmentDatabase, MedicationCategories.create(atcTree))
        val configuration = EnvironmentConfiguration.create(config.overridesYaml).algo
        LOGGER.info(" Loaded algo config: $configuration")

        val maxMolecularTestAge = configuration.maxMolecularTestAgeInDays?.let { referenceDateProvider.date().minus(Period.ofDays(it)) }
        val resources = RuleMappingResources(
            referenceDateProvider = referenceDateProvider,
            doidModel = doidModel,
            icdModel = icdModel,
            functionInputResolver = functionInputResolver,
            atcTree = atcTree,
            treatmentDatabase = treatmentDatabase,
            personalizationDataPath = config.personalizationDataPath,
            treatmentEfficacyPredictionJson = config.treatmentEfficacyPredictionJson,
            algoConfiguration = configuration,
            maxMolecularTestAge = maxMolecularTestAge
        )
        val evidenceEntries = EfficacyEntryFactory(treatmentDatabase).extractEfficacyEvidenceFromCkbFile(config.extendedEfficacyJson)

        val serveJsonFilePath = ServeJson.jsonFilePath(config.serveDirectory)
        LOGGER.info("Loading SERVE database for resistance evidence from {}", serveJsonFilePath)
        val serveRecord = ServeLoader.loadServeRecord(
            serveJsonFilePath,
            patient.molecularHistory.latestOrangeMolecularRecord()?.refGenomeVersion ?: RefGenomeVersion.V37
        )
        LOGGER.info(" Loaded {} evidences", serveRecord.evidences().size)
        val resistanceEvidenceMatcher =
            ResistanceEvidenceMatcher.create(
                doidModel = doidModel,
                tumorDoids = patient.tumor.doids.orEmpty().toSet(),
                evidences = serveRecord.evidences(),
                treatmentDatabase = treatmentDatabase,
                molecularHistory = patient.molecularHistory,
                actionabilityMatcher = ActionabilityMatcher(serveRecord.evidences(), serveRecord.trials())
            )

        val treatmentMatcher = TreatmentMatcher.create(resources, trials, evidenceEntries, resistanceEvidenceMatcher, maxMolecularTestAge)
        val treatmentMatch = treatmentMatcher.run(patient)

        LOGGER.info("Printing treatment match")
        TreatmentMatchPrinter.printMatch(treatmentMatch)
        TreatmentMatchJson.write(treatmentMatch, config.outputDirectory)
        LOGGER.info("Done!")
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