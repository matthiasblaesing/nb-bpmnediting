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

import BpmnModeler from 'bpmn-js/lib/Modeler';

import propertiesPanelModule from 'bpmn-js-properties-panel';
import propertiesProviderModule from 'bpmn-js-properties-panel/lib/provider/camunda';
import camundaModdleDescriptor from 'camunda-bpmn-moddle/resources/camunda.json';
import KeyboardBindings from 'diagram-js/lib/features/keyboard/KeyboardBindings';

import {
  isCmd,
  isKey,
  isShift
} from 'diagram-js/lib/features/keyboard/KeyboardUtil';

var LOW_PRIORITY = 500;

export var KEYCODE_C = 67;
export var KEYCODE_V = 86;
export var KEYCODE_Y = 89;
export var KEYCODE_Z = 90;
export var KEYCODE_DEL = 46;
export var KEYCODE_PLUS = 187;
export var KEYCODE_MINUS = 189;
export var KEYCODE_0 = 48;

export var KEYS_COPY = ['c', 'C', KEYCODE_C];
export var KEYS_PASTE = ['v', 'V', KEYCODE_V];
export var KEYS_REDO = ['y', 'Y', KEYCODE_Y];
export var KEYS_UNDO = ['z', 'Z', KEYCODE_Z];
export var KEYS_ZOOM_IN = ['+', 'Add', '=', KEYCODE_PLUS];
export var KEYS_ZOOM_OUT = ['-', 'Subtract', KEYCODE_MINUS];
export var KEYS_ZOOM_0 = ['0', KEYCODE_0];
export var KEYS_DELETE = ['Backspace', 'Delete', 'Del', KEYCODE_DEL];

/**
 * Adds default keyboard bindings.
 *
 * This does not pull in any features will bind only actions that
 * have previously been registered against the editorActions component.
 *
 * @param {EventBus} eventBus
 * @param {Keyboard} keyboard
 */
function CustomKeyboardBindings(eventBus, keyboard) {

    var self = this;

    eventBus.on('editorActions.init', LOW_PRIORITY, function (event) {

        var editorActions = event.editorActions;

        self.registerBindings(keyboard, editorActions);
    });
}


/**
 * Register available keyboard bindings.
 *
 * @param {Keyboard} keyboard
 * @param {EditorActions} editorActions
 */
CustomKeyboardBindings.prototype.registerBindings = function (keyboard, editorActions) {

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
    // (CTRL|CMD) + Z
    addListener('undo', function (context) {

        var event = context.keyEvent;

        if (isCmd(event) && !isShift(event) && isKey(KEYS_UNDO, event)) {
            editorActions.trigger('undo');

            return true;
        }
    });

    // redo
    // CTRL + Y
    // CMD + SHIFT + Z
    addListener('redo', function (context) {

        var event = context.keyEvent;

        if (isCmd(event) && (isKey(KEYS_REDO, event) || (isKey(KEYS_UNDO, event) && isShift(event)))) {
            editorActions.trigger('redo');

            return true;
        }
    });

    // copy
    // CTRL/CMD + C
    addListener('copy', function (context) {

        var event = context.keyEvent;

        if (isCmd(event) && isKey(KEYS_COPY, event)) {
            editorActions.trigger('copy');

            return true;
        }
    });

    // paste
    // CTRL/CMD + V
    addListener('paste', function (context) {

        var event = context.keyEvent;

        if (isCmd(event) && isKey(KEYS_PASTE, event)) {
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
        if (isKey(KEYS_ZOOM_IN, event) && isCmd(event)) {
            editorActions.trigger('stepZoom', {value: 1});

            return true;
        }
    });

    // zoom out one step
    // CTRL + -
    addListener('stepZoom', function (context) {

        var event = context.keyEvent;

        if (isKey(KEYS_ZOOM_OUT, event) && isCmd(event)) {
            editorActions.trigger('stepZoom', {value: -1});

            return true;
        }
    });

    // zoom to the default level
    // CTRL + 0
    addListener('zoom', function (context) {

        var event = context.keyEvent;

        if (isKey(KEYS_ZOOM_0, event) && isCmd(event)) {
            editorActions.trigger('zoom', {value: 1});

            return true;
        }
    });

    // delete selected element
    // DEL
    addListener('removeSelection', function (context) {

        var event = context.keyEvent;

        if (isKey(KEYS_DELETE, event)) {
            editorActions.trigger('removeSelection');

            return true;
        }
    });
};

/* global javaIntegration */
window.integration = new function () {
    var bpmnModeler;

    this.init = function () {
        // modeler instance
        bpmnModeler = new BpmnModeler({
            container: '#canvas',
            additionalModules: {
                __init__: ["customKeyboardBinding"],
                customKeyboardBinding: ["type", CustomKeyboardBindings],
                propertiesPanelModule,
                propertiesProviderModule
            },
            keyboard: {
                bindTo: document
            },
            propertiesPanel: {
                parent: '#properties'
            },
            moddleExtensions: {
                camunda: camundaModdleDescriptor
            }
        });

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

