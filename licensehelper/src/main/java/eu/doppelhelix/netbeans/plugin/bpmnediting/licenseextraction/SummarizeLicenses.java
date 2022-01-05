/*
 * Copyright 2022 Matthias Bläsing.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.doppelhelix.netbeans.plugin.bpmnediting.licenseextraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Extract license information from dependencies
 */
@Mojo(
        name = "summarize-licenses",
        defaultPhase = LifecyclePhase.PROCESS_RESOURCES
)
public class SummarizeLicenses extends AbstractMojo {

    @Parameter(name = "inputfiles")
    private List<File> inputfiles = new ArrayList<>();

    @Parameter(name = "outputfile")
    private File outputfile;

    @Parameter(name = "prefix")
    private File prefix;

    @Parameter(name = "suffix")
    private File suffix;

    @Override
    public void execute()
            throws MojoExecutionException {
        outputfile.getParentFile().mkdirs();
        try (FileOutputStream fos = new FileOutputStream(outputfile);
                PrintWriter pw = new PrintWriter(fos, true, StandardCharsets.UTF_8)) {

            transferFileText(prefix, pw);

            DocumentBuilder db = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder();
            TreeMap<String,PackageData> unifiedMap = new TreeMap<>();
            for(File inputfile: inputfiles) {
                Document d = db.parse(inputfile);
                NodeList packageList = d.getDocumentElement().getChildNodes();
                for(int i = 0; i < packageList.getLength(); i++) {
                    PackageData pd = PackageData.fromNode(packageList.item(i));
                    if(pd != null) {
                        unifiedMap.put(pd.getId(), pd);
                    }
                }
            }

            Map<String,List<PackageData>> licenseTexts = new LinkedHashMap<>();
            Map<String,List<PackageData>> noticeTexts = new LinkedHashMap<>();

            pw.println("==========================   Summary of dependencies  ==========================\n");

            for(Entry<String,PackageData> e: unifiedMap.entrySet()) {
                PackageData pd = e.getValue();

                if(notBlank(pd.getArtifactLicense())) {
                    licenseTexts
                            .computeIfAbsent(pd.getArtifactLicense(), s -> new ArrayList<>())
                            .add(pd);
                }
                if(notBlank(pd.getArtifactNotice())) {
                    noticeTexts
                            .computeIfAbsent(pd.getArtifactNotice(), s -> new ArrayList<>())
                            .add(pd);
                }

                pw.println(pd.getId());
                if (pd.getName() != null) {
                    pw.printf("  Name:       %s%n", pd.getName());
                }
                if (pd.getUrl() != null) {
                    pw.printf("  URL:        %s%n", pd.getUrl());
                }
                if (pd.getRepository() != null) {
                    pw.printf("  Repository: %s%n", pd.getRepository());
                }
                boolean firstLine = true;
                for(License l: e.getValue().getLicenses()) {
                    if(firstLine) {
                        pw.print("  License:    ");
                        firstLine = false;
                    } else {
                        pw.print("              ");
                    }
                    pw.print(l.getName());
                    if(notBlank(l.getUrl())) {
                        pw.print(" (");
                        pw.print(l.getUrl());
                        pw.print(")");
                    }
                    pw.println("");
                    if(notBlank(l.getComments())) {
                        pw.print("              ");
                        pw.println(l.getComments().trim().replace("\n", "\n\"              \""));
                    }
                }

                pw.println("");
            }

            pw.printf("\n\n==================== %-38.38s ====================", "Notices");

            for(Entry<String,List<PackageData>> e: noticeTexts.entrySet()) {
                pw.println("\n+------------------------------------------------------------------------------+");
                for(PackageData pd: e.getValue()) {
                    pw.printf("|%-78.78s|%n", pd.getId());
                }
                pw.println("+------------------------------------------------------------------------------+");
                pw.println(e.getKey().trim());
            }

            pw.printf("\n\n==================== %-38.38s ====================", "Licensetexts");

            for(Entry<String,List<PackageData>> e: licenseTexts.entrySet()) {
                pw.println("\n+------------------------------------------------------------------------------+");
                for(PackageData pd: e.getValue()) {
                    pw.printf("|%-78.78s|%n", pd.getId());
                }
                pw.println("+------------------------------------------------------------------------------+");
                pw.println(e.getKey().trim());
            }

            transferFileText(suffix, pw);
        } catch (ParserConfigurationException | SAXException | IOException ex) {
            throw new MojoExecutionException("", ex);
        }
    }

    private void transferFileText(File file, final PrintWriter pw) throws IOException {
        if(file != null) {
            try(FileInputStream fis = new FileInputStream(file);
                    InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr)) {
                for(String line = br.readLine(); line != null; line = br.readLine()) {
                    pw.println(line);
                }
            }
        }
    }

    static class PackageData {
        private String group;
        private String artifact;
        private String version;
        private String name;
        private String url;
        private String description;
        private String repository;
        private final List<License> licenses = new ArrayList<>();
        private String artifactLicense;
        private String artifactNotice;

        public String getId() {
            return (notBlank(group) ? (group + ":") : "") + artifact + ":" + version;
        }

        public String getGroup() {
            return group;
        }

        public void setGroup(String group) {
            this.group = group;
        }

        public String getArtifact() {
            return artifact;
        }

        public void setArtifact(String artifact) {
            this.artifact = artifact;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getRepository() {
            return repository;
        }

        public void setRepository(String repository) {
            this.repository = repository;
        }

        public String getArtifactLicense() {
            return artifactLicense;
        }

        public void setArtifactLicense(String artifactLicense) {
            this.artifactLicense = artifactLicense;
        }

        public String getArtifactNotice() {
            return artifactNotice;
        }

        public void setArtifactNotice(String artifactNotice) {
            this.artifactNotice = artifactNotice;
        }

        public List<License> getLicenses() {
            return licenses;
        }

        @SuppressWarnings("ConvertToStringSwitch")
        public static PackageData fromNode(Node element) {
            if(element.getNodeType() != Node.ELEMENT_NODE ||
                    ! "package".equals(((Element) element).getTagName())) {
                return null;
            }
            PackageData packageData = new PackageData();
            NodeList nl = element.getChildNodes();
            for(int i = 0; i < nl.getLength(); i++) {
                if(nl.item(i) instanceof Element) {
                    Element e = (Element) nl.item(i);
                    if("group".equals(e.getTagName())) {
                        packageData.setGroup(e.getTextContent());
                    } else if ("artifact".equals(e.getTagName())) {
                        packageData.setArtifact(e.getTextContent());
                    } else if ("version".equals(e.getTagName())) {
                        packageData.setVersion(e.getTextContent());
                    } else if ("description".equals(e.getTagName())) {
                        packageData.setDescription(e.getTextContent());
                    } else if ("repository".equals(e.getTagName())) {
                        packageData.setRepository(e.getTextContent());
                    } else if ("artifactLicense".equals(e.getTagName())) {
                        packageData.setArtifactLicense(e.getTextContent());
                    } else if ("artifactNotice".equals(e.getTagName())) {
                        packageData.setArtifactNotice(e.getTextContent());
                    } else if ("url".equals(e.getTagName())) {
                        packageData.setUrl(e.getTextContent());
                    } else if ("licenses".equals(e.getTagName())) {
                        NodeList children = e.getChildNodes();
                        for(int j = 0; j < children.getLength(); j++) {
                            License l = License.fromNode(children.item(j));
                            if(l != null) {
                                packageData.getLicenses().add(l);
                            }
                        }
                    }
                }
            }
            return packageData;
        }
    }

    static class License {
        private String name;
        private String comments;
        private String url;

        public License() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getComments() {
            return comments;
        }

        public void setComments(String comments) {
            this.comments = comments;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        @SuppressWarnings("ConvertToStringSwitch")
        public static License fromNode(Node element) {
            if (element.getNodeType() != Node.ELEMENT_NODE
                    || !"license".equals(((Element) element).getTagName())) {
                return null;
            }
            License license = new License();
            NodeList nl = element.getChildNodes();
            for(int i = 0; i < nl.getLength(); i++) {
                if(nl.item(i) instanceof Element) {
                    Element e = (Element) nl.item(i);
                    if("name".equals(e.getTagName())) {
                        license.setName(e.getTextContent());
                    } else if ("url".equals(e.getTagName())) {
                        license.setUrl(e.getTextContent());
                    } else if ("comments".equals(e.getTagName())) {
                        license.setComments(e.getTextContent());
                    }
                }
            }
            return license;
        }
    }

    private static boolean notBlank(String s) {
        return s != null && ! s.isBlank();
    }
}
