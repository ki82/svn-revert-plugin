package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;
import hudson.plugins.claim.ClaimBuildAction;

class Claimer {

    static final String CLAIMED_BY = "Jenkins Revert Plugin";
    private final ChangedRevisions changedRevisions;
    private final boolean claimPluginPresent;

    Claimer(final ChangedRevisions changedRevisions, final boolean claimPluginPresent) {
        this.changedRevisions = changedRevisions;
        this.claimPluginPresent = claimPluginPresent;
    }

    void claim(final AbstractBuild<?, ?> build) {
        if (claimPluginPresent) {
            final ClaimBuildAction claimAction = build.getAction(ClaimBuildAction.class);
            if (claimAction != null) {
                claimAction.claim(CLAIMED_BY, getClaimMessageFor(build), false);
            }
        }
    }

    private String getClaimMessageFor(final AbstractBuild<?, ?> build) {
        final Revisions revisions = changedRevisions.getRevisions();
        final String message = "Reverted revision(s) " + revisions.getAllInOrderAsString();
        return StringHumanizer.pluralize(message, revisions.count());
    }

}
