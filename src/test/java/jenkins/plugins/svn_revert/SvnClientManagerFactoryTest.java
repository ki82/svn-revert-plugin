package jenkins.plugins.svn_revert;

import static org.mockito.Mockito.when;
import hudson.model.AbstractProject;
import hudson.scm.SubversionSCM;
import hudson.scm.SubversionSCM.DescriptorImpl;

import org.junit.Test;
import org.mockito.Mock;


public class SvnClientManagerFactoryTest extends AbstractMockitoTestCase {

    @Mock
    private SubversionSCM scm;
    @Mock
    private DescriptorImpl descriptor;
    @Mock
    private AbstractProject<?, ?> project;

    @Test(expected=NoSvnAuthException.class)
    public void shouldThrowExceptionWhenAuthProviderIsNull() throws Exception {
        when(scm.getDescriptor()).thenReturn(descriptor);
        when(descriptor.createAuthenticationProvider(project)).thenReturn(null);

        final SvnClientManagerFactory clientManagerFactory = new SvnClientManagerFactory();
        clientManagerFactory.create(project, scm);
    }
}
