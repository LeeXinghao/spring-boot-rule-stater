package org.holicc.drools;


import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class DroolsRule {

    private static final String TEMPLATE =
            "package com.trs.ai.rules;\n" +
                    "dialect \"java\";\n" +
                    "import com.trs.ai.ty.drools.FactsProxy;\n" +
                    "global com.trs.ai.ty.drools.RuleResultsContainer rule;\n" +
                    "rule \"%s\" \nsalience %d\nwhen \n %s\n then \n %s\n end";

    private String name;

    private String condition;

    private String actions;

    private int salience;

    private String groupName;

    private Attribute[] attributes;

    public static final String PREFIX = "get('";
    public static final String SUFFIX = "')";
    public static final Pattern pattern = Pattern.compile("^'.*?'$");
    public static final Pattern KEYWORD_MATCH = Pattern.compile("in|[Aa]nd|[Oo]r|contains|not|matches|return");
    public static final Pattern OPERATOR_MATCH = Pattern.compile(" |,|==|!=|>=|<=|>|<|\\(|\\)|'.*?'");

    @Data
    @AllArgsConstructor
    public static class Attribute {
        private String name;
        private String value;
    }

    public String compile() {
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
        //
        return String.format(TEMPLATE,
                name,
                salience,
                StringUtils.isNotBlank(condition) ? convertVariables(condition) : StringUtils.EMPTY,
                actions);
    }

    private String convertVariables(String dlr) {
        //tokenize
        Set<String> variables = tokenize(dlr).stream()
                .filter(this::shouldIgnore)
                .collect(Collectors.toSet());
        //
        StringBuilder builder = new StringBuilder(dlr);
        for (String variable : variables) {
            int i = builder.indexOf(variable);
            builder.insert(i, PREFIX)
                    .insert(i + variable.length() + PREFIX.length(), SUFFIX);
        }
        //
        return builder.toString();
    }

    private boolean shouldIgnore(String s) {
        return !s.isBlank()
                && !s.matches(OPERATOR_MATCH.toString())
                && !s.matches(KEYWORD_MATCH.toString());
    }

    public static Set<String> tokenize(String dlr) {
        return Stream.of(dlr.split(OPERATOR_MATCH.toString())).collect(Collectors.toSet());
    }

}
