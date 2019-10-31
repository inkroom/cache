package cn.inkroom.cache.core.script;

import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class AviatorEngine implements ScriptEngine {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private Map<String, Expression> expressionMap = new HashMap<>();


    @Override
    public String express(String express, Map<String, Object> args) {
        Expression expression = getExpression(express);

        Object execute = expression.execute(args);
        if (execute == null) return null;

        return execute.toString();
    }

    private Expression getExpression(String express) {
        Expression expression;
        if (expressionMap.containsKey(express)) expression = expressionMap.get(express);
        else {
            expression = AviatorEvaluator.compile(express);
            expressionMap.put(express, expression);
        }
        return expression;
    }

    @Override
    public boolean booleanExpress(String express, Map<String, Object> args) {
        Expression expression = getExpression(express);
        Object execute = expression.execute(args);
        if (execute == null) return false;
        return Boolean.parseBoolean(execute.toString());
    }
}
