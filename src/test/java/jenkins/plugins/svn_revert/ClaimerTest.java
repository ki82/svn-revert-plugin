package jenkins.plugins.svn_revert;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.plugins.claim.ClaimBuildAction;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


@SuppressWarnings("unchecked")
public class ClaimerTest extends AbstractMockitoTestCase {

    private Claimer claimer;

    @Mock
    private AbstractBuild<?, ?> build;
    @Mock
    private ClaimBuildAction claimBuildAction;
    @Mock
    private ChangedRevisions changedRevisions;

    @Before
    public void setup() throws Exception {
        when(build.getAction(ClaimBuildAction.class)).thenReturn(claimBuildAction);
        claimer = new Claimer(changedRevisions, true);
    }

    @Test
    public void shouldNotFailWhenClaimPluginNotAvailable() throws Exception {
        when(build.getAction(ClaimBuildAction.class)).thenReturn(null);

        claimer.claim(build);
    }

    @Test
    public void shouldClaimWhenRevertSucceds() throws Exception {
        when(changedRevisions.getRevisions()).thenReturn(Revisions.create(3, 4, 7));
        claimer.claim(build);

        verify(claimBuildAction).claim(Claimer.CLAIMED_BY, "Reverted revisions 3, 4, 7", false);
    }

    @Test
    public void shouldClaimWithRevisionInMessageWhenRevertSuccedsWithOneRevision() throws Exception {
        when(changedRevisions.getRevisions()).thenReturn(Revisions.create(3));
        claimer.claim(build);

        verify(claimBuildAction).claim(Claimer.CLAIMED_BY, "Reverted revision 3", false);
    }

    @Test
    public void shouldNotTryToClaimWhenClaimPluginNotPresent() throws Exception {
        givenClaimPluginNotPresent();

        claimer.claim(build);

        verifyNoMoreInteractions(claimBuildAction);
    }

    private void givenClaimPluginNotPresent() {
        claimer = new Claimer(changedRevisions, false);
        when(build.getAction(ClaimBuildAction.class)).thenThrow(NoClassDefFoundError.class);
    }


}
