package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.plugins.claim.ClaimBuildAction;

import org.apache.commons.lang.StringUtils;

class Claimer {

    static final String CLAIMED_BY = "Jenkins Revert Plugin";
    private final ChangedRevisions changedRevisions;

    Claimer(final ChangedRevisions changedRevisions) {
        this.changedRevisions = changedRevisions;
    }

    void claim(final AbstractBuild<?, ?> build) {
        final ClaimBuildAction c = build.getAction(ClaimBuildAction.class);
        if (c != null) {
            c.claim(CLAIMED_BY, getClaimMessage(build), false);
        }
    }

    private String getClaimMessage(final AbstractBuild<?, ?> build) {
        String revisions = StringUtils.join(changedRevisions.getFor(build).getAllInOrder(), ", ");
        return "Reverted revisions " + revisions;
    }

}
