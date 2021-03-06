/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *     http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.stunner.cm.client.command;

import java.util.List;

import org.kie.workbench.common.stunner.core.client.canvas.AbstractCanvasHandler;
import org.kie.workbench.common.stunner.core.client.canvas.command.AbstractCanvasCommand;
import org.kie.workbench.common.stunner.core.client.command.CanvasViolation;
import org.kie.workbench.common.stunner.core.client.shape.MutationContext;
import org.kie.workbench.common.stunner.core.command.CommandResult;
import org.kie.workbench.common.stunner.core.diagram.Diagram;
import org.kie.workbench.common.stunner.core.graph.Edge;
import org.kie.workbench.common.stunner.core.graph.Node;
import org.kie.workbench.common.stunner.core.graph.content.relationship.Child;
import org.kie.workbench.common.stunner.core.graph.content.view.View;
import org.kie.workbench.common.stunner.core.graph.processing.traverse.content.AbstractChildrenTraverseCallback;
import org.kie.workbench.common.stunner.core.graph.processing.traverse.content.ChildrenTraverseProcessor;

/**
 * Draws the whole Case Management diagram. This implementation does not use Commands since loading cannot be "undone".
 */
public class CaseManagementDrawCommand extends AbstractCanvasCommand {

    private final ChildrenTraverseProcessor childrenTraverseProcessor;

    public CaseManagementDrawCommand(final ChildrenTraverseProcessor childrenTraverseProcessor) {
        this.childrenTraverseProcessor = childrenTraverseProcessor;
    }

    @Override
    @SuppressWarnings("unchecked")
    public CommandResult<CanvasViolation> execute(final AbstractCanvasHandler context) {
        final Diagram diagram = context.getDiagram();
        final String shapeSetId = context.getDiagram().getMetadata().getShapeSetId();

        childrenTraverseProcessor
                .traverse(diagram.getGraph(),
                          new AbstractChildrenTraverseCallback<Node<View, Edge>, Edge<Child, Node>>() {

                              @Override
                              public void startNodeTraversal(final Node<View, Edge> node) {
                                  super.startNodeTraversal(node);
                                  addNode(node);
                              }

                              @Override
                              public boolean startNodeTraversal(final List<Node<View, Edge>> parents,
                                                                final Node<View, Edge> node) {
                                  super.startNodeTraversal(parents,
                                                           node);
                                  addNode(node);
                                  context.addChild(parents.get(parents.size() - 1),
                                                   node);
                                  return true;
                              }

                              private void addNode(final Node<View, Edge> node) {
                                  context.register(shapeSetId,
                                                   node);
                                  context.applyElementMutation(node,
                                                               MutationContext.STATIC);
                              }

                              @Override
                              public void endGraphTraversal() {
                                  super.endGraphTraversal();
                                  context.getCanvas().draw();
                              }
                          });

        return buildResult();
    }

    @Override
    public CommandResult<CanvasViolation> undo(final AbstractCanvasHandler context) {
        throw new UnsupportedOperationException("Draw cannot be undone, yet.");
    }
}
