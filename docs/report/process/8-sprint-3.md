# Sprint 3 (20/10/2025 - 26/10/2025)

## Goal

Obiettivo principale di questo sprint è consolidare la parte logica del gioco e migliorarne l'integrazione con l'interfaccia utente.
In particolare si intende completare l'implementazione del motore di generazione dei casi (rimandato dal primo sprint) e collegarlo a una rappresentazione grafica del knowledge graph per facilitare il debug e la verifica dei casi.
Sul fronte UI, l'obiettivo è completare l'interfaccia di gestione degli indizi e rifinire la pagina principale di gioco e la lavagna (blackboard) per migliorare usabilità e chiarezza informativa.
Infine, si progetta di predisporre la pipeline di Continuous Deployment per automatizzare le distribuzioni e snellire il flusso di rilascio.

## Sprint Backlog
| Priority | Product Backlog Item  | Sprint Task                            |     Assignee      | Initial Estimate of Effort | 1 | 2 | 3 | 4 | 5 | 6 | 7 |
|:--------:|:---------------------:|:---------------------------------------|:-----------------:|:--------------------------:|:-:|:-:|:-:|:-:|:-:|:-:|:-:|
|    1     |   FR3 - Versioning    | Logic Design (#20)                     | Lucia Castellucci |             12             | 3 | 2 | 3 | 2 | 2 | - | - |
|    1     | FR1 - Case Generation | Test and Implementation (#14) - Finish |  Roberto Mitugno  |             14             | 2 | - | 4 | 1 | - | - | - |
|    2     |         View          | Graph Representation (#36)             |  Roberto Mitugno  |             3              | - | - | - | - | - | 2 | 1 |
|    3     |         Setup         | CD (#3)                                |    Luca Samorè    |             5              | - | - | - | - | - | 3 | - |



## Review
La funzionalità di generazione dei casi, prevista inizialmente per il primo sprint, è stata completata in questo ciclo e testata con casi di esempio.
È stata introdotta una rappresentazione grafica del knowledge graph utili per visualizzare le relazioni tra elementi dei casi e supportare il debug.
Sul piano dell'interfaccia sono stati rifiniti il sistema di gestione degli indizi e i principali componenti di gioco (pagina principale e lavagna), migliorandone layout e usabilità.
La pipeline di Continuous Deployment è stata configurata e permette rilasci automatizzati, semplificando il processo di distribuzione.

## Retrospective

Lo sprint ha mostrato un miglior coordinamento nel team e una crescente confidenza con gli strumenti adottati, in particolare con ScalaFX per lo sviluppo delle viste.
La contemporanea evoluzione della logica di dominio e dell'interfaccia ha reso più coeso il prodotto e ha facilitato l'individuazione di punti di integrazione tra modello e view.
Come punto di miglioramento, è emersa la necessità di consolidare ulteriormente l'interfaccia di presentazione dei casi generati, in modo che il flusso end-to-end (generazione → visualizzazione → interazione) risulti fluido e testabile.
Per i prossimi sprint è prioritario rafforzare i test di integrazione tra case generation e componenti UI e prevedere esempi di casi più estesi per la validazione.