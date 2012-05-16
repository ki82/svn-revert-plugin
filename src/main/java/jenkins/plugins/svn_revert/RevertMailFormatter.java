package jenkins.plugins.svn_revert;

import hudson.model.AbstractBuild;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

class RevertMailFormatter {

    private static final String MAIL_SUBJECT = "Reverted revision(s): %s";
    private static final String MAIL_BODY =
            "Revision(s) %s was reverted since they made %s UNSTABLE.\n\nSee: %s";
    private final ChangedRevisions changedRevisions;

    RevertMailFormatter(final ChangedRevisions changedRevisions) {
        this.changedRevisions = changedRevisions;
    }

    public MimeMessage format(final MimeMessage mail, final AbstractBuild<?, ?> build,
            final String jenkinsUrl)
            throws MessagingException {
        final Revisions revisions = changedRevisions.getRevisions();

        final String revisionsInText = revisions.getAllInOrderAsString();
        final String subject = StringHumanizer.pluralize(MAIL_SUBJECT, revisions.count());
        mail.setSubject(String.format(subject, revisionsInText));

        final String jobName = build.getProject().getRootProject().getName();
        final String body = StringHumanizer.pluralize(MAIL_BODY, revisions.count());
        mail.setText(String.format(body, revisionsInText, jobName,  jenkinsUrl + build.getUrl()));

        return mail;
    }

}
