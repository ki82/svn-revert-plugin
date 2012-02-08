package jenkins.plugins.svn_revert;

import static org.mockito.Mockito.verify;

import java.io.PrintStream;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

public class MessengerTest extends AbstractMockitoTestCase {

    @Mock
    private PrintStream logger;
    private Messenger messenger;

    @Before
    public void setup() {
        messenger = new Messenger(logger);
    }

    @Test
    public void logsNotUnstableMessage() throws Exception {
        messenger.informBuildStatusNotUnstable();
        verify(logger).println(Messenger.BUILD_STATUS_NOT_UNSTABLE);
    }

    @Test
    public void logsPreviousNotSuccessMessage() throws Exception {
        messenger.informPreviousBuildStatusNotSuccess();
        verify(logger).println(Messenger.PREVIOUS_BUILD_STATUS_NOT_SUCCESS);
    }

    @Test
    public void logsNotSubversionScm() throws Exception {
        messenger.informNotSubversionSCM();
        verify(logger).println(Messenger.NOT_SUBVERSION_SCM);
    }

    @Test
    public void logsNoSvnAuthProvider() throws Exception {
        messenger.informNoSvnAuthProvider();
        verify(logger).println(Messenger.NO_SVN_AUTH_PROVIDER);
    }
}
