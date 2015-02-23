/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.runner.production;

import com.google.common.annotations.VisibleForTesting;
import com.streamsets.pipeline.config.DataRuleDefinition;
import com.streamsets.pipeline.config.MetricsRuleDefinition;
import com.streamsets.pipeline.config.RuleDefinitions;
import com.streamsets.pipeline.runner.LaneResolver;
import com.streamsets.pipeline.runner.Observer;
import com.streamsets.pipeline.store.PipelineStoreException;
import com.streamsets.pipeline.store.PipelineStoreTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RulesConfigLoader {

  private final PipelineStoreTask pipelineStoreTask;
  private final String pipelineName;
  private final String revision;
  private RuleDefinitions previousRuleDefinitions;


  public RulesConfigLoader(String pipelineName, String revision, PipelineStoreTask pipelineStoreTask) {
    this.pipelineStoreTask = pipelineStoreTask;
    this.pipelineName = pipelineName;
    this.revision = revision;
  }

  public RuleDefinitions load(Observer observer) throws InterruptedException, PipelineStoreException {
    RuleDefinitions newRuleDefinitions = pipelineStoreTask.retrieveRules(pipelineName, revision);
    if (newRuleDefinitions != previousRuleDefinitions) {
      RulesConfigurationChangeRequest rulesConfigurationChangeRequest = detectChanges(previousRuleDefinitions,
        newRuleDefinitions);
      observer.setConfiguration(rulesConfigurationChangeRequest);
    }
    previousRuleDefinitions = newRuleDefinitions;
    return newRuleDefinitions;
  }

  @VisibleForTesting
  RulesConfigurationChangeRequest detectChanges(RuleDefinitions previousRuleDefinitions,
                                                        RuleDefinitions newRuleDefinitions)
    throws InterruptedException {
    //TODO: compute, detect changes etc and upload
    Set<String> rulesToRemove = new HashSet<>();
    Set<String> pipelineAlertsToRemove = new HashSet<>();
    Map<String, Integer> rulesWithSampledRecordSizeChanges = new HashMap<>();
    if (previousRuleDefinitions != null) {
      //detect rules to remove and also the rules whose 'retain sampled record size' has changed
      detectDataRuleChanges(previousRuleDefinitions.getDataRuleDefinitions(),
        newRuleDefinitions.getDataRuleDefinitions(), rulesToRemove, rulesWithSampledRecordSizeChanges);
      //detect metric rules to be removed
      detectMetricRuleChanges(previousRuleDefinitions.getMetricsRuleDefinitions(),
        newRuleDefinitions.getMetricsRuleDefinitions(), pipelineAlertsToRemove);
    }
    Map<String, List<DataRuleDefinition>> laneToDataRule = new HashMap<>();
    for (DataRuleDefinition dataRuleDefinition : newRuleDefinitions.getDataRuleDefinitions()) {
      String lane = LaneResolver.getPostFixedLaneForObserver(dataRuleDefinition.getLane());
      List<DataRuleDefinition> dataRuleDefinitions = laneToDataRule.get(lane);
      if (dataRuleDefinitions == null) {
        dataRuleDefinitions = new ArrayList<>();
        laneToDataRule.put(lane, dataRuleDefinitions);
      }
      dataRuleDefinitions.add(dataRuleDefinition);
    }

    RulesConfigurationChangeRequest rulesConfigurationChangeRequest =
      new RulesConfigurationChangeRequest(newRuleDefinitions, rulesToRemove, pipelineAlertsToRemove,
        laneToDataRule, rulesWithSampledRecordSizeChanges);

    return rulesConfigurationChangeRequest;
  }

  private void detectMetricRuleChanges(List<MetricsRuleDefinition> oldMetricsRuleDefinitions,
                                              List<MetricsRuleDefinition> newMetricsRuleDefinitions,
                                              Set<String> alertsToRemove) {
    if(newMetricsRuleDefinitions != null && oldMetricsRuleDefinitions != null) {
      for(MetricsRuleDefinition oldMetricAlertDefinition : oldMetricsRuleDefinitions) {
        boolean found = false;
        for(MetricsRuleDefinition newMetricAlertDefinition : newMetricsRuleDefinitions) {
          if(oldMetricAlertDefinition.getId().equals(newMetricAlertDefinition.getId())) {
            found = true;
            if(oldMetricAlertDefinition.isEnabled() && !newMetricAlertDefinition.isEnabled()) {
              alertsToRemove.add(oldMetricAlertDefinition.getMetricId());
            }
            if(hasAlertChanged(oldMetricAlertDefinition, newMetricAlertDefinition)) {
              alertsToRemove.add(oldMetricAlertDefinition.getMetricId());
            }
          }
        }
        if(!found) {
          alertsToRemove.add(oldMetricAlertDefinition.getMetricId());
        }
      }
    }
  }

  private void detectDataRuleChanges(List<DataRuleDefinition> oldRuleDefinitions,
                                     List<DataRuleDefinition> newRuleDefinitions,
                                     Set<String> rulesToRemove, Map<String, Integer> rulesToResizeSamplingRecords) {
    if(newRuleDefinitions != null && oldRuleDefinitions != null) {
      for(DataRuleDefinition oldRuleDefinition : oldRuleDefinitions) {
        boolean found = false;
        for(DataRuleDefinition newRuleDefinition : newRuleDefinitions) {
          if(oldRuleDefinition.getId().equals(newRuleDefinition.getId())) {
            found = true;
            if(oldRuleDefinition.isEnabled() && !newRuleDefinition.isEnabled()) {
              rulesToRemove.add(oldRuleDefinition.getId());
            }
            if(hasRuleChanged(oldRuleDefinition, newRuleDefinition)) {
              if(oldRuleDefinition.isAlertEnabled() || oldRuleDefinition.isMeterEnabled()) {
                rulesToRemove.add(oldRuleDefinition.getId());
              }
            }
            if(hasSamplingSizeChanged(oldRuleDefinition, newRuleDefinition)) {
              rulesToResizeSamplingRecords.put(newRuleDefinition.getId(),
                newRuleDefinition.getSamplingRecordsToRetain());
            }
          }
        }
        if(!found) {
          rulesToRemove.add(oldRuleDefinition.getId());
        }
      }
    }
  }

  private boolean hasSamplingSizeChanged(DataRuleDefinition oldRuleDefinition, DataRuleDefinition newRuleDefinition) {
    boolean noChange = true;
    if(newRuleDefinition.isEnabled()) {
      if(oldRuleDefinition.getSamplingRecordsToRetain() != newRuleDefinition.getSamplingRecordsToRetain()) {
        noChange = false;
      }
    }
    return !noChange;
  }

  private boolean hasRuleChanged(DataRuleDefinition oldDataRuleDefinition,
                         DataRuleDefinition newDataRuleDefinition) {
    boolean noChange = true;
    if(newDataRuleDefinition.isEnabled()) {
      noChange &= areStringsSame(oldDataRuleDefinition.getLane(), newDataRuleDefinition.getLane());
      noChange &= areStringsSame(oldDataRuleDefinition.getCondition(), newDataRuleDefinition.getCondition());
      noChange &= areStringsSame(oldDataRuleDefinition.getThresholdValue(), newDataRuleDefinition.getThresholdValue());
      noChange &= areStringsSame(String.valueOf(oldDataRuleDefinition.getMinVolume()),
        String.valueOf(newDataRuleDefinition.getMinVolume()));
      noChange &= areStringsSame(String.valueOf(oldDataRuleDefinition.getSamplingPercentage()),
        String.valueOf(newDataRuleDefinition.getSamplingPercentage()));
      noChange &= areStringsSame(oldDataRuleDefinition.getThresholdType().name(),
        newDataRuleDefinition.getThresholdType().name());
      noChange &= (oldDataRuleDefinition.isEnabled() && newDataRuleDefinition.isEnabled());
    }
    return !noChange;
  }

  private boolean hasAlertChanged(MetricsRuleDefinition oldMetricsRuleDefinition,
                          MetricsRuleDefinition newMetricsRuleDefinition) {
    boolean noChange = true;
    if(newMetricsRuleDefinition.isEnabled()) {
      noChange &= areStringsSame(oldMetricsRuleDefinition.getMetricId(), newMetricsRuleDefinition.getMetricId());
      noChange &= areStringsSame(oldMetricsRuleDefinition.getCondition(), newMetricsRuleDefinition.getCondition());
      noChange &= areStringsSame(oldMetricsRuleDefinition.getMetricType().name(),
        newMetricsRuleDefinition.getMetricType().name());
      noChange &= areStringsSame(oldMetricsRuleDefinition.getMetricElement().name(),
        newMetricsRuleDefinition.getMetricElement().name());
      noChange &= (oldMetricsRuleDefinition.isEnabled() && newMetricsRuleDefinition.isEnabled());
    }
    return !noChange;
  }

  private boolean areStringsSame(String lhs, String rhs) {
    if(lhs == null && rhs != null) {
      return false;
    }
    if(lhs != null && rhs == null) {
      return false;
    }
    return lhs.equals(rhs);
  }

  void setPreviousRuleDefinitions(RuleDefinitions previousRuleDefinitions) {
    this.previousRuleDefinitions = previousRuleDefinitions;
  }
}
