/**
 * (c) 2014 StreamSets, Inc. All rights reserved. May not
 * be copied, modified, or distributed in whole or part without
 * written consent of StreamSets, Inc.
 */
package com.streamsets.pipeline.restapi.bean;

import com.streamsets.pipeline.config.MetricElement;
import org.junit.Assert;
import org.junit.Test;

public class TestMetricElementBean {

  @Test
  public void testMetricElementBeanHistogram() {

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_COUNT) ==
      MetricElementJson.HISTOGRAM_COUNT);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_COUNT)
        == MetricElement.HISTOGRAM_COUNT);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_MAX) ==
      MetricElementJson.HISTOGRAM_MAX);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_MAX)
      == MetricElement.HISTOGRAM_MAX);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_MIN) ==
      MetricElementJson.HISTOGRAM_MIN);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_MIN)
      == MetricElement.HISTOGRAM_MIN);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_MEAN) ==
      MetricElementJson.HISTOGRAM_MEAN);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_MEAN)
      == MetricElement.HISTOGRAM_MEAN);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_MEDIAN) ==
      MetricElementJson.HISTOGRAM_MEDIAN);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_MEDIAN)
      == MetricElement.HISTOGRAM_MEDIAN);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_P75) ==
      MetricElementJson.HISTOGRAM_P75);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_P75)
      == MetricElement.HISTOGRAM_P75);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_P95) ==
      MetricElementJson.HISTOGRAM_P95);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_P95)
      == MetricElement.HISTOGRAM_P95);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_P98) ==
      MetricElementJson.HISTOGRAM_P98);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_P98)
      == MetricElement.HISTOGRAM_P98);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_P99) ==
      MetricElementJson.HISTOGRAM_P99);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_P99)
      == MetricElement.HISTOGRAM_P99);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_P999) ==
      MetricElementJson.HISTOGRAM_P999);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_P999)
      == MetricElement.HISTOGRAM_P999);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.HISTOGRAM_STD_DEV) ==
      MetricElementJson.HISTOGRAM_STD_DEV);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.HISTOGRAM_STD_DEV)
      == MetricElement.HISTOGRAM_STD_DEV);
  }

  @Test
  public void testMetricElementBeanTimer() {

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_M15_RATE) ==
      MetricElementJson.TIMER_M15_RATE);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_M15_RATE)
      == MetricElement.TIMER_M15_RATE);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_COUNT) ==
      MetricElementJson.TIMER_COUNT);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_COUNT)
      == MetricElement.TIMER_COUNT);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_M1_RATE) ==
      MetricElementJson.TIMER_M1_RATE);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_M1_RATE)
      == MetricElement.TIMER_M1_RATE);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_M5_RATE) ==
      MetricElementJson.TIMER_M5_RATE);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_M5_RATE)
      == MetricElement.TIMER_M5_RATE);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_MAX) ==
      MetricElementJson.TIMER_MAX);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_MAX)
      == MetricElement.TIMER_MAX);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_MEAN) ==
      MetricElementJson.TIMER_MEAN);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_MEAN)
      == MetricElement.TIMER_MEAN);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_MEAN_RATE) ==
      MetricElementJson.TIMER_MEAN_RATE);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_MEAN_RATE)
      == MetricElement.TIMER_MEAN_RATE);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_MIN) ==
      MetricElementJson.TIMER_MIN);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_MIN)
      == MetricElement.TIMER_MIN);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_P50) ==
      MetricElementJson.TIMER_P50);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_P50)
      == MetricElement.TIMER_P50);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_P75) ==
      MetricElementJson.TIMER_P75);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_P75)
      == MetricElement.TIMER_P75);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_P95) ==
      MetricElementJson.TIMER_P95);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_P95)
      == MetricElement.TIMER_P95);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_P98) ==
      MetricElementJson.TIMER_P98);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_P98)
      == MetricElement.TIMER_P98);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_P99) ==
      MetricElementJson.TIMER_P99);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_P99)
      == MetricElement.TIMER_P99);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_P999) ==
      MetricElementJson.TIMER_P999);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_P999)
      == MetricElement.TIMER_P999);

    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.TIMER_STD_DEV) ==
      MetricElementJson.TIMER_STD_DEV);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.TIMER_STD_DEV)
      == MetricElement.TIMER_STD_DEV);
  }

  @Test
  public void testMetricElementBeanCounter() {
    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.COUNTER_COUNT) ==
      MetricElementJson.COUNTER_COUNT);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.COUNTER_COUNT)
      == MetricElement.COUNTER_COUNT);
  }

  @Test
  public void testMetricElementBeanMeter() {
    Assert.assertTrue(BeanHelper.wrapMetricElement(MetricElement.METER_COUNT) ==
      MetricElementJson.METER_COUNT);
    Assert.assertTrue(BeanHelper.unwrapMetricElement(MetricElementJson.METER_COUNT)
      == MetricElement.METER_COUNT);

    //TODO<Hari>: add comparisons for other meter elements
  }
}
