package cn.inkroom.cache.core.script;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

/**
 * @author 墨盒
 * @date 2019/10/27
 */
public class SpElEngine implements ScriptEngine {

    private Map<String, Expression> spElMap = new HashMap<>();
    private SpelExpressionParser spelExpressionParser = new SpelExpressionParser();

    @Override
    public String express(String express, Map<String, Object> args) {

        Expression expression = getExpress(express);
        EvaluationContext context = getContext(express, args);
        Object value = expression.getValue(context);
        if (value == null) return null;
        return value.toString();

    }

    private Expression getExpress(String express) {
        Expression expression;
        if (spElMap.containsKey(express)) {
            expression = spElMap.get(express);
        } else {
            expression = spelExpressionParser.parseExpression(express);
            spElMap.put(express, expression);
        }
        return expression;
    }

    public EvaluationContext getContext(String express, Map<String, Object> args) {
        EvaluationContext context = new StandardEvaluationContext(args);
        //必须这样才能通过 # 获取map中的数据
        args.forEach(new BiConsumer<String, Object>() {
            @Override
            public void accept(String s, Object o) {
                context.setVariable(s, o);
            }
        });
        return context;
    }

    @Override
    public boolean booleanExpress(String express, Map<String, Object> args) {
        Expression expression = getExpress(express);
        EvaluationContext context = getContext(express, args);

        return expression.getValue(context, Boolean.class);


    }
}
