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

//Non-modified variables used to create fixed-size arrays
#define ASSUMED_MAX_THREADS 10
#define MAX_PLAYERS_IN_MARKET 200
#define MAX_PLAYER_NAMES 50
#define MAX_MARKET_LINE 256
#define MAX_TEAM_NAME 5

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
};

//Global variable declarations
struct Mercat mercat;
int numero_threads;
int debug = 0;
volatile int id_equips;
pthread_t threads[ASSUMED_MAX_THREADS];
volatile int active_threads[ASSUMED_MAX_THREADS];
volatile int ids_sync = 0;
volatile int actives_sync = 0;

//Function headers
int jugador_apte(struct Equip equip, struct Jugador jugador);
void afegir_jugador(struct Equip *equip, struct Jugador jugador);
void llegir_mercat(char *pathJugadors);
void deepcopy_jugadors(struct JugadorsEquip origen, struct JugadorsEquip *desti);
int trobar_millor_equip(struct Equip *equip, int index);
void *trobar_millor_equip_conc(void *argvs);
void printar_jugador(struct Jugador jugador);
void printar_equip(struct Equip equip);

//Main function
int main(int argc, char* argvs[]){
	if(argc >= 4){
		//Reading the passed parameters
		mercat.pressupost = atoi(argvs[1]);
		numero_threads = atoi(argvs[3]);
		if (numero_threads == 1){
			printf("Passat 1 thread, serà com fer-ho sequencial.");
		}else if(numero_threads == 0){
			printf("Passat 0 threads, es farà amb 1.");
			numero_threads = 1;
		}
		numero_threads = numero_threads - 1;
		
		
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
		//Writing data from the passed .csv file to our global market variable
		llegir_mercat(argvs[2]);
		
		if(debug == 1){
			printf("Numero de jugadors total: %i\n", mercat.num_jugadors);
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
		
		printf("\n");
		//Find the best team and write it into millor_equip
		trobar_millor_equip(&millor_equip, mercat.num_jugadors - 1);
		
		printar_equip(millor_equip);
	}else{
		printf("ERROR: Paràmetres incorrectes\n");
		exit(-1);
	}
}

//This function returns 0 if jugador can be in equip or -1 if it can't
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
int trobar_millor_equip(struct Equip *equip, int index){
	//If index is 0, we are checking for the last player
	//Its value doesn't matter, if we can afford it, we will have more value with him on the team than without
	if(index == 0){
		if(jugador_apte(*equip, mercat.jugadors[index]) == 0){
			if(debug == 1){
				printf("Jugador %s pot ser de l'equip\n",mercat.jugadors[index].nom);
			}
			afegir_jugador(equip, mercat.jugadors[index]);
		}
		
		//Assign an ID to this completed team
		//We use a variable to avoid different teams having the same IDs
		while(ids_sync == 1){
			sleep(0.2);
		}
		ids_sync = 1;
		
		equip -> id = id_equips;
		id_equips++;
		
		ids_sync = 0;
		return equip -> valor;
	}else{
		//If the index is not 0, we need to account for the players we haven't checked yet
		int val_no_agafar, val_agafar = 0, child_thread = -1;
		struct JugadorsEquip no_agafar,agafar;
		struct Equip no_agafar_equip,agafar_equip;
		
		//If we can afford the player, we need to check what's better; having or not having him on the team
		if(jugador_apte(*equip, mercat.jugadors[index]) == 0){
			if(debug == 1){
				printf("Jugador %s pot ser de l'equip\n",mercat.jugadors[index].nom);
			}
			
			//We use a variable to syncronize checking for free space for a thread, so different threads don't try to
			//create different threads on the same space
			while(actives_sync == 1){
				sleep(0.2);
			}
			actives_sync = 1;
			
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
			actives_sync = 0;
			
			//If we have space for a thread
			if(child_thread != -1){
				//Initialize thread parameters
				struct ThreadArgs args;
				args.equip = *equip;
				args.index = index;
				
				//Create a thread that will compute the score of having the player on our team
				if(pthread_create(&threads[child_thread],NULL,trobar_millor_equip_conc,(void *) &args) != 0){
					if(debug == 1){
						printf("ERROR: Error al crear un thread\n");
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
				val_agafar = trobar_millor_equip(&agafar_equip, index - 1);
			}
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
		val_no_agafar = trobar_millor_equip(&no_agafar_equip, index - 1);
		
		//If we used another thread to calculate the best outcome with the player
		if(child_thread != -1){
			
			struct Equip *agafar_thread;
			int ret;
			//We join the created thread, which will return the best team it could create while having the player
			if((ret = pthread_join(threads[child_thread], (void **) &agafar_thread)) != 0){
				printf("ERROR: Error al fer un join %i\n", ret);
				exit(-1);
			}
			
			//We signal other threads that they can use this thread space now
			active_threads[child_thread] = 0;
			
			agafar_equip = *agafar_thread;
			free(agafar_thread);
			val_agafar = agafar_equip.valor;
		}
		//We have both best possible outcomes
		//Either by creating and joining another thread
		//Or by calculating it on the same thread
		if(debug == 1){
			printf("Valor agafar: %i Valor no agafar: %i \n",val_agafar, val_no_agafar);
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
	trobar_millor_equip(&agafar_equip, index - 1);
	
	//Return the calculated team
	struct Equip *max_equip;
	max_equip = (struct Equip *) malloc(sizeof(struct Equip));
	*max_equip = agafar_equip;
	return max_equip;
}

//Used to print a player
void printar_jugador(struct Jugador jugador){
	printf("Nom: %s\n",jugador.nom);
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
	printf("ID de l'equip: %i\nCost total: %i\nValor: %i\n",equip.id, equip.cost, equip.valor);
	printf("\nPORTERS:\n");
	for(int i = 0; i < equip.jugadors.n_porters;i++){
		printar_jugador(equip.jugadors.porters[i]);
		//printf("\n");
	}
	printf("\nDEFENSES:\n");
	for(int i = 0; i < equip.jugadors.n_defenses;i++){
		printar_jugador(equip.jugadors.defenses[i]);
		//printf("\n");
	}
	printf("\nCENTRES:\n");
	for(int i = 0; i < equip.jugadors.n_centres;i++){
		printar_jugador(equip.jugadors.centres[i]);
		//printf("\n");
	}
	printf("\nDAVANTERS:\n");
	for(int i = 0; i < equip.jugadors.n_davanters;i++){
		printar_jugador(equip.jugadors.davanters[i]);
		//printf("\n");
	}
}
