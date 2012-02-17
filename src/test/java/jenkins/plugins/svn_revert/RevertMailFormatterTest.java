package jenkins.plugins.svn_revert;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("rawtypes")
public class RevertMailFormatterTest extends AbstractMockitoTestCase {

    private RevertMailFormatter mailer;

    @Mock
    private ChangedRevisions changedRevisions;
    @Mock
    private AbstractBuild build;
    @Mock
    private MimeMessage mail;

    @Before
    public void setup() throws Exception {
        mailer = new RevertMailFormatter(changedRevisions);
        when(changedRevisions.getFor(build)).thenReturn(Revisions.create(123, 124));
    }

    @Test
    public void shouldSetDescriptiveSubject() throws Exception {
        mail = mailer.format(mail, build);
        verify(mail).setSubject("Reverted revision(s): 123, 124");
    }

    @Test
    public void shouldSetDescriptiveText() throws Exception {
        mail = mailer.format(mail, build);
        verify(mail).setText("Revision(s) 123, 124 was reverted since they made the build became UNSTABLE.");
    }

}
