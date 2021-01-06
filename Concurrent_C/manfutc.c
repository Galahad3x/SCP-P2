
/* ---------------------------------------------------------------
Práctica 2.
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
#include <semaphore.h>

//Non-modified variables used to create fixed-size arrays
#define ASSUMED_MAX_THREADS 10
#define MAX_PLAYERS_IN_MARKET 200
#define MAX_PLAYER_NAMES 50
#define MAX_MARKET_LINE 256
#define MAX_TEAM_NAME 5
#define MAX_MESSAGE_LEN 500

//F7 Lineup
#define MAX_PORTERS 1
#define MAX_DEFENSES 3
#define MAX_CENTRES 2
#define MAX_DAVANTERS 1

//Integer used to save a player's position
#define PORTER 0
#define DEFENSA 1
#define CENTRE 2
#define DAVANTER 3

//Struct with a single player's data
struct Jugador {
	int id;
	char nom[MAX_PLAYER_NAMES];
	int posicio;
	int preu;
	char nom_equip[MAX_TEAM_NAME];
	int valor;
};

//Struct to save market data
struct Mercat {
	struct Jugador jugadors[MAX_PLAYERS_IN_MARKET];
	int num_jugadors;
	int pressupost;
};

//Struct used inside Equip to save a reference to the players in the team
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

//Struct used to save a team's data
struct Equip {
	int id;
	int valor;
	int cost;
	int pressupost;
	struct JugadorsEquip jugadors;
};

//Struct used to pass parameters to a thread
struct ThreadArgs {
	struct Equip equip;
	int index;
	int thread_slot;
};

struct Estadistiques {
	int combinacions_valides;
	int combinacions_evaluades;
	int combinacions_no_valides;
	int cost_total_valides;
	int puntuacio_total_valides;
	int millor_puntuacio;
	int pitjor_puntuacio;
	int etapa;
};

//Global variable declarations
struct Mercat mercat;
int numero_threads;
int debug = 0;
int m = 25000;
volatile int id_equips;

pthread_t threads[ASSUMED_MAX_THREADS];
pthread_t thread_missatges;

volatile int active_threads[ASSUMED_MAX_THREADS];
struct Equip return_values[ASSUMED_MAX_THREADS];
struct Estadistiques stats[ASSUMED_MAX_THREADS];
struct Estadistiques globals;

char msgs[ASSUMED_MAX_THREADS + 1][MAX_MESSAGE_LEN];
char missatges[100][MAX_MESSAGE_LEN];
volatile int numero_missatges;
volatile int missatges_alive = 1;

pthread_mutex_t ids_sync = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t actives_sync = PTHREAD_MUTEX_INITIALIZER;
pthread_mutex_t missatges_sync = PTHREAD_MUTEX_INITIALIZER;
pthread_cond_t missatges_cond;
pthread_barrier_t barriers[ASSUMED_MAX_THREADS];
sem_t semafor_globals;

//Function headers
void llegir_mercat(char *pathJugadors);

int jugador_apte(struct Equip equip, struct Jugador jugador);
void afegir_jugador(struct Equip *equip, struct Jugador jugador);

void deepcopy_jugadors(struct JugadorsEquip origen, struct JugadorsEquip *desti);

int trobar_millor_equip(struct Equip *equip, int index, int thread_slot);
void *trobar_millor_equip_conc(void *argvs);

void *atendre_missatges();
void cprintf(char missatge[MAX_MESSAGE_LEN]);

void printar_jugador(struct Jugador jugador);
void printar_equip(struct Equip equip);
void print_stats(int slot_index);
int necessary_print();
void print_all_stats();

//Main function
int main(int argc, char* argvs[]){
	if(argc >= 4){
		//Reading the passed parameters
		mercat.pressupost = atoi(argvs[1]);
		numero_threads = atoi(argvs[3]);
		if (argc >= 5){
			if(strcmp("-d",argvs[4]) == 0){
				debug = 1;
			}else{
				m = atoi(argvs[4]);
			}
		}
		if(argc >= 6){
			if(strcmp("-d",argvs[5]) == 0){
				debug = 1;
			}
		}
		if (numero_threads == 1){
			cprintf("Passat 1 thread, serà com fer-ho sequencial.");
		}else if(numero_threads == 0){
			cprintf("Passat 0 threads, es farà amb 1.");
			numero_threads = 1;
		}
		numero_threads = numero_threads - 1;
		
		sem_init(&semafor_globals,0,1);
		pthread_cond_init(&missatges_cond, NULL);
		
		pthread_create(&thread_missatges,NULL,atendre_missatges,NULL);
		
		for(int i = 0; i < 100; i++){
			strcpy(missatges[i],"");
		}
		
		for(int i = 0; i < numero_threads; i++){
			active_threads[i] = 0;
		}
		for(int i = 0; i < numero_threads + 1; i++){
			stats[i].combinacions_valides = 0;
			stats[i].combinacions_evaluades = 0;
			stats[i].combinacions_no_valides = 0;
			stats[i].cost_total_valides = 0;
			stats[i].puntuacio_total_valides = 0;
			stats[i].millor_puntuacio = 0;
			stats[i].pitjor_puntuacio = 999999;
			stats[i].etapa = 0;
		}
		
		globals.combinacions_valides = 0;
		globals.combinacions_evaluades = 0;
		globals.combinacions_no_valides = 0;
		globals.cost_total_valides = 0;
		globals.puntuacio_total_valides = 0;
		globals.millor_puntuacio = 0;
		globals.pitjor_puntuacio = 999999;
		globals.etapa = 0;
		
		
		//Writing data from the passed .csv file to our global market variable
		llegir_mercat(argvs[2]);
		
		if(debug == 1){
			sprintf(msgs[0],"Numero de jugadors total: %i\n", mercat.num_jugadors);
			cprintf(msgs[0]);
		}
		
		//Creating an Equip in which we will save our final result.
		struct Equip millor_equip;
		millor_equip.id = 0;
		millor_equip.valor = 0;
		millor_equip.cost = 0;
		millor_equip.pressupost = mercat.pressupost;
		millor_equip.jugadors.n_porters = 0;
		millor_equip.jugadors.n_defenses = 0;
		millor_equip.jugadors.n_centres = 0;
		millor_equip.jugadors.n_davanters = 0;
		
		//Find the best team and write it into millor_equip
		trobar_millor_equip(&millor_equip, mercat.num_jugadors - 1, 0);
		
		printar_equip(millor_equip);
		
		pthread_mutex_lock(&missatges_sync);
		missatges_alive = 0;
		pthread_cond_signal(&missatges_cond);
		pthread_mutex_unlock(&missatges_sync);
		pthread_join(thread_missatges, NULL);
		
		sem_destroy(&semafor_globals);
		pthread_mutex_destroy(&ids_sync);
		pthread_mutex_destroy(&actives_sync);
		pthread_cond_destroy(&missatges_cond);
		
	}else{
		printf("ERROR: Paràmetres incorrectes\n");
		exit(-1);
	}
}

//This function reads the .csv file and writes the players' information into our global variable mercat
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

//This function returns 0 if jugador can be in equip or -1 if it can't
int jugador_apte(struct Equip equip, struct Jugador jugador){
	char msg[MAX_MESSAGE_LEN];
	if(jugador.preu > equip.pressupost){
		if(debug == 1){
			sprintf(msg,"NO APTE: %s és massa car\n",jugador.nom);
			cprintf(msg);
		}
		return -1;
	}
	if(jugador.posicio == PORTER){
		if(equip.jugadors.n_porters < MAX_PORTERS){
			return 0;
		}else{
			if(debug == 1){
				sprintf(msg,"NO APTE: La posició de %s ja està plena\n",jugador.nom);
				cprintf(msg);
			}
			return -1;
		}
	}else if(jugador.posicio == DEFENSA){
		if(equip.jugadors.n_defenses < MAX_DEFENSES){
			return 0;
		}else{
			if(debug == 1){
				sprintf(msg,"NO APTE: La posició de %s ja està plena\n",jugador.nom);
				cprintf(msg);
			}
			return -1;
		}
	}else if(jugador.posicio == CENTRE){
		if(equip.jugadors.n_centres < MAX_CENTRES){
			return 0;
		}else{
			if(debug == 1){
				sprintf(msg,"NO APTE: La posició de %s ja està plena\n",jugador.nom);
				cprintf(msg);
			}
			return -1;
		}
	}else{
		if(equip.jugadors.n_davanters < MAX_DAVANTERS){
			return 0;
		}else{
			if(debug == 1){
				sprintf(msg,"NO APTE: La posició de %s ja està plena\n",jugador.nom);
				cprintf(msg);
			}
			return -1;
		}
	}
}

//This function adds a player to a team, modifying its values
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

//This function creates a deep copy of a JugadorsEquip object to be assigned later to a different team
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

//This is the recursive function that will calculate the best possible team, starting from the end of the market
//until reaching the first player
int trobar_millor_equip(struct Equip *equip, int index, int thread_slot){
	//If index is 0, we are checking for the last player
	//Its value doesn't matter, if we can afford it, we will have more value with him on the team than without
	if(index == 0){
		if(jugador_apte(*equip, mercat.jugadors[index]) == 0){
			if(debug == 1){
				sprintf(msgs[thread_slot],"Jugador %s pot ser de l'equip\n",mercat.jugadors[index].nom);
				cprintf(msgs[thread_slot]);
			}
			afegir_jugador(equip, mercat.jugadors[index]);
		}else{
			stats[thread_slot].combinacions_no_valides++;
			stats[thread_slot].combinacions_evaluades++;
			if(stats[thread_slot].combinacions_evaluades % m == 0){
				stats[thread_slot].etapa++;
			}
			sem_wait(&semafor_globals);
			globals.combinacions_no_valides++;
			globals.combinacions_evaluades++;
			if(necessary_print() == 0){
				print_all_stats();
			}
			sem_post(&semafor_globals);
		}
		
		//Assign an ID to this completed team
		//We use a variable to avoid different teams having the same IDs
		pthread_mutex_lock(&ids_sync);
		
		equip -> id = id_equips;
		id_equips++;
		
		pthread_mutex_unlock(&ids_sync);
		
		//Si l'equip està complet
		if (equip -> jugadors.n_porters + equip -> jugadors.n_defenses + 
				equip -> jugadors.n_centres + equip -> jugadors.n_davanters == 7){
			sem_wait(&semafor_globals);
			globals.combinacions_valides++;
			globals.combinacions_evaluades++;
			if(necessary_print() == 0){
				print_all_stats();
			}
			globals.cost_total_valides += equip -> cost;
			globals.puntuacio_total_valides += equip -> valor;
			if (equip -> valor > globals.millor_puntuacio){
				globals.millor_puntuacio = equip -> valor;
			}else if (equip -> valor < globals.pitjor_puntuacio){
				globals.pitjor_puntuacio = equip -> valor;
			}
			sem_post(&semafor_globals);
			stats[thread_slot].combinacions_valides++;
			stats[thread_slot].combinacions_evaluades++;
			if(stats[thread_slot].combinacions_evaluades % m == 0){
				stats[thread_slot].etapa++;
			}
			stats[thread_slot].cost_total_valides += equip -> cost;
			stats[thread_slot].puntuacio_total_valides += equip -> valor;
			if (equip -> valor > stats[thread_slot].millor_puntuacio){
				stats[thread_slot].millor_puntuacio = equip -> valor;
			}else if (equip -> valor < stats[thread_slot].pitjor_puntuacio){
				stats[thread_slot].pitjor_puntuacio = equip -> valor;
			}
		}
		
		return equip -> valor;
	}else{
		//If the index is not 0, we need to account for the players we haven't checked yet
		int val_no_agafar, val_agafar = 0, child_thread = -1, child_slot = -1;
		struct JugadorsEquip no_agafar,agafar;
		struct Equip no_agafar_equip,agafar_equip;
		
		//If we can afford the player, we need to check what's better; having or not having him on the team
		if(jugador_apte(*equip, mercat.jugadors[index]) == 0){
			if(debug == 1){
				sprintf(msgs[thread_slot],"Jugador %s pot ser de l'equip\n",mercat.jugadors[index].nom);
				cprintf(msgs[thread_slot]);
			}
			
			//We use a variable to syncronize checking for free space for a thread, so different threads don't try to
			//create different threads on the same space
			pthread_mutex_lock(&actives_sync);
			
			for(int i = 0; i < numero_threads; i++){
				if(active_threads[i] == 0){
					child_thread = i;
					break;
				}
			}
			
			//If child_thread is -1, we don't have space for a new thread
			if(child_thread != -1){
				active_threads[child_thread] = 1;
			}
			pthread_mutex_unlock(&actives_sync);
			
			//If we have space for a thread
			if(child_thread != -1){
				//Initialize thread parameters
				struct ThreadArgs args;
				args.equip = *equip;
				args.index = index;
				args.thread_slot = child_thread + 1;
				child_slot = child_thread + 1;
				pthread_barrier_init(&barriers[child_slot],NULL,2);
				
				//Create a thread that will compute the score of having the player on our team
				if(pthread_create(&threads[child_thread],NULL,trobar_millor_equip_conc,(void *) &args) != 0){
					if(debug == 1){
						sprintf(msgs[thread_slot],"ERROR: Error al crear un thread\n");
						cprintf(msgs[thread_slot]);
						exit(-1);
					}
				}
			}else{ 
				//There isn't space for a new thread, we compute both possible options from a single thread
				deepcopy_jugadors(equip -> jugadors,&agafar);
				
				agafar_equip.valor = equip -> valor;
				agafar_equip.id = equip -> id;
				agafar_equip.cost = equip -> cost;
				agafar_equip.pressupost = equip -> pressupost;
				agafar_equip.jugadors = agafar;
				
				afegir_jugador(&agafar_equip, mercat.jugadors[index]);
				
				//Recursive call; having in mind that we now have more value on our team but less budget, 
				//what's the best team we can create with the rest of the players
				val_agafar = trobar_millor_equip(&agafar_equip, index - 1, thread_slot);
			}
		}else{
			stats[thread_slot].combinacions_no_valides++;
			stats[thread_slot].combinacions_evaluades++;
			if(stats[thread_slot].combinacions_evaluades % m == 0){
				stats[thread_slot].etapa++;
			}
			sem_wait(&semafor_globals);
			globals.combinacions_no_valides++;
			globals.combinacions_evaluades++;
			if(necessary_print() == 0){
				print_all_stats();
			}
			sem_post(&semafor_globals);
		}
		
		//The main branch we were using will, meanwhile, calculate the best outcome without having the player on the team
		deepcopy_jugadors(equip -> jugadors,&no_agafar);
		
		no_agafar_equip.valor = equip -> valor;
		no_agafar_equip.cost = equip -> cost;
		no_agafar_equip.id = equip -> id;
		no_agafar_equip.pressupost = equip -> pressupost;
		no_agafar_equip.jugadors = no_agafar;
		
		//Recursive call; having in mind that we now the same value and budget, but less players to choose from, 
		//what's the best team we can create
		val_no_agafar = trobar_millor_equip(&no_agafar_equip, index - 1, thread_slot);
		
		//If we used another thread to calculate the best outcome with the player
		if(child_thread != -1){
			pthread_barrier_wait(&barriers[child_slot]);
			agafar_equip = return_values[child_slot];
			pthread_barrier_destroy(&barriers[child_slot]);
			int ret;
			//We join the created thread, which will return the best team it could create while having the player
			if((ret = pthread_join(threads[child_thread], NULL) != 0)){
				sprintf(msgs[thread_slot],"ERROR: Error al fer un join %i\n", ret);
				cprintf(msgs[thread_slot]);
				exit(-1);
			}
			
			//We signal other threads that they can use this thread space now
			active_threads[child_thread] = 0;
			
			val_agafar = agafar_equip.valor;
		}
		//We have both best possible outcomes
		//Either by creating and joining another thread
		//Or by calculating it on the same thread
		if(debug == 1){
			sprintf(msgs[thread_slot],"Valor agafar: %i Valor no agafar: %i \n",val_agafar, val_no_agafar);
			cprintf(msgs[thread_slot]);
		}
		
		//We check which one of the outputs is the best, and modify equip to be it
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

//This is the function that we will call when creating a new thread
void *trobar_millor_equip_conc(void *argvs){
	//Read the parameters
	struct ThreadArgs args = *((struct ThreadArgs *) argvs);
	
	struct Equip equip = args.equip;
	int index = args.index;
	int thread_slot = args.thread_slot;
	
	struct JugadorsEquip agafar;
	struct Equip agafar_equip;
	
	//Add the player to the team
	deepcopy_jugadors(equip.jugadors,&agafar);
	
	agafar_equip.valor = equip.valor;
	agafar_equip.id = equip.id;
	agafar_equip.cost = equip.cost;
	agafar_equip.pressupost = equip.pressupost;
	agafar_equip.jugadors = agafar;
	
	afegir_jugador(&agafar_equip, mercat.jugadors[index]);
	
	//Recursive call: Find the best team that includes this player
	trobar_millor_equip(&agafar_equip, index - 1, thread_slot);
	
	//Return the calculated team
	return_values[thread_slot] = agafar_equip;
	pthread_barrier_wait(&barriers[thread_slot]);
	return NULL;
}

//This is the function that we will call with the messages thread
void *atendre_missatges(){
	while(missatges_alive == 1){
		pthread_mutex_lock(&missatges_sync);
		while(missatges_alive == 1 && numero_missatges < 100){
			pthread_cond_wait(&missatges_cond, &missatges_sync);
		}
		for(int i = 0; i < 100; i++){
			if(strcmp(missatges[i], "") != 0){
				printf("%s",missatges[i]);
				strcpy(missatges[i],"");
			}
		}
		numero_missatges = 0;
		pthread_mutex_unlock(&missatges_sync);
	}
	return NULL;
}

//Function to simplify adding a message to the queue
void cprintf(char missatge[]){
	while(numero_missatges >= 100);
	pthread_mutex_lock(&missatges_sync);
	if(numero_missatges < 100){
		strcpy(missatges[numero_missatges], missatge);
		numero_missatges++;
	}
	pthread_cond_signal(&missatges_cond);
	pthread_mutex_unlock(&missatges_sync);
}

//Used to print a player
void printar_jugador(struct Jugador jugador){
	char msg[MAX_MESSAGE_LEN];
	sprintf(msg, "- %s\n",jugador.nom);
	cprintf(msg);
	if(jugador.posicio == PORTER){
		//printf("Posició: Porter\n");
	}else if(jugador.posicio == DEFENSA){
		//printf("Posició: Defensa\n");
	}else if(jugador.posicio == CENTRE){
		//printf("Posició: Centre\n");
	}else if(jugador.posicio == DAVANTER){
		//printf("Posició: Davanter\n");
	}
	//printf("Preu: %i\nValor: %i\n",jugador.preu,jugador.valor);
}

//Used to print a team
void printar_equip(struct Equip equip){
	char msg[MAX_MESSAGE_LEN];
	sprintf(msg,"ID de l'equip: %i\nCost total: %i\nValor: %i\n",equip.id, equip.cost, equip.valor);
	cprintf(msg);
	sprintf(msg,"\nPORTERS:\n");
	cprintf(msg);
	for(int i = 0; i < equip.jugadors.n_porters;i++){
		printar_jugador(equip.jugadors.porters[i]);
		//printf("\n");
	}
	sprintf(msg,"\nDEFENSES:\n");
	cprintf(msg);
	for(int i = 0; i < equip.jugadors.n_defenses;i++){
		printar_jugador(equip.jugadors.defenses[i]);
		//printf("\n");
	}
	sprintf(msg,"\nCENTRES:\n");
	cprintf(msg);
	for(int i = 0; i < equip.jugadors.n_centres;i++){
		printar_jugador(equip.jugadors.centres[i]);
		//printf("\n");
	}
	sprintf(msg,"\nDAVANTERS:\n");
	cprintf(msg);
	for(int i = 0; i < equip.jugadors.n_davanters;i++){
		printar_jugador(equip.jugadors.davanters[i]);
		//printf("\n");
	}
}

//Used to print stats
void print_stats(int slot_index){
	if (slot_index < 0){
		sprintf(msgs[slot_index],"============= Parcials Globals =============\nValides totals: %i No valides totals %i Totals %i\nMillor puntuació %i Pitjor puntuació %i\nCost mitjà: %.2f Puntuació mitjana: %.2f\n-------------------------------------------\n",
			globals.combinacions_valides,
			globals.combinacions_no_valides,
			globals.combinacions_evaluades,
			globals.millor_puntuacio,
			globals.pitjor_puntuacio,
			(float) globals.cost_total_valides / (float) globals.combinacions_valides,
			(float) globals.puntuacio_total_valides / (float) globals.combinacions_valides);
		cprintf(msgs[slot_index]);
	}else{
		sprintf(msgs[slot_index],"============= Parcials Slot %i =============\nValides totals: %i No valides totals %i Totals %i\nMillor puntuació %i Pitjor puntuació %i\nCost mitjà: %.2f Puntuació mitjana: %.2f\n-------------------------------------------\n",
			slot_index,
			stats[slot_index].combinacions_valides,
			stats[slot_index].combinacions_no_valides,
			stats[slot_index].combinacions_evaluades,
			stats[slot_index].millor_puntuacio,
			stats[slot_index].pitjor_puntuacio,
			(float) stats[slot_index].cost_total_valides / (float) stats[slot_index].combinacions_valides,
			(float) stats[slot_index].puntuacio_total_valides / (float) stats[slot_index].combinacions_valides);
		cprintf(msgs[slot_index]);
	}
}

//Loop to print all stats, including global ones
void print_all_stats(){
	for(int i = -1; i < numero_threads + 1; i++){
		print_stats(i);
		stats[i].etapa--;
	}
	globals.etapa++;
}

//Loop to check if we must print global stats
int necessary_print(){
	for(int t = 0; t < numero_threads + 1; t++){
		if (stats[t].etapa < 1){
			return 1;
		}
	}
	return 0;
}
