/*******************************************************************************
 * Copyright (c) 2017 Red Hat Inc..
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package com.redhat.fabric8analytics.lsp.eclipse.ui.tests;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Test;

import com.redhat.fabric8analytics.eclipse.ui.internal.WorkspaceFilesFinder;

public class WorkspaceFilesFinderTest {

	private WorkspaceFilesFinder finder = WorkspaceFilesFinder.getInstance();

	@After
	public void after() throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		
		for (IProject project : root.getProjects()) {
			if (!project.isOpen()) {
				project.open(null);
			}
		}
		
		root.delete(true, null);
	}
	
	@Test
	public void testNoSelectionProvider() throws CoreException {
		Set<IFile> files = finder.findPOMs();
		assertThat(files.size(), is(0));
	}

	@Test
	public void testEmptySelection() throws CoreException {
		Set<IFile> files = finder.findPOMs(createEmptySelection());
		assertThat(files.size(), is(0));
	}

	@Test
	public void testOneProjectWithNoPOMs() throws CoreException {
		Set<IFile> files = finder.findPOMs(createSelection(createProject("empty-project")));
		assertThat(files.size(), is(0));
	}

	@Test
	public void testProjects() throws CoreException {
		IProject project1 = createProject("project1");
		addFiles(project1, "file-1", "pom.xml", "file-2");
		
		IProject project2 = createProject("project2");
		addFiles(project2, "file-1", "pom.xml", "file-2");
		
		IProject project3 = createProject("project3");
		addFiles(project3, "file-1", "pom.xml", "file-2");
		
		Set<IFile> files = finder.findPOMs(createSelection(project1, project2, project3));
		
		assertThat(files.size(), is(3));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project1/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project2/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project3/pom.xml")));
	}

	@Test
	public void testMoreFiles() throws CoreException {
		IProject project1 = createProject("project1");
		addFiles(project1, "file-1", "pom.xml", "file-2");
		
		IProject project2 = createProject("project2");
		addFiles(project2, "file-1", "pom.xml", "file-2");
		
		IProject project3 = createProject("project3");
		addFiles(project3, "file-1", "pom.xml", "file-2");
		
		List<IResource> selection = new ArrayList<IResource>();
		selection.addAll(Arrays.asList(project1.members()));
		selection.addAll(Arrays.asList(project2.members()));
		selection.addAll(Arrays.asList(project3.members()));
		
		Set<IFile> files = finder.findPOMs(createSelection(selection.toArray(new Object[selection.size()])));
		
		assertThat(files.size(), is(3));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project1/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project2/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project3/pom.xml")));
	}

	@Test
	public void testRecursiveSearch() throws CoreException {
		IProject project1 = createProject("project1");
		IFolder aFolder = createFolders(project1, "a");
		IFolder bFolder = createFolders(aFolder, "b");
		addFiles(project1, "file-1", "pom.xml", "file-2");
		addFiles(bFolder, "file-3", "pom.xml", "file-4");
		
		IFolder targetFolder = createFolders(aFolder, "target");
		addFiles(targetFolder, "file-5", "pom.xml", "file-6");
		
		IFolder binFolder = createFolders(aFolder, "bin");
		addFiles(binFolder, "file-7", "pom.xml", "file-8");
		
		Set<IFile> files = finder.findPOMs(createSelection(project1));
		
		assertThat(files.size(), is(2));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project1/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project1/a/b/pom.xml")));
	}
	
	@Test
	public void testCombination() throws CoreException {
		IProject project1 = createProject("project1");
		IFolder aFolder = createFolders(project1, "a");
		IFolder bFolder = createFolders(aFolder, "b");
		addFiles(project1, "file-1", "pom.xml", "file-2");
		addFiles(bFolder, "file-3", "pom.xml", "file-4");
		
		IProject project2 = createProject("project2");
		addFiles(project2, "file-1", "pom.xml", "file-2");
		
		IProject project3 = createProject("project3");
		addFiles(project3, "file-1", "pom.xml", "file-2");
		
		List<IResource> selection = new ArrayList<IResource>();
		selection.addAll(Arrays.asList(project1.members()));
		selection.addAll(Arrays.asList(project2.members()));
		selection.addAll(Arrays.asList(project3.members()));
		selection.add(project1);
		selection.add(project2);
		selection.add(project3);
		
		Set<IFile> files = finder.findPOMs(createSelection(selection.toArray(new Object[selection.size()])));
		
		assertThat(files.size(), is(4));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project1/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project1/a/b/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project2/pom.xml")));
		assertThat(files, hasItem(new IFileFullPathMatcher("/project3/pom.xml")));
	}

	@Test
	public void testClosedProject() throws CoreException {
		IProject project = createProject("project1");
		addFiles(project, "file-1", "pom.xml", "file-2");
		project.close(null);
		
		Set<IFile> files = finder.findPOMs(createSelection(project));
		
		assertThat(files.size(), is(0));
	}

	@Test
	public void testUnaccessibleFile() throws CoreException {
		IProject project = createProject("project1");
		addFiles(project, "file-1", "pom.xml", "file-2");
		
		IStructuredSelection selection = createSelection(project);
		project.close(null);

		Set<IFile> files = finder.findPOMs(selection);
		
		assertThat(files.size(), is(0));
	}

	private IStructuredSelection createEmptySelection() {
		return new StructuredSelection();
	}
	
	private IStructuredSelection createSelection(Object...objects) {
		return new StructuredSelection(objects);
	}

	private IProject createProject(String name) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(name);
		project.create(new NullProgressMonitor());
		project.open(null);
		return project;
	}
	

	private void addFiles(IProject project, String... names) throws CoreException {
		for (String name : names) {
			IFile file = project.getFile(name);
			file.create(new ByteArrayInputStream("".getBytes()), IResource.NONE, null);
		}
	}
	
	private IFolder createFolders(IProject project, String name) throws CoreException {
		IFolder folder = project.getFolder(name);
		folder.create(true, true, null);
		return folder;
	}
	
	private IFolder createFolders(IFolder parentFolder, String name) throws CoreException {
		IFolder folder = parentFolder.getFolder(name);
		folder.create(true, true, null);
		return folder;
	}
	
	private void addFiles(IFolder folder, String... names) throws CoreException {
		for (String name : names) {
			IFile file = folder.getFile(name);
			file.create(new ByteArrayInputStream("".getBytes()), IResource.NONE, null);
		}
	}
	
	private class IFileFullPathMatcher extends TypeSafeMatcher<IFile> {

		private String expectedPath;
		
		public IFileFullPathMatcher(String expectedPath) {
			this.expectedPath = expectedPath;
		}
		
		@Override
		protected boolean matchesSafely(IFile file) {
			return expectedPath.equals(file.getFullPath().toString());
		}
	
		@Override
		public void describeTo(Description d) {
			d.appendText("file with path '");
			d.appendText(expectedPath);
			d.appendText("'");
		}
	}
}
