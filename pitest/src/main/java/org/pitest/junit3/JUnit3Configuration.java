package org.pitest.junit3;

import org.pitest.help.PitHelpError;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestSuiteFinder;
import org.pitest.testapi.TestUnitFinder;

import java.util.Collections;
import java.util.Optional;

public class JUnit3Configuration implements Configuration {

    @Override
    public int priority() {
        // run before other plugins
        return 0;
    }

    @Override
    public TestUnitFinder testUnitFinder() {
        return new JUnit3TestFinder();
    }

    @Override
    public TestSuiteFinder testSuiteFinder() {
        return c -> Collections.emptyList();
    }

    @Override
    public Optional<PitHelpError> verifyEnvironment() {
        return Optional.empty();
    }

}
