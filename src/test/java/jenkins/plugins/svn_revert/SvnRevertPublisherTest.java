package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import hudson.model.AbstractBuild;

import org.junit.Test;
import org.mockito.Mock;

public class SvnRevertPublisherTest extends AbstractMockitoTestCase {

    @Mock
    AbstractBuild<?, ?> build;

    @Test
    public void shouldReturnTrueWhenPerformingBuild() throws Exception {
        final SvnRevertPublisher publisher = new SvnRevertPublisher("");
        assertThat(publisher.perform(build, null, null), is(true));
    }
}
