/*
 Copyright 2020 Matthias Bläsing.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

import 'bpmn-js/dist/assets/diagram-js.css';
import 'bpmn-js/dist/assets/bpmn-js.css';

import 'bpmn-js/dist/assets/bpmn-font/css/bpmn-embedded.css';
import '@bpmn-io/properties-panel/assets/properties-panel.css';

import './style.less';

import BpmnModeler from 'bpmn-js/lib/Modeler';

import {
    BpmnPropertiesPanelModule,
    BpmnPropertiesProviderModule,
    CamundaPlatformPropertiesProviderModule

}  from 'bpmn-js-properties-panel';
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';
import KeyboardBindings from 'diagram-js/lib/features/keyboard/KeyboardBindings';

import {
  isCmd,
  isKey,
  isShift
} from 'diagram-js/lib/features/keyboard/KeyboardUtil';

var LOW_PRIORITY = 500;

// Adapted from https://github.com/openjdk/jfx/blob/master/modules/javafx.graphics/src/main/java/javafx/scene/input/KeyCode.java
// JavaFX does not provide the standard properties, but KeyCode values and keyIdentifier seem to match
export var KEYCODE_C = "U+0043";
export var KEYCODE_V = "U+0056";
export var KEYCODE_Y = "U+0059";
export var KEYCODE_Z = "U+005A";
export var KEYCODE_DEL = "U+007F";
export var KEYCODE_PLUS = "U+00BB";
export var KEYCODE_MINUS = "U+00BD";
export var KEYCODE_0 = "U+0030";

/**
 * Register available keyboard bindings to bridge JavaFX problems
 *
 * @param {Keyboard} keyboard
 * @param {EditorActions} editorActions
 */
function registerBindings(keyboard, editorActions) {

    /**
     * Add keyboard binding if respective editor action
     * is registered.
     *
     * @param {string} action name
     * @param {Function} fn that implements the key binding
     */
    function addListener(action, fn) {

        if (editorActions.isRegistered(action)) {
            keyboard.addListener(fn);
        }
    }


    // undo
    // (CTRL|CMD) + U
    addListener('undo', function (context) {
        var event = context.keyEvent;

        if (isCmd(event) && !isShift(event) && event.keyIdentifier === KEYCODE_Z) {
            editorActions.trigger('undo');

            return true;
        }
    });

    // redo
    // CTRL + Y
    // CMD + SHIFT + Z
    addListener('redo', function (context) {

        var event = context.keyEvent;

        if (isCmd(event) && (event.keyIdentifier === KEYCODE_Y || (event.keyIdentifier === KEYCODE_Z && isShift(event)))) {
            editorActions.trigger('redo');

            return true;
        }
    });

    // copy
    // CTRL/CMD + C
    addListener('copy', function (context) {
        var event = context.keyEvent;

        if (isCmd(event) && event.keyIdentifier === KEYCODE_C) {
            editorActions.trigger('copy');

            return true;
        }
    });

    // paste
    // CTRL/CMD + V
    addListener('paste', function (context) {

        var event = context.keyEvent;

        if (isCmd(event) && event.keyIdentifier === KEYCODE_V) {
            editorActions.trigger('paste');

            return true;
        }
    });

    // zoom in one step
    // CTRL/CMD + +
    addListener('stepZoom', function (context) {

        var event = context.keyEvent;

        // quirk: it has to be triggered by `=` as well to work on international keyboard layout
        // cf: https://github.com/bpmn-io/bpmn-js/issues/1362#issuecomment-722989754
        if (event.keyIdentifier === KEYCODE_PLUS && isCmd(event)) {
            editorActions.trigger('stepZoom', {value: 1});

            return true;
        }
    });

    // zoom out one step
    // CTRL + -
    addListener('stepZoom', function (context) {

        var event = context.keyEvent;

        if (event.keyIdentifier === KEYCODE_MINUS && isCmd(event)) {
            editorActions.trigger('stepZoom', {value: -1});

            return true;
        }
    });

    // zoom to the default level
    // CTRL + 0
    addListener('zoom', function (context) {

        var event = context.keyEvent;

        if (event.keyIdentifier === KEYCODE_0 && isCmd(event)) {
            editorActions.trigger('zoom', {value: 1});

            return true;
        }
    });

    // delete selected element
    // DEL
    addListener('removeSelection', function (context) {

        var event = context.keyEvent;

        if (event.keyIdentifier === KEYCODE_DEL) {
            editorActions.trigger('removeSelection');

            return true;
        }
    });

};

/* global javaIntegration */
window.integration = new function () {
    var bpmnModeler;

    this.init = function () {
        bpmnModeler = new BpmnModeler({
            container: '#canvas',
            propertiesPanel: {
                parent: '#properties'
            },
            additionalModules: [
                BpmnPropertiesPanelModule,
                BpmnPropertiesProviderModule,
                CamundaPlatformPropertiesProviderModule
            ],
            moddleExtensions: {
                camunda: camundaModdleDescriptor
            }
        });

        registerBindings(bpmnModeler.get("keyboard"), bpmnModeler.get("editorActions"));

        bpmnModeler.on('commandStack.changed', function () {
            exportDiagram();
        });

        // openDiagram(null, true);
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
                const result = await bpmnModeler.importXML(bpmnXML);
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

