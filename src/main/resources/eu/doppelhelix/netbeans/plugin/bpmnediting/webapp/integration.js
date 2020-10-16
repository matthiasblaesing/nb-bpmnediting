/* global consoleJava */
var integration = new function () {
    var bpmnModeler;

    this.init = function () {
        // modeler instance
        bpmnModeler = new BpmnJS( {
            container: '#canvas',
            keyboard: {
                bindTo: window
            }
        } );

        bpmnModeler.on( 'commandStack.changed', function () {
            exportDiagram();
        } );
    };

    /**
     * Save diagram contents and print them to the console.
     */
    async function exportDiagram( ) {
        try {
            var result = await bpmnModeler.saveXML( { format: true } );
            consoleJava.saveDocument( result.xml );
        } catch ( err ) {
            console.error( 'could not save BPMN 2.0 diagram', err );
        }
    }

    /**
     * Open diagram in our modeler instance.
     *
     * @param {String} bpmnXML diagram to display
     */
    async function openDiagram( bpmnXML, dontZoom ) {
        try {
            await bpmnModeler.importXML( bpmnXML );
            if ( !dontZoom ) {
                var canvas = bpmnModeler.get( 'canvas' );
                canvas.zoom( 'fit-viewport' );
            }
        } catch ( err ) {

            console.error( 'could not import BPMN 2.0 diagram', err );
        }
    }


    this.importDiagram = function(dontZoom) {
        openDiagram( consoleJava.getDocument( ), dontZoom );
    };
};

// see if DOM is already available
if ( document.readyState === "complete" || document.readyState === "interactive" ) {
    // call on next available tick
    setTimeout( () => integration.init(), 1 );
} else {
    document.addEventListener( "DOMContentLoaded", () => integration.init() );
}
