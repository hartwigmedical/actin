package com.hartwig.actin.system

import com.hartwig.actin.TreatmentDatabase
import com.hartwig.actin.TreatmentDatabaseFactory
import com.hartwig.actin.algo.calendar.ReferenceDateProvider
import com.hartwig.actin.algo.evaluation.RuleMappingResources
import com.hartwig.actin.algo.soc.ResistanceEvidenceMatcher
import com.hartwig.actin.configuration.AlgoConfiguration
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.doid.DoidModelFactory
import com.hartwig.actin.doid.config.DoidManualConfig
import com.hartwig.actin.doid.serialization.DoidJson
import com.hartwig.actin.medication.AtcTree
import com.hartwig.actin.medication.MedicationCategories
import com.hartwig.actin.molecular.interpretation.MolecularInputChecker
import com.hartwig.actin.trial.input.FunctionInputResolver
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import java.io.File
import java.time.Period

object LocalExampleFunctions {

    private const val TRIAL_SOURCE = "Example"

    private val LOGGER: Logger = LogManager.getLogger(LocalExampleFunctions::class.java)

    fun createExampleEnvironmentConfiguration(): EnvironmentConfiguration {
        val base = EnvironmentConfiguration.create(null)
        return base.copy(
            algo = AlgoConfiguration(trialSource = TRIAL_SOURCE),
            report = ReportConfiguration(
                includeApprovedTreatmentsInSummary = false,
                includeExternalTrialsInSummary = false,
                includeMolecularDetailsChapter = false,
                includeClinicalDetailsChapter = false
            )
        )
    }

    fun createExampleRuleMappingResources(referenceDateProvider: ReferenceDateProvider): RuleMappingResources {
        val resourceDirectory =
            listOf(System.getProperty("user.home"), "hmf", "repos", "actin-resources-private").joinToString(File.separator)

        val doidJson = listOf(resourceDirectory, "disease_ontology", "doid.json").joinToString(File.separator)
        val atcTreeTsv = listOf(resourceDirectory, "atc_config", "atc_tree.tsv").joinToString(File.separator)
        val treatmentDatabaseDir = listOf(resourceDirectory, "treatment_db").joinToString(File.separator)

        LOGGER.info("Loading DOID tree from {}", doidJson)
        val doidEntry = DoidJson.readDoidOwlEntry(doidJson)
        LOGGER.info(" Loaded {} nodes", doidEntry.nodes.size)
        val doidModel = DoidModelFactory.createFromDoidEntry(doidEntry)

        LOGGER.info("Creating ATC tree from file {}", atcTreeTsv)
        val atcTree = AtcTree.createFromFile(atcTreeTsv)

        val treatmentDatabase = TreatmentDatabaseFactory.createFromPath(treatmentDatabaseDir)

        val functionInputResolver =
            FunctionInputResolver(
                doidModel = doidModel,
                molecularInputChecker = MolecularInputChecker.createAnyGeneValid(),
                treatmentDatabase = treatmentDatabase,
                medicationCategories = MedicationCategories.create(atcTree)
            )

        val environmentConfiguration = createExampleEnvironmentConfiguration()

        return RuleMappingResources(
            referenceDateProvider = referenceDateProvider,
            doidModel = doidModel,
            functionInputResolver = functionInputResolver,
            atcTree = atcTree,
            treatmentDatabase = treatmentDatabase,
            personalizationDataPath = null,
            algoConfiguration = environmentConfiguration.algo,
            maxMolecularTestAge = environmentConfiguration.algo.maxMolecularTestAgeInDays?.let {
                referenceDateProvider.date().minus(Period.ofDays(it))
            }
        )
    }

    fun createEmptyResistanceEvidenceMatcher(): ResistanceEvidenceMatcher {
        return ResistanceEvidenceMatcher(
            doidModel = DoidModel(
                childToParentsMap = emptyMap(),
                termForDoidMap = emptyMap(),
                doidForLowerCaseTermMap = emptyMap(),
                doidManualConfig = DoidManualConfig.create()
            ),
            applicableDoids = emptySet(),
            actionableEvents = ImmutableActionableEvents.builder().build(),
            treatmentDatabase = TreatmentDatabase(
                drugsByName = emptyMap(),
                treatmentsByName = emptyMap()
            ),
            molecularHistory = MolecularHistory(molecularTests = emptyList())
        )
    }

    fun systemTestResourcesDirectory(): String {
        return listOf(
            System.getProperty("user.home"),
            "hmf",
            "repos",
            "actin",
            "system",
            "src",
            "test",
            "resources"
        ).joinToString(File.separator)
    }
}