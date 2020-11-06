#include <stdio.h>
#include <sys/select.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>

#define MAX_PLAYERS_IN_MARKET 1000
#define MAX_PLAYER_NAMES 50
#define MAX_MARKET_LINE 128
#define MAX_TEAM_NAME 5

#define MAX_PORTERS 1
#define MAX_DEFENSES 3
#define MAX_CENTRES 2
#define MAX_DAVANTERS 1

typedef enum {Porter, Defensa, Centre, Davanter} TJugador;

struct Jugador {
	int id;
	char nom[MAX_PLAYER_NAMES];
	TJugador posicio;
	int preu;
	char nom_equip[MAX_TEAM_NAME];
	int valor;
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

struct Mercat mercat;

void llegirMercat(char *pathJugadors) {
	char buffer[256], tipus[10];
	int fdin;
	int nofi;
	
	int ln = 0;
	
	fdin=open(pathJugadors, O_RDONLY);
	
	do {
		int x=0,i,f;

		while((nofi=read(fdin,&buffer[x],1))!=0 && buffer[x++]!='\n');
		buffer[x]='\0';
		
		if (buffer[0]=='#') continue;
		
		i=0;
		for (f=0;buffer[f]!=';';f++);
		buffer[f]=0;
		mercat.jugadors[ln].id = atoi(&(buffer[i]));

		i=++f;
		for (;buffer[f]!=';';f++);
		buffer[f]=0;
		strcpy(mercat.jugadors[ln].nom,&(buffer[i]));

		i=++f;
		for (;buffer[f]!=';';f++);
		buffer[f]=0;
		if (strcmp(&(buffer[i]),"Portero")==0){
			mercat.jugadors[ln].posicio=Porter;
		}
		else if (strcmp(&(buffer[i]),"Defensa")==0){
			mercat.jugadors[ln].posicio=Defensa;
		}
		else if (strcmp(&(buffer[i]),"Medio")==0){
			mercat.jugadors[ln].posicio=Centre;
		}
		else if (strcmp(&(buffer[i]),"Delantero")==0){
			mercat.jugadors[ln].posicio=Davanter;
		}
		
		i=++f;
		for (f=0;buffer[f]!=';';f++);
		buffer[f]=0;
		mercat.jugadors[ln].preu = atoi(&(buffer[i]));
		
		i=++f;
		for (f=0;buffer[f]!=';';f++);
		buffer[f]=0;
		strcpy(mercat.jugadors[ln].nom_equip,&(buffer[i]));

		i=++f;
		for (f=0;buffer[f]!='\n';f++);
		buffer[f]=0;
		mercat.jugadors[ln].valor = atoi(&(buffer[i]));
		
		ln++;
	}
	while(nofi);
	
	close(fdin);
}

int main(int argc, char* argvs[]){
	if(argc >= 4){
		printf("Nom mercat: %s\n",argvs[2]);
		
		llegirMercat(argvs[2]);
	}else{
		printf("ERROR: Parametres incorrectes\n");
		exit(-1);
	}
}
