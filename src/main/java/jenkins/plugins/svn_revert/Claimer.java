package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.plugins.claim.ClaimBuildAction;

class Claimer {

    static final String CLAIMED_BY = "Jenkins Revert Plugin";

    void claim(final AbstractBuild<?, ?> build) {
        final ClaimBuildAction c = build.getAction(ClaimBuildAction.class);
        if (c != null) {
            c.claim(CLAIMED_BY, "Reverted", false);
        }
    }

}