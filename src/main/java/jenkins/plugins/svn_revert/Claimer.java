package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.plugins.claim.ClaimBuildAction;

class Claimer {

    static final String CLAIMED_BY = "Jenkins Revert Plugin";
    private final ChangedRevisions changedRevisions;

    Claimer(final ChangedRevisions changedRevisions) {
        this.changedRevisions = changedRevisions;
    }

    void claim(final AbstractBuild<?, ?> build) {
        final ClaimBuildAction claimAction = build.getAction(ClaimBuildAction.class);
        if (claimAction != null) {
            claimAction.claim(CLAIMED_BY, getClaimMessageFor(build), false);
        }
    }

    private String getClaimMessageFor(final AbstractBuild<?, ?> build) {
        return "Reverted revision(s) " + changedRevisions.getRevisions().getAllInOrderAsString();
    }

}
