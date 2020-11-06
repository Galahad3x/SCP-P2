#include <stdio.h>
#include <sys/select.h>

struct Mercat {
	Jugador jugadors[];
};

struct Jugador {
	char nom[];
	int preu;
	int valor;
}
