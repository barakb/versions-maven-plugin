package com.github.barakb.maven.versions;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.resolver.filter.AndArtifactFilter;
import org.apache.maven.artifact.resolver.filter.ArtifactFilter;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.shared.artifact.filter.PatternExcludesArtifactFilter;
import org.apache.maven.shared.artifact.filter.PatternIncludesArtifactFilter;
import org.codehaus.plexus.util.StringUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.Namespace;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.LineSeparator;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

/**
 * Created by Barak Bar Orion
 * on 6/13/16.*
 */
@SuppressWarnings({"UnusedDeclaration"})
@Mojo(name = "set", defaultPhase = LifecyclePhase.VALIDATE)

public class SetMojo extends AbstractMojo {

    @Component
    private MavenProject project;

    @Parameter(property = "newVersion")
    private String newVersion;

    @Parameter(property = "roots")
    private String roots;

    @Parameter
    private List<String> includes;

    @Parameter
    private List<String> excludes;

    @Component
    private ArtifactFactory factory;

    private static final Namespace ns = Namespace.getNamespace("http://maven.apache.org/POM/4.0.0");

    private final List<String> modified = new ArrayList<>();

    public SetMojo() {
        super();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        final AndArtifactFilter filter = new AndArtifactFilter();

        if (!getIncludes().isEmpty()) {
            final PatternIncludesArtifactFilter includeFilter =
                    new PatternIncludesArtifactFilter(getIncludes(), false);

            filter.add(includeFilter);
        }
        if (!getExcludes().isEmpty()) {
            final PatternExcludesArtifactFilter excludeFilter =
                    new PatternExcludesArtifactFilter(getExcludes(), false);

            filter.add(excludeFilter);
        }

        if (StringUtils.isEmpty(newVersion)) {
            throw new MojoExecutionException("You must specify the new version, by using the newVersion "
                    + "property (that is -DnewVersion=... on the command line).");
        }
        if (StringUtils.isEmpty(roots)) {
            throw new MojoExecutionException("You must specify the at least one root, by using the roots "
                    + "property (that is -Droots=root1,root2).");
        }

        for (String root : roots.split("\\s*,\\s*")) {
            File file = new File(root);
            getLog().info("processing root " + file.getAbsolutePath());
            iteratePoms(file, filter);
        }

        if(modified.isEmpty()){
            getLog().info("files were not modified");
        }else {
            getLog().info(modified.size() + " file(s) modified");
//            for (String mod : modified) {
//                getLog().info(mod);
//            }
        }
    }

    private void iteratePoms(File file, final ArtifactFilter filter) {
        if (file.isDirectory()) {
            //noinspection Convert2Lambda,ResultOfMethodCallIgnored
            file.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if (pathname == null) {
                        return false;
                    }
                    iteratePoms(pathname, filter);
                    return false;
                }
            });
        }
        if ("pom.xml".equalsIgnoreCase(file.getName())) {
            processPom(file, filter);
        }
    }

    private void processPom(File pathname, final ArtifactFilter filter) {
        try {
            boolean changed = process(pathname, filter);
            getLog().info((changed ? "[x] " : "[ ] ") +  pathname.getAbsolutePath());
        } catch (Exception e) {
            getLog().error(e);
        }
    }


    private boolean process(File pomFile, ArtifactFilter filter) throws JDOMException, IOException {
        Document document = new SAXBuilder().build(pomFile);
        Element pom = document.getRootElement();
        Map<String, Element> properties = readProperties(document.getRootElement());
        boolean changed = replaceParentVersion(pom, filter);
        changed = replacePomVersion(pom, filter, properties) || changed;
        changed = replaceDependency(document, filter, properties) || changed;
        if (changed) {
            File backup = getBackupFor(pomFile);
            if (backup != null) {
//                getLog().info("backup file is " + backup.getAbsolutePath());
                Files.copy(pomFile.toPath(), backup.toPath(), REPLACE_EXISTING);
                BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(pomFile), "utf-8"));
                new XMLOutputter(Format.getRawFormat()
                        .setLineSeparator(LineSeparator.UNIX)
                ).output(document, br);
                br.close();
            }
            modified.add(pomFile.getAbsolutePath());
        }
        return changed;
    }

    private Map<String, Element> readProperties(Element rootElement) {
        Map<String, Element> res = new HashMap<>();
        for (Element properties : getElements(rootElement, "properties")) {
            for (Element element : properties.getChildren()) {
                res.put("${" + element.getName() +"}", element);
            }
        }
        return res;
    }


    private boolean replaceDependency(Document pom, ArtifactFilter filter, Map<String, Element> properties) {
        boolean changed = false;
        for (Element dependency : getElements(pom.getRootElement(), "dependency")) {
            String gid = dependency.getChild("groupId", ns).getText();
            String aid = dependency.getChildText("artifactId", ns);
            Element verEL = dependency.getChild("version", ns);
            String ver = dependency.getChildText("version", ns);
            String varName;
            if ((ver != null) && ver.trim().startsWith("$")){
                varName = ver.trim();
                verEL = properties.get(varName);
                if(verEL != null){
                    ver = verEL.getText();
//                    getLog().info("expanding version var " + varName + " : " + ver);
                }else{
                    ver = null;
                }
            }
            if((ver != null) && !ver.equals(newVersion)){
                Artifact artifact = factory.createProjectArtifact(gid, aid, ver);
                if (filter.include(artifact)) {
                    verEL.setText(newVersion);
                    changed = true;
                }
            }
        }
        return changed;
    }

    private File getBackupFor(File file) {
        int index = file.getAbsolutePath().lastIndexOf(".");
        if (-1 < index) {
            return new File(file.getAbsolutePath().substring(0, index) + ".backup.xml");
        }
        return null;
    }

    private boolean replacePomVersion(Element pom, ArtifactFilter filter, Map<String, Element> properties) {
        Element groupIdEl = pom.getChild("groupId", ns);
        Element versionEl = pom.getChild("version", ns);
        if(groupIdEl == null){
            groupIdEl =  pom.getChild("parent", ns).getChild("groupId", ns);
        }
        if(versionEl == null){
            versionEl =  pom.getChild("parent", ns).getChild("version", ns);
        }
        String gid = groupIdEl.getText();
        String ver = versionEl.getText();
        String aid = pom.getChildText("artifactId", ns);
        Artifact artifact = factory.createProjectArtifact(gid, aid, ver);
//        getLog().info("Artifact is " + artifact);
        boolean changed = false;
        if (filter.include(artifact) && !ver.equals(newVersion)) {
            versionEl.setText(newVersion);
            changed = true;
        }
        return changed;
    }

    private boolean replaceParentVersion(Element pom, ArtifactFilter filter) {
        Element parentEL = pom.getChild("parent", ns);
        if(parentEL == null){
            return false;
        }
        Element groupIdEl = parentEL.getChild("groupId", ns);
        Element versionEl = parentEL.getChild("version", ns);
        String gid = groupIdEl.getText();
        String ver = versionEl.getText();
        String aid = pom.getChildText("artifactId", ns);
        Artifact artifact = factory.createProjectArtifact(gid, aid, ver);
//        getLog().info("Artifact is " + artifact);
        boolean changed = false;
        if (filter.include(artifact) && !ver.equals(newVersion)) {
            versionEl.setText(newVersion);
            changed = true;
        }
        return changed;
    }

    private List<Element> getElements(Element root, String name){
        List<Element> results = new ArrayList<>();
        List<Element> roots = new ArrayList<>();
        roots.add(root);
        while(!roots.isEmpty()){
            root = roots.remove(0);
            for (Element element : root.getChildren()) {
                if(name.equals(element.getName())){
                    results.add(element);
                }
                roots.add(element);
            }
        }
        return results;
    }

    private java.util.List<String> getExcludes() {
        if (this.excludes == null) {
            this.excludes = new java.util.ArrayList<>();
        }
        return this.excludes;
    }

    private java.util.List<String> getIncludes() {
        if (this.includes == null) {
            this.includes = new java.util.ArrayList<>();
        }
        return this.includes;
    }

}
