package dev.jianmu.workflow.repository;

import dev.jianmu.workflow.aggregate.definition.Workflow;

import java.util.List;
import java.util.Optional;

public interface WorkflowRepository {
    Optional<Workflow> findByRefAndVersion(String ref, String version);

    Optional<Workflow> findByRefVersion(String refVersion);

    List<Workflow> findByRefVersions(List<String> refVersions);

    List<Workflow> findByRef(String ref);

    Workflow add(Workflow workflow);

    void deleteByRefAndVersion(String ref, String version);

    void deleteByRef(String ref);

    void commitEvents(Workflow workflow);
}
