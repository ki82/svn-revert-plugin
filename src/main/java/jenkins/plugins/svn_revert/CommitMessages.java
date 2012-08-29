package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.scm.ChangeLogSet.Entry;

public class CommitMessages {

    private final AbstractBuild<?, ?> build;

    CommitMessages(final AbstractBuild<?, ?> build) {
        this.build = build;
    }

    boolean anyMessageContains(final String substring) {
        for (final Entry change : build.getChangeSet()) {
            final String message = change.getMsg().toLowerCase();
            if (message.contains(substring.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

}
