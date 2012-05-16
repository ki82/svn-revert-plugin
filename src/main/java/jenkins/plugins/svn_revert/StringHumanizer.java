package jenkins.plugins.svn_revert;

public class StringHumanizer {

    public static String pluralize(final String text, final int amount) {
        if (amount == 1) {
            return text.replace("(s)", "");
        }
        return text.replace("(s)", "s");
    }

}
