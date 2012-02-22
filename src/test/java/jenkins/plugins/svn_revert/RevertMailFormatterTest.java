package jenkins.plugins.svn_revert;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;

import javax.mail.internet.MimeMessage;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

@SuppressWarnings("rawtypes")
public class RevertMailFormatterTest extends AbstractMockitoTestCase {

    private static final String JENKINS_URL = "http://localhost:8080/";
    private static final String BUILD_URL = "job/job-name/911/";

    private static final String LINE_BREAK = "\n";


    private RevertMailFormatter mailer;

    @Mock
    private ChangedRevisions changedRevisions;
    @Mock
    private AbstractBuild build;
    @Mock
    private AbstractProject project;
    @Mock
    private AbstractProject rootProject;
    @Mock
    private MimeMessage mail;

    @Before
    public void setup() throws Exception {
        when(changedRevisions.getRevisions()).thenReturn(Revisions.create(123, 124));
        when(build.getProject()).thenReturn(project);
        when(project.getRootProject()).thenReturn(rootProject);
        when(rootProject.getName()).thenReturn("job-name");
        when(build.getUrl()).thenReturn(BUILD_URL);
        mailer = new RevertMailFormatter(changedRevisions);
    }

    @Test
    public void shouldSetDescriptiveSubject() throws Exception {
        mail = mailer.format(mail, build, JENKINS_URL);
        verify(mail).setSubject("Reverted revision(s): 123, 124");
    }

    @Test
    public void shouldSetDescriptiveText() throws Exception {
        mail = mailer.format(mail, build, JENKINS_URL);
        verify(mail).setText(
                "Revision(s) 123, 124 was reverted since they made job-name UNSTABLE." + LINE_BREAK
                + LINE_BREAK
                + "See: " + JENKINS_URL + BUILD_URL);
    }

}
