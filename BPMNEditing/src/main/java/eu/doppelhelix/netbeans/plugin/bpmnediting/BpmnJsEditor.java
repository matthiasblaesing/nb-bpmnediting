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

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;
import java.io.StringReader;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.concurrent.Worker.State;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.web.WebView;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.undo.UndoableEdit;
import netscape.javascript.JSObject;
import org.netbeans.api.diff.Difference;
import org.netbeans.api.editor.document.LineDocumentUtils;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.editor.BaseDocument;
import org.netbeans.spi.diff.DiffProvider;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.text.CloneableEditorSupport;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.RequestProcessor.Task;
import org.openide.windows.TopComponent;

@Messages("LBL_BPMNJS_EDITOR=Visual Editor")
@MultiViewElement.Registration(
    displayName = "#LBL_BPMNJS_EDITOR",
    iconBase = "eu/doppelhelix/netbeans/plugin/bpmnediting/bpmn.png",
    mimeType = "text/bpmn+xml",
    persistenceType = TopComponent.PERSISTENCE_NEVER,
    preferredID = "BpmnJsEditor",
    position = 200)
public final class BpmnJsEditor extends JPanel implements MultiViewElement {

    private static final Logger LOG = Logger.getLogger(BpmnJsEditor.class.getName());

    private final RequestProcessor requestProcessor = new RequestProcessor();

    private final Task updateTask = this.requestProcessor.create(() -> this.updateDiagramFromDocument());
    private final JToolBar toolbar;
    /**
     * <p>Variable acts as a guard so that update cycles like:</p>
     *
     * <p>diagram change -&gt; document change -&gt; diagram change -&gt; ...</p>
     *
     * <p>are prevented</p>
     */
    private final AtomicInteger initiatedByUs = new AtomicInteger();
    private Lookup lookup;
    private MultiViewElementCallback multiviewCallback;
    private JFXPanel jfxPanel;
    private WebView webview;
    private JavaIntegration javaIntegration = new JavaIntegration();

    public BpmnJsEditor(Lookup lookup) {
        this.toolbar = new JToolBar();
        this.lookup = lookup;
        this.setLayout(new BorderLayout());
        this.jfxPanel = new JFXPanel();
        // Ensure key input is marked as consumed -- they are handled by the
        // webview or bpmn-js
        this.jfxPanel.addKeyListener(new KeyListener() {
            @Override
            public void keyReleased(KeyEvent e) {
                e.consume();
            }

            @Override
            public void keyPressed(KeyEvent e) {
                e.consume();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                e.consume();
            }

        });
        this.add(jfxPanel);
        Platform.runLater(() -> {
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root);

            webview = new WebView();
            root.setCenter(webview);

            webview.getEngine().getLoadWorker().stateProperty().addListener((obs, ov, nv) -> {
                if (nv == State.SUCCEEDED) {
                    JSObject window = (JSObject) webview.getEngine().executeScript("window");
                    window.setMember("javaIntegration", javaIntegration);
                    webview.getEngine().executeScript(
                        """
                        console = {};
                        console.debug = function() {javaIntegration.log(JSON.stringify(arguments))};
                        console.log = function() {javaIntegration.log(JSON.stringify(arguments))};
                        console.error = function() {javaIntegration.log(JSON.stringify(arguments))};
                        console.warn = function() {javaIntegration.log(JSON.stringify(arguments))};
                        """
                    );
                    updateDiagramFromDocumentDelayed();
                }
            });

            URL url = BpmnJsEditor.class.getResource("webapp/index.html");
            webview.getEngine().load(url.toExternalForm());

            jfxPanel.setScene(scene);

            try {
                this.lookup.lookup(EditorCookie.class).openDocument().addDocumentListener(new DocumentListener() {
                    @Override
                    public void insertUpdate(DocumentEvent e) {
                        updateDiagramFromDocumentDelayed();
                    }

                    @Override
                    public void removeUpdate(DocumentEvent e) {
                        updateDiagramFromDocumentDelayed();
                    }

                    @Override
                    public void changedUpdate(DocumentEvent e) {
                        updateDiagramFromDocumentDelayed();
                    }
                });
            } catch (IOException ex) {

            }

        });
    }

    private void updateDiagramFromDocumentDelayed() {
        if(BpmnJsEditor.this.initiatedByUs.get() == 0) {
            updateTask.schedule(500);
        }
    }

    private void updateDiagramFromDocument() {
        if(BpmnJsEditor.this.initiatedByUs.get() == 0) {
            BpmnJsEditor.this.initiatedByUs.incrementAndGet();
            try {
                Platform.runLater(() -> webview.getEngine().executeScript("integration.importDiagram(true)"));
            } finally {
                BpmnJsEditor.this.initiatedByUs.decrementAndGet();
            }
        }
    }

    private void updateDocumentFromDiagram(String newDoc) {
        BpmnJsEditor.this.initiatedByUs.incrementAndGet();
        try {
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (ec != null) {
                try {
                    BaseDocument doc = (BaseDocument) ec.openDocument();
                    if (doc != null) {
                        doc.runAtomic(() -> {
                            sendUndoableEdit(doc, CloneableEditorSupport.BEGIN_COMMIT_GROUP);
                            try {
                                String targetDoc = doc.getText(0, doc.getLength());
                                DiffProvider dp = Lookup.getDefault().lookup(DiffProvider.class);
                                Difference[] differenced = dp.computeDiff(new StringReader(targetDoc), new StringReader(newDoc));
                                for (Difference d : differenced) {
                                    int linestart = LineDocumentUtils.getLineStartFromIndex(doc, d.getSecondStart() - 1);
                                    int lineendRemove = LineDocumentUtils.getLineStartFromIndex(doc, d.getSecondStart() + d.getFirstEnd() - d.getFirstStart());
                                    switch (d.getType()) {
                                        case Difference.DELETE:
                                            doc.replace(linestart, lineendRemove - linestart, "", null);
                                            break;
                                        case Difference.ADD:
                                            doc.insertString(linestart, d.getSecondText(), null);
                                            break;
                                        case Difference.CHANGE:
                                            doc.replace(linestart, lineendRemove - linestart, d.getSecondText(), null);
                                            break;
                                    }
                                }
                            } catch (BadLocationException | IOException ex) {
                                Exceptions.printStackTrace(ex);
                            } finally {
                                sendUndoableEdit(doc, CloneableEditorSupport.END_COMMIT_GROUP);
                            }
                        });
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        } finally {
            BpmnJsEditor.this.initiatedByUs.decrementAndGet();
        }
    }

    private void sendUndoableEdit(BaseDocument d, UndoableEdit ue) {
        UndoableEditListener[] uels = d.getUndoableEditListeners();
        UndoableEditEvent ev = new UndoableEditEvent(d, ue);
        for (UndoableEditListener uel : uels) {
            uel.undoableEditHappened(ev);
        }
    }

    @Override
    public JComponent getVisualRepresentation() {
        return this;
    }

    @Override
    public JComponent getToolbarRepresentation() {
        return this.toolbar;
    }

    @Override
    public Action[] getActions() {
        return multiviewCallback.createDefaultActions();
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void componentOpened() {
    }

    @Override
    public void componentClosed() {
    }

    @Override
    public void componentShowing() {
    }

    @Override
    public void componentHidden() {
    }

    @Override
    public void componentActivated() {
    }

    @Override
    public void componentDeactivated() {
    }

    @Override
    public UndoRedo getUndoRedo() {
        return null;
    }

    @Override
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.multiviewCallback = callback;
    }

    @Override
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }

    public class JavaIntegration {

        public void log(String data) {
            System.out.println(data);
        }

        public String getDocument() {
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (ec != null) {
                try {
                    Document doc = ec.openDocument();
                    if (doc != null) {
                        try {
                            String text = doc.getText(0, doc.getLength());
                            return text;
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return "";
        }

        public void saveDocument(String newDoc) {
            updateDocumentFromDiagram(newDoc);
        }
    }
}
