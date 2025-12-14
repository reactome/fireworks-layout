package org.reactome.server.fireworks.verifier;

import org.reactome.release.verifier.DefaultVerifier;
import org.reactome.release.verifier.Verifier;

import java.io.IOException;

/**
 * @author Joel Weiser (joel.weiser@oicr.on.ca)
 * Created 12/11/2024
 */
public class FireworksVerifier {

    public static void main(String[] args) throws IOException {
        Verifier verifier = new DefaultVerifier("fireworks");
        verifier.parseCommandLineArgs(args);
        verifier.run();
    }
}
