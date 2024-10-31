package com.tahini.lang;

import java.util.List;

class ASTVisualizer implements Expr.Visitor<Object>, Stmt.Visitor<Void> {

    private int indentLevel = 0;

    public void display(List<Stmt> statements) {
        for (Stmt statement : statements) {
            visitStatement(statement);
        }
    }

    private void visitStatement(Stmt stmt) {
        stmt.accept(this);
    }

    @Override
    public Void visitExpressionStmt(Stmt.Expression stmt) {
        formatNode("ExpressionStmt", "expression", stmt.expression);
        return null;
    }

    @Override
    public Void visitPrintStmt(Stmt.Print stmt) {
        formatNode("PrintStmt", "expression", stmt.expression);
        return null;
    }

    @Override
    public Void visitVarStmt(Stmt.Var stmt) {
        formatNode("VarStmt", "name", stmt.name.lexeme, "initializer", stmt.initializer);
        return null;
    }

    @Override
    public Void visitFunctionStmt(Stmt.Function stmt) {
        formatNode("FunctionStmt", "name", stmt.name.lexeme, "params", stmt.params, "body", stmt.body, "preconditions", stmt.preconditions, "postconditions", stmt.postconditions);
        return null;
    }

    @Override
    public Void visitTestStmt(Stmt.Test stmt) {
        formatNode("TestStmt", "name", stmt.name.lexeme, "body", stmt.body);
        return null;
    }

    @Override
    public Void visitIfStmt(Stmt.If stmt) {
        formatNode("IfStmt", "condition", stmt.condition, "thenBranch", stmt.thenBranch, "elseBranch", stmt.elseBranch);
        return null;
    }

    @Override
    public Void visitImportStmt(Stmt.Import stmt) {
        if (stmt.name == null) {
            formatNode("ImportStmt", "path", stmt.path.lexeme);
            return null;
        }
        formatNode("ImportStmt", "path", stmt.path.lexeme, "name", stmt.name.lexeme);
        return null;
    }

    @Override
    public Void visitContractStmt(Stmt.Contract stmt) {
        formatNode("ContractStmt", "type", stmt.type.lexeme, "conditions", stmt.conditions, "msg", stmt.msg);
        return null;
    }

    @Override
    public Void visitReturnStmt(Stmt.Return stmt) {
        formatNode("ReturnStmt", "keyword", stmt.keyword.lexeme, "value", stmt.value);
        return null;
    }

    @Override
    public Void visitBreakStmt(Stmt.Break stmt) {
        formatNode("BreakStmt");
        return null;
    }

    @Override
    public Void visitWhileStmt(Stmt.While stmt) {
        formatNode("WhileStmt", "condition", stmt.condition, "body", stmt.body);
        return null;
    }

    @Override
    public Void visitBlockStmt(Stmt.Block stmt) {
        formatNode("BlockStmt", "statements", stmt.statements);
        return null;
    }

    @Override
    public Object visitAssignExpr(Expr.Assign expr) {
        formatNode("AssignExpr", "name", expr.name.lexeme, "value", expr.value);
        return null;
    }

    @Override
    public Object visitBinaryExpr(Expr.Binary expr) {
        formatNode("BinaryExpr", "left", expr.left, "operator", expr.operator.lexeme, "right", expr.right);
        return null;
    }

    @Override
    public Object visitLiteralExpr(Expr.Literal expr) {
        formatNode("LiteralExpr", "value", expr.value);
        return null;
    }

    @Override
    public Object visitVariableExpr(Expr.Variable expr) {
        formatNode("VariableExpr", "name", expr.name.lexeme);
        return null;
    }

    @Override
    public Object visitCallExpr(Expr.Call expr) {
        formatNode("CallExpr", "callee", expr.callee, "paren", expr.paren.lexeme, "arguments", expr.arguments);
        return null;
    }

    @Override
    public Object visitNamespacedVariableExpr(Expr.NamespacedVariable expr) {
        formatNode("NamespacedVariableExpr", "nameParts", expr.nameParts);
        return null;
    }

    @Override
    public Object visitLogicalExpr(Expr.Logical expr) {
        formatNode("LogicalExpr", "left", expr.left, "operator", expr.operator.lexeme, "right", expr.right);
        return null;
    }

    @Override
    public Object visitTahiniMapExpr(Expr.TahiniMap expr) {
        formatNode("TahiniMapExpr", "keys", expr.keys, "values", expr.values);
        return null;
    }

    @Override
    public Object visitTahiniListExpr(Expr.TahiniList expr) {
        formatNode("TahiniListExpr", "elements", expr.elements);
        return null;
    }

    @Override
    public Object visitTernaryExpr(Expr.Ternary expr) {
        formatNode("TernaryExpr", "condition", expr.condition, "left", expr.left, "right", expr.right);
        return null;
    }

    @Override
    public Object visitUnaryExpr(Expr.Unary expr) {
        formatNode("UnaryExpr", "operator", expr.operator.lexeme, "right", expr.right);
        return null;
    }

    @Override
    public Object visitListAccessExpr(Expr.ListAccess expr) {
        formatNode("ListAccessExpr", "list", expr.list, "index", expr.index);
        return null;
    }

    @Override
    public Object visitListSliceExpr(Expr.ListSlice expr) {
        formatNode("ListSliceExpr", "list", expr.list, "start", expr.start, "end", expr.end);
        return null;
    }

    @Override
    public Object visitGroupingExpr(Expr.Grouping expr) {
        formatNode("GroupingExpr", "expression", expr.expression);
        return null;
    }

    private void formatNode(String nodeName, Object... children) {
        String indentation = "  ".repeat(indentLevel);
        System.out.println(indentation + nodeName + ":");
        indentLevel++;

        for (int i = 0; i < children.length; i += 2) {
            String label = (String) children[i];
            Object child = children[i + 1];

            System.out.print(indentation + "  - " + label + ": ");
            if (child == null) {
                System.out.println("null");
            } else {
                switch (child) {
                    case Expr childExpr -> {
                        System.out.println();
                        childExpr.accept(this);
                    }
                    case Stmt childStmt -> {
                        System.out.println();
                        childStmt.accept(this);
                    }
                    case List<?> list -> {
                        System.out.println();
                        for (Object item : list) {
                            switch (item) {
                                case Expr itemExpr ->
                                    itemExpr.accept(this);
                                case Stmt itemStmt ->
                                    itemStmt.accept(this);
                                default ->
                                    System.out.println(indentation + "    " + item);
                            }
                        }
                    }
                    default -> {
                        System.out.println(child);
                    }
                }
            }
        }

        indentLevel--;
    }
}
