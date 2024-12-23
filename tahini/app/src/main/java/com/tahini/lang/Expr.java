package com.tahini.lang;

import java.util.List;

abstract class Expr {
  interface Visitor<R> {
    R visitAssignExpr(Assign expr);
    R visitBinaryExpr(Binary expr);
    R visitCallExpr(Call expr);
    R visitListAccessExpr(ListAccess expr);
    R visitListSliceExpr(ListSlice expr);
    R visitGroupingExpr(Grouping expr);
    R visitLiteralExpr(Literal expr);
    R visitUnaryExpr(Unary expr);
    R visitTernaryExpr(Ternary expr);
    R visitVariableExpr(Variable expr);
    R visitNamespacedVariableExpr(NamespacedVariable expr);
    R visitLogicalExpr(Logical expr);
    R visitTahiniListExpr(TahiniList expr);
    R visitTahiniMapExpr(TahiniMap expr);
  }
  static class Assign extends Expr {
    Assign(Token name, Expr value) {
      this.name = name;
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitAssignExpr(this);
    }

    final Token name;
    final Expr value;
  }
  static class Binary extends Expr {
    Binary(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitBinaryExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class Call extends Expr {
    Call(Expr callee, Token paren, List<Expr> arguments) {
      this.callee = callee;
      this.paren = paren;
      this.arguments = arguments;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitCallExpr(this);
    }

    final Expr callee;
    final Token paren;
    final List<Expr> arguments;
  }
  static class ListAccess extends Expr {
    ListAccess(Expr list, Token paren, Expr index) {
      this.list = list;
      this.paren = paren;
      this.index = index;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitListAccessExpr(this);
    }

    final Expr list;
    final Token paren;
    final Expr index;
  }
  static class ListSlice extends Expr {
    ListSlice(Expr list, Token paren, Expr start, Expr end) {
      this.list = list;
      this.paren = paren;
      this.start = start;
      this.end = end;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitListSliceExpr(this);
    }

    final Expr list;
    final Token paren;
    final Expr start;
    final Expr end;
  }
  static class Grouping extends Expr {
    Grouping(Expr expression) {
      this.expression = expression;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitGroupingExpr(this);
    }

    final Expr expression;
  }
  static class Literal extends Expr {
    Literal(Object value) {
      this.value = value;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLiteralExpr(this);
    }

    final Object value;
  }
  static class Unary extends Expr {
    Unary(Token operator, Expr right) {
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitUnaryExpr(this);
    }

    final Token operator;
    final Expr right;
  }
  static class Ternary extends Expr {
    Ternary(Expr condition, Expr left, Expr right) {
      this.condition = condition;
      this.left = left;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTernaryExpr(this);
    }

    final Expr condition;
    final Expr left;
    final Expr right;
  }
  static class Variable extends Expr {
    Variable(Token name) {
      this.name = name;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitVariableExpr(this);
    }

    final Token name;
  }
  static class NamespacedVariable extends Expr {
    NamespacedVariable(List<Token> nameParts) {
      this.nameParts = nameParts;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitNamespacedVariableExpr(this);
    }

    final List<Token> nameParts;
  }
  static class Logical extends Expr {
    Logical(Expr left, Token operator, Expr right) {
      this.left = left;
      this.operator = operator;
      this.right = right;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitLogicalExpr(this);
    }

    final Expr left;
    final Token operator;
    final Expr right;
  }
  static class TahiniList extends Expr {
    TahiniList(List<Expr> elements) {
      this.elements = elements;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTahiniListExpr(this);
    }

    final List<Expr> elements;
  }
  static class TahiniMap extends Expr {
    TahiniMap(List<Expr> keys, List<Expr> values) {
      this.keys = keys;
      this.values = values;
    }

    @Override
    <R> R accept(Visitor<R> visitor) {
      return visitor.visitTahiniMapExpr(this);
    }

    final List<Expr> keys;
    final List<Expr> values;
  }

  abstract <R> R accept(Visitor<R> visitor);
}
