
import BpmnModeler from 'bpmn-js/lib/Modeler';

import propertiesPanelModule from 'bpmn-js-properties-panel';
import propertiesProviderModule from 'bpmn-js-properties-panel/lib/provider/camunda';
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';

/* global javaIntegration */
window.integration = new function () {
    var bpmnModeler;

    this.init = function () {
        console.log("Init 1");

        // modeler instance
        bpmnModeler = new BpmnModeler({
            container: '#canvas',
            additionalModules: [
                propertiesPanelModule,
                propertiesProviderModule
            ],
            keyboard: {
                bindTo: window
            },
            propertiesPanel: {
                parent: '#properties'
            },
            moddleExtensions: {
                camunda: camundaModdleDescriptor
            }
        });

        console.log("Init 2");

        bpmnModeler.on('commandStack.changed', function () {
            exportDiagram();
        });

        console.log("Init 3");

        openDiagram(null, true);
    };

    /**
     * Save diagram contents and print them to the console.
     */
    async function exportDiagram( ) {
        try {
            var result = await bpmnModeler.saveXML({format: true});
            javaIntegration.saveDocument(result.xml);
        } catch (err) {
            console.error('could not save BPMN 2.0 diagram', err);
        }
    }

    /**
     * Open diagram in our modeler instance.
     *
     * @param {String} bpmnXML diagram to display
     */
    async function openDiagram(bpmnXML, dontZoom) {
        try {
            if(! bpmnXML) {
                await bpmnModeler.createDiagram();
            } else {
                await bpmnModeler.importXML(bpmnXML);
            }
            if (!dontZoom) {
                var canvas = bpmnModeler.get('canvas');
                canvas.zoom('fit-viewport');
            }
        } catch (err) {

            console.error('could not import BPMN 2.0 diagram', err);
        }
    }


    this.importDiagram = function (dontZoom) {
        openDiagram(javaIntegration.getDocument( ), dontZoom);
    };
};

// see if DOM is already available
if (document.readyState === "complete" || document.readyState === "interactive") {
    // call on next available tick
    setTimeout(() => integration.init(), 1);
} else {
    document.addEventListener("DOMContentLoaded", () => integration.init());
}

setInterval(() => console.log("Timeout"), 10 * 1000);