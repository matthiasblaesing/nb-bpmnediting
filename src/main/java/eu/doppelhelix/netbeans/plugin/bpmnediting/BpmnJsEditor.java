package eu.doppelhelix.netbeans.plugin.bpmnediting;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.URL;
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
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import netscape.javascript.JSObject;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.openide.awt.UndoRedo;
import org.openide.cookies.EditorCookie;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.RequestProcessor;
import org.openide.util.Task;
import org.openide.windows.TopComponent;

@Messages("LBL_BPMNJS_EDITOR=Visual Editor")
@MultiViewElement.Registration(
    displayName = "#LBL_BPMNJS_EDITOR",
    //    iconBase = "nl/cloudfarming/client/farm/model/house.png",
    mimeType = "text/bpmn+xml",
    persistenceType = TopComponent.PERSISTENCE_NEVER,
    preferredID = "BpmnJsEditor",
    position = 200)
public final class BpmnJsEditor extends JPanel implements MultiViewElement {

    private static final Logger LOG = Logger.getLogger(BpmnJsEditor.class.getName());
    private RequestProcessor requestProcessor = new RequestProcessor();

    private JToolBar toolbar;
    private Lookup lookup;
    private MultiViewElementCallback multiviewCallback;
    private JFXPanel jfxPanel;
    private WebView webview;
    private Console console = new Console();

    public BpmnJsEditor(Lookup lookup) {
        this.toolbar = new JToolBar();
        this.lookup = lookup;
        this.setLayout(new BorderLayout());
        this.jfxPanel = new JFXPanel();
        this.add(jfxPanel);
        Platform.runLater(() -> {
            BorderPane root = new BorderPane();
            Scene scene = new Scene(root);

            webview = new WebView();
            root.setCenter(webview);

            URL bpmnJS = BpmnJsEditor.class.getResource("webapp/bpmn-modeler.development.js");
            URL integrationJS = BpmnJsEditor.class.getResource("webapp/integration.js");
            URL bpmnCSS = BpmnJsEditor.class.getResource("webapp/bpmn.css");
            URL diagramCSS = BpmnJsEditor.class.getResource("webapp/diagram-js.css");
            URL styleCSS = BpmnJsEditor.class.getResource("webapp/style.css");

            String data = "<!DOCTYPE html>\n"
                + "<html>\n"
                + "  <head>\n"
                + "    <meta charset=\"UTF-8\" />\n"
                + "    <link rel=\"stylesheet\" href=\"" + diagramCSS + "\">\n"
                + "    <link rel=\"stylesheet\" href=\"" + bpmnCSS + "\">\n"
                + "    <link rel=\"stylesheet\" href=\"" + styleCSS + "\">\n"
                + "    <script src=\"" + bpmnJS + "\"></script>\n"
                + "    <script src=\"" + integrationJS + "\"></script>\n"
                + "  </head>\n"
                + "  <body>\n"
                + "    <div id=\"canvas\"></div>\n"
                + "  </body>\n"
                + "</html>";

            webview.getEngine().getLoadWorker().stateProperty().addListener((obs, ov, nv) -> {
                if (nv == State.SUCCEEDED) {
                    JSObject window = (JSObject) webview.getEngine().executeScript("window");
                    window.setMember("consoleJava", console);
                    webview.getEngine().executeScript("console = {}; console.log = function() {consoleJava.log(JSON.stringify(arguments))};");
                    webview.getEngine().executeScript("integration.importDiagram(false)");
                    try {
                        this.lookup.lookup(EditorCookie.class).openDocument().addDocumentListener(new DocumentListener() {
                            @Override
                            public void insertUpdate(DocumentEvent e) {
                                Platform.runLater(() -> webview.getEngine().executeScript("integration.importDiagram(true)"));
                            }

                            @Override
                            public void removeUpdate(DocumentEvent e) {
                                Platform.runLater(() -> webview.getEngine().executeScript("integration.importDiagram(true)"));
                            }

                            @Override
                            public void changedUpdate(DocumentEvent e) {
                            }
                        });
                    } catch (IOException ex) {
                        
                    }
                }
            });

            webview.getEngine().loadContent(data);

            jfxPanel.setScene(scene);
        });
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
        return new Action[]{};
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

    public class Console {

        public void log(String data) {
            System.out.println(data);
        }

        public String getDocument() {
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if(ec != null) {
                try {
                    Document doc = ec.openDocument();
                    if(doc != null) {
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
            EditorCookie ec = lookup.lookup(EditorCookie.class);
            if (ec != null) {
                try {
                    AbstractDocument doc = (AbstractDocument) ec.openDocument();
                    if (doc != null) {
                        try {
                            doc.replace(0, doc.getLength(), newDoc, null);
                        } catch (BadLocationException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                } catch (IOException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
        }

    }
}
