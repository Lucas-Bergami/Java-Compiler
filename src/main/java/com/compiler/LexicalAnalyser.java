package com.compiler;

import java.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class LexicalAnalyser {
  private Vector<Token> tokens = new Vector<Token>();
  private Vector<Token> errors = new Vector<Token>();
  private boolean usedLast = false;
  private static final String SYMBOLS = "(){},;:+-*/";
  private String lexeme = "";
  private int line = 1;

  public Map<String, Vector<Token>> analyse(Vector<Character> characters) {
    int currentState = 0;

    for (int i = 0; i < characters.size(); i++) {
      char currentChar = characters.get(i);

      switch (currentState) {
        case 0:
          currentState = stateZero(currentChar);
          break;
        case 1:
          currentState = stateOne(currentChar);
          break;
        case 2:
          currentState = stateTwo(currentChar);
          break;
        case 3:
          currentState = stateThree(currentChar);
          break;
        case 4:
          currentState = stateFour(currentChar);
          break;
        case 5:
          currentState = stateFive(currentChar);
          break;
        case 6:
          currentState = stateSix(currentChar);
          break;
        case 7:
          currentState = stateSeven(currentChar);
          break;
        case 8:
          currentState = stateEight(currentChar);
          break;
        case 9:
          currentState = stateNine(currentChar);
          break;
        case 10:
          currentState = stateTen(currentChar);
          break;
        case 11:
          currentState = stateEleven(currentChar);
          break;
        case 12:
          currentState = stateTwelve(currentChar);
          break;
        default:
          currentState = 0;
      }
      if (usedLast) {
        System.err.println("usedLast ativado, voltando um índice i--");
        i--;
        usedLast = false;
      }

    }

    Map<String, Vector<Token>> result = new HashMap<>();
    result.put("tokens", tokens);
    result.put("errors", errors);
    return result;
  }

  private Token createToken() {
    Token.Type type = Token.Type.fromLexeme(lexeme);
    if (type == null) {
      System.err.println("Token não identificado na linha " + line);
    }
    Token newToken = new Token(type, lexeme, line);
    lexeme = "";
    return newToken;
  }

  public static boolean isValidChar(char c) {
    String lexeme = String.valueOf(c); // transforma char em String

    // Verifica se o char pode iniciar algum token pelo enum
    for (Token.Type t : Token.Type.values()) {
      if (t.getPattern() != null) {
        if (t.getPattern().matcher(lexeme).matches()) {
          System.out.println("DEBUG: '" + c + "' é válido para token " + t.name());
          return true;
        }
      }
    }

    // Espaços, tabulação e nova linha
    if (Character.isWhitespace(c)) {
      System.out.println("DEBUG: '" + c + "' é whitespace");
      return true;
    }

    // Aspas simples, duplas e ponto
    if (c == '\'' || c == '"' || c == '.') {
      System.out.println("DEBUG: '" + c + "' é aspas simples, duplas ou ponto");
      return true;
    }

    // Se não bateu com nenhum token nem é espaço nem aspas/ponto
    System.out.println("DEBUG: '" + c + "' não é válido");
    return false;
  }
  // ------------------ ESTADOS ---------------------

  private int stateZero(Character ch) {
    int nextState = 0;

    switch (ch) {
      case '(':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case ')':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case '{':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case '}':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case ':':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case ';':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case ',':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case '+':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case '*':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case '/':
        lexeme += ch;
        tokens.add(createToken());
        lexeme = "";
        break;
      case '\n':
        line++;
        break;
      case ' ':
        break;
      case '-':
        lexeme += ch;
        nextState = 1;
        break;
      case '!':
        lexeme += ch;
        nextState = 2;
        break;
      case '=':
        lexeme += ch;
        nextState = 3;
        break;
      case '>':
        lexeme += ch;
        nextState = 4;
        break;
      case '<':
        lexeme += ch;
        nextState = 5;
        break;
      case '\'':
        lexeme += ch;
        nextState = 10;
        break;
      case '\"':
        lexeme += ch;
        nextState = 12;
        break;
      default:
        if (Character.isLetter(ch)) {
          lexeme += ch;
          nextState = 6;
          break;
        } else if (Character.isDigit(ch)) {
          lexeme += ch;
          nextState = 7;
          break;
        } else if (!isValidChar(ch)) {
          lexeme += ch;
          errors.add(createToken());
        }
    }
    return nextState;
  }

  private int stateOne(Character ch) {
    if (ch == '>') {
      lexeme += ch;
      tokens.add(createToken());
    } else {
      tokens.add(createToken());
      usedLast = true;
    }
    return 0;
  }

  private int stateTwo(Character ch) {
    if (ch == '=') {
      lexeme += ch;
      tokens.add(createToken());
    } else {
      System.err.println("ERRO NA LINHA: " + line);
      System.exit(1);
    }
    return 0;
  }

  private int stateThree(Character ch) {
    if (ch == '=') {
      lexeme += ch;
      tokens.add(createToken());
    } else {
      tokens.add(createToken());
      usedLast = true;
    }
    return 0;
  }

  private int stateFour(Character ch) {
    if (ch == '=') {
      lexeme += ch;
      tokens.add(createToken());
    } else {
      tokens.add(createToken());
      usedLast = true;
    }
    return 0;
  }

  private int stateFive(Character ch) {
    if (ch == '=') {
      lexeme += ch;
      tokens.add(createToken());
    } else {
      tokens.add(createToken());
      usedLast = true;
    }
    return 0;
  }

  private int stateSix(Character ch) {
    if (Character.isLetter(ch) || Character.isDigit(ch) || ch == '_') {
      lexeme += ch;
      return 6;
    } else {
      tokens.add(createToken());
      usedLast = true;
      return 0;
    }
  }

  private int stateSeven(Character ch) {
    if (Character.isDigit(ch)) {
      lexeme += ch;
      return 7;
    } else if (ch == '.') {
      lexeme += ch;
      return 8;
    } else {
      tokens.add(createToken());
      usedLast = true;
      return 0;
    }
  }

  private int stateEight(Character ch) {
    if (!Character.isDigit(ch)) {
      System.err.println("ERRO NA LINHA: " + line);
      System.exit(1);
    }
    lexeme += ch;
    return 9;
  }

  private int stateNine(Character ch) {
    if (Character.isDigit(ch)) {
      lexeme += ch;
      return 9;
    } else {
      tokens.add(createToken());
      usedLast = true;
      return 0;
    }
  }

  private int stateTen(Character ch) {
    lexeme += ch;
    return 11;
  }

  private int stateEleven(Character ch) {
    if (!(ch == '\'')) {
      System.err.println("ERRO NA LINHA: " + line);
      System.exit(1);
    }
    lexeme += ch;
    tokens.add(createToken());
    return 0;
  }

  private int stateTwelve(Character ch) {
    if (ch == '\"') {
      lexeme += ch;
      tokens.add(createToken());
    } else {
      lexeme += ch;
      return 12;
    }
    return 0;
  }

}
