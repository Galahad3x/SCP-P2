#include <sys/select.h>
#include <stdio.h>

int main(int argvs[]){
	fd_set bitset;
	
	FD_ZERO(&bitset);
	FD_SET(1,&bitset);
	
	printf("Is set: %i\n", FD_ISSET(1, &bitset));
	
	FD_ZERO(&bitset);
	FD_CLR(1,&bitset);
	
	printf("Is set: %i\n", FD_ISSET(1, &bitset));
}
