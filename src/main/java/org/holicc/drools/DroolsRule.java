package org.holicc.drools;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class DroolsRule {

    public static final String HEADER = "package com.trs.ai.rules;\n" +
            "dialect \"java\";\n" +
            "import org.holicc.drools.FactsProxy;\n" +
            "global org.holicc.drools.RuleResultsContainer rule;\n";
    private static final String TEMPLATE = "rule \"%s\" \nsalience %d\nwhen \n %s\n then \n %s\n end";

    private String name;

    private String condition;

    private String actions;

    private int salience;

    private String groupName;

    private Attribute[] attributes;

    public static final String PREFIX = "get('";
    public static final String SUFFIX = "')";
    public static final Pattern pattern = Pattern.compile("^'.*?'$");
    //TODO expose a api
    public static final Pattern KEYWORD_MATCH = Pattern.compile("ckm|in|[0-9]|[Aa]nd|[Oo]r|contains|not|true|false|matches|null|return");
    public static final Pattern OPERATOR_MATCH = Pattern.compile(" |,|\\|\\||&&|\\||&|==|!=|>=|<=|>|<|\\(|\\)|'.*?'");

    @Data
    @AllArgsConstructor
    public static class Attribute {
        private String name;
        private String value;
    }

    public String compile(boolean covertVariable) {
        //处理模板变量
        if (Objects.nonNull(attributes) && attributes.length > 0) {
            for (Attribute attribute : attributes) {
                if (actions.contains(attribute.name)) {
                    actions = actions.replace(attribute.name, attribute.value);
                }
                if (condition.contains(attribute.name)) {
                    condition = condition.replace(attribute.name, attribute.value);
                }
            }
        }
        if (covertVariable) {
            condition = convertVariables(condition);
        }
        //
        return String.format(TEMPLATE,
                name,
                salience,
                StringUtils.isNotBlank(condition) ? condition : "1==1",
                actions);
    }


    private String convertVariables(String dlr) {
        //tokenize
        List<String> variables = tokenize(dlr).stream()
                .filter(DroolsRule::shouldIgnore)
                .collect(Collectors.toList());
        //
        StringBuilder builder = new StringBuilder(dlr);
        int lastIndex = -1;
        for (String variable : variables) {
            int i = builder.indexOf(variable, lastIndex);
            builder.insert(i, PREFIX)
                    .insert(i + variable.length() + PREFIX.length(), SUFFIX);
            lastIndex = builder.indexOf(variable, i) + 1;
        }
        //
        return builder.toString();
    }

    public static boolean shouldIgnore(String s) {
        return !s.isBlank()
                && !s.matches(OPERATOR_MATCH.toString())
                && !s.matches(KEYWORD_MATCH.toString());
    }

    public static List<String> tokenize(String dlr) {
        return Stream.of(dlr.split(OPERATOR_MATCH.toString())).collect(Collectors.toList());
    }
}
