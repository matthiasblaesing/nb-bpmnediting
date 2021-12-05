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

import static eu.doppelhelix.netbeans.plugin.bpmnediting.BpmnDataObject.ICON_BASE;
import static eu.doppelhelix.netbeans.plugin.bpmnediting.BpmnDataObject.MIME_TYPE;

@Messages({
    "LBL_BPMN_LOADER=Files of BPMN",
    "LBL_BPMN_SOURCE=Source"
})
@MIMEResolver.NamespaceRegistration(
    displayName = "#LBL_BPMN_LOADER",
    mimeType = MIME_TYPE,
    elementNS = {"http://www.omg.org/spec/BPMN/20100524/MODEL"},
    checkedExtension = {"xml", "bpmn"}
)
@DataObject.Registration(
    mimeType = "text/bpmn+xml",
    iconBase = ICON_BASE,
    displayName = "#LBL_BPMN_LOADER",
    position = 300
)
@ActionReferences({
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.OpenAction"),
        position = 100,
        separatorAfter = 200
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.CutAction"),
        position = 300
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.CopyAction"),
        position = 400,
        separatorAfter = 500
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "Edit", id = "org.openide.actions.DeleteAction"),
        position = 600
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.RenameAction"),
        position = 700,
        separatorAfter = 800
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.SaveAsTemplateAction"),
        position = 900,
        separatorAfter = 1000
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.FileSystemAction"),
        position = 1100,
        separatorAfter = 1200
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.ToolsAction"),
        position = 1300
    ),
    @ActionReference(
        path = "Loaders/" + MIME_TYPE + "/Actions",
        id = @ActionID(category = "System", id = "org.openide.actions.PropertiesAction"),
        position = 1400
    )
})
public class BpmnDataObject extends MultiDataObject {
    public static final String ICON_BASE = "eu/doppelhelix/netbeans/plugin/bpmnediting/bpmn.png";
    public static final String MIME_TYPE = "text/bpmn+xml";

    public BpmnDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        registerEditor(MIME_TYPE, true);
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
        iconBase = ICON_BASE,
        mimeType = MIME_TYPE,
        persistenceType = TopComponent.PERSISTENCE_ONLY_OPENED,
        preferredID = "BpmnDataObjectEditor",
        position = 100
    )
    public static MultiViewEditorElement createEditor(Lookup lkp) {
        return new MultiViewEditorElement(lkp);
    }
}
