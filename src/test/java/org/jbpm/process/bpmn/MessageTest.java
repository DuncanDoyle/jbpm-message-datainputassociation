package org.jbpm.process.bpmn;

import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.jbpm.process.instance.ProcessRuntimeImpl;
import org.jbpm.workflow.instance.impl.WorkflowProcessInstanceImpl;
import org.junit.Test;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.*;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

import java.util.HashMap;

public class MessageTest {

	@Test
    public void workingBpmnTest() {
        ProcessRuntimeImpl processRuntime = getProcessRuntime("workingMessageModel.bpmn");

        createAutocompletingServiceTaskHandler(processRuntime);
        createAutocompletingSendTaskHandler(processRuntime);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("messageContent", "some text");
        processRuntime.startProcess("workingMessageModel", parameters);
    }

    @Test
    public void notWorkingBpmnTest() {
        ProcessRuntimeImpl processRuntime = getProcessRuntime("notWorkingMessageModel.bpmn");

        createAutocompletingServiceTaskHandler(processRuntime);
        createAutocompletingSendTaskHandler(processRuntime);

        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("messageContent", "some text");
        processRuntime.startProcess("workingMessageModel", parameters);
    }

    private void createAutocompletingSendTaskHandler(ProcessRuntimeImpl processRuntime) {
        processRuntime.getWorkItemManager().registerWorkItemHandler("Send Task", new WorkItemHandler() {
            @Override
            public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                System.out.println(workItem.getParameter("Message"));
                manager.completeWorkItem(workItem.getId(), null);
            }

            @Override
            public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
                // nothing to do
            }
        });
    }

    private void createAutocompletingServiceTaskHandler(ProcessRuntimeImpl processRuntime) {
        processRuntime.getWorkItemManager().registerWorkItemHandler("Service Task", new WorkItemHandler() {
            @Override
            public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
                manager.completeWorkItem(workItem.getId(), null);
            }

            @Override
            public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
                // nothing to do
            }
        });
    }

    private synchronized ProcessRuntimeImpl getProcessRuntime(String bpmnFilePath) {
    	RuntimeEnvironment environment = RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .addAsset(ResourceFactory.newClassPathResource(bpmnFilePath), ResourceType.BPMN2)
                .persistence(false)
                .get();

    	//Use bpmn file path as ID.
        RuntimeManager manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment, bpmnFilePath);
        
        RuntimeEngine runtime = manager.getRuntimeEngine(EmptyContext.get());
        
        KieSession kieSession = runtime.getKieSession();
        return (ProcessRuntimeImpl) ((StatefulKnowledgeSessionImpl) kieSession).getProcessRuntime();
    }


}
