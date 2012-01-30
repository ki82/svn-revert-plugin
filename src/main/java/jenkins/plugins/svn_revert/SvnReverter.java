package jenkins.plugins.svn_revert;

public class SvnReverter {

    static SvnReverter create() {
        return new SvnReverter();
    }

    boolean revert() {
        return true;
    }

}
