package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class ModuleFinderTest extends AbstractMockitoTestCase {

    private ModuleFinder locationFinder;

    @Mock
    private AbstractBuild<?, ?> build;
    @Mock
    private BuildListener listener;
    @Mock
    private SubversionSCM subversionScm;
    @Mock
    private EnvVars environmentVariables;

    private final ModuleLocation moduleLocation = new ModuleLocation("remote", "local");
    private final ModuleLocation[] moduleLocations = new ModuleLocation[] { moduleLocation };

    @Before
    public void setUp() throws Exception {
        when(build.getEnvironment(listener)).thenReturn(environmentVariables);
        when(subversionScm.getLocations(environmentVariables, build)).thenReturn(moduleLocations);
        locationFinder = new ModuleFinder(build, listener);
    }

    @Test
    public void shouldReturnTheSameNumberOfModulesAsInScm() throws Exception {
        assertThat(locationFinder.getModules(subversionScm).size(), is(moduleLocations.length));
    }

    @Test
    public void shouldReturnWrappedModuleLocation() throws Exception {
        ModuleLocation actualModuleLocation = locationFinder.getModules(subversionScm).get(0).getModuleLocation();
        assertThat(actualModuleLocation, is(moduleLocation));

    }

}
