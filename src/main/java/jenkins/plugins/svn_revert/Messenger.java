package jenkins.plugins.svn_revert;

import java.io.PrintStream;

class Messenger {

    static final String BUILD_STATUS_NOT_UNSTABLE =
            "Will not revert since build status is not UNSTABLE.";
    static final String PREVIOUS_BUILD_STATUS_NOT_SUCCESS =
            "Will not revert since previous build status is not SUCCESS.";
    static final String NOT_SUBVERSION_SCM =
            "The Subversion Revert Plugin can only be used with Subversion SCM.";
    static final String NO_SVN_AUTH_PROVIDER = "No Subversion credentials available.";
    static final String REVERTED_CHANGES =
            "Reverted changes between %d:%d in %s since build became UNSTABLE.\n";
    private final PrintStream logger;

    Messenger(final PrintStream logger) {
        this.logger = logger;
    }

    void informBuildStatusNotUnstable() {
        logger.println(BUILD_STATUS_NOT_UNSTABLE);
    }

    void informPreviousBuildStatusNotSuccess() {
        logger.println(PREVIOUS_BUILD_STATUS_NOT_SUCCESS);
    }

    void informNotSubversionSCM() {
        logger.println(NOT_SUBVERSION_SCM);
    }

    void informNoSvnAuthProvider() {
        logger.println(NO_SVN_AUTH_PROVIDER);
    }

    void informReverted(final int fromRevision, final int toRevision, final String repository) {
        logger.format(REVERTED_CHANGES, toRevision, fromRevision, repository);
    }

    void printStackTraceFor(final Exception exception) {
        exception.printStackTrace(logger);
    }

    void log(final String string) {
        logger.println(string);
    }

}
