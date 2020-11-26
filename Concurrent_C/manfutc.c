/* ---------------------------------------------------------------
Práctica 1.
Código fuente: manfutc.c
Grau Informàtica
48051307Y Joel Aumedes Serrano
78103400T Joel Farré Cortés
---------------------------------------------------------------*/

#include <stdio.h>
#include <sys/select.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <fcntl.h>
#include <pthread.h>

#define ASSUMED_MAX_THREADS 10

#define MAX_PLAYERS_IN_MARKET 200
#define MAX_PLAYER_NAMES 50
#define MAX_MARKET_LINE 256
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

struct ThreadArgs {
	struct Equip equip;
	int index;
};

struct Mercat mercat;
int numero_threads;
int debug = 0;
volatile int id_equips;
pthread_t threads[ASSUMED_MAX_THREADS];
volatile int active_threads[ASSUMED_MAX_THREADS];
static pthread_mutex_t mutex_ids = PTHREAD_MUTEX_INITIALIZER;
static pthread_mutex_t mutex_actives = PTHREAD_MUTEX_INITIALIZER;

int jugador_apte(struct Equip equip, struct Jugador jugador);
void afegir_jugador(struct Equip *equip, struct Jugador jugador);
void llegir_mercat(char *pathJugadors);
void deepcopy_jugadors(struct JugadorsEquip origen, struct JugadorsEquip *desti);
int trobar_millor_equip(struct Equip *equip, int index);
void *trobar_millor_equip_conc(void *argvs);
void printar_jugador(struct Jugador jugador);
void printar_equip(struct Equip equip);

int main(int argc, char* argvs[]){
	if(argc >= 4){
		mercat.pressupost = atoi(argvs[1]);
		numero_threads = atoi(argvs[3]);
		
		for(int i = 0; i < numero_threads; i++){
			active_threads[i] = 0;
		}
		
		if(argc >= 5){
			if(strcmp("-d",argvs[4]) == 0){
				debug = 1;
			}
		}
		
		if(debug == 1){
			printf("Nom mercat: %s\n",argvs[2]);
		}
		llegir_mercat(argvs[2]);
		
		if(debug == 1){
			printf("Numero de jugadors total: %i\n", mercat.num_jugadors);
		}
		
		struct Equip millor_equip;
		millor_equip.id = 0;
		millor_equip.valor = 0;
		millor_equip.cost = 0;
		millor_equip.pressupost = mercat.pressupost;
		millor_equip.jugadors.n_porters = 0;
		millor_equip.jugadors.n_defenses = 0;
		millor_equip.jugadors.n_centres = 0;
		millor_equip.jugadors.n_davanters = 0;
		
		printf("\n");
		trobar_millor_equip(&millor_equip, mercat.num_jugadors - 1);
		
		printar_equip(millor_equip);
	}else{
		printf("ERROR: Parametres incorrectes\n");
		exit(-1);
	}
}

int jugador_apte(struct Equip equip, struct Jugador jugador){
	if(jugador.preu > equip.pressupost){
		if(debug == 1){
			printf("NO APTE: %s és massa car\n",jugador.nom);
		}
		return -1;
	}
	if(jugador.posicio == PORTER){
		if(equip.jugadors.n_porters < MAX_PORTERS){
			return 0;
		}else{
			if(debug == 1){
				printf("NO APTE: La posició de %s ja està plena\n",jugador.nom);
			}
			return -1;
		}
	}else if(jugador.posicio == DEFENSA){
		if(equip.jugadors.n_defenses < MAX_DEFENSES){
			return 0;
		}else{
			if(debug == 1){
				printf("NO APTE: La posició de %s ja està plena\n",jugador.nom);
			}
			return -1;
		}
	}else if(jugador.posicio == CENTRE){
		if(equip.jugadors.n_centres < MAX_CENTRES){
			return 0;
		}else{
			if(debug == 1){
				printf("NO APTE: La posició de %s ja està plena\n",jugador.nom);
			}
			return -1;
		}
	}else{
		if(equip.jugadors.n_davanters < MAX_DAVANTERS){
			return 0;
		}else{
			if(debug == 1){
				printf("NO APTE: La posició de %s ja està plena\n",jugador.nom);
			}
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
		equip -> jugadors.defenses[equip -> jugadors.n_defenses] = jugador;
		equip -> jugadors.n_defenses++;
	}else if(jugador.posicio == CENTRE){
		equip -> jugadors.centres[equip -> jugadors.n_centres] = jugador;
		equip -> jugadors.n_centres++;
	}else if(jugador.posicio == DAVANTER){
		equip -> jugadors.davanters[equip -> jugadors.n_davanters] = jugador;
		equip -> jugadors.n_davanters++;
	}
}

void llegir_mercat(char *pathJugadors){
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

void deepcopy_jugadors(struct JugadorsEquip origen, struct JugadorsEquip *desti){
	for(int i = 0; i < origen.n_porters; i++){
		desti -> porters[i] = origen.porters[i];
	}
	desti -> n_porters = origen.n_porters;
	for(int i = 0; i < origen.n_defenses; i++){
		desti -> defenses[i] = origen.defenses[i];
	}
	desti -> n_defenses = origen.n_defenses;
	for(int i = 0; i < origen.n_centres; i++){
		desti -> centres[i] = origen.centres[i];
	}
	desti -> n_centres = origen.n_centres;
	for(int i = 0; i < origen.n_davanters; i++){
		desti -> davanters[i] = origen.davanters[i];
	}
	desti -> n_davanters = origen.n_davanters;
}

int trobar_millor_equip(struct Equip *equip, int index){
	if(index == 0){
		if(jugador_apte(*equip, mercat.jugadors[index]) == 0){
			if(debug == 1){
				printf("Jugador %s pot ser de l'equip\n",mercat.jugadors[index].nom);
			}
			afegir_jugador(equip, mercat.jugadors[index]);
		}
		
		pthread_mutex_lock(&mutex_ids);
		
		equip -> id = id_equips;
		id_equips++;
		
		pthread_mutex_unlock(&mutex_ids);
		return equip -> valor;
	}else{
		
		int val_no_agafar, val_agafar = 0, child_thread = -1;
		struct JugadorsEquip no_agafar,agafar;
		struct Equip no_agafar_equip,agafar_equip;
		//printf("Index %i\n",index);
		if(jugador_apte(*equip, mercat.jugadors[index]) == 0){
			if(debug == 1){
				printf("Jugador %s pot ser de l'equip\n",mercat.jugadors[index].nom);
			}
			
			pthread_mutex_lock(&mutex_actives);
			
			for(int i = 0; i < numero_threads; i++){
				if(active_threads[i] == 0){
					child_thread = i;
					break;
				}
			}
			
			if(child_thread != -1){
				active_threads[child_thread] = 1;
			}
			pthread_mutex_unlock(&mutex_actives);
			
			//Si hi ha un thread lliure
			if(child_thread != -1){
				//Fer tot lo del thread
				//Iniciar thread que faci tot aixo i retorni a val_agafar
				//Parametres del thread equip i index
				struct ThreadArgs args;
				args.equip = *equip;
				args.index = index;
				
				if(pthread_create(&threads[child_thread],NULL,trobar_millor_equip_conc,(void *) &args) != 0){
					if(debug == 1){
						printf("ERROR: Error al crear un thread\n");
						exit(-1);
					}
				}
			}else{ //No hi ha espai als threads, ho fa el mateix thread
				deepcopy_jugadors(equip -> jugadors,&agafar);
				
				agafar_equip.valor = equip -> valor;
				agafar_equip.id = equip -> id;
				agafar_equip.cost = equip -> cost;
				agafar_equip.pressupost = equip -> pressupost;
				agafar_equip.jugadors = agafar;
				
				afegir_jugador(&agafar_equip, mercat.jugadors[index]);
				
				val_agafar = trobar_millor_equip(&agafar_equip, index - 1);
			}
		}
		
		deepcopy_jugadors(equip -> jugadors,&no_agafar);
		
		no_agafar_equip.valor = equip -> valor;
		no_agafar_equip.cost = equip -> cost;
		no_agafar_equip.id = equip -> id;
		no_agafar_equip.pressupost = equip -> pressupost;
		no_agafar_equip.jugadors = no_agafar;
		val_no_agafar = trobar_millor_equip(&no_agafar_equip, index - 1);
		
		
		if(child_thread != -1){
			//principal fa join de l'altre
			//El join te l'equip agafar
			struct Equip *agafar_thread;
			int ret;
			if((ret = pthread_join(threads[child_thread], (void **) &agafar_thread)) != 0){
				printf("ERROR: Error al fer un join %i\n", ret);
			}
			
			active_threads[child_thread] = 0;
			
			agafar_equip = *agafar_thread;
		}
		//Els 2 threads han d'haver acabat
		if(debug == 1){
			printf("Valor agafar: %i Valor no agafar: %i \n",val_agafar, val_no_agafar);
		}
		
		if(val_agafar == 0 || val_no_agafar > val_agafar){
			equip -> valor = no_agafar_equip.valor;
			equip -> cost = no_agafar_equip.cost;
			equip -> pressupost = no_agafar_equip.pressupost;
			equip -> jugadors = no_agafar_equip.jugadors;
		}else{
			equip -> valor = agafar_equip.valor;
			equip -> cost = agafar_equip.cost;
			equip -> pressupost = agafar_equip.pressupost;
			equip -> jugadors = agafar_equip.jugadors;
		}
		return equip -> valor;
	}
}

void *trobar_millor_equip_conc(void *argvs){
	//Llegir parametres: equip i index
	struct ThreadArgs args = *((struct ThreadArgs *) argvs);
	
	struct Equip equip = args.equip;
	int index = args.index;
	
	struct JugadorsEquip agafar;
	struct Equip agafar_equip;
	
	deepcopy_jugadors(equip.jugadors,&agafar);
	
	agafar_equip.valor = equip.valor;
	agafar_equip.id = equip.id;
	agafar_equip.cost = equip.cost;
	agafar_equip.pressupost = equip.pressupost;
	agafar_equip.jugadors = agafar;
	
	afegir_jugador(&agafar_equip, mercat.jugadors[index]);
	
	trobar_millor_equip(&agafar_equip, index - 1);
	
	struct Equip *max_equip;
	max_equip = (struct Equip *) malloc(sizeof(struct Equip));
	*max_equip = agafar_equip;
	return max_equip;
	//Retornar equip maxim amb el jugador(valor incluit va dins)
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
		printf("\n");
	}
	printf("\nDEFENSES:\n");
	for(int i = 0; i < equip.jugadors.n_defenses;i++){
		printar_jugador(equip.jugadors.defenses[i]);
		printf("\n");
	}
	printf("\nCENTRES:\n");
	for(int i = 0; i < equip.jugadors.n_centres;i++){
		printar_jugador(equip.jugadors.centres[i]);
		printf("\n");
	}
	printf("\nDAVANTERS:\n");
	for(int i = 0; i < equip.jugadors.n_davanters;i++){
		printar_jugador(equip.jugadors.davanters[i]);
		printf("\n");
	}
}
