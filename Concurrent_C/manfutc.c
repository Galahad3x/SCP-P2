#include <stdio.h>
#include <sys/select.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>

#define MAX_PLAYERS_IN_MARKET 100
#define MAX_PLAYER_NAMES 50
#define MAX_MARKET_LINE 128
#define MAX_TEAM_NAME 5

#define MAX_PORTERS 1
#define MAX_DEFENSES 3
#define MAX_CENTRES 2
#define MAX_DAVANTERS 1

#define PORTER 0
#define DEFENSA 1
#define CENTRE 2
#define DAVANTER 3

struct Jugador {
	int id;
	char nom[MAX_PLAYER_NAMES];
	int posicio;
	int preu;
	char nom_equip[MAX_TEAM_NAME];
	int valor;
};

struct Mercat {
	struct Jugador jugadors[MAX_PLAYERS_IN_MARKET];
	int num_jugadors;
	int pressupost;
};

struct JugadorsEquip {
	struct Jugador porters[MAX_PORTERS];
	int n_porters;
	struct Jugador defenses[MAX_DEFENSES];
	int n_defenses;
	struct Jugador centres[MAX_CENTRES];
	int n_centres;
	struct Jugador davanters[MAX_DAVANTERS];
	int n_davanters;
};

struct Equip {
	int id;
	int valor;
	int cost;
	int pressupost;
	struct JugadorsEquip jugadors;
};

struct Mercat mercat;
int numero_threads;

int jugador_apte(struct Equip equip, struct Jugador jugador){
	if(jugador.preu > equip.pressupost){
		return -1;
	}
	if(jugador.posicio == PORTER){
		if(equip.jugadors.n_porters < MAX_PORTERS){
			return 0;
		}else{
			return -1;
		}
	}else if(jugador.posicio == DEFENSA){
		if(equip.jugadors.n_defenses < MAX_DEFENSES){
			return 0;
		}else{
			return -1;
		}
	}else if(jugador.posicio == CENTRE){
		if(equip.jugadors.n_centres < MAX_CENTRES){
			return 0;
		}else{
			return -1;
		}
	}else{
		if(equip.jugadors.n_davanters < MAX_DAVANTERS){
			return 0;
		}else{
			return -1;
		}
	}
}

void afegir_jugador(struct Equip *equip, struct Jugador jugador){
	equip -> cost += jugador.preu;
	equip -> valor += jugador.valor;
	equip -> pressupost -= jugador.preu;
	if(jugador.posicio == PORTER){
		equip -> jugadors.porters[equip -> jugadors.n_porters] = jugador;
		equip -> jugadors.n_porters++;
	}else if(jugador.posicio == DEFENSA){
		equip -> jugadors.porters[equip -> jugadors.n_defenses] = jugador;
		equip -> jugadors.n_defenses++;
	}else if(jugador.posicio == CENTRE){
		equip -> jugadors.porters[equip -> jugadors.n_centres] = jugador;
		equip -> jugadors.n_centres++;
	}else if(jugador.posicio == DAVANTER){
		equip -> jugadors.porters[equip -> jugadors.n_davanters] = jugador;
		equip -> jugadors.n_davanters++;
	}
}

void llegir_mercat(char *pathJugadors) {
	char buffer[256];
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
			mercat.jugadors[ln].posicio=PORTER;
		}
		else if (strcmp(&(buffer[i]),"Defensa")==0){
			mercat.jugadors[ln].posicio=DEFENSA;
		}
		else if (strcmp(&(buffer[i]),"Medio")==0){
			mercat.jugadors[ln].posicio=CENTRE;
		}
		else if (strcmp(&(buffer[i]),"Delantero")==0){
			mercat.jugadors[ln].posicio=DAVANTER;
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
	
	mercat.num_jugadors = ln;
	
	close(fdin);
}

void trobar_millor_equip(struct Equip *equip, int index, int pressupost){
	if(index == 0){
		printf("%i\n",jugador_apte(*equip, mercat.jugadors[index]));
		if(jugador_apte(*equip, mercat.jugadors[index]) == 0){
			printf("Jugador %s pot ser de l'equip\n",mercat.jugadors[index].nom);
			afegir_jugador(equip, mercat.jugadors[index]);
		}
	}else{
		
	}
}

void printar_jugador(struct Jugador jugador){
	printf("ID del jugador: %i\nNom: %s\nEquip original: %s\n",jugador.id,jugador.nom,jugador.nom_equip);
	if(jugador.posicio == PORTER){
		printf("Posició: Porter\n");
	}else if(jugador.posicio == DEFENSA){
		printf("Posició: Defensa\n");
	}else if(jugador.posicio == CENTRE){
		printf("Posició: Centre\n");
	}else if(jugador.posicio == DAVANTER){
		printf("Posició: Davanter\n");
	}
	printf("Preu: %i\nValor: %i\n",jugador.preu,jugador.valor);
}

void printar_equip(struct Equip equip){
	printf("ID de l'equip: %i\nCost total: %i\nValor: %i\n",equip.id, equip.cost, equip.valor);
	printf("\nPORTERS:\n");
	for(int i = 0; i < equip.jugadors.n_porters;i++){
		printar_jugador(equip.jugadors.porters[i]);
	}
	printf("\nDEFENSES:\n");
	for(int i = 0; i < equip.jugadors.n_defenses;i++){
		printar_jugador(equip.jugadors.defenses[i]);
	}
	printf("\nCENTRES:\n");
	for(int i = 0; i < equip.jugadors.n_centres;i++){
		printar_jugador(equip.jugadors.centres[i]);
	}
	printf("\nDAVANTERS:\n");
	for(int i = 0; i < equip.jugadors.n_davanters;i++){
		printar_jugador(equip.jugadors.davanters[i]);
	}
}

int main(int argc, char* argvs[]){
	if(argc >= 4){
		mercat.pressupost = atoi(argvs[1]);
		numero_threads = atoi(argvs[3]);
		
		printf("Nom mercat: %s\n",argvs[2]);
		llegir_mercat(argvs[2]);
		
		printf("Numero de jugadors total: %i\n", mercat.num_jugadors);
		
		struct Equip millor_equip;
		millor_equip.id = 0;
		millor_equip.valor = 0;
		millor_equip.cost = 0;
		millor_equip.pressupost = mercat.pressupost;
		millor_equip.jugadors.n_porters = 0;
		millor_equip.jugadors.n_defenses = 0;
		millor_equip.jugadors.n_centres = 0;
		millor_equip.jugadors.n_davanters = 0;
		
		trobar_millor_equip(&millor_equip, 0, mercat.pressupost);
		
		printar_equip(millor_equip);
	}else{
		printf("ERROR: Parametres incorrectes\n");
		exit(-1);
	}
}
