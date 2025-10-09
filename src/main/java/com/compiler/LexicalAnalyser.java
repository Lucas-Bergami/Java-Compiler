package com.compiler;

import java.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class LexicalAnalyser {
  private Vector<Token> tokens = new Vector<Token>();
  private Vector<Token> errors = new Vector<Token>();
  private boolean usedLast = false;
  private String lexeme = "";
  private int line = 1;

  public Map<String, Vector<Token>> analyse(Vector<Character> characters) {
    int currentState = 0;

    for (int i = 0; i < characters.size(); i++) {
      char currentChar = characters.get(i);

        currentState = switch (currentState) {
            case 0 -> stateZero(currentChar);
            case 1 -> stateOne(currentChar);
            case 2 -> stateTwo(currentChar);
            case 3 -> stateThree(currentChar);
            case 4 -> stateFour(currentChar);
            case 5 -> stateFive(currentChar);
            case 6 -> stateSix(currentChar);
            case 7 -> stateSeven(currentChar);
            case 8 -> stateEight(currentChar);
            case 9 -> stateNine(currentChar);
            case 10 -> stateTen(currentChar);
            case 11 -> stateEleven(currentChar);
            case 12 -> stateTwelve(currentChar);
            default -> 0;
        };
      if (usedLast) {
        i--;
        usedLast = false;
      }

    }
        if(!lexeme.isEmpty()) {
            Token.Type type = Token.Type.fromLexeme(lexeme);
            if (type == null) {
                errors.add(createToken());
            }else {
                tokens.add(createToken());
            }
        }
    Map<String, Vector<Token>> result = new HashMap<>();
    result.put("tokens", tokens);
    result.put("errors", errors);
    return result;
  }

  private Token createToken() {
    Token.Type type = Token.Type.fromLexeme(lexeme);
    Token newToken = new Token(type, lexeme, line);
    lexeme = "";
    return newToken;
  }

  public static boolean isValidChar(char c) {
    String lexeme = String.valueOf(c);

    for (Token.Type t : Token.Type.values()) {
      if (t.getPattern() != null) {
        if (t.getPattern().matcher(lexeme).matches()) {
          return true;
        }
      }
    }

    if (Character.isWhitespace(c)) {
      return true;
    }

      return c == '\'' || c == '"' || c == '.';

  }

  // ------------------ ESTADOS ---------------------

  private int stateZero(Character ch) {
    int nextState = 0;

    switch (ch) {
      case '(', ')', '{', '}', ':', ';', ',', '+', '*', '/':
        lexeme += ch;
        tokens.add(createToken());
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
        errors.add(createToken());
        lexeme += ch;
        usedLast = true;
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
        lexeme = lexeme.substring(0, lexeme.length() - 1);
        tokens.add(createToken());
        lexeme += '.';
        errors.add(createToken());
        usedLast = true;
        return 0;
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
        lexeme += ch;
        errors.add(createToken());
    }else {
        lexeme += ch;
        tokens.add(createToken());
    }
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
