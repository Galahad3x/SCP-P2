# SCP-P1

> Important: Per compilar sequencial en C ```gcc manfut.c -o manfutc -lm```

## Per implementar arrays de bits:
##### En C:
- #include <sys/select.h>
- fd_set arraydebits;
- FD_ZERO(&arraydebits);
- FD_SET(posicio,&arraydebits);
- FD_CLR(posicio,&arraydebits);
- FD_ISSET(posicio,&arraydebits); //1 si si 0 si no

##### En Java:
- import java.util.BitSet;
- BitSet arraydebits = new BitSet();
- Nose el intellij tu diu lmao
