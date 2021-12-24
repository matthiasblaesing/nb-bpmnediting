package eu.doppelhelix.netbeans.plugin.bpmnediting.licenseextraction;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarFile;
import javax.inject.Inject;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.model.License;
import org.apache.maven.model.building.ModelBuildingRequest;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingException;
import org.apache.maven.project.ProjectBuildingRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Extract license information from dependencies
 */
@Mojo(
        name = "extract-licenses",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME
)
public class ExtractLicenses extends AbstractMojo {

    @Inject
    private MavenProject mavenProject;

    @Inject
    private MavenSession mavenSession;

    @Inject
    private ProjectBuilder mavenProjectBuilder;

    @Parameter(name = "scopes")
    private List<String> scopes = new ArrayList<>();

    @Override
    public void execute()
            throws MojoExecutionException {
        try {
            ProjectBuildingRequest projectBuildingRequest
                    = new DefaultProjectBuildingRequest(mavenSession.getProjectBuildingRequest())
                            .setRemoteRepositories(mavenProject.getRemoteArtifactRepositories())
                            .setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL)
                            .setResolveDependencies(false)
                            .setProcessPlugins(false);

            Document document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument();
            Element elPackages = appendChild(document, document, "packages");

            for (final Artifact artifact : mavenProject.getArtifacts()) {
                if((! (scopes == null || scopes.isEmpty()))) {
                    if(! scopes.contains(artifact.getScope())) {
                        continue;
                    }
                }
                MavenProject project = mavenProjectBuilder.build(artifact, true, projectBuildingRequest)
                        .getProject();
                Element elPackage = appendChild(document, elPackages, "package");
                appendChild(document, elPackage, "scope", artifact.getScope());
                appendChild(document, elPackage, "group", artifact.getGroupId());
                appendChild(document, elPackage, "artifact", artifact.getArtifactId());
                appendChild(document, elPackage, "version", artifact.getVersion());
                appendChild(document, elPackage, "name", project.getName());
                appendChild(document, elPackage, "description", project.getDescription());
                if(notBlank(project.getScm().getUrl())) {
                    appendChild(document, elPackage, "repository", project.getScm().getUrl());
                } else if(notBlank(project.getScm().getConnection())) {
                    appendChild(document, elPackage, "repository", project.getScm().getConnection());
                } else if(notBlank(project.getScm().getDeveloperConnection())) {
                    appendChild(document, elPackage, "repository", project.getScm().getDeveloperConnection());
                }
                Element elLicenses = appendChild(document, elPackage, "licenses");
                for (License l : project.getLicenses()) {
                    Element elLicense = appendChild(document, elLicenses, "license");
                    if(notBlank(l.getName())) {
                        appendChild(document, elLicense, "name", l.getName());
                    }
                    if(notBlank(l.getComments())) {
                        appendChild(document, elLicense, "comments", l.getComments());
                    }
                    if(notBlank(l.getUrl())) {
                        appendChild(document, elLicense, "url", l.getUrl());
                    }
                }
                JarFile jf = new JarFile(artifact.getFile());
                String licenseInfo = jf.stream()
                        .filter(je -> je.getRealName().matches("^META-INF/([Ll][Ii][Cc][Ee][Nn][Ss][Ee])(\\..+)?"))
                        .findFirst()
                        .map(je -> {
                            try {
                                return new String(jf.getInputStream(je).readAllBytes(), StandardCharsets.UTF_8);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .orElse("");
                if(notBlank(licenseInfo)) {
                    appendChild(document, elPackage, "artifactLicense", licenseInfo);
                }
                String noticeInfo = jf.stream()
                        .filter(je -> je.getRealName().matches("^META-INF/([Nn][Oo][Tt][Ii][Cc][Ee])(\\..+)?"))
                        .findFirst()
                        .map(je -> {
                            try {
                                return new String(jf.getInputStream(je).readAllBytes(), StandardCharsets.UTF_8);
                            } catch (IOException ex) {
                                throw new RuntimeException(ex);
                            }
                        })
                        .orElse("");
                if(notBlank(noticeInfo)) {
                    appendChild(document, elPackage, "artifactNotice", noticeInfo);
                }
            }

            Transformer transformer = TransformerFactory.newDefaultInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            File f = new File(mavenProject.getBasedir(), "target/licenses.xml");
            f.getParentFile().mkdirs();
            try(FileOutputStream fos = new FileOutputStream(f)) {
                transformer.transform(new DOMSource(document), new StreamResult(fos));
            }
        } catch (ProjectBuildingException | IOException | ParserConfigurationException | TransformerException ex) {
            throw new MojoExecutionException("", ex);
        }
    }

    private boolean notBlank(String s) {
        return s != null && ! s.isBlank();
    }

    private Element appendChild(Document doc, Node parent, String tagName) {
        Element element = doc.createElement(tagName);
        parent.appendChild(element);
        return element;
    }

    private Element appendChild(Document doc, Node parent, String tagName, String textContent) {
        Element element = doc.createElement(tagName);
        element.appendChild(doc.createTextNode(textContent));
        parent.appendChild(element);
        return element;
    }
}
