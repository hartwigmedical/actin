## ACTIN-Molecular

ACTIN-Molecular interprets molecular results and maps to the datamodel described below. This molecular model is written to a per-sample 
json file. Currently, ACTIN-Molecular only supports interpretation of [ORANGE](https://github.com/hartwigmedical/hmftools/tree/master/orange) 
results as produced by [HMF Platinum](https://github.com/hartwigmedical/platinum) 

This application requires Java 11+ and can be run as follows: 

```
java -cp actin.jar com.hartwig.actin.molecular.orange.OrangeInterpreterApplication \
   -orange_json /path/to/orange.json
   -output_directory /path/to/where/molecular_json_file_is_written
```
## Molecular Datamodel

The following general fields about a molecular experiment can be populated per sample

Field | Example Value | Details
---|---|---
sampleId | ACTN01029999T | Unique identifier for the sample / biopsy.
type | WGS | The type of molecular experiment done. Currently only 'WGS' is supported.
date | 2022-01-14 | The date on which the molecular results were obtained (optional field).
hasReliabilityQuality | 1 | Indicates whether the molecular results can be trusted or need to be interpreted with caution. 
 
The following data is used for matching against ACTIN's treatment eligibility criteria.
Do note that ACTIN itself does not make assumptions about the exact definition of the terms below. 
These decisions are all up to the algorithm interpreting the molecular data and converting this interpretation to the datamodel 
that is described here.

Field | Example Value | Details
---|---|---
mutations | BRAF V600E, EGFR exon 19 deletion | A list of gene-specific mutations.
activatedGenes | KRAS, NRAS | A list of genes considered to be activated.
inactivatedGenes | TP53, RB1 | A list of genes considered to be inactivated (along with a boolean whether they have been deleted completely).
amplifiedGenes | MYC | A list of genes considered to be amplified.
wildtypeGenes | BRAF | A list of genes considered to be wildtype. 
fusions | EML4-ALK | A list of genes considered to be fused together. 
isMicrosatelliteUnstable | 0 | If 1, sample is considered microsatellite unstable
isHomologousRepairDeficient | 0 | If 1, sample is considered homologous repair deficient.
tumorMutationalBurden | 14.2 | Number of mutations in the genome per Mb.
tumorMutationalLoad | 115 | Number of missense mutations across the genome.

The following data is not used in ACTIN's treatment matching but can be used to provide additional context in the ACTIN report. 
Along with evidence matched against the ACTIN treatment database, the datamodel can hold additional trial and general evidence from 
external sources.

Field | Example Value | Details
---|---|---
actinTreatmentEvidence | BRAF V600E -> Trial A | A list of mutations along with the treatment they are associated with within the ACTIN treatment database.
generalTrialSource | CKB | The name of the source that has been used for general trial evidence
generalTrialEvidence | High TMB -> Trial B | A list of mutations along with the trial they are associated with.
generalEvidenceSource | CKB | The name of the source used for general evidence
generalResponsiveEvidence | PIK3CA E545K -> Alpelisib | A list of mutations along with responsive evidence for treatment.
generalResistanceEvidence | KRAS amp -> Erlotinib | A list of mutations along with resistance evidence for treatment.  

### Interpretation of ORANGE results

The mapping of general molecular fields is done as follows:

Field | Mapping
---|---
sampleId | The ORANGE sampleId
type | WGS 
date | The ORANGE date
hasReliabilityQuality | The PURPLE field `hasReliableQuality` 

The events that are used for ACTIN treatment matching are extracted from the PROTECT part of the ORANGE datamodel. It is assumed that PROTECT
has been run on a SERVE database that includes an ACTIN source generated from the ACTIN treatment database using [serve-bridge](../serve-bridge).
This setup allows PROTECT to determine classification for each of the relevant genes and mutations in the ACTIN treatment database.

The following classifications are made from the ACTIN-sourced evidence in PROTECT:
 - mutations: Any reported evidence of type `HOTSPOT_MUTATION`, `CODON_MUTATION` and `EXON_MUTATION` is included in the list of mutations.
 A mapping is performed from the PROTECT evidence to a mutation string that matches with the input mutation string in the ACTIN treatment 
 database. 
 - activated genes: Includes any gene with reported evidence of type `ACTIVATION` or `PROMISCUOUS_FUSION`.
 - inactivated genes: Includes any gene with reported evidence of type `INACTIVATION`. Based on the actual PROTECT event it is 
 determined whether the gene has been deleted or not.
 - amplified genes: Includes any gene with reported evidence of type `AMPLIFICATION`
 - wildtype genes: Not implemented yet
 - fusions: Include any fusion with reported evidence of type `FUSION_PAIR` 
 
The evidence is extracted from the PROTECT part of ORANGE as follows:
 - actin treatment evidence: All reported evidence from the ACTIN source.
 - generalTrialEvidence: All reported evidence from the iClusion source, filtered for applicability
 - generalResponsiveEvidence: All reported responsive evidence from the CKB source, filtered for applicability and if off-label: 
 only added in case the evidence is A-level or B-level non-predicted.
 - generalResistanceEvidence: Reported resistance evidence from the CKB source in case reported responsive evidence is found for the same 
 treatment with lower (or equal) evidence level.    

### Version History and Download Links
 - Upcoming (first release) 
