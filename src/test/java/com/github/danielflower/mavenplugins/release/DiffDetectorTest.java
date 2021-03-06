package com.github.danielflower.mavenplugins.release;

import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.Test;
import scaffolding.TestProject;

import java.io.IOException;
import java.util.ArrayList;

import static com.github.danielflower.mavenplugins.release.AnnotatedTagFinderTest.saveFileInModule;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class DiffDetectorTest {

    @Test
    public void canDetectIfFilesHaveBeenChangedForAModuleSinceSomeSpecificTag() throws Exception {
        TestProject project = TestProject.independentVersionsProject();

        AnnotatedTag tag1 = saveFileInModule(project, "console-app", "1.2", 3);
        AnnotatedTag tag2 = saveFileInModule(project, "core-utils", "2", 0);
        AnnotatedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);

        DiffDetector detector = new TreeWalkingDiffDetector(project.local.getRepository());

        assertThat(detector.hasChangedSince("core-utils", noChildModules(), asList(tag2)), is(false));
        assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag2)), is(true));
        assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag3)), is(false));
    }

    @Test
    public void canDetectThingsInTheRoot() throws IOException, GitAPIException {
        TestProject simple = TestProject.singleModuleProject();
        AnnotatedTag tag1 = saveFileInModule(simple, ".", "1.0", 1);
        simple.commitRandomFile(".");
        DiffDetector detector = new TreeWalkingDiffDetector(simple.local.getRepository());
        assertThat(detector.hasChangedSince(".", noChildModules(), asList(tag1)), is(true));

        AnnotatedTag tag2 = saveFileInModule(simple, ".", "1.0", 2);
        assertThat(detector.hasChangedSince(".", noChildModules(), asList(tag2)), is(false));
    }

    @Test
    public void canDetectChangesAfterTheLastTag() throws IOException, GitAPIException {
        TestProject project = TestProject.independentVersionsProject();

        saveFileInModule(project, "console-app", "1.2", 3);
        saveFileInModule(project, "core-utils", "2", 0);
        AnnotatedTag tag3 = saveFileInModule(project, "console-app", "1.2", 4);
        project.commitRandomFile("console-app");

        DiffDetector detector = new TreeWalkingDiffDetector(project.local.getRepository());
        assertThat(detector.hasChangedSince("console-app", noChildModules(), asList(tag3)), is(true));
    }

    @Test
    public void canIgnoreChangesInModuleFolders() throws IOException, GitAPIException {
        TestProject project = TestProject.nestedProject();

        AnnotatedTag tag1 = saveFileInModule(project, "server-modules", "1.0.2.4", 0);
        project.commitRandomFile("server-modules/server-module-a");

        DiffDetector detector = new TreeWalkingDiffDetector(project.local.getRepository());
        assertThat(detector.hasChangedSince("server-modules", asList("server-module-a", "server-module-b"),
            asList(tag1)), is(false));
    }

    @Test
    public void canDetectLocalChangesWithModuleFolders() throws IOException, GitAPIException {
        TestProject project = TestProject.nestedProject();

        AnnotatedTag tag1 = saveFileInModule(project, "server-modules", "1.0.2.4", 0);
        project.commitRandomFile("server-modules");

        DiffDetector detector = new TreeWalkingDiffDetector(project.local.getRepository());
        assertThat(detector.hasChangedSince("server-modules", asList("server-module-a", "server-module-b"),
            asList(tag1)), is(true));
    }


    private static java.util.List<String> noChildModules() {
        return new ArrayList<String>();
    }
}
