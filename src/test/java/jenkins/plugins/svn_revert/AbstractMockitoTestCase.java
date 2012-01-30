package jenkins.plugins.svn_revert;

import org.junit.Before;
import org.mockito.MockitoAnnotations;

/**
 * Super class for unit tests using Mockito.
 * This class removes the need of initialing the mocks when using the @Mock annotation. */
public abstract class AbstractMockitoTestCase {

    @Before
    public void initMocks() {
        MockitoAnnotations.initMocks(this);
    }

}
