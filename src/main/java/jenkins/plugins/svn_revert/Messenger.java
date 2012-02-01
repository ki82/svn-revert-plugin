package jenkins.plugins.svn_revert;

import java.io.PrintStream;

class Messenger {

    static final String BUILD_STATUS_NOT_UNSTABLE =
            "Will not revert since build status is not UNSTABLE.";
    static final String PREVIOUS_BUILD_STATUS_NOT_SUCCESS =
            "Will not revert since previous build status is not SUCCESS.";
    static final String NOT_SUBVERSION_SCM = "This plugin can only be used with Subversion SCM";
    private final PrintStream logger;

    public Messenger(final PrintStream logger) {
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

}
