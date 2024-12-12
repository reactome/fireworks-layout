package org.reactome.server.fireworks.verifier;

import com.martiansoftware.jsap.*;
import org.reactome.server.fireworks.Main;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/11/2024
 */
public class Verifier {
    private String outputDirectory;
    private int releaseNumber;

    public static void main(String[] args) throws JSAPException {
        Verifier verifier = new Verifier();
        verifier.parseCommandLineArgs(args);
        verifier.run();
    }

    public void parseCommandLineArgs(String[] args) throws JSAPException {
        SimpleJSAP jsap = new SimpleJSAP(Main.class.getName(), "Verify Fireworks Layout ran correctly",
            new Parameter[]{
                new FlaggedOption("output", JSAP.STRING_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'o', "output", "The folder where the results are written to."),
                new FlaggedOption("releaseNumber", JSAP.INTEGER_PARSER, JSAP.NO_DEFAULT, JSAP.REQUIRED, 'r', "releaseNumber", "The most recent Reactome release version")

            }
        );

        JSAPResult config = jsap.parse(args);
        if (jsap.messagePrinted()) System.exit(1);

        this.outputDirectory = config.getString("output");
        this.releaseNumber = config.getInt("releaseNumber");
    }

    public void run() {
        List<String> errorMessages = verifyFireworksLayoutRanCorrectly();
        if (errorMessages.isEmpty()) {
            System.out.println("Fireworks Layout has run correctly!");
        } else {
            errorMessages.forEach(System.err::println);
            System.exit(1);
        }
    }

    private List<String> verifyFireworksLayoutRanCorrectly() {
        List<String> errorMessages = new ArrayList<>();

        errorMessages.addAll(checkFireworksFolderExists());
        errorMessages.addAll(checkJSONFilesForAllSpeciesExist());
        errorMessages.addAll(checkJSONFileSizesComparedToPreviousRelease());

        return errorMessages;
    }

    private List<String> checkFireworksFolderExists() {
        return !Files.exists(Paths.get(this.outputDirectory)) ?
            Arrays.asList(this.outputDirectory + " does not exist; Expected fireworks output files at this location") :
            new ArrayList<>();
    }

    private List<String> checkJSONFilesForAllSpeciesExist() {
        List<String> errorMessages = new ArrayList<>();

        List<String> jsonFileNames = getJSONFileNamesForAllSpecies();
        for (String jsonFileName : jsonFileNames) {
            Path jsonFilePath = Paths.get(this.outputDirectory, jsonFileName);
            if (!Files.exists(jsonFilePath)) {
                errorMessages.add("File " + jsonFilePath + " does not exist");
            }
        }

        return errorMessages;
    }

    private List<String> checkJSONFileSizesComparedToPreviousRelease() {
        return new ArrayList<>();
    }

    private List<String> getJSONFileNamesForAllSpecies() {
        List<String> species = Arrays.asList("Bos_taurus","Caenorhabditis_elegans", "Canis_familiaris", "Danio_rerio",
            "Dictyostelium_discoideum","Drosophila_melanogaster", "Gallus_gallus", "Homo_sapiens", "Mus_musculus",
            "Mycobacterium_tuberculosis", "Plasmodium_falciparum", "Rattus_norvegicus", "Saccharomyces_cerevisiae",
            "Schizosaccharomyces_pombe", "Sus_scrofa", "Xenopus_tropicalis");

        return species.stream().map(sp -> sp.concat(".json")).collect(Collectors.toList());
    }
}
