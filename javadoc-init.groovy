projectsEvaluated {
    rootProject.subprojects {
        if (project.hasProperty('android')) {
            JavadocHelper.addJavadocTask(project)
        }
    }
}

final class JavadocHelper {
private static final String APP_VARIANTS = 'applicationVariants'
private static final String LIB_VARIANTS = 'libraryVariants'

static void addJavadocTask(final Project project) {
//        task javadoc (type: Javadoc, dependsOn: project.tasks.assembleDebug) {
    project.tasks.create([name:'javadoc',
                          type:Javadoc,
                          dependsOn:[project.assembleRelease]]) {
        classpath = project.configurations.compile +
                    project.configurations.testCompile +
                    project.configurations.androidTestCompile +
                    project.files(project.android.bootClasspath)
//        classpath = project.configurations.compile + files(android.bootClasspath)
        for (final String variantType : [APP_VARIANTS, LIB_VARIANTS]) {
            if (project.android.hasProperty(variantType)) {
                classpath += getClasspathForVariantType(variantType, project)
            }
        }
        exclude '**/*.aj'
        source = [project.android.sourceSets.main.java.srcDirs,
                  project.android.sourceSets.androidTest.java.srcDirs,
                  project.android.sourceSets.test.java.srcDirs]
//        source = [android.sourceSets.androidTest.java.srcDirs]
        options.setLinksOffline([new JavadocOfflineLink(
                'https://developer.android.com/reference',
                project.android.sdkDirectory.absolutePath
                        .replace(File.separatorChar, (char)'/')
                    + '/docs/reference')])
        options.encoding('utf-8')
    }
}

private static FileCollection getClasspathForVariantType(final String variantType, final Project project) {
    FileCollection classpath = project.files()
    project.android."${variantType}".all { variant ->
        if (variant.buildType.name == 'release') {
            classpath += project.files("${variant.javaCompile.destinationDir}")
            if ("${variantType}" == APP_VARIANTS) {
                variant.compileLibraries.each { lib ->
                    classpath += project.files(lib)
                }
            } else if ("${variantType}" == LIB_VARIANTS) {
                classpath += variant.javaCompile.classpath
            }
        }
    }
    classpath
}
}
