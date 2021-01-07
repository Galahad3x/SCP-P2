# SCP-P2
## Anàlisi de rendiment
Amb 4 threads i 200 de pressupost, ja que fem servir un PC d'1 socket, 2 cores per socket i 2 threads per core  

| mercat | Secuencial_C  | Secuencial_Java | Concurrent_C_P1 | Concurrent_Java_P1 | Concurrent_C_P2 | Concurrent_Java_P2 |
|--------|---------------|-----------------|-----------------|--------------------|-----------------|--------------------|
| 15     | 0,116s        | 0,202s          | 0,004s          | 0,410s             | 0,039s          | 0,53s              |
| 25     | 3,979s        | 0,8s            | 0,018s          | 0,552s             | 0,072s          | 0,232s             |
| 50     | 3m36,661s     | 45,321s         | 0,595s          | 3,399s             | 1,973s          | 4,004s             |
| 60     | 24m14,709s    | 5m12,662s       | 5,202s          | 18,543s            | 17,867s         | 45,023s            |
| 75     | 2h45m52,911s  | 3h12m34,865s    | 34,422s         | 2m13,874s          | 1m29,043s       | 4m22,078s          |
| 100    | 23h13m32,031s | 22h28m54,274s   | 4m58,954s       | 16m42,990s         | 10m39,431s      | 27m15,234s         |

## Coses a fer
 + Sincronitzacions  
 ++ ~Semafors: A les globals, el farem servir de mutex~  
 ++ ~Mutex: Thread missatges~  
 ++ ~Barreres: Al acabar cada thread~  
 ++ ~Variables de condició: Thread missatges~
 + Evitar las condiciones de carrera  
Implementar sincronització que sigui per a que sigui determinista
 + Thread de missatges  
Crear un thread que s'esperi a que a un array hi tinguem 100 missatges. Amb variables de condicio. Cada thread bloqueja un mutex, afegeix el seu missatge i desperta al de missatges. El thread de missatges o printa i reinicia l'array o se'n torna a dormir fins que el tornin a despertar.
 + ~Espera resultat final~  
Amb una barrera? Crear una barrera per cada bifurcacio on es crea un thread, quan el fill acaba planta la barrera i el pare te la barrera plantada abans de necessitar el resultat. Quan surten de la barrera el fill se mor i el pare agafe el resultat d'una variable compartida.
 + Estadístiques  
Contar-les tal com li vam dir al tita. Explicar molt bé al informe com es calculen i dir-li que si no li agrade el numero que surt se fot. Cada thread calcule les seves, i quan s'hagin de calcular les globals un thread qualsevol agafa i les calcula. Amb un semafor que fa de mutex
 + Progrés  
La granularitat dels threads es variable, per tant no podem fer la merda que diu al anunci perque seria possible que un thread arribés i parés, i els altres threads no arribessin perque el thread pare pararia de generar fills. Farem que si 1 thread fa un M printi el seu parcial i sumi/modifiqui les globals. Printarem les globals quan tots els threads hagin passat d'etapa. Contador d'etapes als threads, comença a 0, cada etapa va sumant, printarem les globals quan tots siguin >= 1, >= 2,etc. IMPORTANT explicar aixo molt be al informe per k el tita despues no digui merdes. Les estadistiques aquestes segurament no donaran deterministes, pero el resultat final si que u donara.

### Errors del tita
#### C
+ ~Si passen 0 per parametre: Avisar k sen fa servir 1 enlloc de 0~
+ ~No determinista: Amb 50 jugadors i 200 i amb multithreads, a vegades done 194-113 amb el cristiano i a vegades 169-118 sense el cristiano, mirar on falle, es algo de threads.~ Lo mateix que al final
+ ~Alliberar espais de memoria~
+ ~No mostrar 7 jugadors en alguna execucio: Per algun motiu prioritze el cristiano tot i que te menos valor i mes cost, mirar wtf pase~ El programa se pensave que no podie agafar un jugador calculat amb el thread

#### Java
+ ~Si passen 0 per parametre: Avisar k sen fa servir 1 enlloc de 0~
+ ~Suma jugadors incorrecta: No se que diu el tita, la suma done correcta~
+ ~Fils daemons: Explicar que si tots els threads usuari moren els threads daemon tambe, es a dir, k els fils no se queden oberts i el tita se va equivocar~
