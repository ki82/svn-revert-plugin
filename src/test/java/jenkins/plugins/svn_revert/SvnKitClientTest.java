package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

import java.io.File;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;


@SuppressWarnings("deprecation")
public class SvnKitClientTest extends AbstractMockitoTestCase{

    @Mock
    private SVNClientManager clientManager;
    @Mock
    private SVNCommitClient commitClient;
    @Mock
    private File file;

    private SvnKitClient svnKitClient;

    @Before
    public void setup() throws Exception {
        svnKitClient = new SvnKitClient(clientManager);
        when(clientManager.getCommitClient()).thenReturn(commitClient);
    }

    @Test
    public void shouldReturnFalseWhenFileIsOutOfDate() throws Exception {
        when(commitClient.doCommit(any(File[].class), anyBoolean(), anyString(), anyBoolean(), anyBoolean())).thenThrow(
                new SVNException(SVNErrorMessage.create(SVNErrorCode.FS_TXN_OUT_OF_DATE, "")));

        assertThat(svnKitClient.commit(null, file), is(false));
    }

    @Test
    public void shouldReturnTrueWhenCommitSucceds() throws Exception {

        assertThat(svnKitClient.commit(null, file), is(true));
    }
}
