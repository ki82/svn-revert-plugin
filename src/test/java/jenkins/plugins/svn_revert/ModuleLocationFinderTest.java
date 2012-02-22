package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import hudson.EnvVars;
import hudson.model.BuildListener;
import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;

public class ModuleLocationFinderTest extends AbstractMockitoTestCase {

    private ModuleLocationFinder locationFinder;

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
        locationFinder = new ModuleLocationFinder(build, listener);
    }

    @Test
    public void getsModuleLocationsAsList() throws Exception {
        final List<ModuleLocation> locationsAsList = Lists.newArrayList(moduleLocations);
        assertThat(locationFinder.getModuleLocations(subversionScm),
                equalTo(locationsAsList));
    }

}
