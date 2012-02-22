package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.ModuleLocation;

import java.util.List;

import org.junit.Before;
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

    private final List<ModuleLocation> moduleLocations = Lists.newArrayList();


    @Before
    public void setUp() throws Exception {
        when(locationFinder.getModuleLocations(subversionScm)).thenReturn(moduleLocations);
        changeLocator = new ChangeLocator(build, locationFinder, changedFiles);
    }

    @Test
    public void returnsFalseIfAllChangesInWorkspace() throws Exception {
        assertThat(changeLocator.changesOutsideWorkspace(subversionScm), is(false));
    }

}
