package nl.tudelft.in4303.grading.tests;

import org.metaborg.spoofax.core.SpoofaxModule;
import org.metaborg.sunshine.environment.SunshineMainArguments;

/**
 * The parse-spt-file code depends on SpoofaxTestingJSGLRI, which uses the ServiceRegistry
 * in Sunshine to get a LaunchConfiguration, which needs an SunshineMainArguments to be
 * created. Even though we don't want to use Sunshine, we need to..
 */
public class NewSpoofaxModule extends SpoofaxModule {
    @Override
    protected void bindOther() {
        // TODO: Make path dynamic
        SunshineMainArguments args = new SunshineMainArguments();
        args.project = "/tmp/student-mdwars/MiniJava-tests-syntax";

        bind(SunshineMainArguments.class).toInstance(args);
    }
}
