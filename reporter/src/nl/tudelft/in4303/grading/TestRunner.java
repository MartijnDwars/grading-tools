package nl.tudelft.in4303.grading;

import com.google.inject.Inject;
import org.apache.commons.vfs2.AllFileSelector;
import org.apache.commons.vfs2.FileObject;
import org.metaborg.spoofax.core.SpoofaxException;
import org.metaborg.spoofax.core.context.ContextIdentifier;
import org.metaborg.spoofax.core.context.SpoofaxContext;
import org.metaborg.spoofax.core.language.ILanguage;
import org.metaborg.spoofax.core.language.ILanguageDiscoveryService;
import org.metaborg.spoofax.core.language.ILanguageIdentifierService;
import org.metaborg.spoofax.core.resource.IResourceService;
import org.metaborg.spoofax.core.stratego.IStrategoRuntimeService;
import org.metaborg.spoofax.core.terms.ITermFactoryService;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.terms.ITermFactory;
import org.strategoxt.HybridInterpreter;

import java.io.File;

public class TestRunner {
    private final IResourceService resourceService;
    private final ILanguageDiscoveryService languageDiscoveryService;
    private final IStrategoRuntimeService strategoRuntimeService;
    private ITermFactoryService termFactoryService;
    private ILanguageIdentifierService languageIdentifierService;

    private ILanguage spt;
    private FileObject languageFile;
    private ILanguage languageLanguage;

    @Inject
    public TestRunner(
            IResourceService resourceService,
            ILanguageDiscoveryService languageDiscoveryService,
            IStrategoRuntimeService strategoRuntimeService,
            ITermFactoryService termFactoryService,
            ILanguageIdentifierService languageIdentifierService) {
        this.resourceService = resourceService;
        this.languageDiscoveryService = languageDiscoveryService;
        this.strategoRuntimeService = strategoRuntimeService;
        this.termFactoryService = termFactoryService;
        this.languageIdentifierService = languageIdentifierService;
    }

    public void registerSPT() throws Exception {
        FileObject tmpSPTLocation = resourceService.resolve(System.getProperty("java.io.tmpdir") + "/spt");
        final FileObject sptLocation = resourceService.resolve("res:spt");
        tmpSPTLocation.delete(new AllFileSelector());
        tmpSPTLocation.copyFrom(sptLocation, new AllFileSelector());
        Iterable<ILanguage> languages = languageDiscoveryService.discover(tmpSPTLocation);
        spt = languages.iterator().next();
    }

    public void registerLanguage(File file) throws Exception {
        languageFile = resourceService.resolve(file);
        Iterable<ILanguage> languages = languageDiscoveryService.discover(languageFile);
        languageLanguage = languages.iterator().next();
    }

    public boolean runTests(File file) throws InterpreterException, SpoofaxException {
        FileObject testsFile = resourceService.resolve(file);

        final HybridInterpreter interpreter = strategoRuntimeService.runtime(
                new SpoofaxContext(resourceService, new ContextIdentifier(testsFile, spt))
        );

        ITermFactory termFactory = termFactoryService.get(spt);

        // There needs to be a current term. Otherwise strange things happen!
        interpreter.setCurrent(termFactory.makeString("Dummy"));

        return interpreter.invoke("test-runner");
    }
}
