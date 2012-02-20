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
import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.wc.SVNClientManager;
import org.tmatesoft.svn.core.wc.SVNCommitClient;
import org.tmatesoft.svn.core.wc.SVNCommitPacket;


public class SvnKitClientTest extends AbstractMockitoTestCase{

    private SvnKitClient svnKitClient;

    @Mock
    private SVNClientManager clientManager;
    @Mock
    private SVNCommitClient commitClient;
    @Mock
    private File file;
    @Mock
    private SVNErrorMessage errorMessage;
    @Mock
    private SVNCommitInfo commitInfo;

    private SVNCommitInfo[] commitInfos;


    @Before
    public void setup() throws Exception {
        svnKitClient = new SvnKitClient(clientManager);
        when(clientManager.getCommitClient()).thenReturn(commitClient);
        commitInfos = new SVNCommitInfo[]{ commitInfo };
        when(commitClient.doCommit(any(SVNCommitPacket[].class), anyBoolean(), anyString()))
            .thenReturn(commitInfos);
    }

    @Test
    public void shouldReturnTrueWhenCommitSucceds() throws Exception {
        assertThat(svnKitClient.commit(null, file), is(true));
    }

    @Test
    public void shouldReturnFalseWhenFileIsOutOfDate() throws Exception {
        when(commitInfo.getErrorMessage()).thenReturn(errorMessage);
        when(errorMessage.getErrorCode()).thenReturn(SVNErrorCode.FS_TXN_OUT_OF_DATE);

        assertThat(svnKitClient.commit(null, file), is(false));
    }

    @Test(expected=SVNException.class)
    public void shouldThrowSvnExceptionIfErrorsOnCommit() throws Exception {
        when(commitInfo.getErrorMessage()).thenReturn(errorMessage);
        svnKitClient.commit(null, file);
    }

}
