package jenkins.plugins.svn_revert;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.plugins.claim.ClaimBuildAction;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;


public class ClaimerTest extends AbstractMockitoTestCase {

    private Claimer claimer;
    @Mock
    private AbstractBuild<?, ?> build;
    @Mock
    private ClaimBuildAction claimBuildAction;

    @Before
    public void setup() throws Exception {
        when(build.getAction(ClaimBuildAction.class)).thenReturn(claimBuildAction);
        claimer = new Claimer();
    }

    @Test
    public void shouldNotFailWhenClaimPluginNotAvailable() throws Exception {
        when(build.getAction(ClaimBuildAction.class)).thenReturn(null);

        claimer.claim(build);
    }

    @Test
    public void shouldClaimWhenRevertSucceds() throws Exception {
        claimer.claim(build);

        verify(claimBuildAction).claim(Claimer.CLAIMED_BY, "Reverted", false);
    }


}
