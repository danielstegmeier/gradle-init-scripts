projectsEvaluated {
    rootProject.subprojects {
        if (project.hasProperty('android')) {
//        task javadoc (type: Javadoc, dependsOn: project.tasks.assembleDebug) {
            task javadoc (type:Javadoc, dependsOn:project.assembleRelease) {
                classpath = project.configurations.compile +
                            project.configurations.testCompile +
                            project.configurations.androidTestCompile +
                            files(project.android.bootClasspath)
//                classpath = project.configurations.compile + files(android.bootClasspath)
                if (android.hasProperty('applicationVariants')) {
                    project.android.applicationVariants.all { variant ->
//            project.android.testVariants.all { variant ->
//                if(variant.buildType.name == 'debug') {
                        if (variant.buildType.name == 'release') {
                            classpath += files("${variant.javaCompile.destinationDir}")
                            variant.compileLibraries.each { lib ->
                                classpath += files(lib)
                            }
                        }
                    }
                } else if (android.hasProperty('libraryVariants')) {
                    project.android.libraryVariants.all { variant ->
                        if (variant.buildType.name == 'release') {
                            classpath += files("${variant.javaCompile.destinationDir}")
                            classpath += variant.javaCompile.classpath
                        }
                    }
                }
                exclude '**/*.aj'
                source = [android.sourceSets.main.java.srcDirs,
                          android.sourceSets.androidTest.java.srcDirs,
                          android.sourceSets.test.java.srcDirs]
//            source = [android.sourceSets.androidTest.java.srcDirs]
                options.setLinksOffline([new JavadocOfflineLink(
                        'https://developer.android.com/reference',
                        project.android.sdkDirectory.getAbsolutePath()
                                .replace(File.separatorChar, (char)'/')
                            + '/docs/reference')])
                options.encoding('utf-8')
            }
        }
    }
}
