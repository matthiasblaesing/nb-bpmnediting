
package eu.doppelhelix.netbeans.plugin.bpmnediting;

import javafx.application.Platform;
import org.openide.modules.OnStart;

@OnStart
public class JavaFxPreventExit implements Runnable {

    @Override
    public void run() {
        Platform.setImplicitExit(false);
    }

}
