# Java P Compiler

Este repositório contém um compilador para a linguagem P escrito em Java.

## Pré-requisitos

* Java JDK instalado (recomenda-se versão 17 ou superior)
* Maven instalado

---

## Instalando o Maven

Para instalar o Maven, execute:

```bash
sudo pacman -Syu maven
```

Verifique a instalação com:

```bash
mvn -v
```

---

## Rodando o projeto

1. Abra o terminal na pasta raiz do projeto (onde está o `pom.xml`).
2. Compile o projeto e baixe as dependências:

```bash
mvn compile
```

3. Execute a aplicação:


 Para executar normalmente sem argumentos, o programa usará o arquivo padrão `example.txt`:

```bash
mvn exec:java
```
Para executar passando um arquivo específico como argumento:

```bash
mvn exec:java -Dexec.args="arquivo.txt"
```
