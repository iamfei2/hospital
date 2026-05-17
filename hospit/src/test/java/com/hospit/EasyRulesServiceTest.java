package com.hospit;

import com.hospit.entity.*;
import com.hospit.rules.LabResultFacts;
import com.hospit.service.EasyRulesService;
import com.hospit.service.IStatisticsComputeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

// 预警规则评估测试 - 覆盖所有条件类型
class EasyRulesServiceTest {

    private EasyRulesService easyRulesService;

    @BeforeEach
    void setUp() throws Exception {
        easyRulesService = new EasyRulesService();
        // 通过反射注入 mock
        Field f = EasyRulesService.class.getDeclaredField("statisticsComputeService");
        f.setAccessible(true);
        f.set(easyRulesService, mock(IStatisticsComputeService.class));
    }

    @Test
    void testEvaluateAbove() {
        WarningRule rule = createRule("ABOVE", new BigDecimal("10.0"), null);
        LabResult result = createResult(BigDecimal.valueOf(12));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), new HashMap<>(), new HashMap<>());

        assertEquals(1, records.size());
    }

    @Test
    void testEvaluateAbove_Normal() {
        WarningRule rule = createRule("ABOVE", new BigDecimal("10.0"), null);
        LabResult result = createResult(BigDecimal.valueOf(8));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), new HashMap<>(), new HashMap<>());

        assertEquals(0, records.size());
    }

    @Test
    void testEvaluateBelow() {
        WarningRule rule = createRule("BELOW", null, new BigDecimal("3.0"));
        LabResult result = createResult(BigDecimal.valueOf(2));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), new HashMap<>(), new HashMap<>());

        assertEquals(1, records.size());
    }

    @Test
    void testEvaluateRange() {
        WarningRule rule = createRule("RANGE", new BigDecimal("6.0"), new BigDecimal("4.0"));
        LabResult result = createResult(BigDecimal.valueOf(7));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), new HashMap<>(), new HashMap<>());

        assertEquals(1, records.size());
    }

    @Test
    void testEvaluateRange_Normal() {
        WarningRule rule = createRule("RANGE", new BigDecimal("6.0"), new BigDecimal("4.0"));
        LabResult result = createResult(BigDecimal.valueOf(5));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), new HashMap<>(), new HashMap<>());

        assertEquals(0, records.size());
    }

    @Test
    void testDisabledRule() {
        WarningRule rule = createRule("ABOVE", new BigDecimal("10.0"), null);
        rule.setEnabled(false);
        LabResult result = createResult(BigDecimal.valueOf(12));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), new HashMap<>(), new HashMap<>());

        assertEquals(0, records.size());
    }

    @Test
    void testNoRuleEnabled() {
        LabResult result = createResult(BigDecimal.valueOf(12));
        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                new ArrayList<>(), new HashMap<>(), new HashMap<>());

        assertEquals(0, records.size());
    }

    @Test
    void testEvaluateTrendUp() {
        WarningRule rule = createRule("TREND_UP", new BigDecimal("30"), null);
        LabResult result = createResult(BigDecimal.valueOf(60));
        Map<String, BigDecimal> recentValues = Map.of("1", BigDecimal.valueOf(60));
        Map<String, BigDecimal> previousValues = Map.of("1", BigDecimal.valueOf(40));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), recentValues, previousValues);

        assertEquals(1, records.size());
    }

    @Test
    void testEvaluateTrendUp_Normal() {
        WarningRule rule = createRule("TREND_UP", new BigDecimal("30"), null);
        LabResult result = createResult(BigDecimal.valueOf(48));
        Map<String, BigDecimal> recentValues = Map.of("1", BigDecimal.valueOf(48));
        Map<String, BigDecimal> previousValues = Map.of("1", BigDecimal.valueOf(40));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), recentValues, previousValues);

        assertEquals(0, records.size());
    }

    @Test
    void testEvaluateTrendDown() {
        WarningRule rule = createRule("TREND_DOWN", new BigDecimal("30"), null);
        LabResult result = createResult(BigDecimal.valueOf(20));
        Map<String, BigDecimal> recentValues = Map.of("1", BigDecimal.valueOf(20));
        Map<String, BigDecimal> previousValues = Map.of("1", BigDecimal.valueOf(40));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), recentValues, previousValues);

        assertEquals(1, records.size());
    }

    @Test
    void testEvaluateContinueUp() {
        WarningRule rule = createRule("CONTINUE_UP", new BigDecimal("20"), null);
        LabResult result = createResult(BigDecimal.valueOf(60));
        Map<String, BigDecimal> recentValues = Map.of("1", BigDecimal.valueOf(60));
        Map<String, BigDecimal> previousValues = Map.of("1", BigDecimal.valueOf(40));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), recentValues, previousValues);

        assertEquals(1, records.size());
    }

    @Test
    void testEvaluateContinueDown() {
        WarningRule rule = createRule("CONTINUE_DOWN", new BigDecimal("20"), null);
        LabResult result = createResult(BigDecimal.valueOf(30));
        Map<String, BigDecimal> recentValues = Map.of("1", BigDecimal.valueOf(30));
        Map<String, BigDecimal> previousValues = Map.of("1", BigDecimal.valueOf(40));

        List<WarningRecord> records = easyRulesService.evaluateLabResult(result,
                List.of(rule), recentValues, previousValues);

        assertEquals(1, records.size());
    }

    // 创建规则
    private WarningRule createRule(String conditionType, BigDecimal high, BigDecimal low) {
        WarningRule rule = new WarningRule();
        rule.setRuleId(1L);
        rule.setRuleType("LAB");
        rule.setEnabled(true);
        rule.setConditionType(conditionType);
        rule.setThresholdHigh(high);
        rule.setThresholdLow(low);
        rule.setItemId(1);
        rule.setSeverity("WARNING");
        rule.setRuleName("Test Rule");
        return rule;
    }

    // 创建检验结果
    private LabResult createResult(BigDecimal value) {
        LabResult result = new LabResult();
        result.setResultId(1L);
        result.setPatientId("P001");
        result.setItemId(1);
        result.setResultValue(value);
        result.setResultUnit("U/L");
        return result;
    }

    // 创建检验事实
    private LabResultFacts createFacts(LabResult result) {
        LabResultFacts facts = new LabResultFacts();
        facts.setPatientId(result.getPatientId());
        facts.setItemId(result.getItemId());
        facts.setResultValue(result.getResultValue());
        facts.setResultUnit(result.getResultUnit());
        facts.setResultId(result.getResultId());
        return facts;
    }
}
