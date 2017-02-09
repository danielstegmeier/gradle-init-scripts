// Find application projects and insert correct signingConfig.
import java.nio.file.FileSystems

settingsEvaluated { settings ->
    settings.rootProject.children.each {
        try {
            final BufferedReader READER = new BufferedReader(new FileReader(it.buildFile))
            String line = ''
            while (line != null) {
                if (line.matches('^\\s*apply\\s+plugin\\s*:\\s*[\'"]com.android.application[\'"]\\s*$')) {
                    it.setBuildFileName(
                            FileSystems.getDefault().getPath(it.dir.toString())
                                    .relativize(FileSystems.getDefault().getPath(new File(
                                            buildscript.getSourceFile().getParentFile(),
                                            'fix-signing-config.groovy').toString()))
                                    .toString())
                    break
                }
                line = READER.readLine()
            }
            READER.close()
        } catch (final IOException e) {
            rootProject.logger.log(LogLevel.ERROR, e.getLocalizedMessage, e)
        }
    }
}
