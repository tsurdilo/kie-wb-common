/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.workbench.common.stunner.bpmn.backend.indexing;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jbpm.bpmn2.core.ItemDefinition;
import org.jbpm.bpmn2.core.Message;
import org.jbpm.compiler.xml.ProcessDataEventListener;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.node.RuleSetNode;
import org.jbpm.workflow.core.node.SubProcessNode;
import org.jbpm.workflow.core.node.WorkItemNode;
import org.kie.api.definition.process.Process;
import org.kie.workbench.common.services.refactoring.Resource;
import org.kie.workbench.common.services.refactoring.backend.server.impact.ResourceReferenceCollector;
import org.kie.workbench.common.services.refactoring.service.PartType;
import org.kie.workbench.common.services.refactoring.service.ResourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This listener is called by the build process and immediately stores the indexing information (as it is also a
 * {@link ResourceReferenceCollector})
 * </p>
 * In the {@link #onProcessAdded(Process)} method, it stores itself in the {@link Process}, allowing the {@link BpmnFileIndexer}
 * to retrieve the {@link BpmnProcessDataEventListener} instance later and add it to the indexing information.
 */
public class BpmnProcessDataEventListener
        extends ResourceReferenceCollector
        implements ProcessDataEventListener,
                   Serializable

{

    private static final Logger logger = LoggerFactory.getLogger(BpmnProcessDataEventListener.class);

    public static final String NAME = "BPMNProcessInfoCollector";

    private List<Variable> variables = null;

    private Set<String> signals = new HashSet<>();
    private Set<String> messages = new HashSet<>();
    private Map<String, ItemDefinition> itemDefinitions = new HashMap<>();

    private Process process;

    private Set<String> referencedClasses;

    private Set<String> unqualifiedClasses;
    // can be transient as it's only used when building
    private transient Resource resource;

    public Process getProcess() {
        return process;
    }

    // ProcessDataEventListener methods -------------------------------------------------------------------------------------------

    @Override
    public void onNodeAdded(Node node) {
        if (node instanceof RuleSetNode) {
            RuleSetNode ruleSetNode = (RuleSetNode) node;
            String ruleFlowGroup = ruleSetNode.getRuleFlowGroup();
            if (ruleFlowGroup != null) {
                addSharedReference(ruleFlowGroup,
                                   PartType.RULEFLOW_GROUP);
            }
        } else if (node instanceof WorkItemNode) {
            String taskName = ((WorkItemNode) node).getWork().getName();
            addSharedReference(taskName,
                               PartType.TASK_NAME);
        } else if (node instanceof SubProcessNode) {
            SubProcessNode subProcess = (SubProcessNode) node;

            String processName = subProcess.getProcessName();
            if (!StringUtils.isEmpty(processName)) {
                addResourceReference(processName,
                                     ResourceType.BPMN2_NAME);
            }
            String processId = subProcess.getProcessId();
            if (!StringUtils.isEmpty(processId)) {
                addResourceReference(processId,
                                     ResourceType.BPMN2);
            }
        }
    }

    @Override
    public void onProcessAdded(Process process) {
        logger.debug("Added process with id {} and name {}",
                     process.getId(),
                     process.getName());
        this.process = process;
        resource = addResource(process.getId(),
                               ResourceType.BPMN2);
        addResource(process.getName(),
                    ResourceType.BPMN2_NAME);

        //add process descriptor as process meta data
        process.getMetaData().put(NAME,
                                  this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onMetaDataAdded(String name,
                                Object data) {
        if (name.equals("Variable")) {
            if (variables == null) {
                variables = new ArrayList<>();
            }
            variables.add((Variable) data);
        } else if ("ItemDefinitions".equals(name)) {
            itemDefinitions = (Map<String, ItemDefinition>) data;
        } else if ("signalNames".equals(name)) {
            signals = (Set<String>) data;
        } else if ("Messages".equals(name)) {
            Map<String, Message> builderMessagesMap = (Map<String, Message>) data;
            //JDK-6750650 - cant serialize the keySet itself - so copy is needed
            messages = new HashSet(builderMessagesMap.keySet());
        }
    }

    @Override
    public void onComplete(Process process) {
        // process item definitions
        visitItemDefinitions();

        // process globals
        Map<String, String> globals = ((RuleFlowProcess) process).getGlobals();
        visitGlobals(globals);

        // process imports
        Set<String> imports = ((RuleFlowProcess) process).getImports();
        visitImports(imports);
    }

    private void visitItemDefinitions() {
        if (itemDefinitions != null) {
            for (ItemDefinition item : itemDefinitions.values()) {
                String structureRef = item.getStructureRef();
                if (structureRef.contains(".")) {
                    getReferencedClasses().add(structureRef);
                } else {
                    getUnqualifiedClasses().add(structureRef);
                }
            }
        }
    }

    private void visitGlobals(Map<String, String> globals) {
        if (globals != null) {
            Set<String> globalNames = new HashSet<>();
            for (Map.Entry<String, String> globalEntry : globals.entrySet()) {
                globalNames.add(globalEntry.getKey());
                String type = globalEntry.getValue();
                if (type.contains(".")) {
                    getReferencedClasses().add(type);
                } else {
                    getUnqualifiedClasses().add(type);
                }
            }
            for (String globalName : globalNames) {
                addSharedReference(globalName,
                                   PartType.GLOBAL);
            }
        }
    }

    private void visitImports(Set<String> imports) {
        if (imports != null) {
            for (String type : imports) {
                if (type.contains(".")) {
                    getReferencedClasses().add(type);
                } else {
                    getUnqualifiedClasses().add(type);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onBuildComplete(Process process) {
        // process java dialect types
        Set<String> referencedTypes = (Set<String>) process.getMetaData().get("JavaDialectReferencedTypes");
        if (referencedTypes != null && !referencedTypes.isEmpty()) {
            getReferencedClasses().addAll(referencedTypes);
        }
        Set<String> unqualifiedClasses = (Set<String>) process.getMetaData().get("JavaDialectUnqualifiedTypes");
        if (unqualifiedClasses != null && !unqualifiedClasses.isEmpty()) {
            getUnqualifiedClasses().addAll(unqualifiedClasses);
        }

        // process java return value types
        referencedTypes = (Set<String>) process.getMetaData().get("JavaReturnValueReferencedTypes");
        if (referencedTypes != null && !referencedTypes.isEmpty()) {
            getReferencedClasses().addAll(referencedTypes);
        }
        unqualifiedClasses = (Set<String>) process.getMetaData().get("JavaReturnValueUnqualifiedTypes");
        if (unqualifiedClasses != null && !unqualifiedClasses.isEmpty()) {
            getUnqualifiedClasses().addAll(unqualifiedClasses);
        }

        // process mvel dialect types
        referencedTypes = (Set<String>) process.getMetaData().get("MVELDialectReferencedTypes");
        if (referencedTypes != null && !referencedTypes.isEmpty()) {
            getReferencedClasses().addAll(referencedTypes);
        }

        // process mvel return value types
        referencedTypes = (Set<String>) process.getMetaData().get("MVELReturnValueReferencedTypes");
        if (referencedTypes != null && !referencedTypes.isEmpty()) {
            getReferencedClasses().addAll(referencedTypes);
        }

        // process unqualified classes
        resolveUnqualifiedClasses();

        // process variables
        if (variables != null) {
            for (Variable data : variables) {
                String type = data.getType().getStringType();
                String itemSubjectRef = (String) data.getMetaData("ItemSubjectRef");
                if (itemSubjectRef != null && itemDefinitions != null) {
                    ItemDefinition itemDef = itemDefinitions.get(itemSubjectRef);
                    type = itemDef.getStructureRef();
                }

                resource.addPart(data.getName(),
                                 PartType.VARIABLE);
                if (type.contains(".")) {
                    getReferencedClasses().add(type);
                } else {
                    getUnqualifiedClasses().add(type);
                }
            }
        }

        // process signals, messages, etc.
        visitSignals(signals);
        visitSignals(messages);

        // (DRL) function imports
        visitFunctionImports(((RuleFlowProcess) process).getFunctionImports());
    }

    private void visitFunctionImports(List<String> functionImports) {
        if (functionImports != null) {
            for (String functionImport : functionImports) {
                if (!functionImport.endsWith("*")) {
                    addResourceReference(functionImport,
                                         ResourceType.FUNCTION);
                }
            }
        }
    }

    private void visitSignals(Collection<String> signals) {
        if (signals != null) {
            for (String signal : signals) {
                addSharedReference(signal,
                                   PartType.SIGNAL);
            }
        }
    }

    // Un/Qualified classes -------------------------------------------------------------------------------------------------------

    private void resolveUnqualifiedClasses() {
        Set<String> qualifiedClassSimpleNames = new HashSet<String>();
        for (String className : getReferencedClasses()) {
            qualifiedClassSimpleNames.add(className.substring(className.lastIndexOf('.') + 1));
        }
        for (Iterator<String> iter = getUnqualifiedClasses().iterator(); iter.hasNext(); ) {
            if (qualifiedClassSimpleNames.contains(iter.next())) {
                iter.remove();
            }
        }
        for (Iterator<String> iter = getUnqualifiedClasses().iterator(); iter.hasNext(); ) {
            String name = iter.next();
            if ("Object".equals(name) || "String".equals(name)
                    || "Float".equals(name) || "Integer".equals(name)
                    || "Boolean".equals(name)) {
                getReferencedClasses().add("java.lang." + name);
                iter.remove();
            }
        }
        for (String className : getUnqualifiedClasses()) {
            logger.warn("Unable to resolve unqualified class name, adding to list of classes: '{}'",
                        className);
            getReferencedClasses().add(className);
        }
    }

    private Set<String> getReferencedClasses() {
        if (referencedClasses == null) {
            referencedClasses = new HashSet<>(4);
        }
        return referencedClasses;
    }

    private Set<String> getUnqualifiedClasses() {
        if (unqualifiedClasses == null) {
            unqualifiedClasses = new HashSet<>(4);
        }
        return unqualifiedClasses;
    }
}
