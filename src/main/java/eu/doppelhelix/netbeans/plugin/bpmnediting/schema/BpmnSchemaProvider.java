
package eu.doppelhelix.netbeans.plugin.bpmnediting.schema;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.net.URL;
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
