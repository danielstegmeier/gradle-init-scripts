projectsEvaluated {
    rootProject.subprojects {
        task sourcesJar(type:Jar) {
            classifier = 'sources'
            from android.sourceSets.main.java.sourceFiles
        }
    }
}
