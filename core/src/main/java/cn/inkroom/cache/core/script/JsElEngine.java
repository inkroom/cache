package cn.inkroom.cache.core.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xidea.el.Expression;
import org.xidea.el.impl.ExpressionImpl;

import java.util.Map;

public class JsElEngine implements ScriptEngine {

    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String express(String express, Map<String, Object> args) {
        Expression expression = new ExpressionImpl(express);
        Object evaluate = expression.evaluate(args);
        if (evaluate == null)
            return null;
        return evaluate.toString();
    }

    @Override
    public boolean booleanExpress(String express, Map<String, Object> args) {
        Expression expression = new ExpressionImpl(express);
        Object evaluate = expression.evaluate(args);
        if (evaluate == null)
            return false;
        return Boolean.parseBoolean(evaluate.toString());
    }
}
