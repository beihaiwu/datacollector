/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.runner;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.streamsets.pipeline.api.Record;
import com.streamsets.pipeline.config.StageType;
import com.streamsets.pipeline.api.impl.Utils;
import com.streamsets.pipeline.record.RecordImpl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FullPipeBatch implements PipeBatch {
  private final SourceOffsetTracker offsetTracker;
  private final int batchSize;
  private final Map<String, List<Record>> fullPayload;
  private final Set<String> processedStages;
  private final List<StageOutput> stageOutputSnapshot;
  private final ErrorSink errorSink;
  private String newOffset;
  private int inputRecords;
  private int outputRecords;

  public FullPipeBatch(SourceOffsetTracker offsetTracker, int batchSize, boolean snapshotStagesOutput) {
    this.offsetTracker = offsetTracker;
    this.batchSize = batchSize;
    fullPayload = new HashMap<>();
    processedStages = new HashSet<>();
    stageOutputSnapshot = (snapshotStagesOutput) ? new ArrayList<StageOutput>() : null;
    errorSink = new ErrorSink();
  }

  @VisibleForTesting
  Map<String, List<Record>> getFullPayload() {
    return fullPayload;
  }

  @Override
  public int getBatchSize() {
    return batchSize;
  }

  @Override
  public String getPreviousOffset() {
    return offsetTracker.getOffset();
  }

  @Override
  public void setNewOffset(String offset) {
    newOffset = offset;
    offsetTracker.setOffset(offset);
  }

  @Override
  public BatchImpl getBatch(final Pipe pipe) {
    List<Record> records = new ArrayList<>();
    for (String inputLane : pipe.getInputLanes()) {
      records.addAll(fullPayload.remove(inputLane));
    }
    if (pipe.getStage().getDefinition().getType() == StageType.TARGET) {
      outputRecords += records.size();
    }
    return new BatchImpl(pipe.getStage().getInfo().getInstanceName(), offsetTracker.getOffset(), records);
  }

  @Override
  public BatchMakerImpl startStage(StagePipe pipe) {
    String stageName = pipe.getStage().getInfo().getInstanceName();
    Preconditions.checkState(!processedStages.contains(stageName), Utils.formatL(
      "The stage '{}' has been processed already", stageName));
    processedStages.add(stageName);
    for (String output : pipe.getOutputLanes()) {
      fullPayload.put(output, null);
    }
    int recordAllowance = (pipe.getStage().getDefinition().getType() == StageType.SOURCE)
                          ? getBatchSize() : Integer.MAX_VALUE;
    return new BatchMakerImpl(pipe, stageOutputSnapshot != null, recordAllowance);
  }

  @Override
  public void completeStage(BatchMakerImpl batchMaker) {
    StagePipe pipe = batchMaker.getStagePipe();
    if (pipe.getStage().getDefinition().getType() == StageType.SOURCE) {
      inputRecords += batchMaker.getSize();
    }
    Map<String, List<Record>> stageOutput = batchMaker.getStageOutput();
    // convert lane names from stage naming to pipe naming when adding to the payload
    // leveraging the fact that the stage output lanes and the pipe output lanes are in the same order
    List<String> stageLaneNames = pipe.getStage().getConfiguration().getOutputLanes();
    for (int i = 0; i < stageLaneNames.size() ; i++) {
      String stageLaneName = stageLaneNames.get(i);
      String pipeLaneName = pipe.getOutputLanes().get(i);
      fullPayload.put(pipeLaneName, stageOutput.get(stageLaneName));
    }
    if (stageOutputSnapshot != null) {
      String instanceName = pipe.getStage().getInfo().getInstanceName();
      stageOutputSnapshot.add(new StageOutput(instanceName, batchMaker.getStageOutputSnapshot(), errorSink));
    }
    if (pipe.getStage().getDefinition().getType() == StageType.TARGET) {
      outputRecords -= errorSink.getErrorRecords(pipe.getStage().getInfo().getInstanceName()).size();
    }
  }

  @Override
  public void commitOffset() {
    offsetTracker.commitOffset();
  }

  @Override
  public Map<String, List<Record>> getLaneOutputRecords(List<String> pipeLanes) {
    Map<String, List<Record>> snapshot = new HashMap<>();
    for (String pipeLane : pipeLanes) {
      //The observer will copy
      snapshot.put(pipeLane, fullPayload.get(pipeLane));
    }
    return snapshot;
  }

  //TODO rename method
  private List<Record> createSnapshot(List<Record> records) {
    List<Record> list = new ArrayList<>(records.size());
    for (Record record : records) {
      list.add(((RecordImpl) record).clone());
    }
    return list;
  }

  private Map<String, List<Record>> createSnapshot(Map<String, List<Record>> output) {
    Map<String, List<Record>> copy = new HashMap<>();
    for (Map.Entry<String, List<Record>> entry : output.entrySet()) {
      copy.put(entry.getKey(), createSnapshot(entry.getValue()));
    }
    return copy;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void overrideStageOutput(StagePipe pipe, StageOutput stageOutput) {
    startStage(pipe);
    for (String pipeLaneName : pipe.getOutputLanes()) {
      String stageLaneName = LaneResolver.removePostFixFromLane(pipeLaneName);
      fullPayload.put(pipeLaneName, stageOutput.getOutput().get(stageLaneName));
    }
    if (stageOutputSnapshot != null) {
      stageOutputSnapshot.add(new StageOutput(stageOutput.getInstanceName(),
                                              (Map) createSnapshot(stageOutput.getOutput()),
                                              (List) stageOutput.getErrorRecords(), stageOutput.getStageErrors()));
    }
  }

  @Override
  public List<StageOutput> getSnapshotsOfAllStagesOutput() {
    return stageOutputSnapshot;
  }

  @Override
  public ErrorSink getErrorSink() {
    return errorSink;
  }

  @Override
  public void moveLane(String inputLane, String outputLane) {
    fullPayload.put(outputLane, Preconditions.checkNotNull(fullPayload.remove(inputLane), Utils.formatL(
        "Stream '{}' does not exist", inputLane)));
  }

  @Override
  public void moveLaneCopying(String inputLane, List<String> outputLanes) {
    List<Record> records = Preconditions.checkNotNull(fullPayload.remove(inputLane), Utils.formatL(
        "Stream '{}' does not exist", inputLane));
    for (String lane : outputLanes) {
      Preconditions.checkNotNull(fullPayload.containsKey(lane), Utils.formatL("Lane '{}' does not exist", lane));
      fullPayload.put(lane, createCopy(records));
    }
  }

  private List<Record> createCopy(List<Record> records) {
    List<Record> list = new ArrayList<>(records.size());
    for (Record record : records) {
      list.add(((RecordImpl) record).clone());
    }
    return list;
  }

  private List<String> remove(List<String> from, Collection<String> values) {
    List<String> list = new ArrayList<>(from);
    list.removeAll(values);
    return list;
  }

  @Override
  public void combineLanes(List<String> lanes, String to) {
    List<String> undefLanes = remove(lanes, fullPayload.keySet());
    Preconditions.checkState(undefLanes.isEmpty(), Utils.formatL("Lanes '{}' does not exist", undefLanes));
    fullPayload.put(to, new ArrayList<Record>());
    for (String lane : lanes) {
      List<Record> records = Preconditions.checkNotNull(fullPayload.remove(lane), Utils.formatL(
          "Stream '{}' does not exist", lane));
      fullPayload.get(to).addAll(records);
    }
  }

  @Override
  public int getInputRecords() {
    return inputRecords;
  }

  @Override
  public int getOutputRecords() {
    return outputRecords;
  }

  @Override
  public int getErrorRecords() {
    return errorSink.getTotalErrorRecords();
  }

  @Override
  public int getErrorMessages() {
    return errorSink.getTotalErrorMessages();
  }

  @Override
  public String toString() {
    return Utils.format(
        "PipeBatch[previousOffset='{}' currentOffset='{}' batchSize='{}' keepSnapshot='{}' errorRecords='{}]'",
        offsetTracker.getOffset(), newOffset, batchSize, stageOutputSnapshot != null, errorSink.size());
  }

}
