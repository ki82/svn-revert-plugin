package jenkins.plugins.svn_revert;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import org.junit.Test;

public class StringHumanizerTest {

    private static final String MESSAGE = "I like dog(s)";
    private static final String SINGULAR = "I like dog";
    private static final String PLURAL = "I like dogs";

    @Test
    public void shouldPluralizeWhenMoreThanOne() throws Exception {
        assertThat(StringHumanizer.pluralize(MESSAGE, 2), equalTo(PLURAL));
    }

    @Test
    public void shouldNotPluralizeWhenOne() throws Exception {
        assertThat(StringHumanizer.pluralize(MESSAGE, 1), equalTo(SINGULAR));
    }

    @Test
    public void shouldNotPluralizeWhenZero() throws Exception {
        assertThat(StringHumanizer.pluralize(MESSAGE, 0), equalTo(PLURAL));
    }

}
