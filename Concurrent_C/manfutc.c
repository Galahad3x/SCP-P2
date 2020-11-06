#include <stdio.h>
#include <sys/select.h>
#include <string.h>
#include <stdlib.h>

#define MAX_PLAYERS_IN_MARKET 1000
#define MAX_PLAYER_NAMES 50
#define MAX_PORTERS 1
#define MAX_DEFENSES 3
#define MAX_CENTRES 2
#define MAX_DAVANTERS 1

typedef enum {Porter, Defensa, Centre, Davanter} TJugador;

struct Jugador {
	char nom[MAX_PLAYER_NAMES];
	int preu;
	int valor;
	TJugador posicio;
};

struct Mercat {
	struct Jugador jugadors[MAX_PLAYERS_IN_MARKET];
};

struct JugadorsEquip {
	struct Jugador porters[MAX_PORTERS];
	struct Jugador defenses[MAX_DEFENSES];
	struct Jugador centres[MAX_CENTRES];
	struct Jugador davanters[MAX_DAVANTERS];
};

struct Equip {
	int id;
	int valor;
	int cost;
	struct JugadorsEquip jugadors;
};

int main(int argc, char* argvs[]){
	if(argc > 1){
		char nom_mercat[30];
		strcpy(nom_mercat, argvs[1]);
		printf("Nom mercat: %s\n",nom_mercat);
		struct Mercat mercat;
		struct Jugador j1;
		j1.preu = 20;
		struct Jugador j2;
		j2.valor = 50;
		struct Jugador j3;
		mercat.jugadors[0] = j1;
	}else{
		printf("ERROR: Parametres incorrectes\n");
		exit(-1);
	}
}
