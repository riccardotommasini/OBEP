/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package sr.obep.data.content;

import com.espertech.esper.client.soda.*;

import java.io.StringWriter;

/**
 * Coalesce-function which returns the first non-null value in a list of values.
 */
public class MergeContentExpression extends ExpressionBase {
    private static final long serialVersionUID = 2591445292377310176L;

    /**
     * Ctor - for use to create an expression tree, without child expression.
     * <p>
     * Use add methods to add child expressions to acts upon.
     */
    public MergeContentExpression() {
    }

    /**
     * Ctor.
     *
     * @param moreProperties optional more properties in the expression
     */
    public MergeContentExpression(String... moreProperties) {
        for (int i = 0; i < moreProperties.length; i++) {
            addChild(new PropertyValueExpression(moreProperties[i]));
        }
    }

    /**
     * Ctor.
     *
     * @param exprOne         provides the first value in the expression
     * @param exprTwo         provides the second value in the expression
     * @param moreExpressions optional more expressions that are part of the function
     */
    public MergeContentExpression(Expression exprOne, Expression exprTwo, Expression... moreExpressions) {
        addChild(exprOne);
        addChild(exprTwo);
        for (int i = 0; i < moreExpressions.length; i++) {
            addChild(moreExpressions[i]);
        }
    }

    /**
     * Add a constant to include in the computation.
     *
     * @param object constant to add
     * @return expression
     */
    public MergeContentExpression add(Object object) {
        this.getChildren().add(new ConstantExpression(object));
        return this;
    }

    /**
     * Add an expression to include in the computation.
     *
     * @param expression to add
     * @return expression
     */
    public MergeContentExpression add(Expression expression) {
        this.getChildren().add(expression);
        return this;
    }

    /**
     * Add a property to include in the computation.
     *
     * @param propertyName is the name of the property
     * @return expression
     */
    public MergeContentExpression add(String propertyName) {
        this.getChildren().add(new PropertyValueExpression(propertyName));
        return this;
    }

    public ExpressionPrecedenceEnum getPrecedence() {
        return ExpressionPrecedenceEnum.UNARY;
    }

    public void toPrecedenceFreeEPL(StringWriter writer) {
        toPrecedenceFreeEPL("merge2", this.getChildren(), writer);
    }
}
