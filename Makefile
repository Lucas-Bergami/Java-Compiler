# Nome da classe principal
MAIN = Main

# Compila os arquivos .java
compile:
	javac *.java

# Roda o programa (depois de compilar)
run: compile
	java $(MAIN)

# Limpa os arquivos compilados (.class)
clean:
	rm -f *.class
