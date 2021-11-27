/*
 * Copyright 2020 Matthias Bläsing.
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

package eu.doppelhelix.netbeans.plugin.bpmnediting.schema;

import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Logger;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor2;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.lookup.ServiceProvider;

@ServiceProvider(service=CatalogReader.class, path="Plugins/XML/UserCatalogs")
public class BpmnSchemaProvider implements CatalogReader, CatalogDescriptor2 {
    private static final Logger LOG = Logger.getLogger(BpmnSchemaProvider.class.getName());

    @Override
    public Iterator getPublicIDs() {
        return Collections.EMPTY_LIST.iterator();
    }

    @Override
    public void refresh() {
    }

    @Override
    public String getSystemID(String string) {
        return null;
    }

    @Override
    public String resolveURI(String uri) {
        switch (uri) {
            case "http://www.omg.org/spec/BPMN/20100524/MODEL":
                return BpmnSchemaProvider.class.getResource("BPMN20.xsd").toString();
            case "http://www.omg.org/spec/BPMN/20100524/DI":
                return BpmnSchemaProvider.class.getResource("BPMNDI.xsd").toString();
            case "http://www.omg.org/spec/DD/20100524/DC":
                return BpmnSchemaProvider.class.getResource("DC.xsd").toString();
            case "http://www.omg.org/spec/DD/20100524/DI":
                return BpmnSchemaProvider.class.getResource("DI.xsd").toString();
            case "http://bpmn.io/schema/bpmn/biocolor/1.0":
                return BpmnSchemaProvider.class.getResource("bpmnio-color.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.0.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.0.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.2.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.2.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.3.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.3.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.4.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.4.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.10.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.10.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.11.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.11.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.15.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.15.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-5.18.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-5.18.xsd").toString();
            case "http://activiti.org/bpmn/activiti-bpmn-extensions-6.0.xsd":
                return BpmnSchemaProvider.class.getResource("activiti-bpmn-extensions-6.0.xsd").toString();
            case "http://camunda.org/schema/1.0/bpmn":
                return BpmnSchemaProvider.class.getResource("camunda.xsd").toString();
            default:
                return null;
        }
    }

    @Override
    public String resolvePublic(String string) {
        return null;
    }

    @Override
    public void addCatalogListener(CatalogListener cl) {
    }

    @Override
    public void removeCatalogListener(CatalogListener cl) {
    }

    @Override
    public String getIconResource(int i) {
        throw null;
    }

    @Override
    public String getDisplayName() {
        return "BPMN Schema Provider";
    }

    @Override
    public String getShortDescription() {
        return "Provides the schema definitions for Business Process Model and Notation (BPMN)";
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener pl) {
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener pl) {
    }

}
