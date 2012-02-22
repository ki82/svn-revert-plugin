package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;

import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;

import com.google.common.collect.Lists;

public class ChangeLocatorTest extends AbstractMockitoTestCase {

    private ChangeLocator changeLocator;

    @Mock
    private AbstractBuild<?, ?> build;
    @Mock
    private SubversionSCM subversionScm;
    @Mock
    private ModuleLocationFinder locationFinder;
    @Mock
    private ChangedFiles changedFiles;
    @Mock
    private Module module;

    private final List<Module> moduleLocations = Lists.newArrayList();

    @Before
    public void setUp() throws Exception {
        moduleLocations.add(module);
        when(locationFinder.getModules(subversionScm)).thenReturn(moduleLocations);
        changeLocator = new ChangeLocator(build, locationFinder, changedFiles);
    }

    @Test
    public void returnsFalseIfAllChangesInWorkspace() throws Exception {
        assertThat(changeLocator.changesOutsideWorkspace(subversionScm), is(false));
    }

    @Ignore
    @Test(expected = IllegalStateException.class)
    public void shouldThrowWhenModuleUrlDoesNotMatchRepositoryUrl() throws Exception {
//        when(moduleLocation).
    }

}
