package dev.jianmu.workflow;

import dev.jianmu.workflow.event.DomainEventPublisher;
import dev.jianmu.workflow.event.DomainEventSubscriber;
import dev.jianmu.workflow.event.definition.NodeActivatingEvent;
import dev.jianmu.workflow.event.definition.WorkflowEndEvent;
import dev.jianmu.workflow.event.definition.WorkflowStartEvent;
import dev.jianmu.workflow.event.process.*;

/**
 * @program: workflow
 * @description 一个简单打印事件的领域事件订阅者
 * @author Ethan Liu
 * @create 2021-01-24 12:57
*/
public class PrintEventSubscriber {
    public static void sub() {
        DomainEventSubscriber<NodeActivatingEvent> nodeActivatingEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": " + event.toString());
        DomainEventSubscriber<TaskActivatingEvent> activatingEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": " + event.toString());
        DomainEventSubscriber<TaskTerminatingEvent> terminatingEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": "  + event.toString());

        DomainEventSubscriber<TaskRunningEvent> runningEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": "  + event.toString());
        DomainEventSubscriber<TaskSucceededEvent> succeededEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": "  + event.toString());
        DomainEventSubscriber<TaskFailedEvent> failedEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": "  + event.toString());

        DomainEventSubscriber<WorkflowStartEvent> workflowStartEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": "  + event.toString());
        DomainEventSubscriber<WorkflowEndEvent> workflowEndEventDomainEventSubscriber = event -> System.out.println(event.getName() + ": "  + event.toString());

        DomainEventPublisher.subscribe(NodeActivatingEvent.class, nodeActivatingEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(TaskActivatingEvent.class, activatingEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(TaskTerminatingEvent.class, terminatingEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(TaskRunningEvent.class, runningEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(TaskSucceededEvent.class, succeededEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(TaskFailedEvent.class, failedEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(WorkflowStartEvent.class, workflowStartEventDomainEventSubscriber);
        DomainEventPublisher.subscribe(WorkflowEndEvent.class, workflowEndEventDomainEventSubscriber);
    }
}
