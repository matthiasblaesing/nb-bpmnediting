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

@TemplateRegistration(
        folder = "Other",
        content = "BPMNTemplate.bpmn",
        requireProject = false,
        scriptEngine = "freemarker",
        displayName = "BPMN",
        targetName = "process",
        iconBase = ICON_BASE
)
package eu.doppelhelix.netbeans.plugin.bpmnediting;

import org.netbeans.api.templates.TemplateRegistration;
import static eu.doppelhelix.netbeans.plugin.bpmnediting.BpmnDataObject.ICON_BASE;
