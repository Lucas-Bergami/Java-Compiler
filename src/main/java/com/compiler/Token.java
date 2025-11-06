package com.compiler;

import java.util.regex.Pattern;

public class Token {
  public enum Type {
    // Palavras-chave e símbolos fixos
    FUNCTION("fn"),
    MAIN("main"),
    LET("let"),
    INT("int"),
    FLOAT("float"),
    CHAR("char"),
    IF("if"),
    ELSE("else"),
    WHILE("while"),
    PRINTLN("println"),
    RETURN("return"),
    LBRACKET("\\("),
    RBRACKET("\\)"),
    LBRACE("\\{"),
    RBRACE("\\}"),
    ARROW("->"),
    COLON(":"),
    SEMICOLON(";"),
    COMMA(","),
    ASSIGN("="),
    EQ("=="),
    NE("!="),
    GT(">"),
    GE(">="),
    LT("<"),
    LE("<="),
    PLUS("\\+"),
    MINUS("-"),
    MULT("\\*"),
    DIV("/"),

    // Tipos dinâmicos com regex
    ID("[a-zA-Z][a-zA-Z0-9_]*"),
    INT_CONST("[0-9]+"),
    FLOAT_CONST("[0-9]+\\.[0-9]+"),
    CHAR_LITERAL("'[^']'"),
    FMT_STRING("\"[^\"]*\"");

    private final Pattern pattern;

    Type(String regex) {
      if (regex != null) {
        this.pattern = Pattern.compile(regex);
      } else {
        this.pattern = null;
      }
    }

    public Pattern getPattern() {
      return pattern;
    }

    public boolean matches(String lexeme) {
      return pattern != null && pattern.matcher(lexeme).matches();
    }

    public static Type fromLexeme(String lexeme) {
      for (Type t : Type.values()) {
        if (t.pattern != null) {
          boolean matches = t.pattern.matcher(lexeme).matches();
          if (matches) {
            return t;
          }
        }
      }
      return null;
    }

  }

  private Type type;
  private String lexeme;
  private int lineNumber;

  public Token(Type type, String lexeme, int lineNumber) {
    this.type = type;
    this.lexeme = lexeme;
    this.lineNumber = lineNumber;
  }

  public Type getType() {
    return type;
  }

  public String getLexeme() {
    return lexeme;
  }

  public int getLineNumber() {
    return lineNumber;
  }

  @Override
  public String toString() {
    return "Token{" + type + ", \"" + lexeme + "\", line=" + lineNumber + "}";
  }
}
