package io.jenkins.plugins.Presenters;

import hudson.FilePath;
import hudson.model.TaskListener;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Paths;

public final class Utils {

    private static String[] invalidChars = {" ", "-", "~", "<", "!", "@", "/", "$", "%", "^", "&", "#", "№", "(", ")", "?", ">", ",", "*", "|", "'"};

    public static boolean isValidPath(String path) {

        try {
            Paths.get(path).toAbsolutePath();
        } catch (java.nio.file.InvalidPathException e) {
            return false;
        }
        return true;
    }

    public static FilePath generateScriptFile(TaskListener listener, FilePath workspace, String script, String extension) {

        try {
            FilePath filePath = workspace.createTempFile("PowerShell\\dbforge_devops_script_", extension);
            try (PrintWriter out = new PrintWriter(new File(filePath.getRemote()), "UTF-8")) {
                out.println(script);
            }
            return filePath;
        } catch (IOException e) {
            listener.error("Unexpected I/O exception executing script: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            listener.error("Unexpected thread interruption executing script");
            return null;
        }
    }

    public static boolean isValidPackageId(String packageId) {

        return !(packageId.length() > 100 || StringUtils.indexOfAny(packageId, invalidChars) > -1);
    }
}
