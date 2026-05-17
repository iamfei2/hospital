package com.hospit.vo;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public class RuleExpression implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String logic;
    private List<RuleCondition> conditions;
    private List<RuleExpression> expressions;
    
    public String getLogic() { return logic; }
    public void setLogic(String logic) { this.logic = logic; }
    public List<RuleCondition> getConditions() { return conditions; }
    public void setConditions(List<RuleCondition> conditions) { this.conditions = conditions; }
    public List<RuleExpression> getExpressions() { return expressions; }
    public void setExpressions(List<RuleExpression> expressions) { this.expressions = expressions; }

    public static class RuleCondition implements Serializable {
        private static final long serialVersionUID = 1L;
        
        private Integer itemId;
        private String operator;
        private BigDecimal value;
        private String conditionType;
        
        public Integer getItemId() { return itemId; }
        public void setItemId(Integer itemId) { this.itemId = itemId; }
        public String getOperator() { return operator; }
        public void setOperator(String operator) { this.operator = operator; }
        public BigDecimal getValue() { return value; }
        public void setValue(BigDecimal value) { this.value = value; }
        public String getConditionType() { return conditionType; }
        public void setConditionType(String conditionType) { this.conditionType = conditionType; }
    }
}
