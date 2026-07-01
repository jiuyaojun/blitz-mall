package com.blitz.seckill.infrastructure.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@org.springframework.context.annotation.Profile("!minimal")
public class SentinelConfig {

    @PostConstruct
    public void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();

        FlowRule seckillRule = new FlowRule();
        seckillRule.setResource("seckill_execute");
        seckillRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        seckillRule.setCount(5000);
        rules.add(seckillRule);

        FlowRule pathRule = new FlowRule();
        pathRule.setResource("seckill_path");
        pathRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        pathRule.setCount(10000);
        rules.add(pathRule);

        FlowRuleManager.loadRules(rules);
    }
}
