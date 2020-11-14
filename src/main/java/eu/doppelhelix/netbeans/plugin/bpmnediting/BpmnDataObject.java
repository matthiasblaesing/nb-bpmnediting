/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.doppelhelix.netbeans.plugin.bpmnediting;

import java.io.IOException;
import javax.xml.transform.Source;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.text.MultiViewEditorElement;
import org.netbeans.spi.xml.cookies.CheckXMLSupport;
import org.netbeans.spi.xml.cookies.DataObjectAdapters;
import org.netbeans.spi.xml.cookies.TransformableSupport;
import org.netbeans.spi.xml.cookies.ValidateXMLSupport;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.MIMEResolver;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import org.xml.sax.InputSource;

@Messages({
    "LBL_BPMN_LOADER=Files of BPMN",
    "LBL_BPMN_SOURCE=Source"
})
@MIMEResolver.NamespaceRegistration(
    displayName = "#LBL_BPMN_LOADER",
    mimeType = "text/bpmn+xml",
    elementNS = {"http://www.omg.org/spec/BPMN/20100524/MODEL"},
    checkedExtension = {"xml", "bpmn"}
)
@DataObject.Registration(
    mimeType = "text/bpmn+xml",
    iconBase = "eu/doppelhelix/netbeans/plugin/bpmnediting/bpmn.png",
    displayName = "#LBL_BPMN_LOADER",
    position = 300
)
@ActionReferences({
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
        position = 100,
        separatorAfter = 200
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
        position = 300
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
        position = 400,
        separatorAfter = 500
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
        position = 600
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
        position = 700,
        separatorAfter = 800
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
        position = 900,
        separatorAfter = 1000
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
        position = 1100,
        separatorAfter = 1200
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
        position = 1300
    ),
    @ActionReference(
        path = "Loaders/text/bpmn+xml/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
        position = 1400
    )
})
public class BpmnDataObject extends MultiDataObject {

    public BpmnDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor("text/bpmn+xml", true);
        CookieSet cookies = getCookieSet();
        InputSource is = DataObjectAdapters.inputSource(this);
        Source source = DataObjectAdapters.source(this);
        cookies.add(new CheckXMLSupport(is));
        cookies.add(new ValidateXMLSupport(is));
        cookies.add(new TransformableSupport(source));
//        cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
    }

    @Override
    protected int associateLookup() {
        return 1;
    }

    @MultiViewElement.Registration(
        displayName = "#LBL_BPMN_SOURCE",
        iconBase = "eu/doppelhelix/netbeans/plugin/bpmnediting/bpmn.png",
        mimeType = "text/bpmn+xml",
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "BpmnDataObjectEditor",
        position = 100
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
}
