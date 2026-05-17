package com.hospit.service;

import com.hospit.entity.IsolationForestModel;
import com.hospit.entity.IsolationForestRule;
import com.hospit.entity.LabResult;

import java.util.List;

public interface IIsolationForestService {

    IsolationResult jointDetect(List<LabResult> results, IsolationForestRule rule);

    void buildModel(IsolationForestRule rule);

    void trainModel(Long ruleId);

    IsolationForestRule getRuleById(Long ruleId);

    List<IsolationForestRule> getAllEnabledRules();

    void rebuildAllModels();

    class IsolationResult {
        private double isolationScore;
        private double zscoreScore;
        private double combinedScore;
        private double threshold;
        private String anomalyLevel;
        private java.util.Map<Integer, Double> zscoreAnomalies = new java.util.HashMap<>();
        private java.util.List<String> alertReasons = new java.util.ArrayList<>();
        private String alertMessage;

        public double getIsolationScore() { return isolationScore; }
        public void setIsolationScore(double isolationScore) { this.isolationScore = isolationScore; }
        public double getZscoreScore() { return zscoreScore; }
        public void setZscoreScore(double zscoreScore) { this.zscoreScore = zscoreScore; }
        public double getCombinedScore() { return combinedScore; }
        public void setCombinedScore(double combinedScore) { this.combinedScore = combinedScore; }
        public double getThreshold() { return threshold; }
        public void setThreshold(double threshold) { this.threshold = threshold; }
        public String getAnomalyLevel() { return anomalyLevel; }
        public void setAnomalyLevel(String anomalyLevel) { this.anomalyLevel = anomalyLevel; }
        public java.util.Map<Integer, Double> getZscoreAnomalies() { return zscoreAnomalies; }
        public void setZscoreAnomalies(java.util.Map<Integer, Double> zscoreAnomalies) { this.zscoreAnomalies = zscoreAnomalies; }
        public java.util.List<String> getAlertReasons() { return alertReasons; }
        public void setAlertReasons(java.util.List<String> alertReasons) { this.alertReasons = alertReasons; }
        public String getAlertMessage() { return alertMessage; }
        public void setAlertMessage(String alertMessage) { this.alertMessage = alertMessage; }
    }
}
