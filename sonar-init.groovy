import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection

rootProject {
    buildscript {
        repositories {
            maven {
                url 'https://plugins.gradle.org/m2/'
            }
        }
        dependencies {
            classpath 'org.sonarsource.scanner.gradle:sonarqube-gradle-plugin:2.6.2'
        }
    }
}

projectsEvaluated {
    rootProject {
        apply plugin:'org.sonarqube'

        tasks.sonarqube.group 'Verification'
        sonarqube {
            properties {
                property 'sonar.sourceEncoding', 'UTF-8'
            }
        }
        ext.applySonar = {
            if (project.hasProperty('android')) {
                setSonarProperties(project)
                rootProject.tasks.sonarqube.dependsOn project.tasks.assemble
            }
        }
        if (subprojects.isEmpty()) {
            rootProject applySonar
        } else {
            subprojects applySonar
        }
    }
}

void setSonarProperties(final Project project) {
    setApplicationProperties(project)
    setTestProperties(project)
}

void setApplicationProperties(final Project project) {
    project.sonarqube.properties {
        property 'sonar.projectBaseDir', rootProject.projectDir.absolutePath
        if (!project.android.sourceSets.main.java.sourceFiles.isEmpty()) {
            property 'sonar.sources', project.android.sourceSets.main.java.srcDirs
            if (project.android.hasProperty('applicationVariants')) {
                project.android.applicationVariants.all { variant ->
                    final ConfigurableFileCollection JAVA_CLASSES =
                            files(variant.javaCompileProvider.get().destinationDir)
                    property 'sonar.java.binaries', JAVA_CLASSES +
                            file("${project.buildDir}/tmp/kotlin-classes/${variant.name}")
                    property 'sonar.java.libraries',
                            JAVA_CLASSES + files(project.android.bootClasspath)
                    final String VARIANT_NAME = variant.name[0].toUpperCase() + variant.name[1..-1]
                    final Task DEVICE_TEST_TASK =
                            project.tasks.findByName("connected${VARIANT_NAME}AndroidTest")
                    if (DEVICE_TEST_TASK?.resultsDir?.isDirectory() &&
                            DEVICE_TEST_TASK?.resultsDir?.listFiles()?.length) {
                        final String TESTS_PROPERTY = 'sonar.tests'
                        final String JUNIT_PATH_PROPERTY = 'sonar.junit.reportPaths'
                        properties[TESTS_PROPERTY] = properties[TESTS_PROPERTY] ?: [] +
                                "${project.android.sourceSets.androidTest.java.srcDirs},"
                        properties[JUNIT_PATH_PROPERTY] = properties[JUNIT_PATH_PROPERTY] ?: [] +
                                "${DEVICE_TEST_TASK.resultsDir},"
                    }
                    final Task COVERAGE_REPORT_TASK =
                            project.tasks.findByName("create${VARIANT_NAME}AndroidTestCoverageReport")
                    if (COVERAGE_REPORT_TASK?.coverageFile?.exists()) {
                        final String JACOCO_PATH_PROPERTY = 'sonar.jacoco.reportPaths'
                        properties[JACOCO_PATH_PROPERTY] = properties[JACOCO_PATH_PROPERTY] ?: [] +
                                "${COVERAGE_REPORT_TASK.coverageFile},"
                        property 'sonar.java.coveragePlugin', 'jacoco'
                        property 'sonar.dynamicAnalysis', 'reuseReports'
                    }
                }
            }
        }
    }
}

void setTestProperties(final Project project) {
    if (project.android.hasProperty('testVariants')) {
        project.sonarqube.properties {
            project.android.testVariants.all { variant ->
                final ConfigurableFileCollection JAVA_CLASSES =
                        files(variant.javaCompileProvider.get().destinationDir)
                property 'sonar.java.test.binaries', JAVA_CLASSES
                property 'sonar.java.test.libraries', JAVA_CLASSES + files(project.android.bootClasspath)
            }
        }
    }
}
