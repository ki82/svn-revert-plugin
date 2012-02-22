package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.scm.SubversionSCM;

import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.tmatesoft.svn.core.SVNException;

import com.google.common.collect.Lists;

@SuppressWarnings("unchecked")
public class ChangeLocatorTest extends AbstractMockitoTestCase {

    private ChangeLocator changeLocator;

    @Mock
    private AbstractBuild<?, ?> build;
    @Mock
    private SubversionSCM subversionScm;
    @Mock
    private ModuleFinder locationFinder;
    @Mock
    private ChangedFiles changedFiles;
    @Mock
    private Module module;

    private final List<Module> moduleLocations = Lists.newArrayList();

    private final List<String> changedFilePaths = Lists.newArrayList();

    @Before
    public void setUp() throws Exception {
        moduleLocations.add(module);
        when(locationFinder.getModules(subversionScm)).thenReturn(moduleLocations);
        when(changedFiles.getRepositoryPathsFor(build)).thenReturn(changedFilePaths);
        changeLocator = new ChangeLocator(build, locationFinder, changedFiles);
    }

    @Test
    public void returnsFalseIfAllChangesInWorkspace() throws Exception {
        when(module.getRepositoryPath(build)).thenReturn("path/to/module");
        changedFilePaths.add("path/to/module/with_file.txt");
        assertThat(changeLocator.changesOutsideWorkspace(subversionScm), is(false));
    }

    @Test
    public void shouldReturnTrueWhenModuleThrowsException() throws Exception {
        when(module.getRepositoryPath(build)).thenThrow(SVNException.class);
        assertThat(changeLocator.changesOutsideWorkspace(subversionScm), is(true));
    }

    @Test
    public void shouldReturnTrueWhenChangesNotInWorkspace() throws Exception {
        when(module.getRepositoryPath(build)).thenReturn("path/to/module");
        changedFilePaths.add("otherModule");
        assertThat(changeLocator.changesOutsideWorkspace(subversionScm), is(true));
    }

}
