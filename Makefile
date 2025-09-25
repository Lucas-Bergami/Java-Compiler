# Nome da classe principal
MAIN = Main

# Compila os arquivos .java
compile:
  javac $(MAIN).java Token.java

# Roda o programa (depois de compilar)
run: compile
	java $(MAIN)

# Limpa os arquivos compilados (.class)
clean:
	rm -f *.class
